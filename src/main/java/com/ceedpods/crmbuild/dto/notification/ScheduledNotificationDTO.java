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
public class ScheduledNotificationDTO {

    private String id;
    private String creatorId;
    private List<String> recipientUserIds;
    private String topic;

    private String title;
    private String body;
    private String imageUrl;
    private Map<String, String> data;
    private String clickAction;

    private NotificationType type;
    private NotificationStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private String notificationId;
    private String cancellationReason;
}
