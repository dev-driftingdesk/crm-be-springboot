package com.ceedpods.crmbuild.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PermissionDTO extends BaseDTO {
    
    private String id;
    private String permissionCode;
    private String displayName;
    private String description;
    private String category;
    private boolean assignable;
    private boolean active;
    
    // Additional metadata
    private long assignedUsersCount;
    private boolean isSystemPermission;
}