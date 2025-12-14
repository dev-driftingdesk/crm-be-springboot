package com.ceedpods.crmbuild.entity.actionitem;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.entity.BaseEntity;
import com.ceedpods.crmbuild.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = AppConstants.MongoDB.COLLECTION_ACTION_ITEMS)
public class ActionItem extends BaseEntity {

    @Id
    private String id;

    private String dealId;

    private String title;

    private LocalDate dueDate;

    private String assignedTo; // User ID

    private Priority priority;

    @Builder.Default
    private String status = "pending"; // pending, in_progress, completed
}
