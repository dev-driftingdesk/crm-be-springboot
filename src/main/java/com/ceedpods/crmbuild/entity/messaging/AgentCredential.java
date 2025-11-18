package com.ceedpods.crmbuild.entity.messaging;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = AppConstants.MongoDB.COLLECTION_AGENT_CREDENTIALS)
public class AgentCredential extends BaseEntity {

    @Id
    private String id;

    @Indexed
    private String agentId; // Keycloak user ID

    @Indexed
    private String channel; // Channel type: WHATSAPP, EMAIL, etc.

    // Encrypted vendor credentials
    // For WhatsApp: "accessToken", "phoneNumberId", "businessAccountId"
    // For Email: "connectionString", "senderAddress"
    private Map<String, String> encryptedCredentials;

    private boolean active = true;
}
