package com.ceedpods.crmbuild.dto.lead;

import com.ceedpods.crmbuild.enums.LeadOriginatedFrom;
import com.ceedpods.crmbuild.enums.LeadStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"id", "leadName", "status", "originatedFrom",
                     "personalInfo", "communication", "companyDetails",
                     "statistics", "deals",
                     "createdAt", "updatedAt", "createdBy", "updatedBy"})
public class LeadDetailDTO {

    // Lead identification
    private String id;
    private String leadName;
    private LeadStatus status;
    private LeadOriginatedFrom originatedFrom;

    // Personal Info section
    private PersonalInfoDTO personalInfo;

    // Communication section (phone, email, WhatsApp, etc.)
    private List<Map<String, String>> communication;

    // Company Details section
    private CompanyDetailsDTO companyDetails;

    // Lead Statistics section
    private LeadStatisticsDTO statistics;

    // All deals under this lead
    private List<DealDetailDTO> deals;

    // Audit fields
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;
    private String createdByName; // Enriched: Name of user who created the lead
    private String updatedBy;
    private String updatedByName; // Enriched: Name of user who last updated the lead

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalInfoDTO {
        private String contactPersonName; // Same as leadName
        private String contactNumber;
        private String platform;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyDetailsDTO {
        private String companyName;
        private String companyAddress;
        private String companyWebsite;
        private String industry; // May be null if not set
    }
}
