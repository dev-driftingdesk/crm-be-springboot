package com.ceedpods.crmbuild.dto.product;

import com.ceedpods.crmbuild.dto.BaseDTO;
import com.ceedpods.crmbuild.enums.ProductStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
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
@JsonPropertyOrder({
    "id", "productName", "createdUserName", "inDealCount", "basePrice", "totalSales", "revenue",
    "productDescription", "productSubDescription", "productValue", "productStatus",
    "createdAt", "updatedAt", "createdBy", "updatedBy", "deleted", "deletedAt", "deletedBy"
})
public class ProductDTO extends BaseDTO {

    private String id;

    private String productName;

    private String productDescription;

    private String productSubDescription;

    private BigDecimal productValue;

    private ProductStatus productStatus;

    // ===== Enhanced fields for Get All Products API =====

    @Schema(description = "Full name of the user who created this product", example = "John Doe")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String createdUserName;

    @Schema(description = "Number of deals that include this product", example = "15")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long inDealCount;

    @Schema(description = "Base price of the product (same as productValue)", example = "5999.99")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal basePrice;

    @Schema(description = "Total number of sales (deals) for this product", example = "15")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long totalSales;

    @Schema(description = "Total revenue generated (basePrice × totalSales)", example = "89999.85")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal revenue;
}
