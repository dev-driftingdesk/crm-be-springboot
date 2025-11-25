package com.ceedpods.crmbuild.dto.request;

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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDealRequest {

    // Note: dealId will be auto-generated as UUID by the service layer

    @NotBlank(message = "Deal name is required")
    @Size(max = 200, message = "Deal name must not exceed 200 characters")
    private String dealName;

    @NotNull(message = "Product IDs are required")
    @NotEmpty(message = "At least one product ID is required")
    private List<String> productIds; // List of product IDs (one or more required)

    @Valid
    private List<SalesRepAssignment> salesReps; // List of sales rep assignments with id and position (optional)

    @Size(max = 50, message = "Lead ID must not exceed 50 characters")
    private String leadId; // Single lead ID (optional)
}
