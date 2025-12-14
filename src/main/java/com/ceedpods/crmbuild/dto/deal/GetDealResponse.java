package com.ceedpods.crmbuild.dto.deal;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Detailed response DTO for Get Deal by ID API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"dealId", "dealName", "createdBy", "dealValue", "totalCommission", "linkedLead",
        "products", "activities", "actionItems", "notes", "assignedAgents", "status", "createdAt", "updatedAt"})
public class GetDealResponse {

    private String dealId;
    private String dealName;
    private CreatedByInfo createdBy;
    private BigDecimal dealValue;
    private BigDecimal totalCommission;
    private LinkedLeadInfo linkedLead;
    private List<ProductInfo> products;
    private List<ActivityInfo> activities;
    private List<ActionItemInfo> actionItems;
    private List<NoteInfo> notes;
    private List<AssignedAgentInfo> assignedAgents;
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;

    /**
     * Created by user info
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatedByInfo {
        private String userId;
        private String fullName;
    }

    /**
     * Linked lead info
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkedLeadInfo {
        private String leadId;
        private String leadName;
    }

    /**
     * Product info in the deal
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private String productId;
        private String productName;
        private String description;
        private BigDecimal dealValue;
        private BigDecimal commission;
    }

    /**
     * Activity info (placeholder for future implementation)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityInfo {
        private String activityId;
        private String type;
        private String description;
        private String performedBy;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        private LocalDateTime performedAt;
    }

    /**
     * Action item info (placeholder for future implementation)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionItemInfo {
        private String actionId;
        private String title;
        private String dueDate;
        private String assignedTo;
        private String status;
        private String priority;
    }

    /**
     * Note info
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoteInfo {
        private String noteId;
        private String content;
        private String createdBy;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        private LocalDateTime createdAt;
    }

    /**
     * Assigned agent info
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignedAgentInfo {
        private String userId;
        private String fullName;
        private String role;
        private String profilePicture;
    }
}
