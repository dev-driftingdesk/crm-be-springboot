package com.ceedpods.crmbuild.mapper;

import com.ceedpods.crmbuild.dto.messaging.MessageDTO;
import com.ceedpods.crmbuild.entity.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public MessageDTO toDTO(Message message) {
        if (message == null) {
            return null;
        }

        return MessageDTO.builder()
                .id(message.getId())
                .agentId(message.getAgentId())
                .channel(message.getChannel())
                .status(message.getStatus())
                .recipientPhone(message.getRecipientPhone())
                .recipientEmail(message.getRecipientEmail())
                .subject(message.getSubject())
                .messageBody(message.getMessageBody())
                .conferenceName(message.getConferenceName())
                .callerCallSid(message.getCallerCallSid())
                .recipientCallSid(message.getRecipientCallSid())
                .callerUserId(message.getCallerUserId())
                .recipientUserId(message.getRecipientUserId())
                .callerPhone(message.getCallerPhone())
                .conferenceStatus(message.getConferenceStatus())
                .recordingUrl(message.getRecordingUrl())
                .recordingSid(message.getRecordingSid())
                .callDuration(message.getCallDuration())
                .callStartedAt(message.getCallStartedAt())
                .callEndedAt(message.getCallEndedAt())
                .transcriptionText(message.getTranscriptionText())
                .transcriptionSid(message.getTranscriptionSid())
                .transcriptionStatus(message.getTranscriptionStatus())
                .transcriptionUrl(message.getTranscriptionUrl())
                .transcriptionCompletedAt(message.getTranscriptionCompletedAt())
                .callSid(message.getCallSid())
                .callStatus(message.getCallStatus())
                .vendorMessageId(message.getVendorMessageId())
                .sentAt(message.getSentAt())
                .failureReason(message.getFailureReason())
                .retryCount(message.getRetryCount())
                .maxRetries(message.getMaxRetries())
                .nextRetryAt(message.getNextRetryAt())
                .lastRetryAt(message.getLastRetryAt())
                .build();
    }
}
