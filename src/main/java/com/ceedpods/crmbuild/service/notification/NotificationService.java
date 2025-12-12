package com.ceedpods.crmbuild.service.notification;

import com.ceedpods.crmbuild.dto.notification.DeviceTokenDTO;
import com.ceedpods.crmbuild.dto.notification.NotificationDTO;
import com.ceedpods.crmbuild.dto.notification.ScheduledNotificationDTO;
import com.ceedpods.crmbuild.dto.request.*;
import com.ceedpods.crmbuild.entity.notification.DeviceToken;
import com.ceedpods.crmbuild.entity.notification.Notification;
import com.ceedpods.crmbuild.entity.notification.ScheduledNotification;
import com.ceedpods.crmbuild.entity.notification.UserNotificationStatus;
import com.ceedpods.crmbuild.enums.NotificationStatus;
import com.ceedpods.crmbuild.enums.NotificationType;
import com.ceedpods.crmbuild.exception.BadRequestException;
import com.ceedpods.crmbuild.exception.ResourceNotFoundException;
import com.ceedpods.crmbuild.mapper.NotificationMapper;
import com.ceedpods.crmbuild.repository.DeviceTokenRepository;
import com.ceedpods.crmbuild.repository.NotificationRepository;
import com.ceedpods.crmbuild.repository.ScheduledNotificationRepository;
import com.ceedpods.crmbuild.repository.UserNotificationStatusRepository;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationRepository notificationRepository;
    private final ScheduledNotificationRepository scheduledNotificationRepository;
    private final UserNotificationStatusRepository userNotificationStatusRepository;
    private final FirebasePushNotificationService firebasePushService;
    private final NotificationMapper notificationMapper;

    @Value("${app.push-notification.retry.max-retries:3}")
    private int maxRetries;

    @Value("${app.push-notification.retry.initial-delay-minutes:5}")
    private int initialDelayMinutes;

    // ==================== Device Token Management ====================

    public DeviceTokenDTO registerDeviceToken(String userId, RegisterDeviceTokenRequest request) {
        log.info("Registering device token for user {}", userId);

        Optional<DeviceToken> existingToken = deviceTokenRepository.findByToken(request.getToken());

        DeviceToken deviceToken;
        if (existingToken.isPresent()) {
            deviceToken = existingToken.get();
            deviceToken.setUserId(userId);
            deviceToken.setPlatform(request.getPlatform());
            deviceToken.setDeviceName(request.getDeviceName());
            deviceToken.setDeviceModel(request.getDeviceModel());
            deviceToken.setOsVersion(request.getOsVersion());
            deviceToken.setAppVersion(request.getAppVersion());
            deviceToken.setActive(true);
            log.info("Updating existing device token");
        } else {
            deviceToken = DeviceToken.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .token(request.getToken())
                    .platform(request.getPlatform())
                    .deviceName(request.getDeviceName())
                    .deviceModel(request.getDeviceModel())
                    .osVersion(request.getOsVersion())
                    .appVersion(request.getAppVersion())
                    .active(true)
                    .registeredAt(LocalDateTime.now())
                    .subscribedTopics(new ArrayList<>())
                    .build();
            log.info("Creating new device token");
        }

        DeviceToken saved = deviceTokenRepository.save(deviceToken);
        return notificationMapper.toDTO(saved);
    }

    public void unregisterDeviceToken(String userId, String token) {
        log.info("Unregistering device token for user {}", userId);

        DeviceToken deviceToken = deviceTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Device token not found"));

        if (!deviceToken.getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized to unregister this device token");
        }

        deviceToken.setActive(false);
        deviceTokenRepository.save(deviceToken);
    }

    public List<DeviceTokenDTO> getUserDeviceTokens(String userId) {
        List<DeviceToken> tokens = deviceTokenRepository.findActiveByUserId(userId);
        return notificationMapper.toDeviceTokenDTOList(tokens);
    }

    // ==================== Send Notifications ====================

    public NotificationDTO sendNotification(String senderId, SendNotificationRequest request) {
        log.info("Sending notification to {} users", request.getRecipientUserIds().size());

        List<DeviceToken> deviceTokens = deviceTokenRepository
                .findActiveByUserIds(request.getRecipientUserIds());

        if (deviceTokens.isEmpty()) {
            log.warn("No active device tokens found for recipients");
            throw new BadRequestException("No active device tokens found for the specified users");
        }

        List<String> tokens = deviceTokens.stream()
                .map(DeviceToken::getToken)
                .collect(Collectors.toList());

        Notification notification = Notification.builder()
                .id(UUID.randomUUID().toString())
                .senderId(senderId)
                .recipientUserIds(request.getRecipientUserIds())
                .type(NotificationType.MANUAL)
                .status(NotificationStatus.PENDING)
                .title(request.getTitle())
                .body(request.getBody())
                .imageUrl(request.getImageUrl())
                .data(request.getData())
                .clickAction(request.getClickAction())
                .retryCount(0)
                .maxRetries(maxRetries)
                .build();

        try {
            BatchResponse response = firebasePushService.sendToTokens(
                    tokens, request.getTitle(), request.getBody(),
                    request.getImageUrl(), request.getData());

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setSuccessCount(response.getSuccessCount());
            notification.setFailureCount(response.getFailureCount());

            if (response.getFailureCount() > 0) {
                List<String> failedTokens = new ArrayList<>();
                for (int i = 0; i < response.getResponses().size(); i++) {
                    SendResponse sr = response.getResponses().get(i);
                    if (!sr.isSuccessful()) {
                        failedTokens.add(tokens.get(i));
                    }
                }
                notification.setFailedTokens(failedTokens);
                deactivateFailedTokens(failedTokens);
            }

            log.info("Notification sent. Success: {}, Failure: {}",
                    response.getSuccessCount(), response.getFailureCount());

        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage(), e);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailureReason(e.getMessage());
            notification.setNextRetryAt(LocalDateTime.now().plusMinutes(initialDelayMinutes));
        }

        Notification saved = notificationRepository.save(notification);

        // Create UserNotificationStatus for each recipient
        createNotificationStatusForRecipients(saved.getId(), request.getRecipientUserIds());

        return notificationMapper.toDTO(saved);
    }

    public NotificationDTO sendTopicNotification(String senderId, SendTopicNotificationRequest request) {
        log.info("Sending notification to topic: {}", request.getTopic());

        Notification notification = Notification.builder()
                .id(UUID.randomUUID().toString())
                .senderId(senderId)
                .topic(request.getTopic())
                .type(NotificationType.MANUAL)
                .status(NotificationStatus.PENDING)
                .title(request.getTitle())
                .body(request.getBody())
                .imageUrl(request.getImageUrl())
                .data(request.getData())
                .clickAction(request.getClickAction())
                .retryCount(0)
                .maxRetries(maxRetries)
                .build();

        try {
            String fcmResponse = firebasePushService.sendToTopic(
                    request.getTopic(), request.getTitle(), request.getBody(),
                    request.getImageUrl(), request.getData());

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setFcmMessageId(fcmResponse);
            notification.setSuccessCount(1);
            notification.setFailureCount(0);

            log.info("Topic notification sent successfully");

        } catch (Exception e) {
            log.error("Failed to send topic notification: {}", e.getMessage(), e);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailureReason(e.getMessage());
            notification.setNextRetryAt(LocalDateTime.now().plusMinutes(initialDelayMinutes));
        }

        Notification saved = notificationRepository.save(notification);
        return notificationMapper.toDTO(saved);
    }

    // ==================== Topic Subscription ====================

    public void subscribeToTopic(String userId, String topicName, String deviceToken) {
        log.info("Subscribing user {} to topic {}", userId, topicName);

        DeviceToken token = deviceTokenRepository.findByToken(deviceToken)
                .orElseThrow(() -> new ResourceNotFoundException("Device token not found"));

        if (!token.getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized to manage this device token");
        }

        try {
            firebasePushService.subscribeToTopic(List.of(deviceToken), topicName);

            if (token.getSubscribedTopics() == null) {
                token.setSubscribedTopics(new ArrayList<>());
            }
            if (!token.getSubscribedTopics().contains(topicName)) {
                token.getSubscribedTopics().add(topicName);
            }
            deviceTokenRepository.save(token);

            log.info("Successfully subscribed to topic");
        } catch (Exception e) {
            log.error("Failed to subscribe to topic: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to subscribe to topic: " + e.getMessage());
        }
    }

    public void unsubscribeFromTopic(String userId, String topicName, String deviceToken) {
        log.info("Unsubscribing user {} from topic {}", userId, topicName);

        DeviceToken token = deviceTokenRepository.findByToken(deviceToken)
                .orElseThrow(() -> new ResourceNotFoundException("Device token not found"));

        if (!token.getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized to manage this device token");
        }

        try {
            firebasePushService.unsubscribeFromTopic(List.of(deviceToken), topicName);

            if (token.getSubscribedTopics() != null) {
                token.getSubscribedTopics().remove(topicName);
            }
            deviceTokenRepository.save(token);

            log.info("Successfully unsubscribed from topic");
        } catch (Exception e) {
            log.error("Failed to unsubscribe from topic: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to unsubscribe from topic: " + e.getMessage());
        }
    }

    // ==================== Notification History ====================

    public Page<NotificationDTO> getNotificationHistory(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notifications = notificationRepository.findByRecipientUserId(userId, pageable);

        // Fetch read statuses for these notifications
        List<String> notificationIds = notifications.getContent().stream()
                .map(Notification::getId)
                .collect(Collectors.toList());

        Map<String, UserNotificationStatus> statusMap = userNotificationStatusRepository
                .findByUserIdAndNotificationIds(userId, notificationIds)
                .stream()
                .collect(Collectors.toMap(UserNotificationStatus::getNotificationId, s -> s));

        return notifications.map(n -> notificationMapper.toDTO(n, statusMap.get(n.getId())));
    }

    public NotificationDTO getNotificationById(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        return notificationMapper.toDTO(notification);
    }

    // ==================== Scheduled Notifications ====================

    public ScheduledNotificationDTO scheduleNotification(String creatorId, ScheduleNotificationRequest request) {
        log.info("Scheduling notification for {}", request.getScheduledAt());

        if (request.getRecipientUserIds() == null && request.getTopic() == null) {
            throw new BadRequestException("Either recipientUserIds or topic must be provided");
        }

        ScheduledNotification scheduled = ScheduledNotification.builder()
                .id(UUID.randomUUID().toString())
                .creatorId(creatorId)
                .recipientUserIds(request.getRecipientUserIds())
                .topic(request.getTopic())
                .title(request.getTitle())
                .body(request.getBody())
                .imageUrl(request.getImageUrl())
                .data(request.getData())
                .clickAction(request.getClickAction())
                .type(NotificationType.SCHEDULED)
                .status(NotificationStatus.SCHEDULED)
                .scheduledAt(request.getScheduledAt())
                .build();

        ScheduledNotification saved = scheduledNotificationRepository.save(scheduled);
        log.info("Notification scheduled with ID: {}", saved.getId());

        return notificationMapper.toDTO(saved);
    }

    public void cancelScheduledNotification(String creatorId, String scheduledId, String reason) {
        log.info("Cancelling scheduled notification {}", scheduledId);

        ScheduledNotification scheduled = scheduledNotificationRepository.findById(scheduledId)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled notification not found"));

        if (!scheduled.getCreatorId().equals(creatorId)) {
            throw new BadRequestException("Unauthorized to cancel this scheduled notification");
        }

        if (scheduled.getStatus() != NotificationStatus.SCHEDULED) {
            throw new BadRequestException("Cannot cancel notification with status: " + scheduled.getStatus());
        }

        scheduled.setStatus(NotificationStatus.CANCELLED);
        scheduled.setCancellationReason(reason);
        scheduledNotificationRepository.save(scheduled);

        log.info("Scheduled notification cancelled");
    }

    public Page<ScheduledNotificationDTO> getScheduledNotifications(String creatorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "scheduledAt"));
        Page<ScheduledNotification> scheduled = scheduledNotificationRepository
                .findByCreatorId(creatorId, pageable);
        return scheduled.map(notificationMapper::toDTO);
    }

    // ==================== Event-Based Notifications ====================

    public void sendEventNotification(NotificationType type, String title, String body,
                                      List<String> recipientUserIds, String relatedEntityType,
                                      String relatedEntityId, Map<String, String> data) {
        log.info("Sending {} event notification to {} users", type, recipientUserIds.size());

        List<DeviceToken> deviceTokens = deviceTokenRepository.findActiveByUserIds(recipientUserIds);
        if (deviceTokens.isEmpty()) {
            log.warn("No device tokens found for event notification");
            return;
        }

        List<String> tokens = deviceTokens.stream()
                .map(DeviceToken::getToken)
                .collect(Collectors.toList());

        Notification notification = Notification.builder()
                .id(UUID.randomUUID().toString())
                .recipientUserIds(recipientUserIds)
                .type(type)
                .status(NotificationStatus.PENDING)
                .title(title)
                .body(body)
                .data(data)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .retryCount(0)
                .maxRetries(maxRetries)
                .build();

        try {
            BatchResponse response = firebasePushService.sendToTokens(tokens, title, body, null, data);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setSuccessCount(response.getSuccessCount());
            notification.setFailureCount(response.getFailureCount());
        } catch (Exception e) {
            log.error("Failed to send event notification: {}", e.getMessage(), e);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailureReason(e.getMessage());
            notification.setNextRetryAt(LocalDateTime.now().plusMinutes(initialDelayMinutes));
        }

        Notification saved = notificationRepository.save(notification);

        // Create UserNotificationStatus for each recipient
        createNotificationStatusForRecipients(saved.getId(), recipientUserIds);
    }

    // ==================== Read Status Management ====================

    /**
     * Mark a notification as read for a user
     */
    public void markAsRead(String userId, String notificationId) {
        log.info("Marking notification {} as read for user {}", notificationId, userId);

        UserNotificationStatus status = userNotificationStatusRepository
                .findByUserIdAndNotificationId(userId, notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found for this user"));

        if (!status.isRead()) {
            status.setRead(true);
            status.setReadAt(LocalDateTime.now());
            userNotificationStatusRepository.save(status);
        }
    }

    /**
     * Mark all notifications as read for a user
     */
    public long markAllAsRead(String userId) {
        log.info("Marking all notifications as read for user {}", userId);
        return userNotificationStatusRepository.markAllAsReadForUser(userId, LocalDateTime.now());
    }

    /**
     * Get unread notifications for a user (paginated)
     */
    public Page<NotificationDTO> getUnreadNotifications(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UserNotificationStatus> statuses = userNotificationStatusRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(userId, pageable);

        // Fetch the full notification objects
        List<String> notificationIds = statuses.getContent().stream()
                .map(UserNotificationStatus::getNotificationId)
                .collect(Collectors.toList());

        Map<String, Notification> notificationMap = notificationRepository.findAllById(notificationIds)
                .stream()
                .collect(Collectors.toMap(Notification::getId, n -> n));

        Map<String, UserNotificationStatus> statusMap = statuses.getContent().stream()
                .collect(Collectors.toMap(UserNotificationStatus::getNotificationId, s -> s));

        List<NotificationDTO> dtos = notificationIds.stream()
                .filter(notificationMap::containsKey)
                .map(id -> notificationMapper.toDTO(notificationMap.get(id), statusMap.get(id)))
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(
                dtos, pageable, statuses.getTotalElements());
    }

    /**
     * Get unread notification count for a user
     */
    public long getUnreadCount(String userId) {
        return userNotificationStatusRepository.countByUserIdAndReadFalse(userId);
    }

    // ==================== Helper Methods ====================

    /**
     * Create UserNotificationStatus records for each recipient
     */
    private void createNotificationStatusForRecipients(String notificationId, List<String> recipientUserIds) {
        if (recipientUserIds == null || recipientUserIds.isEmpty()) {
            return;
        }

        List<UserNotificationStatus> statuses = recipientUserIds.stream()
                .map(userId -> UserNotificationStatus.builder()
                        .id(UUID.randomUUID().toString())
                        .notificationId(notificationId)
                        .userId(userId)
                        .read(false)
                        .createdAt(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());

        userNotificationStatusRepository.saveAll(statuses);
        log.debug("Created {} notification status records for notification {}",
                statuses.size(), notificationId);
    }

    private void deactivateFailedTokens(List<String> failedTokens) {
        List<DeviceToken> tokens = deviceTokenRepository.findByTokens(failedTokens);
        tokens.forEach(token -> {
            token.setActive(false);
            log.info("Deactivating invalid token for user: {}", token.getUserId());
        });
        deviceTokenRepository.saveAll(tokens);
    }
}
