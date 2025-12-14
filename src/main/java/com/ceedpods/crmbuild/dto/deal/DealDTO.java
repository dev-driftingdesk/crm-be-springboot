package com.ceedpods.crmbuild.dto.deal;

import com.ceedpods.crmbuild.dto.BaseDTO;
import com.ceedpods.crmbuild.enums.DealStatus;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for Deal entity representing the full deal information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({"id", "dealName", "status", "dealValue", "commission", "products", "salesRepresentatives", "leadId",
                     "createdAt", "updatedAt", "createdBy", "updatedBy", "deleted", "deletedAt", "deletedBy"})
public class DealDTO extends BaseDTO {

    private String id; // UUID as string (dealId)
    private String dealName;
    private DealStatus status; // Deal status (OPEN, WON, LOST, PENDING, NEGOTIATION)
    private BigDecimal dealValue; // Calculated total value of the deal from products
    private BigDecimal commission; // Commission amount for this deal
    private List<DealProduct> products; // List of products with productId, packageType, quantity
    private List<SalesRepAssignment> salesRepresentatives; // List of sales rep assignments with userId and role
    private String leadId; // Associated lead ID
}
