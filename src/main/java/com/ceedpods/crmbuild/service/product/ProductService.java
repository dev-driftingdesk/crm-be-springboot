package com.ceedpods.crmbuild.service.product;

import com.ceedpods.crmbuild.dto.product.ProductDTO;
import com.ceedpods.crmbuild.dto.request.CreateProductRequest;
import com.ceedpods.crmbuild.dto.request.UpdateProductRequest;
import com.ceedpods.crmbuild.entity.product.Product;
import com.ceedpods.crmbuild.exception.BadRequestException;
import com.ceedpods.crmbuild.exception.ResourceNotFoundException;
import com.ceedpods.crmbuild.mapper.ProductMapper;
import com.ceedpods.crmbuild.repository.ProductRepository;
import com.ceedpods.crmbuild.service.auditLogService.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final AuditLogService auditLogService;

    /**
     * Get all products (excluding soft-deleted)
     */
    public List<ProductDTO> getAllProducts() {
        log.info("Fetching all products");
        List<Product> products = productRepository.findByDeletedFalse();
        return productMapper.toDTO(products);
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
