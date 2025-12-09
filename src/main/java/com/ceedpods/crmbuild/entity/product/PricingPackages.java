package com.ceedpods.crmbuild.entity.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Embedded document wrapper for pricing packages configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingPackages {

    private Boolean enabled;

    private List<PricingPackage> packages;
}
