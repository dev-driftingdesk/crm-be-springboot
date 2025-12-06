package com.ceedpods.crmbuild.service.product;

import com.ceedpods.crmbuild.dto.product.ProductDTO;
import com.ceedpods.crmbuild.dto.request.CreateProductRequest;
import com.ceedpods.crmbuild.dto.request.UpdateProductRequest;
import com.ceedpods.crmbuild.entity.product.Product;
import com.ceedpods.crmbuild.entity.user.User;
import com.ceedpods.crmbuild.exception.BadRequestException;
import com.ceedpods.crmbuild.exception.ResourceNotFoundException;
import com.ceedpods.crmbuild.mapper.ProductMapper;
import com.ceedpods.crmbuild.repository.DealRepository;
import com.ceedpods.crmbuild.repository.ProductRepository;
import com.ceedpods.crmbuild.repository.UserRepository;
import com.ceedpods.crmbuild.service.auditLogService.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // Base price is the product value
        BigDecimal basePrice = product.getProductValue() != null
            ? product.getProductValue()
            : BigDecimal.ZERO;

        // Total sales equals the deal count
        long totalSales = inDealCount;

        // Revenue = basePrice × totalSales
        BigDecimal revenue = basePrice.multiply(BigDecimal.valueOf(totalSales));

        // Build the enhanced DTO
        ProductDTO dto = ProductDTO.builder()
            .id(product.getId())
            .productName(product.getProductName())
            .productDescription(product.getProductDescription())
            .productSubDescription(product.getProductSubDescription())
            .productValue(product.getProductValue())
            .productStatus(product.getProductStatus())
            // Enhanced fields
            .createdUserName(createdUserName)
            .createdUserProfilePicture(createdUserProfilePicture)
            .inDealCount(inDealCount)
            .basePrice(basePrice)
            .totalSales(totalSales)
            .revenue(revenue)
            .build();

        // Set audit fields from base entity
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        dto.setCreatedBy(product.getCreatedBy());
        dto.setUpdatedBy(product.getUpdatedBy());
        dto.setDeleted(product.isDeleted());
        dto.setDeletedAt(product.getDeletedAt());
        dto.setDeletedBy(product.getDeletedBy());

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
     * Create new product
     */
    @Transactional
    public ProductDTO createProduct(CreateProductRequest request) {
        log.info("Creating new product: {}", request.getProductName());

        // Convert request to DTO first
        ProductDTO dto = ProductDTO.builder()
            .productName(request.getProductName())
            .productDescription(request.getProductDescription())
            .productSubDescription(request.getProductSubDescription())
            .productValue(request.getProductValue())
            .productStatus(request.getProductStatus())
            .build();

        // Create product entity with UUID generation
        Product product = productMapper.toEntityForCreation(dto);

        // Save product (audit fields are automatically handled by Spring Data Auditing)
        Product savedProduct = productRepository.save(product);
        log.info("Successfully created product with UUID: {}", savedProduct.getId());

        // Log audit event
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        auditLogService.logProductCreated(authentication, savedProduct.getId(), savedProduct.getProductName());

        return productMapper.toDTO(savedProduct);
    }

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

        // Update fields if provided
        if (request.getProductName() != null) {
            product.setProductName(request.getProductName());
        }
        if (request.getProductDescription() != null) {
            product.setProductDescription(request.getProductDescription());
        }
        if (request.getProductSubDescription() != null) {
            product.setProductSubDescription(request.getProductSubDescription());
        }
        if (request.getProductValue() != null) {
            product.setProductValue(request.getProductValue());
        }
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
