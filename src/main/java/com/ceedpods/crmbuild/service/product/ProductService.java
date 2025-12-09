package com.ceedpods.crmbuild.service.product;

import com.ceedpods.crmbuild.dto.product.ProductDTO;
import com.ceedpods.crmbuild.dto.request.CreateProductRequest;
import com.ceedpods.crmbuild.dto.request.UpdateProductRequest;
import com.ceedpods.crmbuild.dto.response.CreateProductResponse;
import com.ceedpods.crmbuild.entity.product.DiscountAddOn;
import com.ceedpods.crmbuild.entity.product.PricingPackage;
import com.ceedpods.crmbuild.entity.product.PricingPackages;
import com.ceedpods.crmbuild.entity.product.Product;
import com.ceedpods.crmbuild.entity.user.User;
import com.ceedpods.crmbuild.exception.BadRequestException;
import com.ceedpods.crmbuild.exception.ResourceNotFoundException;
import com.ceedpods.crmbuild.mapper.ProductMapper;
import com.ceedpods.crmbuild.repository.DealRepository;
import com.ceedpods.crmbuild.repository.ProductRepository;
import com.ceedpods.crmbuild.repository.UserRepository;
import com.ceedpods.crmbuild.service.auditLogService.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final AuditLogService auditLogService;
    private final DealRepository dealRepository;
    private final UserRepository userRepository;

    /**
     * Get all products with enhanced summary information (excluding soft-deleted)
     * Includes: Product Name, Created User Name, Created User Profile Picture, In Deal Count, Base Price, Total Sales, Revenue
     */
    public List<ProductDTO> getAllProducts() {
        log.info("Fetching all products with enhanced summary");

        List<Product> products = productRepository.findByDeletedFalse();

        if (products.isEmpty()) {
            log.info("No products found");
            return List.of();
        }

        // Collect all unique createdBy (MongoDB User ID) values to batch fetch users
        Set<String> creatorUserIds = products.stream()
            .map(Product::getCreatedBy)
            .filter(id -> id != null && !id.isEmpty() && !id.equals("system"))
            .collect(Collectors.toSet());

        // Batch fetch users by MongoDB ID and create a map for quick lookup (stores full User object for name and profile picture)
        Map<String, User> userIdToUser = creatorUserIds.stream()
            .map(userId -> userRepository.findById(userId).orElse(null))
            .filter(user -> user != null)
            .collect(Collectors.toMap(
                User::getId,
                user -> user,
                (existing, replacement) -> existing // Handle duplicates
            ));

        // Transform products to enhanced ProductDTO
        return products.stream()
            .map(product -> mapToEnhancedProductDTO(product, userIdToUser))
            .collect(Collectors.toList());
    }

    /**
     * Maps a Product entity to ProductDTO with calculated statistics
     */
    private ProductDTO mapToEnhancedProductDTO(Product product, Map<String, User> userIdToUser) {
        // Get the created user info from the map (createdBy stores MongoDB User ID)
        String createdUserName = null;
        String createdUserProfilePicture = null;
        if (product.getCreatedBy() != null && !product.getCreatedBy().equals("system")) {
            User createdUser = userIdToUser.get(product.getCreatedBy());
            if (createdUser != null) {
                createdUserName = createdUser.getFullName();
                createdUserProfilePicture = createdUser.getProfilePicture();
            } else {
                createdUserName = "Unknown User";
            }
        } else if ("system".equals(product.getCreatedBy())) {
            createdUserName = "System";
        }

        // Count deals containing this product
        long inDealCount = dealRepository.countByProductIdAndDeletedFalse(product.getId());

        // Base price from product
        BigDecimal basePrice = product.getBasePrice() != null
            ? product.getBasePrice()
            : BigDecimal.ZERO;

        // Total sales equals the deal count
        long totalSales = inDealCount;

        // Revenue = basePrice × totalSales
        BigDecimal revenue = basePrice.multiply(BigDecimal.valueOf(totalSales));

        // Build the enhanced DTO using the mapper's toDTO method and then enhance it
        ProductDTO dto = productMapper.toDTO(product);

        // Set enhanced fields (audit fields are already set by the mapper)
        dto.setCreatedUserName(createdUserName);
        dto.setCreatedUserProfilePicture(createdUserProfilePicture);
        dto.setInDealCount(inDealCount);
        dto.setBasePrice(basePrice);
        dto.setTotalSales(totalSales);
        dto.setRevenue(revenue);

        return dto;
    }

    /**
     * Get product by ID with enhanced summary information
     * Includes: Product Name, Created User Name, Created User Profile Picture, In Deal Count, Base Price, Total Sales, Revenue
     */
    public ProductDTO getProductById(String id) {
        log.info("Fetching product with ID: {}", id);
        Product product = productRepository.findById(id)
            .filter(p -> !p.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        // Fetch the created user for enhanced fields
        Map<String, User> userIdToUser = new java.util.HashMap<>();
        if (product.getCreatedBy() != null && !product.getCreatedBy().equals("system")) {
            userRepository.findById(product.getCreatedBy())
                .ifPresent(user -> userIdToUser.put(user.getId(), user));
        }

        return mapToEnhancedProductDTO(product, userIdToUser);
    }


    /**
     * Create new product with the new structure
     * Optimized: Uses async audit logging for better response time
     */
    @Transactional
    public CreateProductResponse createProduct(CreateProductRequest request) {
        String productName = request.getBasicInformation().getProductName();
        log.info("Creating new product: {}", productName);

        // Validate pricing packages if enabled
        CreateProductRequest.PricingPackagesRequest pricingPackages = request.getPricingPackages();
        if (pricingPackages != null &&
            Boolean.TRUE.equals(pricingPackages.getEnabled()) &&
            (pricingPackages.getPackages() == null || pricingPackages.getPackages().isEmpty())) {
            throw new BadRequestException("Packages are required when pricing packages is enabled");
        }

        // Extract audit context before async call (must be done in request thread)
        AuditContext auditContext = extractAuditContext();

        // Create and save product entity
        Product product = productMapper.toEntityForCreation(request);
        Product savedProduct = productRepository.save(product);

        String productId = savedProduct.getId();
        log.info("Successfully created product with UUID: {}", productId);

        // Log audit event asynchronously (non-blocking)
        auditLogService.logProductCreatedAsync(
            auditContext.username,
            auditContext.userId,
            auditContext.userEmail,
            productId,
            productName,
            auditContext.ipAddress
        );

        return CreateProductResponse.builder()
            .productId(productId)
            .build();
    }

    /**
     * Extract audit context from current request thread
     * Must be called before async operations as RequestContext is thread-bound
     */
    private AuditContext extractAuditContext() {
        String username = "anonymous";
        String userId = null;
        String userEmail = null;
        String ipAddress = "unknown";

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            userId = jwt.getClaimAsString("sub");
            userEmail = jwt.getClaimAsString("email");
            username = userEmail != null ? userEmail : jwt.getClaimAsString("preferred_username");
        }

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    ipAddress = xForwardedFor.split(",")[0].trim();
                } else {
                    ipAddress = request.getRemoteAddr();
                }
            }
        } catch (Exception e) {
            log.debug("Could not determine client IP address: {}", e.getMessage());
        }

        return new AuditContext(username, userId, userEmail, ipAddress);
    }

    /**
     * Simple record to hold audit context data
     */
    private record AuditContext(String username, String userId, String userEmail, String ipAddress) {}

    /**
     * Update existing product
     */
    @Transactional
    public ProductDTO updateProduct(String id, UpdateProductRequest request) {
        log.info("Updating product with ID: {}", id);

        // Find existing product
        Product product = productRepository.findById(id)
            .filter(p -> !p.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        // Update basic information fields if provided
        if (request.getProductName() != null) {
            product.setProductName(request.getProductName());
        }
        if (request.getBasePrice() != null) {
            product.setBasePrice(request.getBasePrice());
        }
        if (request.getKeyLearningOutcomes() != null) {
            product.setKeyLearningOutcomes(request.getKeyLearningOutcomes());
        }
        if (request.getFormat() != null) {
            product.setFormat(request.getFormat());
        }
        if (request.getDuration() != null) {
            product.setDuration(request.getDuration());
        }
        if (request.getLevel() != null) {
            product.setLevel(request.getLevel());
        }
        if (request.getInstructors() != null) {
            product.setInstructors(request.getInstructors());
        }

        // Update pricing packages if provided
        if (request.getPricingPackages() != null) {
            PricingPackages pricingPackages = mapUpdatePricingPackagesToEntity(request.getPricingPackages());
            product.setPricingPackages(pricingPackages);
        }

        // Update discounts and add-ons if provided
        if (request.getDiscountsAddOns() != null) {
            List<DiscountAddOn> discountsAddOns = mapUpdateDiscountsAddOnsToEntity(request.getDiscountsAddOns());
            product.setDiscountsAddOns(discountsAddOns);
        }

        // Update product status if provided
        if (request.getProductStatus() != null) {
            product.setProductStatus(request.getProductStatus());
        }

        // Save updated product (updatedAt and updatedBy are automatically handled by Spring Data Auditing)
        Product updatedProduct = productRepository.save(product);
        log.info("Successfully updated product with ID: {}", updatedProduct.getId());

        // Log audit event
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        auditLogService.logProductUpdated(authentication, updatedProduct.getId(), updatedProduct.getProductName());

        return productMapper.toDTO(updatedProduct);
    }

    private PricingPackages mapUpdatePricingPackagesToEntity(UpdateProductRequest.PricingPackagesUpdateRequest request) {
        if (request == null) {
            return null;
        }

        List<PricingPackage> packages = null;
        if (request.getPackages() != null) {
            packages = request.getPackages().stream()
                .map(pkg -> PricingPackage.builder()
                    .packageType(pkg.getPackageType())
                    .description(pkg.getDescription())
                    .price(pkg.getPrice())
                    .commissionRate(pkg.getCommissionRate())
                    .notes(pkg.getNotes())
                    .build())
                .collect(Collectors.toList());
        }

        return PricingPackages.builder()
            .enabled(request.getEnabled())
            .packages(packages)
            .build();
    }

    private List<DiscountAddOn> mapUpdateDiscountsAddOnsToEntity(List<UpdateProductRequest.DiscountAddOnUpdateRequest> requests) {
        if (requests == null) {
            return null;
        }

        return requests.stream()
            .map(item -> DiscountAddOn.builder()
                .type(item.getType())
                .description(item.getDescription())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * Delete product (soft delete)
     */
    @Transactional
    public void deleteProduct(String id, String deletedBy) {
        log.info("Deleting product with ID: {}", id);

        // Find existing product
        Product product = productRepository.findById(id)
            .filter(p -> !p.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        // Store product name before deletion for audit log
        String productName = product.getProductName();

        // Soft delete
        product.markAsDeleted(deletedBy);
        productRepository.save(product);

        log.info("Successfully deleted product with ID: {}", id);

        // Log audit event
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        auditLogService.logProductDeleted(authentication, id, productName);
    }

    /**
     * Search products by keyword with enhanced summary information
     * Includes: Product Name, Created User Name, Created User Profile Picture, In Deal Count, Base Price, Total Sales, Revenue
     */
    public List<ProductDTO> searchProducts(String searchTerm) {
        log.info("Searching products with term: {}", searchTerm);
        List<Product> products = productRepository.searchProducts(searchTerm);

        if (products.isEmpty()) {
            return List.of();
        }

        // Collect all unique createdBy (MongoDB User ID) values to batch fetch users
        Set<String> creatorUserIds = products.stream()
            .map(Product::getCreatedBy)
            .filter(id -> id != null && !id.isEmpty() && !id.equals("system"))
            .collect(Collectors.toSet());

        // Batch fetch users by MongoDB ID and create a map for quick lookup
        Map<String, User> userIdToUser = creatorUserIds.stream()
            .map(userId -> userRepository.findById(userId).orElse(null))
            .filter(user -> user != null)
            .collect(Collectors.toMap(
                User::getId,
                user -> user,
                (existing, replacement) -> existing
            ));

        // Transform products to enhanced ProductDTO
        return products.stream()
            .map(product -> mapToEnhancedProductDTO(product, userIdToUser))
            .collect(Collectors.toList());
    }

    /**
     * Get total product count (excluding soft-deleted)
     */
    public long getTotalProductCount() {
        return productRepository.countByDeletedFalse();
    }
}
