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

    private MessageChannel channel; // WHATSAPP

    @Indexed
    private MessageStatus status; // PENDING, SENT, FAILED

    private String recipientPhone; // WhatsApp phone number
    private String messageBody; // Message text

    private String vendorMessageId; // Meta message ID
    private LocalDateTime sentAt;
    private String failureReason;
}
