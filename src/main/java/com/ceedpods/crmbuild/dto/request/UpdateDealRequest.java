package com.ceedpods.crmbuild.dto.request;

import com.ceedpods.crmbuild.dto.deal.SalesRepAssignment;
import jakarta.validation.Valid;
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
public class UpdateDealRequest {

    // Note: dealId (UUID) cannot be updated

    @Size(max = 200, message = "Deal name must not exceed 200 characters")
    private String dealName;

    private List<String> productIds; // List of product IDs (one or more)

    @Valid
    private List<SalesRepAssignment> salesReps; // List of sales rep assignments with id and position

    @Size(max = 50, message = "Lead ID must not exceed 50 characters")
    private String leadId; // Single lead ID
}
