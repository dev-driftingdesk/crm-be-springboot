package com.ceedpods.crmbuild.service.product;

import com.ceedpods.crmbuild.dto.deal.DealProduct;
import com.ceedpods.crmbuild.dto.product.ProductDTO;
import com.ceedpods.crmbuild.dto.product.ProductListResponse;
import com.ceedpods.crmbuild.dto.request.CreateProductRequest;
import com.ceedpods.crmbuild.dto.request.UpdateProductRequest;
import com.ceedpods.crmbuild.dto.response.CreateProductResponse;
import com.ceedpods.crmbuild.dto.response.UpdateProductResponse;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final MongoTemplate mongoTemplate;

    /**
     * Get all products (excluding soft-deleted)
     * Optimized: Uses batch queries to minimize database round-trips
     * Returns products sorted by createdAt descending (newest first)
     */
    public List<ProductListResponse> getAllProducts() {
        log.info("Fetching all products");

        // Sort by createdAt descending so newly created products appear first
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<Product> products = productRepository.findByDeletedFalse(sort);

        if (products.isEmpty()) {
            log.info("No products found");
            return List.of();
        }

        // Collect all product IDs for batch deal count lookup
        List<String> productIds = products.stream()
            .map(Product::getId)
            .collect(Collectors.toList());

        // Collect all unique createdBy (MongoDB User ID) values for batch user lookup
        List<String> creatorUserIds = products.stream()
            .map(Product::getCreatedBy)
            .filter(id -> id != null && !id.isEmpty() && !id.equals("system"))
            .distinct()
            .collect(Collectors.toList());

        // Batch fetch users in a single query
        Map<String, User> userIdToUser = creatorUserIds.isEmpty()
            ? Map.of()
            : userRepository.findByIdInAndDeletedFalse(creatorUserIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user, (a, b) -> a));

        // Batch fetch all deals containing any of these products in a single query
        // Then count in memory
        Map<String, Long> productDealCounts = dealRepository.findByProductIdsInAndDeletedFalse(productIds).stream()
            .flatMap(deal -> deal.getProducts() != null ? deal.getProducts().stream().map(DealProduct::getProductId) : java.util.stream.Stream.<String>empty())
            .filter(productIds::contains)
            .collect(Collectors.groupingBy(id -> id, Collectors.counting()));

        return products.stream()
            .map(product -> mapToProductListResponse(product, userIdToUser, productDealCounts))
            .collect(Collectors.toList());
    }

    /**
     * Maps a Product entity to ProductListResponse
     */
    private ProductListResponse mapToProductListResponse(Product product, Map<String, User> userIdToUser, Map<String, Long> productDealCounts) {
        // Build createdBy object
        ProductListResponse.CreatedBy createdBy = null;
        if (product.getCreatedBy() != null && !product.getCreatedBy().equals("system")) {
            User createdUser = userIdToUser.get(product.getCreatedBy());
            if (createdUser != null) {
                createdBy = ProductListResponse.CreatedBy.builder()
                    .userId(createdUser.getId())
                    .name(createdUser.getFullName())
                    .profilePicture(null) // Will be populated after Minio deployment
                    .build();
            } else {
                createdBy = ProductListResponse.CreatedBy.builder()
                    .userId(product.getCreatedBy())
                    .name("Unknown User")
                    .profilePicture(null)
                    .build();
            }
        } else if ("system".equals(product.getCreatedBy())) {
            createdBy = ProductListResponse.CreatedBy.builder()
                .userId("system")
                .name("System")
                .profilePicture(null)
                .build();
        }

        // Get deal count from pre-computed map (O(1) lookup)
        long inDeals = productDealCounts.getOrDefault(product.getId(), 0L);
        long totalSales = inDeals;

        // Revenue = basePrice × totalSales
        BigDecimal basePrice = product.getBasePrice() != null ? product.getBasePrice() : BigDecimal.ZERO;
        BigDecimal revenue = basePrice.multiply(BigDecimal.valueOf(totalSales));

        return ProductListResponse.builder()
            .productId(product.getId())
            .productName(product.getProductName())
            .basePrice(basePrice)
            .createdBy(createdBy)
            .inDeals(inDeals)
            .totalSales(totalSales)
            .revenue(revenue)
            .build();
    }

    /**
     * Get product by ID with full details
     */
    public ProductDTO getProductById(String id) {
        log.info("Fetching product with ID: {}", id);
        Product product = productRepository.findById(id)
            .filter(p -> !p.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        return productMapper.toDTO(product);
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
     * Optimized: Uses async audit logging for better response time
     */
    @Transactional
    public UpdateProductResponse updateProduct(String id, UpdateProductRequest request) {
        log.info("Updating product with ID: {}", id);

        // Extract audit context early (must be done in request thread before any async operations)
        AuditContext auditContext = extractAuditContext();

        // Validate pricing packages if enabled
        UpdateProductRequest.PricingPackagesRequest pricingPackagesReq = request.getPricingPackages();
        if (pricingPackagesReq != null &&
            Boolean.TRUE.equals(pricingPackagesReq.getEnabled()) &&
            (pricingPackagesReq.getPackages() == null || pricingPackagesReq.getPackages().isEmpty())) {
            throw new BadRequestException("Packages are required when pricing packages is enabled");
        }

        // Find existing product
        Product product = productRepository.findById(id)
            .filter(p -> !p.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        // Apply updates using mapper (separation of concerns)
        productMapper.applyUpdates(product, request);

        // Save updated product (updatedAt and updatedBy are automatically handled by Spring Data Auditing)
        Product updatedProduct = productRepository.save(product);
        String productId = updatedProduct.getId();
        String productName = updatedProduct.getProductName();
        log.info("Successfully updated product with ID: {}", productId);

        // Log audit event asynchronously (non-blocking)
        auditLogService.logProductUpdatedAsync(
            auditContext.username,
            auditContext.userId,
            auditContext.userEmail,
            productId,
            productName,
            auditContext.ipAddress
        );

        return UpdateProductResponse.builder()
            .productId(productId)
            .updatedAt(updatedProduct.getUpdatedAt())
            .build();
    }

    /**
     * Delete product (hard delete - permanently removes from database)
     */
    public void deleteProduct(String id, String deletedBy) {
        log.info("Deleting product with ID: {}", id);

        // Find existing product
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        // Store product name before deletion for audit log
        String productName = product.getProductName();

        // Hard delete - permanently remove from database
        productRepository.deleteById(id);

        log.info("Successfully deleted product with ID: {}", id);

        // Log audit event
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        auditLogService.logProductDeleted(authentication, id, productName);
    }

    /**
     * Search products by keyword
     * Optimized: Uses batch queries to minimize database round-trips
     */
    public List<ProductListResponse> searchProducts(String searchTerm) {
        log.info("Searching products with term: {}", searchTerm);
        List<Product> products = productRepository.searchProducts(searchTerm);

        if (products.isEmpty()) {
            return List.of();
        }

        // Collect all product IDs for batch deal count lookup
        List<String> productIds = products.stream()
            .map(Product::getId)
            .collect(Collectors.toList());

        // Collect all unique createdBy (MongoDB User ID) values for batch user lookup
        List<String> creatorUserIds = products.stream()
            .map(Product::getCreatedBy)
            .filter(id -> id != null && !id.isEmpty() && !id.equals("system"))
            .distinct()
            .collect(Collectors.toList());

        // Batch fetch users in a single query
        Map<String, User> userIdToUser = creatorUserIds.isEmpty()
            ? Map.of()
            : userRepository.findByIdInAndDeletedFalse(creatorUserIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user, (a, b) -> a));

        // Batch fetch all deals containing any of these products in a single query
        Map<String, Long> productDealCounts = dealRepository.findByProductIdsInAndDeletedFalse(productIds).stream()
            .flatMap(deal -> deal.getProducts() != null ? deal.getProducts().stream().map(DealProduct::getProductId) : java.util.stream.Stream.<String>empty())
            .filter(productIds::contains)
            .collect(Collectors.groupingBy(id -> id, Collectors.counting()));

        return products.stream()
            .map(product -> mapToProductListResponse(product, userIdToUser, productDealCounts))
            .collect(Collectors.toList());
    }

    /**
     * Get total product count (excluding soft-deleted)
     */
    public long getTotalProductCount() {
        return productRepository.countByDeletedFalse();
    }
}
