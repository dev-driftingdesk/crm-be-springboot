package com.ceedpods.crmbuild.service.notification;

import com.ceedpods.crmbuild.dto.request.SendNotificationRequest;
import com.ceedpods.crmbuild.dto.request.SendTopicNotificationRequest;
import com.ceedpods.crmbuild.entity.notification.ScheduledNotification;
import com.ceedpods.crmbuild.enums.NotificationStatus;
import com.ceedpods.crmbuild.repository.ScheduledNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledNotificationService {

    private final ScheduledNotificationRepository scheduledNotificationRepository;
    private final NotificationService notificationService;

    public void processScheduledNotifications() {
        log.debug("Processing scheduled notifications...");

        List<ScheduledNotification> dueNotifications = scheduledNotificationRepository
                .findDueNotifications(LocalDateTime.now());

        if (dueNotifications.isEmpty()) {
            log.debug("No scheduled notifications due");
            return;
        }

        log.info("Found {} scheduled notifications to process", dueNotifications.size());

        for (ScheduledNotification scheduled : dueNotifications) {
            try {
                processScheduledNotification(scheduled);
            } catch (Exception e) {
                log.error("Error processing scheduled notification {}: {}",
                        scheduled.getId(), e.getMessage(), e);
                scheduled.setStatus(NotificationStatus.FAILED);
                scheduledNotificationRepository.save(scheduled);
            }
        }
    }

    private void processScheduledNotification(ScheduledNotification scheduled) {
        log.info("Processing scheduled notification: {}", scheduled.getId());

        if (scheduled.getTopic() != null) {
            SendTopicNotificationRequest request = SendTopicNotificationRequest.builder()
                    .topic(scheduled.getTopic())
                    .title(scheduled.getTitle())
                    .body(scheduled.getBody())
                    .imageUrl(scheduled.getImageUrl())
                    .data(scheduled.getData())
                    .clickAction(scheduled.getClickAction())
                    .build();

            var result = notificationService.sendTopicNotification(scheduled.getCreatorId(), request);
            scheduled.setNotificationId(result.getId());
        } else {
            SendNotificationRequest request = SendNotificationRequest.builder()
                    .recipientUserIds(scheduled.getRecipientUserIds())
                    .title(scheduled.getTitle())
                    .body(scheduled.getBody())
                    .imageUrl(scheduled.getImageUrl())
                    .data(scheduled.getData())
                    .clickAction(scheduled.getClickAction())
                    .build();

            var result = notificationService.sendNotification(scheduled.getCreatorId(), request);
            scheduled.setNotificationId(result.getId());
        }

        scheduled.setStatus(NotificationStatus.SENT);
        scheduled.setSentAt(LocalDateTime.now());
        scheduledNotificationRepository.save(scheduled);

        log.info("Scheduled notification {} processed successfully", scheduled.getId());
    }
}
