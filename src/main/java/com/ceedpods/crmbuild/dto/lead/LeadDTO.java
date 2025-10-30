package com.ceedpods.crmbuild.dto.lead;

import com.ceedpods.crmbuild.dto.BaseDTO;
import com.ceedpods.crmbuild.enums.LeadOriginatedFrom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LeadDTO extends BaseDTO {

    private String id;
    private String leadId; // UUID as string
    private LeadOriginatedFrom originatedFrom;
    private String leadName;
    private String company;
    private String companyAddress;
    private String companyWebsite;
    private List<Map<String, String>> communication;
    private String platform;
    private String contactNumber;
    private String dealId;
}
