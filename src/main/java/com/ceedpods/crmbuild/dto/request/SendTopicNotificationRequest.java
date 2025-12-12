package com.ceedpods.crmbuild.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendTopicNotificationRequest {

    @NotBlank(message = "Topic is required")
    private String topic;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Body is required")
    private String body;

    private String imageUrl;
    private Map<String, String> data;
    private String clickAction;
}
