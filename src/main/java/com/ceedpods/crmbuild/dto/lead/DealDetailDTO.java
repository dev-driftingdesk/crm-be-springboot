package com.ceedpods.crmbuild.dto.lead;

import com.ceedpods.crmbuild.enums.DealStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"id", "dealName", "status", "commission", "dealValue", "products", "salesReps",
                     "createdAt", "updatedAt"})
public class DealDetailDTO {

    private String id;
    private String dealName;
    private DealStatus status;
    private BigDecimal commission;
    private BigDecimal dealValue; // Sum of product values in this deal

    private List<ProductSummaryDTO> products; // List of products with details
    private List<SalesRepDetailDTO> salesReps; // List of sales reps with details

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSummaryDTO {
        private String id;
        private String productName;
        private BigDecimal productValue;
        private String productStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesRepDetailDTO {
        private String id;
        private String fullName;
        private String email;
        private String position; // PRIMARY, CO_PRIMARY, CONSULTANT
    }
}
