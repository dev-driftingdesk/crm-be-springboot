package com.ceedpods.crmbuild.entity.audit;

import com.ceedpods.crmbuild.constants.AppConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * AuditLog entity - stores detailed audit trail for all critical system operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = AppConstants.MongoDB.COLLECTION_AUDIT_LOGS)
public class AuditLog {

    @Id
    private String id; // UUID as primary ID

    /**
     * Username or identifier of the user who performed the action
     */
    @Indexed
    private String username;

    /**
     * Keycloak user ID (if available)
     */
    @Indexed
    private String userId;

    /**
     * Email of the user (if available)
     */
    private String userEmail;

    /**
     * Action performed: LOGIN, CREATED, UPDATED, DELETED
     */
    @Indexed
    private String action;

    /**
     * Module/Entity affected: PRODUCT, LEAD, DEAL, DEAL_NOTE, LEAD_NOTE, etc.
     */
    @Indexed
    private String entityType;

    /**
     * ID of the affected entity
     */
    @Indexed
    private String entityId;

    /**
     * Entity name or description for easier identification
     */
    private String entityName;

    /**
     * Additional details about the operation (optional)
     */
    private String details;

    /**
     * IP address of the client (optional)
     */
    private String ipAddress;

    /**
     * Timestamp when the action was performed
     */
    @CreatedDate
    @Indexed
    private LocalDateTime timestamp;

    /**
     * Status of the operation: SUCCESS, FAILED
     */
    private String status;

    /**
     * Error message if operation failed
     */
    private String errorMessage;
}
