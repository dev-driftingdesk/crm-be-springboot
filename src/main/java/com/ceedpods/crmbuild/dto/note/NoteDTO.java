package com.ceedpods.crmbuild.dto.note;

import com.ceedpods.crmbuild.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Note entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({"id", "dealId", "noteTitle", "noteContent",
                     "createdAt", "updatedAt", "createdBy", "updatedBy", "deleted", "deletedAt", "deletedBy"})
public class NoteDTO extends BaseDTO {

    private String id; // UUID as string (noteId)
    private String dealId; // Deal ID reference
    private String noteTitle; // Note title
    private String noteContent; // Note content/description
}
