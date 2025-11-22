package com.ceedpods.crmbuild.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing lead note
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLeadNoteRequest {

    @Size(max = 200, message = "Note title must not exceed 200 characters")
    private String noteTitle; // Note title (optional)

    @Size(max = 5000, message = "Note content must not exceed 5000 characters")
    private String noteContent; // Note content/description (optional)
}
