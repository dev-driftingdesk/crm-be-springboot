package com.ceedpods.crmbuild.controller.product;

import com.ceedpods.crmbuild.dto.product.ProductDTO;
import com.ceedpods.crmbuild.dto.request.CreateProductRequest;
import com.ceedpods.crmbuild.dto.request.UpdateProductRequest;
import com.ceedpods.crmbuild.dto.response.ApiResponse;
import com.ceedpods.crmbuild.security.CustomPermissionEvaluator;
import com.ceedpods.crmbuild.security.RequirePermission;
import com.ceedpods.crmbuild.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Product Management", description = "Operations for managing products and services in the catalog")
@SecurityRequirement(name = "Bearer Authentication")
public class ProductController {

    private final ProductService productService;
    private final CustomPermissionEvaluator permissionEvaluator;

    /**
     * Create a new product (Admin only)
     */
    @Operation(summary = "Create Product", description = "Creates a new product in the catalog. Requires PRODUCT_CREATE permission.")
    @PostMapping
    @RequirePermission("PRODUCT_CREATE")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @Parameter(hidden = true) Authentication authentication) {
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
     * Get all products with enhanced summary information
     */
    @Operation(
        summary = "Get All Products",
        description = "Retrieves all products from the catalog with enhanced summary information including: " +
                      "Product Name, Created User Name, In Deal Count, Base Price, Total Sales, and Revenue"
    )
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
    @Operation(summary = "Get Product by ID", description = "Retrieves a specific product by its UUID")
    @GetMapping("/{id}")
    @RequirePermission("PRODUCT_VIEW_ALL")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(
            @Parameter(description = "Product UUID", required = true) @PathVariable String id) {
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
    @Operation(summary = "Update Product", description = "Updates an existing product. Requires PRODUCT_EDIT permission.")
    @PutMapping("/{id}")
    @RequirePermission("PRODUCT_EDIT")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @Parameter(description = "Product UUID", required = true) @PathVariable String id,
            @Valid @RequestBody UpdateProductRequest request,
            @Parameter(hidden = true) Authentication authentication) {
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
    @Operation(summary = "Delete Product", description = "Soft deletes a product. Requires PRODUCT_DELETE permission.")
    @DeleteMapping("/{id}")
    @RequirePermission("PRODUCT_DELETE")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @Parameter(description = "Product UUID", required = true) @PathVariable String id,
            @Parameter(hidden = true) Authentication authentication) {
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
    @Operation(summary = "Search Products", description = "Searches products by name or other fields")
    @GetMapping("/search")
    @RequirePermission("PRODUCT_VIEW_ALL")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> searchProducts(
            @Parameter(description = "Search query", required = true) @RequestParam String query) {
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
