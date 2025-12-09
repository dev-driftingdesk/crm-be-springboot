package com.ceedpods.crmbuild.dto.request;

import com.ceedpods.crmbuild.enums.DiscountAddOnType;
import com.ceedpods.crmbuild.enums.PackageType;
import com.ceedpods.crmbuild.enums.ProductFormat;
import com.ceedpods.crmbuild.enums.ProductLevel;
import com.ceedpods.crmbuild.enums.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
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
public class UpdateProductRequest {

    @Valid
    private BasicInformation basicInformation;

    @Valid
    private PricingPackagesRequest pricingPackages;

    private List<@Valid DiscountAddOnRequest> discountsAddOns;

    private ProductStatus productStatus;

    // ===== Nested DTOs =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BasicInformation {

        @Size(max = 200, message = "Product name must not exceed 200 characters")
        private String productName;

        @DecimalMin(value = "0.0", inclusive = true, message = "Base price must be greater than or equal to 0")
        private BigDecimal basePrice;

        @Size(max = 2000, message = "Key learning outcomes must not exceed 2000 characters")
        private String keyLearningOutcomes;

        private ProductFormat format;

        @Size(max = 100, message = "Duration must not exceed 100 characters")
        private String duration;

        private ProductLevel level;

        private List<String> instructors;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingPackagesRequest {
        private Boolean enabled;
        private List<@Valid PackageRequest> packages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PackageRequest {

        private PackageType packageType;

        @Size(max = 500, message = "Package description must not exceed 500 characters")
        private String description;

        @DecimalMin(value = "0.0", inclusive = true, message = "Package price must be greater than or equal to 0")
        private BigDecimal price;

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

        private DiscountAddOnType type;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;
    }
}
