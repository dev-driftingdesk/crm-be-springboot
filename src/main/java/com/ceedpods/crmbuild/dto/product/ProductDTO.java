package com.ceedpods.crmbuild.dto.product;

import com.ceedpods.crmbuild.dto.BaseDTO;
import com.ceedpods.crmbuild.enums.ProductStatus;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({"id", "productId", "productName", "productDescription", "productSubDescription",
                     "productValue", "productStatus",
                     "createdAt", "updatedAt", "createdBy", "updatedBy", "deleted", "deletedAt", "deletedBy"})
public class ProductDTO extends BaseDTO {

    private String id;
    private String productId;
    private String productName;
    private String productDescription;
    private String productSubDescription;
    private BigDecimal productValue;
    private ProductStatus productStatus;
}
