package com.ceedpods.crmbuild.dto.request;

import jakarta.validation.constraints.Email;
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
public class SaveSmtpEmailCredentialRequest {

    @NotBlank(message = "SMTP host is required")
    private String smtpHost;

    @NotBlank(message = "SMTP port is required")
    @Pattern(regexp = "^[0-9]{1,5}$", message = "SMTP port must be a valid port number (1-65535)")
    private String smtpPort;

    @NotBlank(message = "SMTP username is required")
    private String smtpUsername;

    @NotBlank(message = "SMTP password is required")
    private String smtpPassword;

    @NotBlank(message = "Sender email address is required")
    @Email(message = "Sender email address must be valid")
    private String senderAddress;
}
