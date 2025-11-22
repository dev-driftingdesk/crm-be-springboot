package com.ceedpods.crmbuild.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new lead note
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeadNoteRequest {

    @NotBlank(message = "Lead ID is required")
    @Size(max = 50, message = "Lead ID must not exceed 50 characters")
    private String leadId; // Lead ID reference (required)

    @Size(max = 200, message = "Note title must not exceed 200 characters")
    private String noteTitle; // Note title (optional)

    @NotBlank(message = "Note content is required")
    @Size(max = 5000, message = "Note content must not exceed 5000 characters")
    private String noteContent; // Note content/description (required)
}
