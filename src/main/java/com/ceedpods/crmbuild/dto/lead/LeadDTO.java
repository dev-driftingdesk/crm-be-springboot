package com.ceedpods.crmbuild.dto.lead;

import com.ceedpods.crmbuild.dto.BaseDTO;
import com.ceedpods.crmbuild.enums.LeadOriginatedFrom;
import com.ceedpods.crmbuild.enums.LeadStatus;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({"id", "originatedFrom", "status", "leadName", "createdUserName", "company", "companyAddress",
                     "companyWebsite", "communication", "platform", "contactNumber", "dealIds",
                     "totalValue", "totalDeals",
                     "createdAt", "updatedAt", "createdBy", "updatedBy", "deleted", "deletedAt", "deletedBy"})
public class LeadDTO extends BaseDTO {

    private String id; // UUID as string
    private LeadOriginatedFrom originatedFrom;
    private LeadStatus status;
    private String leadName;
    private String createdUserName; // Name of user who created the lead
    private String company;
    private String companyAddress;
    private String companyWebsite;
    private List<Map<String, String>> communication;
    private String platform;
    private String contactNumber;
    private List<String> dealIds;

    // Calculated fields
    private BigDecimal totalValue; // Sum of product values from associated deals
    private long totalDeals; // Count of deals under this lead
}
