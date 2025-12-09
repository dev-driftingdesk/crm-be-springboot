package com.ceedpods.crmbuild.entity.product;

import com.ceedpods.crmbuild.enums.DiscountAddOnType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded document for discount or add-on within a Product
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountAddOn {

    private DiscountAddOnType type;

    private String description;
}
