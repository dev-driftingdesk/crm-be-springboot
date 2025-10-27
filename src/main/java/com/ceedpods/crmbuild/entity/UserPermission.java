package com.ceedpods.crmbuild.entity;

import com.ceedpods.crmbuild.constants.AppConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = AppConstants.MongoDB.COLLECTION_USER_PERMISSIONS)
@CompoundIndexes({
    @CompoundIndex(name = "user_permission_idx", def = "{'userId' : 1, 'permissionCode' : 1}"),
    @CompoundIndex(name = "user_active_permissions_idx", def = "{'userId' : 1, 'active' : 1}")
})
public class UserPermission extends BaseEntity {
    
    @Id
    private String id;
    
    @Indexed
    private String userId; // Keycloak user ID
    
    @Indexed
    private String permissionCode;
    
    private String assignedBy; // Admin who assigned this permission
    private LocalDateTime assignedAt;
    private LocalDateTime expiresAt; // Optional expiry
    private boolean active = true;
    private String notes; // Reason for assignment
    private String revokedBy;
    private LocalDateTime revokedAt;
    private String revocationReason;
    
    public void revoke(String revokedBy, String reason) {
        this.active = false;
        this.revokedBy = revokedBy;
        this.revokedAt = LocalDateTime.now();
        this.revocationReason = reason;
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isEffective() {
        return active && !isExpired();
    }
}