package com.ceedpods.crmbuild.service.notification;

import com.ceedpods.crmbuild.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to send push notifications for CRM events.
 * This can be called from LeadService, DealService, MessageDispatchService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CrmEventNotificationService {

    private final NotificationService notificationService;

    public void notifyNewLead(String leadId, String leadName, List<String> recipientUserIds) {
        log.info("Sending new lead notification for lead: {}", leadId);

        Map<String, String> data = new HashMap<>();
        data.put("type", "NEW_LEAD");
        data.put("leadId", leadId);

        notificationService.sendEventNotification(
                NotificationType.NEW_LEAD,
                "New Lead Created",
                "New lead '" + leadName + "' has been created",
                recipientUserIds,
                "LEAD",
                leadId,
                data
        );
    }

    public void notifyLeadUpdated(String leadId, String leadName, List<String> recipientUserIds) {
        log.info("Sending lead updated notification for lead: {}", leadId);

        Map<String, String> data = new HashMap<>();
        data.put("type", "LEAD_UPDATED");
        data.put("leadId", leadId);

        notificationService.sendEventNotification(
                NotificationType.LEAD_UPDATED,
                "Lead Updated",
                "Lead '" + leadName + "' has been updated",
                recipientUserIds,
                "LEAD",
                leadId,
                data
        );
    }

    public void notifyDealCreated(String dealId, String dealName, List<String> recipientUserIds) {
        log.info("Sending deal created notification for deal: {}", dealId);

        Map<String, String> data = new HashMap<>();
        data.put("type", "DEAL_CREATED");
        data.put("dealId", dealId);

        notificationService.sendEventNotification(
                NotificationType.DEAL_CREATED,
                "New Deal Created",
                "New deal '" + dealName + "' has been created",
                recipientUserIds,
                "DEAL",
                dealId,
                data
        );
    }

    public void notifyDealUpdated(String dealId, String dealName, String status, List<String> recipientUserIds) {
        log.info("Sending deal updated notification for deal: {}", dealId);

        NotificationType type;
        String title;

        switch (status) {
            case "WON" -> {
                type = NotificationType.DEAL_WON;
                title = "Deal Won!";
            }
            case "LOST" -> {
                type = NotificationType.DEAL_LOST;
                title = "Deal Lost";
            }
            default -> {
                type = NotificationType.DEAL_UPDATED;
                title = "Deal Updated";
            }
        }

        Map<String, String> data = new HashMap<>();
        data.put("type", type.name());
        data.put("dealId", dealId);
        data.put("status", status);

        notificationService.sendEventNotification(
                type,
                title,
                "Deal '" + dealName + "' status: " + status,
                recipientUserIds,
                "DEAL",
                dealId,
                data
        );
    }

    public void notifyInboundMessage(String messageId, String channel, String fromPhone,
                                     List<String> recipientUserIds) {
        log.info("Sending inbound message notification");

        Map<String, String> data = new HashMap<>();
        data.put("type", "INBOUND_MESSAGE");
        data.put("messageId", messageId);
        data.put("channel", channel);
        data.put("from", fromPhone);

        notificationService.sendEventNotification(
                NotificationType.INBOUND_MESSAGE,
                "New " + channel + " Message",
                "You have a new message from " + fromPhone,
                recipientUserIds,
                "MESSAGE",
                messageId,
                data
        );
    }
}
