package com.ceedpods.crmbuild.entity.product;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.entity.BaseEntity;
import com.ceedpods.crmbuild.enums.ProductFormat;
import com.ceedpods.crmbuild.enums.ProductLevel;
import com.ceedpods.crmbuild.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = AppConstants.MongoDB.COLLECTION_PRODUCTS)
public class Product extends BaseEntity {

    @Id
    private String id;

    // ===== Basic Information =====
    private String productName;
    private BigDecimal basePrice;
    private String keyLearningOutcomes;
    private ProductFormat format;
    private String duration;
    private ProductLevel level;
    private List<String> instructors;

    // ===== Pricing & Packages =====
    private PricingPackages pricingPackages;

    // ===== Discounts & Add-ons =====
    private List<DiscountAddOn> discountsAddOns;

    // ===== Product Status =====
    private ProductStatus productStatus;
}
