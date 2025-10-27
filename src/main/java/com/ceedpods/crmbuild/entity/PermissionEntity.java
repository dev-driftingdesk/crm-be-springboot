package com.ceedpods.crmbuild.entity;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.enums.PermissionCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = AppConstants.MongoDB.COLLECTION_PERMISSIONS)
public class PermissionEntity extends BaseEntity {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String permissionCode;
    
    private String displayName;
    private String description;
    private PermissionCategory category;
    private boolean adminOnly;
    private boolean assignable;
    private boolean active = true;
}