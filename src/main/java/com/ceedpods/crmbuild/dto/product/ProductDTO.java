package com.ceedpods.crmbuild.dto.product;

import com.ceedpods.crmbuild.dto.BaseDTO;
import com.ceedpods.crmbuild.enums.DiscountAddOnType;
import com.ceedpods.crmbuild.enums.PackageType;
import com.ceedpods.crmbuild.enums.ProductFormat;
import com.ceedpods.crmbuild.enums.ProductLevel;
import com.ceedpods.crmbuild.enums.ProductStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({
    "id", "productName", "basePrice", "keyLearningOutcomes", "format", "duration", "level", "instructors",
    "pricingPackages", "discountsAddOns", "productStatus",
    "createdUserName", "createdUserProfilePicture", "inDealCount", "totalSales", "revenue",
    "createdAt", "updatedAt", "createdBy", "updatedBy", "deleted", "deletedAt", "deletedBy"
})
public class ProductDTO extends BaseDTO {

    private String id;

    // ===== Basic Information =====
    private String productName;

    @Schema(description = "Base price of the product", example = "1500.00")
    private BigDecimal basePrice;

    @Schema(description = "Key learning outcomes", example = "Master advanced sales techniques")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String keyLearningOutcomes;

    @Schema(description = "Product format", example = "hybrid")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ProductFormat format;

    @Schema(description = "Duration of the course/product", example = "6 weeks")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String duration;

    @Schema(description = "Difficulty level", example = "intermediate")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ProductLevel level;

    @Schema(description = "List of instructor IDs", example = "[\"instructor-01\", \"instructor-02\"]")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> instructors;

    // ===== Pricing & Packages =====
    @Schema(description = "Pricing packages configuration")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PricingPackagesDTO pricingPackages;

    // ===== Discounts & Add-ons =====
    @Schema(description = "List of discounts and add-ons")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<DiscountAddOnDTO> discountsAddOns;

    // ===== Product Status =====
    private ProductStatus productStatus;

    // ===== Enhanced fields for Get All Products API =====

    @Schema(description = "Full name of the user who created this product", example = "John Doe")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String createdUserName;

    @Schema(description = "Profile picture of the user who created this product (Base64 encoded)", example = "data:image/png;base64,iVBORw0KGgo...")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String createdUserProfilePicture;

    @Schema(description = "Number of deals that include this product", example = "15")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long inDealCount;

    @Schema(description = "Total number of sales (deals) for this product", example = "15")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long totalSales;

    @Schema(description = "Total revenue generated (basePrice × totalSales)", example = "89999.85")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal revenue;

    // ===== Nested DTOs =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingPackagesDTO {
        private Boolean enabled;
        private List<PackageDTO> packages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PackageDTO {
        private PackageType packageType;
        private String description;
        private BigDecimal price;
        private BigDecimal commissionRate;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscountAddOnDTO {
        private DiscountAddOnType type;
        private String description;
    }
}
