package com.ceedpods.crmbuild.dto.request;

import com.ceedpods.crmbuild.enums.NotificationPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDeviceTokenRequest {

    @NotBlank(message = "Device token is required")
    private String token;

    @NotNull(message = "Platform is required")
    private NotificationPlatform platform;

    private String deviceName;
    private String deviceModel;
    private String osVersion;
    private String appVersion;
}
