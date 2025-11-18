package com.ceedpods.crmbuild.service.messaging;

import com.ceedpods.crmbuild.entity.messaging.AgentCredential;
import com.ceedpods.crmbuild.entity.messaging.Message;
import com.ceedpods.crmbuild.enums.MessageChannel;
import com.ceedpods.crmbuild.enums.MessageStatus;
import com.ceedpods.crmbuild.repository.AgentCredentialRepository;
import com.ceedpods.crmbuild.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageRetryService {

    private final MessageRepository messageRepository;
    private final AgentCredentialRepository credentialRepository;
    private final EncryptionService encryptionService;
    private final MetaWhatsAppService metaWhatsAppService;
    private final SmtpEmailService smtpEmailService;
    private final TwilioSmsService twilioSmsService;

    @Value("${app.messaging.retry.initial-delay-minutes:5}")
    private int initialDelayMinutes;

    @Value("${app.messaging.retry.backoff-multiplier:2}")
    private int backoffMultiplier;

    /**
     * Process all messages that are eligible for retry
     */
    public void processRetries() {
        log.info("Starting message retry process...");

        LocalDateTime now = LocalDateTime.now();
        List<Message> messagesToRetry = messageRepository.findMessagesForRetry(now);

        if (messagesToRetry.isEmpty()) {
            log.debug("No messages found for retry");
            return;
        }

        log.info("Found {} messages eligible for retry", messagesToRetry.size());

        for (Message message : messagesToRetry) {
            retryMessage(message);
        }

        log.info("Message retry process completed");
    }

    /**
     * Retry a single message
     */
    public void retryMessage(Message message) {
        log.info("Retrying message {} (attempt {}/{})",
                message.getId(), message.getRetryCount() + 1, message.getMaxRetries());

        try {
            // Get agent's credentials for the channel
            AgentCredential credential = credentialRepository
                    .findActiveByAgentIdAndChannel(message.getAgentId(), message.getChannel().name())
                    .orElseThrow(() -> new Exception("No credentials found for agent and channel"));

            // Decrypt credentials
            Map<String, String> decryptedCredentials = encryptionService.decryptMap(
                    credential.getEncryptedCredentials()
            );

            // Send message via appropriate channel
            String vendorMessageId = sendMessageByChannel(message, decryptedCredentials);

            // Success - mark as sent
            message.setStatus(MessageStatus.SENT);
            message.setSentAt(LocalDateTime.now());
            message.setVendorMessageId(vendorMessageId);
            message.setLastRetryAt(LocalDateTime.now());
            message.setFailureReason(null);
            message.setNextRetryAt(null);

            log.info("Message {} successfully sent on retry attempt {}",
                    message.getId(), message.getRetryCount() + 1);

        } catch (Exception e) {
            log.error("Retry attempt {} failed for message {}: {}",
                    message.getRetryCount() + 1, message.getId(), e.getMessage());

            // Increment retry count
            message.setRetryCount(message.getRetryCount() + 1);
            message.setLastRetryAt(LocalDateTime.now());
            message.setFailureReason(e.getMessage());

            // Calculate next retry time using exponential backoff
            if (message.getRetryCount() < message.getMaxRetries()) {
                int delayMinutes = calculateRetryDelay(message.getRetryCount());
                LocalDateTime nextRetry = LocalDateTime.now().plusMinutes(delayMinutes);
                message.setNextRetryAt(nextRetry);

                log.info("Message {} will retry again in {} minutes (attempt {}/{})",
                        message.getId(), delayMinutes, message.getRetryCount() + 1, message.getMaxRetries());
            } else {
                log.warn("Message {} has exhausted all retry attempts ({}/{}). Marking as permanently failed.",
                        message.getId(), message.getRetryCount(), message.getMaxRetries());
                message.setNextRetryAt(null);
            }
        }

        // Save updated message
        messageRepository.save(message);
    }

    /**
     * Send message via the appropriate channel
     */
    private String sendMessageByChannel(Message message, Map<String, String> credentials) throws Exception {
        MessageChannel channel = message.getChannel();

        return switch (channel) {
            case WHATSAPP -> metaWhatsAppService.sendMessage(
                    message.getRecipientPhone(),
                    message.getMessageBody(),
                    credentials
            );
            case EMAIL -> smtpEmailService.sendEmail(
                    message.getRecipientEmail(),
                    message.getSubject(),
                    message.getMessageBody(),
                    credentials
            );
            case SMS -> twilioSmsService.sendSms(
                    message.getRecipientPhone(),
                    message.getMessageBody(),
                    credentials
            );
        };
    }

    /**
     * Calculate retry delay using exponential backoff
     * Formula: initialDelay * (backoffMultiplier ^ retryCount)
     *
     * Example with initialDelay=5, backoffMultiplier=2:
     * - Retry 1: 5 minutes
     * - Retry 2: 10 minutes
     * - Retry 3: 20 minutes
     */
    private int calculateRetryDelay(int retryCount) {
        return initialDelayMinutes * (int) Math.pow(backoffMultiplier, retryCount);
    }
}
