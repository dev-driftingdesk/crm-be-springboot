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
@Document(collection = AppConstants.MongoDB.COLLECTION_NOTIFICATIONS)
public class Notification extends BaseEntity {

    @Id
    private String id;

    @Indexed
    private String senderId;

    @Indexed
    private List<String> recipientUserIds;

    private String topic;

    private NotificationType type;

    @Indexed
    private NotificationStatus status;

    private String title;
    private String body;
    private String imageUrl;
    private Map<String, String> data;
    private String clickAction;

    private String fcmMessageId;
    private Integer successCount;
    private Integer failureCount;
    private List<String> failedTokens;

    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;

    @Builder.Default
    private Integer retryCount = 0;

    @Builder.Default
    private Integer maxRetries = 3;

    private LocalDateTime nextRetryAt;
    private LocalDateTime lastRetryAt;
    private String failureReason;

    private String relatedEntityType;
    private String relatedEntityId;
}
