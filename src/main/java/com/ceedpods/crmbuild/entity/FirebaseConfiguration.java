package com.ceedpods.crmbuild.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "firebase_configurations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseConfiguration {

    @Id
    private String id;

    private String type;
    private String projectId;
    private String privateKeyId;
    private String privateKey;
    private String clientEmail;
    private String clientId;
    private String authUri;
    private String tokenUri;
    private String authProviderX509CertUrl;
    private String clientX509CertUrl;
    private String universeDomain;

    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
