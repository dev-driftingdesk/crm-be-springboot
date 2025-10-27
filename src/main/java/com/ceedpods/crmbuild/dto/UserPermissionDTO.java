package com.ceedpods.crmbuild.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserPermissionDTO extends BaseDTO {
    
    private String id;
    private String userId;
    private String permissionCode;
    private String assignedBy;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignedAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;
    
    private boolean active;
    private String notes;
    private String revokedBy;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime revokedAt;
    
    private String revocationReason;
    
    // Related entities
    private UserDTO user;
    private PermissionDTO permission;
    private UserDTO assignedByUser;
    private UserDTO revokedByUser;
    
    // Computed fields
    private boolean isExpired;
    private boolean isEffective;
    private long daysUntilExpiry;
    private long daysSinceAssigned;
}