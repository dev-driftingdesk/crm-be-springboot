package com.ceedpods.crmbuild.entity.product;

import com.ceedpods.crmbuild.enums.PackageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Embedded document for pricing package within a Product
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingPackage {

    private PackageType packageType;

    private String description;

    private BigDecimal price;

    private BigDecimal commissionRate;

    private String notes;
}
