package com.ceedpods.crmbuild.controller.messaging;

import com.ceedpods.crmbuild.dto.messaging.MessageDTO;
import com.ceedpods.crmbuild.dto.request.SaveTwilioSmsCredentialRequest;
import com.ceedpods.crmbuild.dto.request.SendSmsRequest;
import com.ceedpods.crmbuild.dto.response.ApiResponse;
import com.ceedpods.crmbuild.entity.messaging.AgentCredential;
import com.ceedpods.crmbuild.entity.messaging.Message;
import com.ceedpods.crmbuild.mapper.MessageMapper;
import com.ceedpods.crmbuild.repository.AgentCredentialRepository;
import com.ceedpods.crmbuild.security.RequirePermission;
import com.ceedpods.crmbuild.service.auditLogService.AuditLogService;
import com.ceedpods.crmbuild.service.messaging.EncryptionService;
import com.ceedpods.crmbuild.service.messaging.MessageDispatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
@Slf4j
public class SmsController {

    private final MessageDispatchService messageDispatchService;
    private final AgentCredentialRepository credentialRepository;
    private final EncryptionService encryptionService;
    private final MessageMapper messageMapper;
    private final AuditLogService auditLogService;

    /**
     * Save Twilio SMS credentials
     * POST /api/v1/sms/credentials
     */
    @PostMapping("/credentials")
    @RequirePermission("COMMUNICATION_SEND_SMS")
    public ResponseEntity<ApiResponse<String>> saveCredentials(
            @Valid @RequestBody SaveTwilioSmsCredentialRequest request,
            Authentication authentication) {
        try {
            String agentId = getKeycloakId(authentication);
            log.info("Agent {} saving Twilio SMS credentials", agentId);

            // Build credentials map
            Map<String, String> credentials = new HashMap<>();
            credentials.put("accountSid", request.getAccountSid());
            credentials.put("authToken", request.getAuthToken());
            credentials.put("phoneNumber", request.getPhoneNumber());

            // Encrypt credentials
            Map<String, String> encrypted = encryptionService.encryptMap(credentials);

            // Check if credential exists
            AgentCredential credential = credentialRepository.findActiveByAgentIdAndChannel(agentId, "SMS")
                    .orElse(AgentCredential.builder()
                            .id(UUID.randomUUID().toString())
                            .agentId(agentId)
                            .channel("SMS")
                            .build());

            boolean isUpdate = credential.getEncryptedCredentials() != null && !credential.getEncryptedCredentials().isEmpty();
            credential.setEncryptedCredentials(encrypted);
            credential.setActive(true);

            AgentCredential savedCredential = credentialRepository.save(credential);

            // Log audit event
            auditLogService.logSmsCredentialsSaved(authentication, savedCredential.getId(), isUpdate);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Twilio SMS credentials saved successfully", null));

        } catch (Exception e) {
            log.error("Error saving SMS credentials: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to save SMS credentials: " + e.getMessage()));
        }
    }

    /**
     * Send SMS via Twilio
     * POST /api/v1/sms/send
     */
    @PostMapping("/send")
    @RequirePermission("COMMUNICATION_SEND_SMS")
    public ResponseEntity<ApiResponse<MessageDTO>> sendSms(
            @Valid @RequestBody SendSmsRequest request,
            Authentication authentication) {
        try {
            String agentId = getKeycloakId(authentication);
            log.info("Agent {} sending SMS", agentId);

            Message message = messageDispatchService.sendSms(
                    agentId,
                    request.getRecipientPhone(),
                    request.getMessageBody()
            );

            // Log audit event
            String messagePreview = request.getMessageBody().length() > 50
                    ? request.getMessageBody().substring(0, 50) + "..."
                    : request.getMessageBody();
            auditLogService.logSmsSent(authentication, message.getId(), request.getRecipientPhone(), messagePreview);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("SMS sent", messageMapper.toDTO(message)));

        } catch (Exception e) {
            log.error("Error sending SMS: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to send SMS: " + e.getMessage()));
        }
    }

    /**
     * Get message by ID
     * GET /api/v1/sms/messages/{id}
     */
    @GetMapping("/messages/{id}")
    @RequirePermission("COMMUNICATION_SEND_SMS")
    public ResponseEntity<ApiResponse<MessageDTO>> getMessage(
            @PathVariable String id,
            Authentication authentication) {
        try {
            String agentId = getKeycloakId(authentication);
            Message message = messageDispatchService.getMessageById(agentId, id);

            return ResponseEntity.ok(ApiResponse.success(messageMapper.toDTO(message)));

        } catch (Exception e) {
            log.error("Error fetching message: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to fetch message: " + e.getMessage()));
        }
    }

    private String getKeycloakId(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getSubject();
    }
}
