package com.ceedpods.crmbuild.scheduler;

import com.ceedpods.crmbuild.service.notification.ScheduledNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        name = "app.push-notification.scheduled.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class ScheduledNotificationScheduler {

    private final ScheduledNotificationService scheduledNotificationService;

    @Scheduled(cron = "${app.push-notification.scheduled.cron:0 * * * * *}")
    public void processScheduledNotifications() {
        log.debug("Scheduled notification processor triggered");
        try {
            scheduledNotificationService.processScheduledNotifications();
        } catch (Exception e) {
            log.error("Error during scheduled notification processing: {}", e.getMessage(), e);
        }
    }
}
