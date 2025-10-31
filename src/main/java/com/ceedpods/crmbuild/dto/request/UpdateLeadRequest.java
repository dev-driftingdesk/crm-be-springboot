package com.ceedpods.crmbuild.dto.request;

import com.ceedpods.crmbuild.enums.LeadOriginatedFrom;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLeadRequest {

    // Note: leadId (UUID) cannot be updated

    private LeadOriginatedFrom originatedFrom;

    @Size(max = 200, message = "Lead name must not exceed 200 characters")
    private String leadName;

    @Size(max = 200, message = "Company must not exceed 200 characters")
    private String company;

    @Size(max = 500, message = "Company address must not exceed 500 characters")
    private String companyAddress;

    @Size(max = 200, message = "Company website must not exceed 200 characters")
    private String companyWebsite;

    private List<Map<String, String>> communication; // Array of key-value pairs

    @Size(max = 100, message = "Platform must not exceed 100 characters")
    private String platform;

    @Size(max = 20, message = "Contact number must not exceed 20 characters")
    private String contactNumber;

    @NotBlank(message = "Deal ID is required")
    @Size(max = 50, message = "Deal ID must not exceed 50 characters")
    private String dealId; // Required field - dealId must be assigned
}
