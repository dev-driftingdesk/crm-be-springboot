package com.ceedpods.crmbuild.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicSubscriptionRequest {

    @NotBlank(message = "Device token is required")
    private String deviceToken;
}
