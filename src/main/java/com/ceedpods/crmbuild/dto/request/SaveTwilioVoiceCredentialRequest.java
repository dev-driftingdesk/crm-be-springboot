package com.ceedpods.crmbuild.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveTwilioVoiceCredentialRequest {

    @NotBlank(message = "Twilio Account SID is required")
    private String accountSid;

    @NotBlank(message = "Twilio Auth Token is required")
    private String authToken;

    @NotBlank(message = "Twilio phone number is required")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone number must be in E.164 format (e.g., +14155552671)")
    private String phoneNumber;
}
