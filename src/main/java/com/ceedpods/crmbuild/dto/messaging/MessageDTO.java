package com.ceedpods.crmbuild.dto.messaging;

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
    private MessageStatus status;
    private String recipientPhone;
    private String messageBody;
    private String vendorMessageId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentAt;

    private String failureReason;
}
