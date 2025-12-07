package com.ceedpods.crmbuild.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for bulk user registration
 * Contains a list of email addresses to register as SALES_REP users
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for bulk user registration with multiple email addresses")
public class BulkUserRegistrationRequest {

    @NotEmpty(message = "Email list cannot be empty")
    @Size(min = 1, max = 100, message = "Email list must contain between 1 and 100 emails")
    @Schema(description = "List of email addresses to register as sales users",
            example = "[\"lahiru@gmail.com\", \"kasun@gmail.com\", \"nimal@company.com\"]",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> emails;
}
