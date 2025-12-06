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
     * Includes: Product Name, Created User Name, In Deal Count, Base Price, Total Sales, Revenue
     */
    public List<ProductDTO> getAllProducts() {
        log.info("Fetching all products with enhanced summary");

        List<Product> products = productRepository.findByDeletedFalse();

        if (products.isEmpty()) {
            log.info("No products found");
            return List.of();
        }

        // Collect all unique createdBy (keycloakId) values to batch fetch users
        Set<String> creatorKeycloakIds = products.stream()
            .map(Product::getCreatedBy)
            .filter(id -> id != null && !id.isEmpty())
            .collect(Collectors.toSet());

        // Batch fetch users and create a map for quick lookup
        Map<String, String> keycloakIdToUserName = creatorKeycloakIds.stream()
            .map(keycloakId -> userRepository.findByKeycloakId(keycloakId).orElse(null))
            .filter(user -> user != null)
            .collect(Collectors.toMap(
                User::getKeycloakId,
                User::getFullName,
                (existing, replacement) -> existing // Handle duplicates
            ));

        // Transform products to enhanced ProductDTO
        return products.stream()
            .map(product -> mapToEnhancedProductDTO(product, keycloakIdToUserName))
            .collect(Collectors.toList());
    }

    /**
     * Maps a Product entity to ProductDTO with calculated statistics
     */
    private ProductDTO mapToEnhancedProductDTO(Product product, Map<String, String> keycloakIdToUserName) {
        // Get the created user name from the map
        String createdUserName = null;
        if (product.getCreatedBy() != null) {
            createdUserName = keycloakIdToUserName.getOrDefault(product.getCreatedBy(), "Unknown User");
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
     * Get product by ID
     */
    public ProductDTO getProductById(String id) {
        log.info("Fetching product with ID: {}", id);
        Product product = productRepository.findById(id)
            .filter(p -> !p.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return productMapper.toDTO(product);
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
     * Search products by keyword
     */
    public List<ProductDTO> searchProducts(String searchTerm) {
        log.info("Searching products with term: {}", searchTerm);
        List<Product> products = productRepository.searchProducts(searchTerm);
        return productMapper.toDTO(products);
    }

    /**
     * Get total product count (excluding soft-deleted)
     */
    public long getTotalProductCount() {
        return productRepository.countByDeletedFalse();
    }
}
