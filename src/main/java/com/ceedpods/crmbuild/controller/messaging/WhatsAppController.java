package com.ceedpods.crmbuild.controller.messaging;

import com.ceedpods.crmbuild.dto.messaging.MessageDTO;
import com.ceedpods.crmbuild.dto.request.SaveMetaCredentialRequest;
import com.ceedpods.crmbuild.dto.request.SendWhatsAppMessageRequest;
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
@RequestMapping("/whatsapp")
@RequiredArgsConstructor
@Slf4j
public class WhatsAppController {

    private final MessageDispatchService messageDispatchService;
    private final AgentCredentialRepository credentialRepository;
    private final EncryptionService encryptionService;
    private final MessageMapper messageMapper;
    private final AuditLogService auditLogService;

    /**
     * Save Meta WhatsApp credentials
     * POST /api/v1/whatsapp/credentials
     */
    @PostMapping("/credentials")
    @RequirePermission("COMMUNICATION_SEND_SMS")
    public ResponseEntity<ApiResponse<String>> saveCredentials(
            @Valid @RequestBody SaveMetaCredentialRequest request,
            Authentication authentication) {
        try {
            String agentId = getKeycloakId(authentication);
            log.info("Agent {} saving WhatsApp credentials", agentId);

            // Build credentials map
            Map<String, String> credentials = new HashMap<>();
            credentials.put("accessToken", request.getAccessToken());
            credentials.put("phoneNumberId", request.getPhoneNumberId());
            credentials.put("businessAccountId", request.getBusinessAccountId());

            // Encrypt credentials
            Map<String, String> encrypted = encryptionService.encryptMap(credentials);

            // Check if credential exists
            AgentCredential credential = credentialRepository.findActiveByAgentIdAndChannel(agentId, "WHATSAPP")
                    .orElse(AgentCredential.builder()
                            .id(UUID.randomUUID().toString())
                            .agentId(agentId)
                            .channel("WHATSAPP")
                            .build());

            boolean isUpdate = credential.getEncryptedCredentials() != null && !credential.getEncryptedCredentials().isEmpty();
            credential.setEncryptedCredentials(encrypted);
            credential.setActive(true);

            AgentCredential savedCredential = credentialRepository.save(credential);

            // Log audit event
            auditLogService.logWhatsAppCredentialsSaved(authentication, savedCredential.getId(), isUpdate);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("WhatsApp credentials saved successfully", null));

        } catch (Exception e) {
            log.error("Error saving credentials: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to save credentials: " + e.getMessage()));
        }
    }

    /**
     * Send WhatsApp message
     * POST /api/v1/whatsapp/send
     */
    @PostMapping("/send")
    @RequirePermission("COMMUNICATION_SEND_SMS")
    public ResponseEntity<ApiResponse<MessageDTO>> sendMessage(
            @Valid @RequestBody SendWhatsAppMessageRequest request,
            Authentication authentication) {
        try {
            String agentId = getKeycloakId(authentication);
            log.info("Agent {} sending WhatsApp message", agentId);

            Message message = messageDispatchService.sendWhatsAppMessage(
                    agentId,
                    request.getRecipientPhone(),
                    request.getMessageBody()
            );

            // Log audit event
            String messagePreview = request.getMessageBody().length() > 50
                    ? request.getMessageBody().substring(0, 50) + "..."
                    : request.getMessageBody();
            auditLogService.logWhatsAppSent(authentication, message.getId(), request.getRecipientPhone(), messagePreview);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Message sent", messageMapper.toDTO(message)));

        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to send message: " + e.getMessage()));
        }
    }

    /**
     * Get message by ID
     * GET /api/v1/whatsapp/messages/{id}
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
