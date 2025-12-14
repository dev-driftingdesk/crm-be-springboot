package com.ceedpods.crmbuild.dto.deal;

import com.ceedpods.crmbuild.enums.PackageType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for product items within a deal.
 * Contains product reference, package type selection, and quantity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealProduct {

    @NotBlank(message = "Product ID is required")
    private String productId;

    private PackageType packageType; // standard, pro, or enterprise (optional)

    @Min(value = 1, message = "Quantity must be at least 1")
    @Builder.Default
    private Integer quantity = 1; // Default to 1 if not provided
}
