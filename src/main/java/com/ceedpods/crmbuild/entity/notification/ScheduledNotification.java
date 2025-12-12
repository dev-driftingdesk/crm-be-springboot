package com.ceedpods.crmbuild.entity.notification;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.entity.BaseEntity;
import com.ceedpods.crmbuild.enums.NotificationStatus;
import com.ceedpods.crmbuild.enums.NotificationType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = AppConstants.MongoDB.COLLECTION_SCHEDULED_NOTIFICATIONS)
public class ScheduledNotification extends BaseEntity {

    @Id
    private String id;

    @Indexed
    private String creatorId;

    private List<String> recipientUserIds;
    private String topic;

    private String title;
    private String body;
    private String imageUrl;
    private Map<String, String> data;
    private String clickAction;

    private NotificationType type;

    @Indexed
    private NotificationStatus status;

    @Indexed
    private LocalDateTime scheduledAt;

    private LocalDateTime sentAt;

    private String notificationId;

    private String cancellationReason;
}
