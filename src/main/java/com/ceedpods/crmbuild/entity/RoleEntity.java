package com.ceedpods.crmbuild.entity;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = AppConstants.MongoDB.COLLECTION_ROLES)
public class RoleEntity extends BaseEntity {
    
    @Id
    private String id; // This will be set as UUID
    
    @Indexed(unique = true)
    private UserRole roleName;
    
    private String displayName;
    private String description;
    private List<String> permissionCodes; // List of permission codes assigned to this role
    private boolean active = true;
    private boolean isSystemRole = true; // System-defined roles cannot be deleted
}