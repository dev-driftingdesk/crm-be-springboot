package com.ceedpods.crmbuild.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveMetaCredentialRequest {

    @NotBlank(message = "Access token is required")
    private String accessToken;

    @NotBlank(message = "Phone number ID is required")
    private String phoneNumberId;

    @NotBlank(message = "Business account ID is required")
    private String businessAccountId;
}
