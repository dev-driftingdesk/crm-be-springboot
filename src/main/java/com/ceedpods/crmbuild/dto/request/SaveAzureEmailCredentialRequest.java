package com.ceedpods.crmbuild.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveAzureEmailCredentialRequest {

    @NotBlank(message = "Azure Communication Services connection string is required")
    private String connectionString;

    @NotBlank(message = "Sender email address is required")
    @Email(message = "Sender email address must be valid")
    private String senderAddress;
}
