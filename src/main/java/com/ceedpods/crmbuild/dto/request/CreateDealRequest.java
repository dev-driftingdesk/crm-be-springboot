package com.ceedpods.crmbuild.dto.request;

import com.ceedpods.crmbuild.dto.deal.DealProduct;
import com.ceedpods.crmbuild.dto.deal.SalesRepAssignment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating a new deal.
 * Note: dealId will be auto-generated as UUID by the service layer.
 * Note: dealValue will be calculated from products in the service layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDealRequest {

    @NotBlank(message = "Deal name is required")
    @Size(max = 200, message = "Deal name must not exceed 200 characters")
    private String dealName;

    @NotBlank(message = "Lead ID is required")
    @Size(max = 50, message = "Lead ID must not exceed 50 characters")
    private String leadId; // Associated lead ID (required)

    @NotNull(message = "Sales representatives are required")
    @NotEmpty(message = "At least one sales representative is required")
    @Valid
    private List<SalesRepAssignment> salesRepresentatives; // Agents assigned to deal (required)

    @NotNull(message = "Products are required")
    @NotEmpty(message = "At least one product is required")
    @Valid
    private List<DealProduct> products; // Products in the deal (required)
}
