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
 * Summary DTO for deal list responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"dealId", "dealName", "dealValue", "status", "linkedLead", "assignedAgents", "createdAt", "updatedAt"})
public class DealListItem {

    private String dealId;
    private String dealName;
    private BigDecimal dealValue;
    private String status;
    private LinkedLeadInfo linkedLead;
    private List<AssignedAgentInfo> assignedAgents;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkedLeadInfo {
        private String leadId;
        private String leadName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignedAgentInfo {
        private String userId;
        private String fullName;
        private String role;
    }
}
