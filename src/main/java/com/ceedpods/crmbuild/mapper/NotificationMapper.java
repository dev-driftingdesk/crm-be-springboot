package com.ceedpods.crmbuild.mapper;

import com.ceedpods.crmbuild.dto.notification.DeviceTokenDTO;
import com.ceedpods.crmbuild.dto.notification.NotificationDTO;
import com.ceedpods.crmbuild.dto.notification.ScheduledNotificationDTO;
import com.ceedpods.crmbuild.entity.notification.DeviceToken;
import com.ceedpods.crmbuild.entity.notification.Notification;
import com.ceedpods.crmbuild.entity.notification.ScheduledNotification;
import com.ceedpods.crmbuild.entity.notification.UserNotificationStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotificationMapper {

    public NotificationDTO toDTO(Notification notification) {
        if (notification == null) return null;

        return NotificationDTO.builder()
                .id(notification.getId())
                .senderId(notification.getSenderId())
                .recipientUserIds(notification.getRecipientUserIds())
                .topic(notification.getTopic())
                .type(notification.getType())
                .status(notification.getStatus())
                .title(notification.getTitle())
                .body(notification.getBody())
                .imageUrl(notification.getImageUrl())
                .data(notification.getData())
                .clickAction(notification.getClickAction())
                .fcmMessageId(notification.getFcmMessageId())
                .successCount(notification.getSuccessCount())
                .failureCount(notification.getFailureCount())
                .sentAt(notification.getSentAt())
                .deliveredAt(notification.getDeliveredAt())
                .createdAt(notification.getCreatedAt())
                .relatedEntityType(notification.getRelatedEntityType())
                .relatedEntityId(notification.getRelatedEntityId())
                .build();
    }

    public List<NotificationDTO> toNotificationDTOList(List<Notification> notifications) {
        return notifications.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Convert Notification to DTO with read status from UserNotificationStatus
     */
    public NotificationDTO toDTO(Notification notification, UserNotificationStatus readStatus) {
        if (notification == null) return null;

        return NotificationDTO.builder()
                .id(notification.getId())
                .senderId(notification.getSenderId())
                .recipientUserIds(notification.getRecipientUserIds())
                .topic(notification.getTopic())
                .type(notification.getType())
                .status(notification.getStatus())
                .title(notification.getTitle())
                .body(notification.getBody())
                .imageUrl(notification.getImageUrl())
                .data(notification.getData())
                .clickAction(notification.getClickAction())
                .fcmMessageId(notification.getFcmMessageId())
                .successCount(notification.getSuccessCount())
                .failureCount(notification.getFailureCount())
                .sentAt(notification.getSentAt())
                .deliveredAt(notification.getDeliveredAt())
                .createdAt(notification.getCreatedAt())
                .relatedEntityType(notification.getRelatedEntityType())
                .relatedEntityId(notification.getRelatedEntityId())
                .read(readStatus != null ? readStatus.isRead() : null)
                .readAt(readStatus != null ? readStatus.getReadAt() : null)
                .build();
    }

    public DeviceTokenDTO toDTO(DeviceToken deviceToken) {
        if (deviceToken == null) return null;

        return DeviceTokenDTO.builder()
                .id(deviceToken.getId())
                .userId(deviceToken.getUserId())
                .platform(deviceToken.getPlatform())
                .deviceName(deviceToken.getDeviceName())
                .deviceModel(deviceToken.getDeviceModel())
                .osVersion(deviceToken.getOsVersion())
                .appVersion(deviceToken.getAppVersion())
                .active(deviceToken.isActive())
                .subscribedTopics(deviceToken.getSubscribedTopics())
                .registeredAt(deviceToken.getRegisteredAt())
                .lastUsedAt(deviceToken.getLastUsedAt())
                .build();
    }

    public List<DeviceTokenDTO> toDeviceTokenDTOList(List<DeviceToken> deviceTokens) {
        return deviceTokens.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ScheduledNotificationDTO toDTO(ScheduledNotification scheduled) {
        if (scheduled == null) return null;

        return ScheduledNotificationDTO.builder()
                .id(scheduled.getId())
                .creatorId(scheduled.getCreatorId())
                .recipientUserIds(scheduled.getRecipientUserIds())
                .topic(scheduled.getTopic())
                .title(scheduled.getTitle())
                .body(scheduled.getBody())
                .imageUrl(scheduled.getImageUrl())
                .data(scheduled.getData())
                .clickAction(scheduled.getClickAction())
                .type(scheduled.getType())
                .status(scheduled.getStatus())
                .scheduledAt(scheduled.getScheduledAt())
                .sentAt(scheduled.getSentAt())
                .createdAt(scheduled.getCreatedAt())
                .notificationId(scheduled.getNotificationId())
                .cancellationReason(scheduled.getCancellationReason())
                .build();
    }

    public List<ScheduledNotificationDTO> toScheduledNotificationDTOList(List<ScheduledNotification> scheduled) {
        return scheduled.stream().map(this::toDTO).collect(Collectors.toList());
    }
}
