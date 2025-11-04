package com.ceedpods.crmbuild.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for initiating password reset process.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {

    /**
     * User's email address to send the reset code to
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
}
