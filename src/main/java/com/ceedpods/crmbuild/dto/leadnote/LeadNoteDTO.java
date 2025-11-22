package com.ceedpods.crmbuild.dto.leadnote;

import com.ceedpods.crmbuild.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Response DTO for LeadNote entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({"id", "leadId", "noteTitle", "noteContent",
                     "createdAt", "updatedAt", "createdBy", "updatedBy", "deleted", "deletedAt", "deletedBy"})
public class LeadNoteDTO extends BaseDTO {

    private String id; // UUID as string (leadNoteId)
    private String leadId; // Lead ID reference
    private String noteTitle; // Note title
    private String noteContent; // Note content/description
}
