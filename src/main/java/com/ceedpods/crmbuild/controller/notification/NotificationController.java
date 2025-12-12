package com.ceedpods.crmbuild.controller.notification;

import com.ceedpods.crmbuild.dto.notification.DeviceTokenDTO;
import com.ceedpods.crmbuild.dto.notification.NotificationDTO;
import com.ceedpods.crmbuild.dto.notification.ScheduledNotificationDTO;
import com.ceedpods.crmbuild.dto.request.*;
import com.ceedpods.crmbuild.dto.response.ApiResponse;
import com.ceedpods.crmbuild.dto.response.PaginatedResponse;
import com.ceedpods.crmbuild.exception.BadRequestException;
import com.ceedpods.crmbuild.exception.ResourceNotFoundException;
import com.ceedpods.crmbuild.security.RequirePermission;
import com.ceedpods.crmbuild.service.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Push Notifications", description = "Push Notification management endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    // ==================== Device Token Management ====================

    @PostMapping("/device-token")
    @Operation(summary = "Register a device token for push notifications")
    public ResponseEntity<ApiResponse<DeviceTokenDTO>> registerDeviceToken(
            @Valid @RequestBody RegisterDeviceTokenRequest request,
            Authentication authentication) {
        try {
            String userId = getKeycloakId(authentication);
            log.info("Registering device token for user: {}", userId);

            DeviceTokenDTO result = notificationService.registerDeviceToken(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Device token registered successfully", result));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error registering device token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    @DeleteMapping("/device-token")
    @Operation(summary = "Unregister a device token")
    public ResponseEntity<Void> unregisterDeviceToken(
            @RequestParam String token,
            Authentication authentication) {
        String userId = getKeycloakId(authentication);
        log.info("Unregistering device token for user: {}", userId);
        notificationService.unregisterDeviceToken(userId, token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/device-tokens")
    @Operation(summary = "Get all device tokens for current user")
    public ResponseEntity<ApiResponse<List<DeviceTokenDTO>>> getDeviceTokens(Authentication authentication) {
        try {
            String userId = getKeycloakId(authentication);
            List<DeviceTokenDTO> tokens = notificationService.getUserDeviceTokens(userId);
            return ResponseEntity.ok(ApiResponse.success(tokens));
        } catch (Exception e) {
            log.error("Error fetching device tokens: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    // ==================== Send Notifications ====================

    @PostMapping("/send")
    // @RequirePermission("COMMUNICATION_SEND_NOTIFICATION") // TODO: Uncomment after testing
    @Operation(summary = "Send push notification to specific users")
    public ResponseEntity<ApiResponse<NotificationDTO>> sendNotification(
            @Valid @RequestBody SendNotificationRequest request,
            Authentication authentication) {
        try {
            String senderId = getKeycloakId(authentication);
            log.info("Sending notification to {} users", request.getRecipientUserIds().size());

            NotificationDTO result = notificationService.sendNotification(senderId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Notification sent", result));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    @PostMapping("/send-topic")
    // @RequirePermission("COMMUNICATION_SEND_NOTIFICATION") // TODO: Uncomment after testing
    @Operation(summary = "Send push notification to a topic")
    public ResponseEntity<ApiResponse<NotificationDTO>> sendTopicNotification(
            @Valid @RequestBody SendTopicNotificationRequest request,
            Authentication authentication) {
        try {
            String senderId = getKeycloakId(authentication);
            log.info("Sending notification to topic: {}", request.getTopic());

            NotificationDTO result = notificationService.sendTopicNotification(senderId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Topic notification sent", result));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error sending topic notification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    // ==================== Topic Subscription ====================

    @PostMapping("/topics/{topicName}/subscribe")
    @Operation(summary = "Subscribe to a topic")
    public ResponseEntity<ApiResponse<Void>> subscribeToTopic(
            @PathVariable String topicName,
            @Valid @RequestBody TopicSubscriptionRequest request,
            Authentication authentication) {
        try {
            String userId = getKeycloakId(authentication);
            log.info("Subscribing user {} to topic {}", userId, topicName);

            notificationService.subscribeToTopic(userId, topicName, request.getDeviceToken());
            return ResponseEntity.ok(ApiResponse.success("Subscribed to topic successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error subscribing to topic: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    @PostMapping("/topics/{topicName}/unsubscribe")
    @Operation(summary = "Unsubscribe from a topic")
    public ResponseEntity<ApiResponse<Void>> unsubscribeFromTopic(
            @PathVariable String topicName,
            @Valid @RequestBody TopicSubscriptionRequest request,
            Authentication authentication) {
        try {
            String userId = getKeycloakId(authentication);
            log.info("Unsubscribing user {} from topic {}", userId, topicName);

            notificationService.unsubscribeFromTopic(userId, topicName, request.getDeviceToken());
            return ResponseEntity.ok(ApiResponse.success("Unsubscribed from topic successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error unsubscribing from topic: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    // ==================== Notification History ====================

    @GetMapping("/history")
    @Operation(summary = "Get notification history for current user")
    public ResponseEntity<ApiResponse<PaginatedResponse<NotificationDTO>>> getNotificationHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        try {
            String userId = getKeycloakId(authentication);
            Page<NotificationDTO> notifications = notificationService
                    .getNotificationHistory(userId, page, size);

            PaginatedResponse<NotificationDTO> response = PaginatedResponse.of(
                    notifications.getContent(),
                    notifications.getTotalElements(),
                    notifications.getTotalPages(),
                    page,
                    size,
                    notifications.hasNext(),
                    notifications.hasPrevious()
            );

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error fetching notification history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID")
    public ResponseEntity<ApiResponse<NotificationDTO>> getNotificationById(@PathVariable String id) {
        try {
            NotificationDTO notification = notificationService.getNotificationById(id);
            return ResponseEntity.ok(ApiResponse.success(notification));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching notification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    // ==================== Read Status Management ====================

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable String id,
            Authentication authentication) {
        try {
            String userId = getKeycloakId(authentication);
            log.info("Marking notification {} as read for user {}", id, userId);

            notificationService.markAsRead(userId, id);
            return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error marking notification as read: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Long>> markAllAsRead(Authentication authentication) {
        try {
            String userId = getKeycloakId(authentication);
            log.info("Marking all notifications as read for user {}", userId);

            long count = notificationService.markAllAsRead(userId);
            return ResponseEntity.ok(ApiResponse.success("Marked " + count + " notifications as read", count));
        } catch (Exception e) {
            log.error("Error marking all notifications as read: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications for current user")
    public ResponseEntity<ApiResponse<PaginatedResponse<NotificationDTO>>> getUnreadNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        try {
            String userId = getKeycloakId(authentication);
            Page<NotificationDTO> notifications = notificationService
                    .getUnreadNotifications(userId, page, size);

            PaginatedResponse<NotificationDTO> response = PaginatedResponse.of(
                    notifications.getContent(),
                    notifications.getTotalElements(),
                    notifications.getTotalPages(),
                    page,
                    size,
                    notifications.hasNext(),
                    notifications.hasPrevious()
            );

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error fetching unread notifications: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    // ==================== Scheduled Notifications ====================

    @PostMapping("/schedule")
    // @RequirePermission("COMMUNICATION_SEND_NOTIFICATION") // TODO: Uncomment after testing
    @Operation(summary = "Schedule a notification for future delivery")
    public ResponseEntity<ApiResponse<ScheduledNotificationDTO>> scheduleNotification(
            @Valid @RequestBody ScheduleNotificationRequest request,
            Authentication authentication) {
        try {
            String creatorId = getKeycloakId(authentication);
            log.info("Scheduling notification for {}", request.getScheduledAt());

            ScheduledNotificationDTO result = notificationService.scheduleNotification(creatorId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Notification scheduled", result));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error scheduling notification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    @DeleteMapping("/schedule/{id}")
    // @RequirePermission("COMMUNICATION_SEND_NOTIFICATION") // TODO: Uncomment after testing
    @Operation(summary = "Cancel a scheduled notification")
    public ResponseEntity<Void> cancelScheduledNotification(
            @PathVariable String id,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        String creatorId = getKeycloakId(authentication);
        log.info("Cancelling scheduled notification {}", id);
        notificationService.cancelScheduledNotification(creatorId, id, reason);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/scheduled")
    // @RequirePermission("COMMUNICATION_SEND_NOTIFICATION") // TODO: Uncomment after testing
    @Operation(summary = "Get scheduled notifications for current user")
    public ResponseEntity<ApiResponse<PaginatedResponse<ScheduledNotificationDTO>>> getScheduledNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        try {
            String creatorId = getKeycloakId(authentication);
            Page<ScheduledNotificationDTO> scheduled = notificationService
                    .getScheduledNotifications(creatorId, page, size);

            PaginatedResponse<ScheduledNotificationDTO> response = PaginatedResponse.of(
                    scheduled.getContent(),
                    scheduled.getTotalElements(),
                    scheduled.getTotalPages(),
                    page,
                    size,
                    scheduled.hasNext(),
                    scheduled.hasPrevious()
            );

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error fetching scheduled notifications: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    private String getKeycloakId(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getSubject();
    }
}
