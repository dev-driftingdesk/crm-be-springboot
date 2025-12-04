package com.ceedpods.crmbuild.entity;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.enums.Permission;
import com.ceedpods.crmbuild.enums.PermissionScope;
import com.ceedpods.crmbuild.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing the default permission scope matrix for each role
 * This defines what scope each role gets for each permission
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = AppConstants.MongoDB.COLLECTION_USER_ROLE_PERMISSIONS)
@CompoundIndexes({
    @CompoundIndex(name = "role_permission_idx", def = "{'role': 1, 'permission': 1}", unique = true)
})
public class UserRolePermission extends BaseEntity {
    
    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    
    private UserRole role;
    private Permission permission;
    private PermissionScope scope; // Default scope for this role-permission combination
    private boolean enabled; // Whether this permission is enabled for this role
    
    // Additional configuration for specific permissions
    private Map<String, Object> configuration; // Store permission-specific configs (e.g., discount limits)
    
    // Audit fields
    private LocalDateTime lastModified;
    private String modifiedBy;
    
    /**
     * Factory method to create a role permission with scope
     */
    public static UserRolePermission create(UserRole role, Permission permission, PermissionScope scope) {
        return UserRolePermission.builder()
            .role(role)
            .permission(permission)
            .scope(scope)
            .enabled(true)
            .build();
    }
    
    /**
     * Factory method to create a disabled role permission
     */
    public static UserRolePermission createDisabled(UserRole role, Permission permission) {
        return UserRolePermission.builder()
            .role(role)
            .permission(permission)
            .scope(PermissionScope.NONE)
            .enabled(false)
            .build();
    }
    
    /**
     * Get the effective scope for this permission
     */
    public PermissionScope getEffectiveScope() {
        return enabled ? scope : PermissionScope.NONE;
    }
}