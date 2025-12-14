package com.ceedpods.crmbuild.dto.request;

import com.ceedpods.crmbuild.dto.deal.DealProduct;
import com.ceedpods.crmbuild.dto.deal.SalesRepAssignment;
import com.ceedpods.crmbuild.enums.DealStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for updating an existing deal.
 * Note: dealId (UUID) cannot be updated.
 * Note: dealValue will be recalculated from products if products are updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDealRequest {

    @Size(max = 200, message = "Deal name must not exceed 200 characters")
    private String dealName;

    private DealStatus status; // Deal status

    @DecimalMin(value = "0.0", message = "Commission must be a positive value")
    private BigDecimal commission; // Commission amount for this deal

    @Valid
    private List<DealProduct> products; // Products in the deal with productId, packageType, quantity

    @Valid
    private List<SalesRepAssignment> assignedAgents; // Agents assigned to deal with userId and role

    @Size(max = 50, message = "Lead ID must not exceed 50 characters")
    private String leadId; // Associated lead ID
}
