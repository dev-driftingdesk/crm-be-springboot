package com.ceedpods.crmbuild.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for AuditLog
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {

    private String id;
    private String username;
    private String userId;
    private String userEmail;
    private String action;
    private String entityType;
    private String entityId;
    private String entityName;
    private String details;
    private String ipAddress;
    private LocalDateTime timestamp;
    private String status;
    private String errorMessage;
}
