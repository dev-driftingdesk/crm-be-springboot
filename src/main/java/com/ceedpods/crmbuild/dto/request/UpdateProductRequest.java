package com.ceedpods.crmbuild.dto.request;

import com.ceedpods.crmbuild.enums.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    @Size(max = 50, message = "Product ID must not exceed 50 characters")
    private String productId;

    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String productName;

    @Size(max = 1000, message = "Product description must not exceed 1000 characters")
    private String productDescription;

    @Size(max = 1000, message = "Product sub-description must not exceed 1000 characters")
    private String productSubDescription;

    @DecimalMin(value = "0.0", inclusive = true, message = "Product value must be greater than or equal to 0")
    private BigDecimal productValue;

    private ProductStatus productStatus;
}
