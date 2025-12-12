package com.ceedpods.crmbuild.entity.notification;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.entity.BaseEntity;
import com.ceedpods.crmbuild.enums.NotificationPlatform;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = AppConstants.MongoDB.COLLECTION_DEVICE_TOKENS)
public class DeviceToken extends BaseEntity {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed(unique = true)
    private String token;

    private NotificationPlatform platform;

    private String deviceName;
    private String deviceModel;
    private String osVersion;
    private String appVersion;

    @Builder.Default
    private boolean active = true;

    private LocalDateTime lastUsedAt;
    private LocalDateTime registeredAt;

    @Builder.Default
    private List<String> subscribedTopics = new ArrayList<>();
}
