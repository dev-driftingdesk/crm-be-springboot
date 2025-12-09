package com.ceedpods.crmbuild.dto.request;

import com.ceedpods.crmbuild.enums.DiscountAddOnType;
import com.ceedpods.crmbuild.enums.PackageType;
import com.ceedpods.crmbuild.enums.ProductFormat;
import com.ceedpods.crmbuild.enums.ProductLevel;
import com.ceedpods.crmbuild.enums.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @Valid
    @NotNull(message = "Basic information is required")
    private BasicInformation basicInformation;

    @Valid
    private PricingPackagesRequest pricingPackages;

    private List<@Valid DiscountAddOnRequest> discountsAddOns;

    @NotNull(message = "Product status is required")
    private ProductStatus productStatus;

    // ===== Nested DTOs =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BasicInformation {

        @NotBlank(message = "Product name is required")
        @Size(max = 200, message = "Product name must not exceed 200 characters")
        private String productName;

        @NotNull(message = "Base price is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Base price must be greater than or equal to 0")
        private BigDecimal basePrice;

        @NotBlank(message = "Key learning outcomes is required")
        @Size(max = 2000, message = "Key learning outcomes must not exceed 2000 characters")
        private String keyLearningOutcomes;

        @NotNull(message = "Format is required")
        private ProductFormat format;

        @NotBlank(message = "Duration is required")
        @Size(max = 100, message = "Duration must not exceed 100 characters")
        private String duration;

        @NotNull(message = "Level is required")
        private ProductLevel level;

        private List<String> instructors;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingPackagesRequest {

        @NotNull(message = "Enabled flag is required for pricing packages")
        private Boolean enabled;

        private List<@Valid PackageRequest> packages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PackageRequest {

        @NotNull(message = "Package type is required")
        private PackageType packageType;

        @NotBlank(message = "Package description is required")
        @Size(max = 500, message = "Package description must not exceed 500 characters")
        private String description;

        @NotNull(message = "Package price is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Package price must be greater than or equal to 0")
        private BigDecimal price;

        @NotNull(message = "Commission rate is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Commission rate must be greater than or equal to 0")
        private BigDecimal commissionRate;

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscountAddOnRequest {

        @NotNull(message = "Discount/Add-on type is required")
        private DiscountAddOnType type;

        @NotBlank(message = "Description is required")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;
    }
}
