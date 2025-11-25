package com.ceedpods.crmbuild.dto.deal;

import com.ceedpods.crmbuild.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({"id", "dealName", "productIds", "salesReps", "leadId",
                     "createdAt", "updatedAt", "createdBy", "updatedBy", "deleted", "deletedAt", "deletedBy"})
public class DealDTO extends BaseDTO {

    private String id; // UUID as string (dealId)
    private String dealName;
    private List<String> productIds;
    private List<SalesRepAssignment> salesReps; // List of sales rep assignments with id and position
    private String leadId;
}
