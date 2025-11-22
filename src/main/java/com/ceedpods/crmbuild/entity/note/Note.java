package com.ceedpods.crmbuild.entity.note;

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
 * Note entity - represents notes attached to deals
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = AppConstants.MongoDB.COLLECTION_DEAL_NOTES)
public class Note extends BaseEntity {

    @Id
    private String id; // UUID as primary ID (noteId)

    private String dealId; // Deal ID reference (required)

    private String noteTitle; // Note title (optional)

    private String noteContent; // Note content/description (required)
}
