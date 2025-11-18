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
import org.springframework.beans.factory.annotation.Value;
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
    private final SmtpEmailService smtpEmailService;
    private final TwilioSmsService twilioSmsService;

    @Value("${app.messaging.retry.initial-delay-minutes:5}")
    private int initialDelayMinutes;

    @Value("${app.messaging.retry.max-retries:3}")
    private int maxRetries;

    public Message sendWhatsAppMessage(String agentId, String recipientPhone, String messageBody) {
        log.info("Agent {} sending WhatsApp message to {}", agentId, recipientPhone);

        // Get agent's WhatsApp credentials
        AgentCredential credential = credentialRepository.findActiveByAgentIdAndChannel(agentId, "WHATSAPP")
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
                .retryCount(0)
                .maxRetries(maxRetries)
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

            // Schedule first retry
            LocalDateTime nextRetry = LocalDateTime.now().plusMinutes(initialDelayMinutes);
            message.setNextRetryAt(nextRetry);
            log.info("Message will retry in {} minutes at {}", initialDelayMinutes, nextRetry);
        }

        return messageRepository.save(message);
    }

    public Message sendEmail(String agentId, String recipientEmail, String subject, String messageBody) {
        log.info("Agent {} sending email to {}", agentId, recipientEmail);

        // Get agent's email credentials
        AgentCredential credential = credentialRepository.findActiveByAgentIdAndChannel(agentId, "EMAIL")
                .orElseThrow(() -> new BadRequestException("No SMTP email credentials found for agent"));

        // Decrypt credentials
        Map<String, String> decryptedCredentials = encryptionService.decryptMap(credential.getEncryptedCredentials());

        // Create message record
        Message message = Message.builder()
                .id(UUID.randomUUID().toString())
                .agentId(agentId)
                .channel(MessageChannel.EMAIL)
                .status(MessageStatus.PENDING)
                .recipientEmail(recipientEmail)
                .subject(subject)
                .messageBody(messageBody)
                .retryCount(0)
                .maxRetries(maxRetries)
                .build();

        try {
            // Send via SMTP
            String vendorMessageId = smtpEmailService.sendEmail(recipientEmail, subject, messageBody, decryptedCredentials);

            // Mark as sent
            message.setStatus(MessageStatus.SENT);
            message.setSentAt(LocalDateTime.now());
            message.setVendorMessageId(vendorMessageId);

            log.info("Email sent successfully. ID: {}", message.getId());

        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage(), e);
            message.setStatus(MessageStatus.FAILED);
            message.setFailureReason(e.getMessage());

            // Schedule first retry
            LocalDateTime nextRetry = LocalDateTime.now().plusMinutes(initialDelayMinutes);
            message.setNextRetryAt(nextRetry);
            log.info("Email will retry in {} minutes at {}", initialDelayMinutes, nextRetry);
        }

        return messageRepository.save(message);
    }

    public Message sendSms(String agentId, String recipientPhone, String messageBody) {
        log.info("Agent {} sending SMS to {}", agentId, recipientPhone);

        // Get agent's SMS credentials
        AgentCredential credential = credentialRepository.findActiveByAgentIdAndChannel(agentId, "SMS")
                .orElseThrow(() -> new BadRequestException("No Twilio SMS credentials found for agent"));

        // Decrypt credentials
        Map<String, String> decryptedCredentials = encryptionService.decryptMap(credential.getEncryptedCredentials());

        // Create message record
        Message message = Message.builder()
                .id(UUID.randomUUID().toString())
                .agentId(agentId)
                .channel(MessageChannel.SMS)
                .status(MessageStatus.PENDING)
                .recipientPhone(recipientPhone)
                .messageBody(messageBody)
                .retryCount(0)
                .maxRetries(maxRetries)
                .build();

        try {
            // Send via Twilio
            String vendorMessageId = twilioSmsService.sendSms(recipientPhone, messageBody, decryptedCredentials);

            // Mark as sent
            message.setStatus(MessageStatus.SENT);
            message.setSentAt(LocalDateTime.now());
            message.setVendorMessageId(vendorMessageId);

            log.info("SMS sent successfully. ID: {}", message.getId());

        } catch (Exception e) {
            log.error("Failed to send SMS: {}", e.getMessage(), e);
            message.setStatus(MessageStatus.FAILED);
            message.setFailureReason(e.getMessage());

            // Schedule first retry
            LocalDateTime nextRetry = LocalDateTime.now().plusMinutes(initialDelayMinutes);
            message.setNextRetryAt(nextRetry);
            log.info("SMS will retry in {} minutes at {}", initialDelayMinutes, nextRetry);
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
