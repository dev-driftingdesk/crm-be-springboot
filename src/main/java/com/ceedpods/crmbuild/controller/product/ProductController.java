package com.ceedpods.crmbuild.controller.product;

import com.ceedpods.crmbuild.dto.product.ProductDTO;
import com.ceedpods.crmbuild.dto.request.CreateProductRequest;
import com.ceedpods.crmbuild.dto.request.UpdateProductRequest;
import com.ceedpods.crmbuild.dto.response.ApiResponse;
import com.ceedpods.crmbuild.security.CustomPermissionEvaluator;
import com.ceedpods.crmbuild.security.RequirePermission;
import com.ceedpods.crmbuild.service.product.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final CustomPermissionEvaluator permissionEvaluator;

    /**
     * Create a new product (Admin only)
     */
    @PostMapping
    @RequirePermission("PRODUCT_CREATE")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            Authentication authentication) {
        try {
            log.info("Creating product: {}", request.getProductName());
            ProductDTO product = productService.createProduct(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", product));
        } catch (Exception e) {
            log.error("Error creating product: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to create product: " + e.getMessage()));
        }
    }

    /**
     * Get all products
     */
    @GetMapping
    @RequirePermission("PRODUCT_VIEW_ALL")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts() {
        try {
            log.info("Fetching all products");
            List<ProductDTO> products = productService.getAllProducts();
            return ResponseEntity.ok(ApiResponse.success(products));
        } catch (Exception e) {
            log.error("Error fetching products: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to fetch products: " + e.getMessage()));
        }
    }

    /**
     * Get product by ID
     */
    @GetMapping("/{id}")
    @RequirePermission("PRODUCT_VIEW_ALL")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable String id) {
        try {
            log.info("Fetching product with ID: {}", id);
            ProductDTO product = productService.getProductById(id);
            return ResponseEntity.ok(ApiResponse.success(product));
        } catch (Exception e) {
            log.error("Error fetching product: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to fetch product: " + e.getMessage()));
        }
    }


    /**
     * Update product
     */
    @PutMapping("/{id}")
    @RequirePermission("PRODUCT_EDIT")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody UpdateProductRequest request,
            Authentication authentication) {
        try {
            log.info("Updating product with ID: {}", id);
            ProductDTO product = productService.updateProduct(id, request);
            return ResponseEntity.ok(ApiResponse.success("Product updated successfully", product));
        } catch (Exception e) {
            log.error("Error updating product: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to update product: " + e.getMessage()));
        }
    }

    /**
     * Delete product (soft delete)
     */
    @DeleteMapping("/{id}")
    @RequirePermission("PRODUCT_DELETE")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable String id,
            Authentication authentication) {
        try {
            String deletedBy = permissionEvaluator.getCurrentKeycloakId(authentication);
            log.info("Deleting product with ID: {} by user: {}", id, deletedBy);
            productService.deleteProduct(id, deletedBy);
            return ResponseEntity.ok(ApiResponse.success("Product deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting product: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to delete product: " + e.getMessage()));
        }
    }

    /**
     * Search products
     */
    @GetMapping("/search")
    @RequirePermission("PRODUCT_VIEW_ALL")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> searchProducts(
            @RequestParam String query) {
        try {
            log.info("Searching products with query: {}", query);
            List<ProductDTO> products = productService.searchProducts(query);
            return ResponseEntity.ok(ApiResponse.success(products));
        } catch (Exception e) {
            log.error("Error searching products: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to search products: " + e.getMessage()));
        }
    }

    /**
     * Get total product count
     */
    @GetMapping("/count")
    @RequirePermission("PRODUCT_VIEW_ALL")
    public ResponseEntity<ApiResponse<Long>> getProductCount() {
        try {
            log.info("Fetching product count");
            long count = productService.getTotalProductCount();
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            log.error("Error fetching product count: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to fetch product count: " + e.getMessage()));
        }
    }
}
