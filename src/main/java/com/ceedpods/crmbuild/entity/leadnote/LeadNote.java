package com.ceedpods.crmbuild.entity.leadnote;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * LeadNote entity - represents notes attached to leads
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = AppConstants.MongoDB.COLLECTION_LEAD_NOTES)
public class LeadNote extends BaseEntity {

    @Id
    private String id; // UUID as primary ID (leadNoteId)

    private String leadId; // Lead ID reference (required)

    private String noteTitle; // Note title (optional)

    private String noteContent; // Note content/description (required)
}
