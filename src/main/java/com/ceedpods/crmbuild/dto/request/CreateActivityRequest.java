package com.ceedpods.crmbuild.dto.request;

import com.ceedpods.crmbuild.enums.ActivityType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for creating a new activity on a deal.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateActivityRequest {

    @NotNull(message = "Activity type is required")
    private ActivityType type;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Performed at timestamp is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime performedAt;
}
