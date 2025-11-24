package com.ceedpods.crmbuild.service.auditLogService;

import com.ceedpods.crmbuild.dto.audit.AuditLogDTO;
import com.ceedpods.crmbuild.entity.audit.AuditLog;
import com.ceedpods.crmbuild.enums.AuditAction;
import com.ceedpods.crmbuild.enums.AuditEntityType;
import com.ceedpods.crmbuild.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Comprehensive Audit Logging Service
 * Stores detailed audit logs in the database for all critical operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Log a generic audit event
     */
    public void logAudit(String username, String userId, String userEmail,
                        AuditAction action, AuditEntityType entityType,
                        String entityId, String entityName, String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .id(UUID.randomUUID().toString())
                    .username(username)
                    .userId(userId)
                    .userEmail(userEmail)
                    .action(action.name())
                    .entityType(entityType.name())
                    .entityId(entityId)
                    .entityName(entityName)
                    .details(details)
                    .ipAddress(getClientIpAddress())
                    .timestamp(LocalDateTime.now())
                    .status("SUCCESS")
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} - {} - {} - {}", username, action, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }

    /**
     * Log audit event with Authentication object
     */
    public void logAudit(Authentication authentication, AuditAction action,
                        AuditEntityType entityType, String entityId,
                        String entityName, String details) {
        String username = "anonymous";
        String userId = null;
        String userEmail = null;

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            userId = jwt.getClaimAsString("sub");
            userEmail = jwt.getClaimAsString("email");
            username = userEmail != null ? userEmail : jwt.getClaimAsString("preferred_username");
        }

        logAudit(username, userId, userEmail, action, entityType, entityId, entityName, details);
    }

    /**
     * Log user login event
     */
    public void logUserLogin(String username, String userId, String userEmail, boolean success) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .id(UUID.randomUUID().toString())
                    .username(username)
                    .userId(userId)
                    .userEmail(userEmail)
                    .action(success ? AuditAction.LOGIN.name() : AuditAction.FAILED_LOGIN.name())
                    .entityType(AuditEntityType.AUTHENTICATION.name())
                    .entityId(userId)
                    .entityName(username)
                    .details(success ? "User logged in successfully" : "Failed login attempt")
                    .ipAddress(getClientIpAddress())
                    .timestamp(LocalDateTime.now())
                    .status(success ? "SUCCESS" : "FAILED")
                    .errorMessage(success ? null : "Invalid credentials")
                    .build();

            auditLogRepository.save(auditLog);
            log.info("AUDIT: User {} - {} at {}", username, success ? "LOGIN SUCCESS" : "LOGIN FAILED", LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to log user login audit: {}", e.getMessage(), e);
        }
    }

    /**
     * Log user logout event
     */
    public void logUserLogout(String username, String userId, String userEmail) {
        logAudit(username, userId, userEmail, AuditAction.LOGOUT,
                AuditEntityType.AUTHENTICATION, userId, username, "User logged out");
    }

    /**
     * Log product creation
     */
    public void logProductCreated(Authentication authentication, String productId, String productName) {
        logAudit(authentication, AuditAction.CREATED, AuditEntityType.PRODUCT,
                productId, productName, "Product created: " + productName);
    }

    /**
     * Log product update
     */
    public void logProductUpdated(Authentication authentication, String productId, String productName) {
        logAudit(authentication, AuditAction.UPDATED, AuditEntityType.PRODUCT,
                productId, productName, "Product updated: " + productName);
    }

    /**
     * Log product deletion
     */
    public void logProductDeleted(Authentication authentication, String productId, String productName) {
        logAudit(authentication, AuditAction.DELETED, AuditEntityType.PRODUCT,
                productId, productName, "Product deleted: " + productName);
    }

    /**
     * Log lead creation
     */
    public void logLeadCreated(Authentication authentication, String leadId, String leadName) {
        logAudit(authentication, AuditAction.CREATED, AuditEntityType.LEAD,
                leadId, leadName, "Lead created: " + leadName);
    }

    /**
     * Log lead update
     */
    public void logLeadUpdated(Authentication authentication, String leadId, String leadName) {
        logAudit(authentication, AuditAction.UPDATED, AuditEntityType.LEAD,
                leadId, leadName, "Lead updated: " + leadName);
    }

    /**
     * Log lead deletion
     */
    public void logLeadDeleted(Authentication authentication, String leadId, String leadName) {
        logAudit(authentication, AuditAction.DELETED, AuditEntityType.LEAD,
                leadId, leadName, "Lead deleted: " + leadName);
    }

    /**
     * Log deal creation
     */
    public void logDealCreated(Authentication authentication, String dealId, String dealName) {
        logAudit(authentication, AuditAction.CREATED, AuditEntityType.DEAL,
                dealId, dealName, "Deal created: " + dealName);
    }

    /**
     * Log deal update
     */
    public void logDealUpdated(Authentication authentication, String dealId, String dealName) {
        logAudit(authentication, AuditAction.UPDATED, AuditEntityType.DEAL,
                dealId, dealName, "Deal updated: " + dealName);
    }

    /**
     * Log deal deletion
     */
    public void logDealDeleted(Authentication authentication, String dealId, String dealName) {
        logAudit(authentication, AuditAction.DELETED, AuditEntityType.DEAL,
                dealId, dealName, "Deal deleted: " + dealName);
    }

    /**
     * Log deal note creation
     */
    public void logDealNoteCreated(Authentication authentication, String noteId, String noteTitle) {
        logAudit(authentication, AuditAction.CREATED, AuditEntityType.DEAL_NOTE,
                noteId, noteTitle != null ? noteTitle : "Untitled Note",
                "Deal note created");
    }

    /**
     * Log deal note update
     */
    public void logDealNoteUpdated(Authentication authentication, String noteId, String noteTitle) {
        logAudit(authentication, AuditAction.UPDATED, AuditEntityType.DEAL_NOTE,
                noteId, noteTitle != null ? noteTitle : "Untitled Note",
                "Deal note updated");
    }

    /**
     * Log deal note deletion
     */
    public void logDealNoteDeleted(Authentication authentication, String noteId, String noteTitle) {
        logAudit(authentication, AuditAction.DELETED, AuditEntityType.DEAL_NOTE,
                noteId, noteTitle != null ? noteTitle : "Untitled Note",
                "Deal note deleted");
    }

    /**
     * Log lead note creation
     */
    public void logLeadNoteCreated(Authentication authentication, String noteId, String noteTitle) {
        logAudit(authentication, AuditAction.CREATED, AuditEntityType.LEAD_NOTE,
                noteId, noteTitle != null ? noteTitle : "Untitled Note",
                "Lead note created");
    }

    /**
     * Log lead note update
     */
    public void logLeadNoteUpdated(Authentication authentication, String noteId, String noteTitle) {
        logAudit(authentication, AuditAction.UPDATED, AuditEntityType.LEAD_NOTE,
                noteId, noteTitle != null ? noteTitle : "Untitled Note",
                "Lead note updated");
    }

    /**
     * Log lead note deletion
     */
    public void logLeadNoteDeleted(Authentication authentication, String noteId, String noteTitle) {
        logAudit(authentication, AuditAction.DELETED, AuditEntityType.LEAD_NOTE,
                noteId, noteTitle != null ? noteTitle : "Untitled Note",
                "Lead note deleted");
    }

    // ==================== Communication Audit Logging Methods ====================

    /**
     * Log email sent via SMTP
     */
    public void logEmailSent(Authentication authentication, String messageId, String recipient, String subject) {
        logAudit(authentication, AuditAction.SEND_EMAIL, AuditEntityType.EMAIL,
                messageId, subject != null ? subject : "No Subject",
                "Email sent to: " + recipient);
    }

    /**
     * Log SMS sent via Twilio
     */
    public void logSmsSent(Authentication authentication, String messageSid, String recipient, String messagePreview) {
        logAudit(authentication, AuditAction.SEND_SMS, AuditEntityType.SMS,
                messageSid, "SMS to " + recipient,
                "SMS sent to: " + recipient + " - " + (messagePreview != null ? messagePreview : ""));
    }

    /**
     * Log WhatsApp message sent
     */
    public void logWhatsAppSent(Authentication authentication, String messageId, String recipient, String messagePreview) {
        logAudit(authentication, AuditAction.SEND_WHATSAPP, AuditEntityType.WHATSAPP,
                messageId, "WhatsApp to " + recipient,
                "WhatsApp message sent to: " + recipient + " - " + (messagePreview != null ? messagePreview : ""));
    }

    /**
     * Log voice call initiated via Twilio
     */
    public void logVoiceCallInitiated(Authentication authentication, String callSid, String recipient, String callType) {
        logAudit(authentication, AuditAction.MAKE_CALL, AuditEntityType.VOICE_CALL,
                callSid, callType + " call to " + recipient,
                "Voice call initiated to: " + recipient + " (Type: " + callType + ")");
    }

    // ==================== Configuration/Credentials Audit Logging Methods ====================

    /**
     * Log email credentials saved/updated
     */
    public void logEmailCredentialsSaved(Authentication authentication, String credentialId, boolean isUpdate) {
        AuditAction action = isUpdate ? AuditAction.UPDATE_CREDENTIALS : AuditAction.SAVE_CREDENTIALS;
        logAudit(authentication, action, AuditEntityType.EMAIL_CREDENTIALS,
                credentialId, "SMTP Email Credentials",
                isUpdate ? "Email credentials updated" : "Email credentials saved");
    }

    /**
     * Log SMS credentials saved/updated
     */
    public void logSmsCredentialsSaved(Authentication authentication, String credentialId, boolean isUpdate) {
        AuditAction action = isUpdate ? AuditAction.UPDATE_CREDENTIALS : AuditAction.SAVE_CREDENTIALS;
        logAudit(authentication, action, AuditEntityType.SMS_CREDENTIALS,
                credentialId, "Twilio SMS Credentials",
                isUpdate ? "SMS credentials updated" : "SMS credentials saved");
    }

    /**
     * Log WhatsApp credentials saved/updated
     */
    public void logWhatsAppCredentialsSaved(Authentication authentication, String credentialId, boolean isUpdate) {
        AuditAction action = isUpdate ? AuditAction.UPDATE_CREDENTIALS : AuditAction.SAVE_CREDENTIALS;
        logAudit(authentication, action, AuditEntityType.WHATSAPP_CREDENTIALS,
                credentialId, "WhatsApp Credentials",
                isUpdate ? "WhatsApp credentials updated" : "WhatsApp credentials saved");
    }

    /**
     * Log voice call credentials saved/updated
     */
    public void logVoiceCredentialsSaved(Authentication authentication, String credentialId, boolean isUpdate) {
        AuditAction action = isUpdate ? AuditAction.UPDATE_CREDENTIALS : AuditAction.SAVE_CREDENTIALS;
        logAudit(authentication, action, AuditEntityType.VOICE_CREDENTIALS,
                credentialId, "Twilio Voice Credentials",
                isUpdate ? "Voice call credentials updated" : "Voice call credentials saved");
    }

    /**
     * Log system configuration saved/updated
     */
    public void logSystemConfigSaved(Authentication authentication, String configKey, String configName, boolean isUpdate) {
        AuditAction action = isUpdate ? AuditAction.UPDATE_CONFIG : AuditAction.SAVE_CONFIG;
        logAudit(authentication, action, AuditEntityType.SYSTEM_CONFIG,
                configKey, configName,
                isUpdate ? "System configuration updated: " + configKey : "System configuration saved: " + configKey);
    }

    /**
     * Get all audit logs with pagination
     */
    public Page<AuditLog> getAllAuditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
    }

    /**
     * Get audit logs by username
     */
    public Page<AuditLog> getAuditLogsByUsername(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return auditLogRepository.findByUsername(username, pageable);
    }

    /**
     * Get audit logs by entity type
     */
    public Page<AuditLog> getAuditLogsByEntityType(String entityType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return auditLogRepository.findByEntityType(entityType, pageable);
    }

    /**
     * Get audit logs by entity ID
     */
    public Page<AuditLog> getAuditLogsByEntityId(String entityId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return auditLogRepository.findByEntityId(entityId, pageable);
    }

    /**
     * Get audit logs by action
     */
    public Page<AuditLog> getAuditLogsByAction(String action, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return auditLogRepository.findByAction(action, pageable);
    }

    /**
     * Get audit logs within a date range
     */
    public Page<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return auditLogRepository.findByTimestampBetween(startDate, endDate, pageable);
    }

    /**
     * Get client IP address from HTTP request
     */
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Could not determine client IP address: {}", e.getMessage());
        }
        return "unknown";
    }
}
