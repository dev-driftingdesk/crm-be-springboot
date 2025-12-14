package com.ceedpods.crmbuild.dto.deal;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for sales representative assignment within a deal.
 * Contains user reference and their role on the deal.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesRepAssignment {

    @NotBlank(message = "User ID is required")
    private String userId; // User ID (UUID)

    @NotBlank(message = "Role is required")
    private String role; // Role on the deal (e.g., "Account Manager", "Sales Representative")
}
