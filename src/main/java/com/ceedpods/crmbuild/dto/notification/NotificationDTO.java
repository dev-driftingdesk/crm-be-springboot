package com.ceedpods.crmbuild.dto.notification;

import com.ceedpods.crmbuild.enums.NotificationStatus;
import com.ceedpods.crmbuild.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private String id;
    private String senderId;
    private List<String> recipientUserIds;
    private String topic;

    private NotificationType type;
    private NotificationStatus status;

    private String title;
    private String body;
    private String imageUrl;
    private Map<String, String> data;
    private String clickAction;

    private String fcmMessageId;
    private Integer successCount;
    private Integer failureCount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveredAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private String relatedEntityType;
    private String relatedEntityId;

    // Read status (populated when fetching for a specific user)
    private Boolean read;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readAt;
}
