package com.ceedpods.crmbuild.service.notification;

import com.ceedpods.crmbuild.entity.notification.DeviceToken;
import com.ceedpods.crmbuild.entity.notification.Notification;
import com.ceedpods.crmbuild.enums.NotificationStatus;
import com.ceedpods.crmbuild.repository.DeviceTokenRepository;
import com.ceedpods.crmbuild.repository.NotificationRepository;
import com.google.firebase.messaging.BatchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationRetryService {

    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final FirebasePushNotificationService firebasePushService;

    @Value("${app.push-notification.retry.initial-delay-minutes:5}")
    private int initialDelayMinutes;

    @Value("${app.push-notification.retry.backoff-multiplier:2}")
    private int backoffMultiplier;

    public void processRetries() {
        log.debug("Processing notification retries...");

        List<Notification> notificationsToRetry = notificationRepository
                .findNotificationsForRetry(LocalDateTime.now());

        if (notificationsToRetry.isEmpty()) {
            log.debug("No notifications found for retry");
            return;
        }

        log.info("Found {} notifications for retry", notificationsToRetry.size());

        for (Notification notification : notificationsToRetry) {
            retryNotification(notification);
        }
    }

    private void retryNotification(Notification notification) {
        log.info("Retrying notification {} (attempt {}/{})",
                notification.getId(), notification.getRetryCount() + 1, notification.getMaxRetries());

        try {
            if (notification.getTopic() != null) {
                String response = firebasePushService.sendToTopic(
                        notification.getTopic(), notification.getTitle(),
                        notification.getBody(), notification.getImageUrl(),
                        notification.getData());

                notification.setFcmMessageId(response);
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                notification.setSuccessCount(1);
                notification.setFailureCount(0);

            } else if (notification.getRecipientUserIds() != null) {
                List<DeviceToken> deviceTokens = deviceTokenRepository
                        .findActiveByUserIds(notification.getRecipientUserIds());

                List<String> tokens = deviceTokens.stream()
                        .map(DeviceToken::getToken)
                        .collect(Collectors.toList());

                if (tokens.isEmpty()) {
                    log.warn("No active tokens found for retry");
                    notification.setStatus(NotificationStatus.FAILED);
                    notification.setFailureReason("No active device tokens");
                } else {
                    BatchResponse response = firebasePushService.sendToTokens(
                            tokens, notification.getTitle(), notification.getBody(),
                            notification.getImageUrl(), notification.getData());

                    notification.setStatus(NotificationStatus.SENT);
                    notification.setSentAt(LocalDateTime.now());
                    notification.setSuccessCount(response.getSuccessCount());
                    notification.setFailureCount(response.getFailureCount());
                }
            }

            notification.setLastRetryAt(LocalDateTime.now());
            notification.setFailureReason(null);
            notification.setNextRetryAt(null);

            log.info("Notification {} successfully sent on retry", notification.getId());

        } catch (Exception e) {
            log.error("Retry failed for notification {}: {}", notification.getId(), e.getMessage());

            notification.setRetryCount(notification.getRetryCount() + 1);
            notification.setLastRetryAt(LocalDateTime.now());
            notification.setFailureReason(e.getMessage());

            if (notification.getRetryCount() < notification.getMaxRetries()) {
                int delayMinutes = calculateRetryDelay(notification.getRetryCount());
                notification.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));
                log.info("Scheduling retry in {} minutes", delayMinutes);
            } else {
                log.warn("Notification {} exhausted all retries", notification.getId());
                notification.setNextRetryAt(null);
            }
        }

        notificationRepository.save(notification);
    }

    private int calculateRetryDelay(int retryCount) {
        return initialDelayMinutes * (int) Math.pow(backoffMultiplier, retryCount);
    }
}
