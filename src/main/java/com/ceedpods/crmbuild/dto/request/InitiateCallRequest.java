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
public class InitiateCallRequest {

    @NotBlank(message = "Recipient user ID is required")
    private String recipientUserId; // Keycloak ID of the user to call

    private Boolean record; // Whether to record the call (optional, defaults to false)

    private String statusCallbackUrl; // Optional webhook URL for call status updates
}
