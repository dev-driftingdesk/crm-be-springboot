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
    private final TwilioVoiceService twilioVoiceService;
    private final com.ceedpods.crmbuild.repository.UserRepository userRepository;

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

    public Message initiateCall(String callerUserId, String recipientUserId,
                               Boolean record, String statusCallbackUrl) {
        log.info("User {} initiating voice call to user {}", callerUserId, recipientUserId);

        // Get caller's voice credentials
        AgentCredential credential = credentialRepository.findActiveByAgentIdAndChannel(callerUserId, "VOICE")
                .orElseThrow(() -> new BadRequestException("No Twilio Voice credentials found for caller"));

        // Decrypt credentials
        Map<String, String> decryptedCredentials = encryptionService.decryptMap(credential.getEncryptedCredentials());

        // Fetch both users from database
        var caller = userRepository.findByKeycloakId(callerUserId)
                .orElseThrow(() -> new BadRequestException("Caller user not found"));

        var recipient = userRepository.findByKeycloakId(recipientUserId)
                .orElseThrow(() -> new BadRequestException("Recipient user not found"));

        // Validate phone numbers
        if (caller.getPhoneNumber() == null || caller.getPhoneNumber().isEmpty()) {
            throw new BadRequestException("Caller does not have a phone number configured");
        }
        if (recipient.getPhoneNumber() == null || recipient.getPhoneNumber().isEmpty()) {
            throw new BadRequestException("Recipient does not have a phone number configured");
        }

        // Generate unique conference name
        String conferenceName = "conf-" + UUID.randomUUID().toString();

        // Create message record
        Message message = Message.builder()
                .id(UUID.randomUUID().toString())
                .agentId(callerUserId)
                .channel(MessageChannel.VOICE)
                .status(MessageStatus.PENDING)
                .recipientPhone(recipient.getPhoneNumber())
                .callerUserId(callerUserId)
                .recipientUserId(recipientUserId)
                .callerPhone(caller.getPhoneNumber())
                .conferenceName(conferenceName)
                .conferenceStatus("initiated")
                .retryCount(0)
                .maxRetries(maxRetries)
                .build();

        try {
            // Call the caller first (they initiate the call, so they start the conference)
            String callerCallSid = twilioVoiceService.initiateConferenceCall(
                caller.getPhoneNumber(), conferenceName, decryptedCredentials, record, statusCallbackUrl
            );

            message.setCallerCallSid(callerCallSid);
            message.setCallSid(callerCallSid); // For backward compatibility

            // Call the recipient (they join the conference after caller)
            String recipientCallSid = twilioVoiceService.initiateConferenceCall(
                recipient.getPhoneNumber(), conferenceName, decryptedCredentials, record, statusCallbackUrl
            );

            message.setRecipientCallSid(recipientCallSid);

            // Mark as sent (both calls initiated)
            message.setStatus(MessageStatus.SENT);
            message.setSentAt(LocalDateTime.now());
            message.setVendorMessageId(conferenceName); // Store conference name as vendorMessageId
            message.setCallStatus("queued"); // Initial status

            log.info("Conference call initiated successfully. ID: {}, Conference: {}, Caller SID: {}, Recipient SID: {}",
                    message.getId(), conferenceName, callerCallSid, recipientCallSid);

        } catch (Exception e) {
            log.error("Failed to initiate conference call: {}", e.getMessage(), e);
            message.setStatus(MessageStatus.FAILED);
            message.setFailureReason(e.getMessage());
            message.setConferenceStatus("failed");

            // Schedule first retry
            LocalDateTime nextRetry = LocalDateTime.now().plusMinutes(initialDelayMinutes);
            message.setNextRetryAt(nextRetry);
            log.info("Conference call will retry in {} minutes at {}", initialDelayMinutes, nextRetry);
        }

        return messageRepository.save(message);
    }

    public void terminateCall(String agentId, String callSid) {
        log.info("Agent {} terminating call {}", agentId, callSid);

        // Find the message by callSid
        Message message = messageRepository.findByCallSid(callSid)
                .orElseThrow(() -> new BadRequestException("Call not found"));

        // Verify agent owns this call
        if (!message.getAgentId().equals(agentId)) {
            throw new BadRequestException("Unauthorized access to call");
        }

        // Get agent's voice credentials
        AgentCredential credential = credentialRepository.findActiveByAgentIdAndChannel(agentId, "VOICE")
                .orElseThrow(() -> new BadRequestException("No Twilio Voice credentials found for agent"));

        // Decrypt credentials
        Map<String, String> decryptedCredentials = encryptionService.decryptMap(credential.getEncryptedCredentials());

        try {
            // Terminate call via Twilio
            twilioVoiceService.terminateCall(callSid, decryptedCredentials);

            // Update message status
            message.setCallStatus("completed");
            message.setCallEndedAt(LocalDateTime.now());
            messageRepository.save(message);

            log.info("Voice call terminated successfully. Call SID: {}", callSid);

        } catch (Exception e) {
            log.error("Failed to terminate voice call: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to terminate call: " + e.getMessage());
        }
    }

    public Message updateCallStatus(String callSid, String callStatus, Long duration, String recordingUrl) {
        log.info("Updating call status for Call SID: {}, Status: {}", callSid, callStatus);

        // Find the message by callSid
        Message message = messageRepository.findByCallSid(callSid)
                .orElseThrow(() -> new BadRequestException("Call not found"));

        // Update call status
        message.setCallStatus(callStatus);

        // Update timestamps based on status
        if ("in-progress".equalsIgnoreCase(callStatus) && message.getCallStartedAt() == null) {
            message.setCallStartedAt(LocalDateTime.now());
        }

        if (("completed".equalsIgnoreCase(callStatus) || "failed".equalsIgnoreCase(callStatus) ||
             "busy".equalsIgnoreCase(callStatus) || "no-answer".equalsIgnoreCase(callStatus)) &&
             message.getCallEndedAt() == null) {
            message.setCallEndedAt(LocalDateTime.now());
        }

        // Update duration if provided
        if (duration != null && duration > 0) {
            message.setCallDuration(duration);
        }

        // Update recording URL if provided
        if (recordingUrl != null && !recordingUrl.isEmpty()) {
            message.setRecordingUrl(recordingUrl);
        }

        // Update message status based on call status
        if ("completed".equalsIgnoreCase(callStatus)) {
            message.setStatus(MessageStatus.SENT);
        } else if ("failed".equalsIgnoreCase(callStatus) || "busy".equalsIgnoreCase(callStatus) ||
                   "no-answer".equalsIgnoreCase(callStatus) || "canceled".equalsIgnoreCase(callStatus)) {
            message.setStatus(MessageStatus.FAILED);
            if (message.getFailureReason() == null || message.getFailureReason().isEmpty()) {
                message.setFailureReason("Call ended with status: " + callStatus);
            }
        }

        messageRepository.save(message);
        log.info("Call status updated successfully. Message ID: {}", message.getId());

        return message;
    }

    public void updateRecordingInfo(String recordingSid, String recordingUrl, String callSid) {
        log.info("Updating recording info for RecordingSid: {}, CallSid: {}", recordingSid, callSid);

        try {
            // Find message by callSid (could be caller or recipient)
            Message message = messageRepository.findByCallSid(callSid)
                    .or(() -> messageRepository.findByCallerCallSid(callSid))
                    .or(() -> messageRepository.findByRecipientCallSid(callSid))
                    .orElse(null);

            if (message != null) {
                message.setRecordingSid(recordingSid);
                message.setRecordingUrl(recordingUrl);
                message.setTranscriptionStatus("in-progress"); // Transcription will be processed next
                messageRepository.save(message);
                log.info("Recording info saved for message ID: {}", message.getId());
            } else {
                log.warn("No message found for CallSid: {}", callSid);
            }
        } catch (Exception e) {
            log.error("Error updating recording info: {}", e.getMessage(), e);
        }
    }

    public void updateTranscription(String transcriptionSid, String transcriptionText,
                                   String transcriptionStatus, String transcriptionUrl,
                                   String recordingSid) {
        log.info("Updating transcription for TranscriptionSid: {}, RecordingSid: {}", transcriptionSid, recordingSid);

        try {
            // Find message by recordingSid
            Message message = messageRepository.findByRecordingSid(recordingSid)
                    .orElse(null);

            if (message != null) {
                message.setTranscriptionSid(transcriptionSid);
                message.setTranscriptionText(transcriptionText);
                message.setTranscriptionStatus(transcriptionStatus);
                message.setTranscriptionUrl(transcriptionUrl);
                message.setTranscriptionCompletedAt(LocalDateTime.now());

                messageRepository.save(message);
                log.info("Transcription saved successfully for message ID: {}. Text length: {}",
                        message.getId(), transcriptionText != null ? transcriptionText.length() : 0);
            } else {
                log.warn("No message found for RecordingSid: {}", recordingSid);
            }
        } catch (Exception e) {
            log.error("Error updating transcription: {}", e.getMessage(), e);
        }
    }
}
