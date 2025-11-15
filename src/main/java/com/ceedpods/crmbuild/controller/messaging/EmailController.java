package com.ceedpods.crmbuild.controller.messaging;

import com.ceedpods.crmbuild.dto.messaging.MessageDTO;
import com.ceedpods.crmbuild.dto.request.SaveAzureEmailCredentialRequest;
import com.ceedpods.crmbuild.dto.request.SendEmailRequest;
import com.ceedpods.crmbuild.dto.response.ApiResponse;
import com.ceedpods.crmbuild.entity.messaging.AgentCredential;
import com.ceedpods.crmbuild.entity.messaging.Message;
import com.ceedpods.crmbuild.mapper.MessageMapper;
import com.ceedpods.crmbuild.repository.AgentCredentialRepository;
import com.ceedpods.crmbuild.security.RequirePermission;
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
@RequestMapping("/email")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final MessageDispatchService messageDispatchService;
    private final AgentCredentialRepository credentialRepository;
    private final EncryptionService encryptionService;
    private final MessageMapper messageMapper;

    /**
     * Save Azure Communication Services Email credentials
     * POST /api/v1/email/credentials
     */
    @PostMapping("/credentials")
    @RequirePermission("COMMUNICATION_SEND_SMS")
    public ResponseEntity<ApiResponse<String>> saveCredentials(
            @Valid @RequestBody SaveAzureEmailCredentialRequest request,
            Authentication authentication) {
        try {
            String agentId = getKeycloakId(authentication);
            log.info("Agent {} saving Azure email credentials", agentId);

            // Build credentials map
            Map<String, String> credentials = new HashMap<>();
            credentials.put("connectionString", request.getConnectionString());
            credentials.put("senderAddress", request.getSenderAddress());

            // Encrypt credentials
            Map<String, String> encrypted = encryptionService.encryptMap(credentials);

            // Check if credential exists
            AgentCredential credential = credentialRepository.findActiveByAgentIdAndChannel(agentId, "EMAIL")
                    .orElse(AgentCredential.builder()
                            .id(UUID.randomUUID().toString())
                            .agentId(agentId)
                            .channel("EMAIL")
                            .build());

            credential.setEncryptedCredentials(encrypted);
            credential.setActive(true);

            credentialRepository.save(credential);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Azure email credentials saved successfully", null));

        } catch (Exception e) {
            log.error("Error saving email credentials: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to save email credentials: " + e.getMessage()));
        }
    }

    /**
     * Send email via Azure Communication Services
     * POST /api/v1/email/send
     */
    @PostMapping("/send")
    @RequirePermission("COMMUNICATION_SEND_SMS")
    public ResponseEntity<ApiResponse<MessageDTO>> sendEmail(
            @Valid @RequestBody SendEmailRequest request,
            Authentication authentication) {
        try {
            String agentId = getKeycloakId(authentication);
            log.info("Agent {} sending email", agentId);

            Message message = messageDispatchService.sendEmail(
                    agentId,
                    request.getRecipientEmail(),
                    request.getSubject(),
                    request.getMessageBody()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Email sent", messageMapper.toDTO(message)));

        } catch (Exception e) {
            log.error("Error sending email: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to send email: " + e.getMessage()));
        }
    }

    /**
     * Get message by ID
     * GET /api/v1/email/messages/{id}
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
