package com.ceedpods.crmbuild.entity.messaging;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.entity.BaseEntity;
import com.ceedpods.crmbuild.enums.MessageChannel;
import com.ceedpods.crmbuild.enums.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = AppConstants.MongoDB.COLLECTION_MESSAGES)
public class Message extends BaseEntity {

    @Id
    private String id;

    @Indexed
    private String agentId; // Keycloak user ID

    private MessageChannel channel; // WHATSAPP, EMAIL, etc.

    @Indexed
    private MessageStatus status; // PENDING, SENT, FAILED

    // Recipient fields (channel-specific)
    private String recipientPhone; // WhatsApp phone number
    private String recipientEmail; // Email address

    // Message content
    private String subject; // Email subject (null for WhatsApp)
    private String messageBody; // Message text

    // Voice call specific fields - Conference-based two-way calls
    private String conferenceName; // Unique conference room identifier
    private String callerCallSid; // Twilio Call SID for caller (User A)
    private String recipientCallSid; // Twilio Call SID for recipient (User B)
    private String callerUserId; // Keycloak ID of the caller
    private String recipientUserId; // Keycloak ID of the recipient
    private String callerPhone; // Phone number of caller
    private String conferenceStatus; // Conference status (initiated, in-progress, completed)
    private String recordingUrl; // URL to call recording if enabled
    private String recordingSid; // Twilio Recording SID
    private Long callDuration; // Duration of call in seconds
    private LocalDateTime callStartedAt; // When call actually started
    private LocalDateTime callEndedAt; // When call ended

    // Transcription fields
    private String transcriptionText; // Full transcript text
    private String transcriptionSid; // Twilio Transcription SID
    private String transcriptionStatus; // in-progress, completed, failed
    private String transcriptionUrl; // URL to transcript JSON from Twilio
    private LocalDateTime transcriptionCompletedAt; // When transcription finished

    // Legacy field for backward compatibility
    private String callSid; // Primary call SID (usually caller's)
    private String callStatus; // Call status

    // Vendor tracking
    private String vendorMessageId; // Vendor-specific message ID
    private LocalDateTime sentAt;
    private String failureReason;

    // Retry mechanism
    private Integer retryCount = 0; // Number of retry attempts made
    private Integer maxRetries = 3; // Maximum retry attempts allowed
    private LocalDateTime nextRetryAt; // When to attempt next retry
    private LocalDateTime lastRetryAt; // When last retry was attempted
}
