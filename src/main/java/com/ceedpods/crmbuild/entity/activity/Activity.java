package com.ceedpods.crmbuild.entity.activity;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.entity.BaseEntity;
import com.ceedpods.crmbuild.enums.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Activity entity - represents activities performed on deals
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = AppConstants.MongoDB.COLLECTION_ACTIVITIES)
public class Activity extends BaseEntity {

    @Id
    private String id; // UUID as primary ID (activityId)

    private String dealId; // Deal ID reference (required)

    private ActivityType type; // Activity type (call, email, meeting, etc.)

    private String description; // Activity description

    private LocalDateTime performedAt; // When the activity was performed

    private String performedBy; // User ID who performed the activity (keycloakId)
}
