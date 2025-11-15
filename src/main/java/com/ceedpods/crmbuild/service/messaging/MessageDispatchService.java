package com.ceedpods.crmbuild.service.messaging;

import com.ceedpods.crmbuild.entity.messaging.AgentCredential;
import com.ceedpods.crmbuild.entity.messaging.Message;
import com.ceedpods.crmbuild.enums.MessageChannel;
import com.ceedpods.crmbuild.enums.MessageStatus;
import com.ceedpods.crmbuild.exception.BadRequestException;
import com.ceedpods.crmbuild.repository.AgentCredentialRepository;
import com.ceedpods.crmbuild.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageDispatchService {

    private final MessageRepository messageRepository;
    private final AgentCredentialRepository credentialRepository;
    private final EncryptionService encryptionService;
    private final MetaWhatsAppService metaWhatsAppService;

    public Message sendWhatsAppMessage(String agentId, String recipientPhone, String messageBody) {
        log.info("Agent {} sending WhatsApp message to {}", agentId, recipientPhone);

        // Get agent's credentials
        AgentCredential credential = credentialRepository.findActiveByAgentId(agentId)
                .orElseThrow(() -> new BadRequestException("No WhatsApp credentials found for agent"));

        // Decrypt credentials
        Map<String, String> decryptedCredentials = encryptionService.decryptMap(credential.getEncryptedCredentials());

        // Create message record
        Message message = Message.builder()
                .id(UUID.randomUUID().toString())
                .agentId(agentId)
                .channel(MessageChannel.WHATSAPP)
                .status(MessageStatus.PENDING)
                .recipientPhone(recipientPhone)
                .messageBody(messageBody)
                .build();

        try {
            // Send via Meta API
            String vendorMessageId = metaWhatsAppService.sendMessage(recipientPhone, messageBody, decryptedCredentials);

            // Mark as sent
            message.setStatus(MessageStatus.SENT);
            message.setSentAt(LocalDateTime.now());
            message.setVendorMessageId(vendorMessageId);

            log.info("Message sent successfully. ID: {}", message.getId());

        } catch (Exception e) {
            log.error("Failed to send message: {}", e.getMessage(), e);
            message.setStatus(MessageStatus.FAILED);
            message.setFailureReason(e.getMessage());
        }

        return messageRepository.save(message);
    }

    public Message getMessageById(String agentId, String messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BadRequestException("Message not found"));

        if (!message.getAgentId().equals(agentId)) {
            throw new BadRequestException("Unauthorized access to message");
        }

        return message;
    }
}
