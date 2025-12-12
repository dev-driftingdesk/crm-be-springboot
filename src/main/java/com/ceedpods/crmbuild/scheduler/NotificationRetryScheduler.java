package com.ceedpods.crmbuild.scheduler;

import com.ceedpods.crmbuild.service.notification.NotificationRetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        name = "app.push-notification.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class NotificationRetryScheduler {

    private final NotificationRetryService notificationRetryService;

    @Scheduled(cron = "${app.messaging.retry.cron:0 */2 * * * *}")
    public void processNotificationRetries() {
        log.debug("Notification retry scheduler triggered");
        try {
            notificationRetryService.processRetries();
        } catch (Exception e) {
            log.error("Error during notification retry process: {}", e.getMessage(), e);
        }
    }
}
