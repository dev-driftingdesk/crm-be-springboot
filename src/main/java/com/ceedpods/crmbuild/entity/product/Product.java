package com.ceedpods.crmbuild.entity.product;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.entity.BaseEntity;
import com.ceedpods.crmbuild.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = AppConstants.MongoDB.COLLECTION_PRODUCTS)
public class Product extends BaseEntity {

    @Id
    private String id;


    private String productName;
    private String productDescription;
    private String productSubDescription;
    private BigDecimal productValue;
    private ProductStatus productStatus;
}
