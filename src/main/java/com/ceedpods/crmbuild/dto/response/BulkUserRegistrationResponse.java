package com.ceedpods.crmbuild.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for bulk user registration
 * Contains detailed results for each email address processed
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing results of bulk user registration")
public class BulkUserRegistrationResponse {

    @Schema(description = "Summary of the bulk registration operation")
    private Summary summary;

    @Schema(description = "Detailed results for each email address")
    private List<UserRegistrationResult> results;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Timestamp when the operation was completed")
    private LocalDateTime timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Summary statistics of the bulk registration")
    public static class Summary {
        @Schema(description = "Total number of emails processed", example = "10")
        private int totalProcessed;

        @Schema(description = "Number of users successfully created", example = "7")
        private int created;

        @Schema(description = "Number of users that already existed", example = "2")
        private int alreadyExists;

        @Schema(description = "Number of registrations that failed", example = "1")
        private int failed;

        @Schema(description = "Number of welcome emails successfully sent", example = "7")
        private int emailsSent;

        @Schema(description = "Number of welcome emails that failed to send", example = "0")
        private int emailsFailed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Result of registration for a single email")
    public static class UserRegistrationResult {
        @Schema(description = "Email address that was processed", example = "lahiru@gmail.com")
        private String email;

        @Schema(description = "Status of the registration", example = "created",
                allowableValues = {"created", "already_exists", "failed"})
        private String status;

        @Schema(description = "Temporary password generated (only for created users)", example = "lahiru123@")
        private String temporaryPassword;

        @Schema(description = "Status of welcome email sending", example = "sent",
                allowableValues = {"sent", "failed", "not_applicable"})
        private String emailStatus;

        @Schema(description = "Error message if registration or email failed")
        private String errorMessage;

        @Schema(description = "User ID if successfully created")
        private String userId;
    }
}
