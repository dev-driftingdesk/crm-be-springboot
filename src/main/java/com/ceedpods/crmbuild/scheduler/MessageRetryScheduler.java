package com.ceedpods.crmbuild.scheduler;

import com.ceedpods.crmbuild.service.messaging.MessageRetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for processing message retries
 * Runs every 2 minutes by default
 * Can be disabled by setting app.messaging.retry.enabled=false
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        name = "app.messaging.retry.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class MessageRetryScheduler {

    private final MessageRetryService messageRetryService;

    /**
     * Process message retries every 2 minutes
     * Cron expression: 0 star-slash-2 star star star star = Every 2 minutes
     */
    @Scheduled(cron = "${app.messaging.retry.cron:0 */2 * * * *}")
    public void processMessageRetries() {
        log.debug("Message retry scheduler triggered");
        try {
            messageRetryService.processRetries();
        } catch (Exception e) {
            log.error("Error during scheduled retry process: {}", e.getMessage(), e);
        }
    }
}
