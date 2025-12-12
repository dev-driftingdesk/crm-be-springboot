package com.ceedpods.crmbuild.dto.notification;

import com.ceedpods.crmbuild.enums.NotificationPlatform;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTokenDTO {

    private String id;
    private String userId;
    private NotificationPlatform platform;
    private String deviceName;
    private String deviceModel;
    private String osVersion;
    private String appVersion;
    private boolean active;
    private List<String> subscribedTopics;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registeredAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUsedAt;
}
