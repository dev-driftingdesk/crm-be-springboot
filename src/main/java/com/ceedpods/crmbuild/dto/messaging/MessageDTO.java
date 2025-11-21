package com.ceedpods.crmbuild.dto.messaging;

import com.ceedpods.crmbuild.enums.MessageChannel;
import com.ceedpods.crmbuild.enums.MessageStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {

    private String id;
    private String agentId;
    private MessageChannel channel;
    private MessageStatus status;

    // Recipient fields (channel-specific)
    private String recipientPhone;
    private String recipientEmail;

    // Message content
    private String subject;
    private String messageBody;

    // Voice call specific fields - Conference-based two-way calls
    private String conferenceName;
    private String callerCallSid;
    private String recipientCallSid;
    private String callerUserId;
    private String recipientUserId;
    private String callerPhone;
    private String conferenceStatus;
    private String recordingUrl;
    private String recordingSid;
    private Long callDuration;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime callStartedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime callEndedAt;

    // Transcription fields
    private String transcriptionText;
    private String transcriptionSid;
    private String transcriptionStatus;
    private String transcriptionUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transcriptionCompletedAt;

    // Legacy fields
    private String callSid;
    private String callStatus;

    private String vendorMessageId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentAt;

    private String failureReason;

    // Retry mechanism
    private Integer retryCount;
    private Integer maxRetries;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nextRetryAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastRetryAt;
}
