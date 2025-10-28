package com.ceedpods.crmbuild.dto.product;

import com.ceedpods.crmbuild.dto.BaseDTO;
import com.ceedpods.crmbuild.enums.ProductStatus;
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
public class ProductDTO extends BaseDTO {

    private String id;
    private String productId;
    private String productName;
    private String productDescription;
    private String productSubDescription;
    private BigDecimal productValue;
    private ProductStatus productStatus;
}
