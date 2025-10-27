package com.ceedpods.crmbuild.entity.user;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.entity.BaseEntity;
import com.ceedpods.crmbuild.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = AppConstants.MongoDB.COLLECTION_USERS)
public class User extends BaseEntity {
    
    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @Indexed(unique = true)
    private String keycloakId;

    @Indexed(unique = true)
    private String email;
    
    private String firstName;
    private String lastName;
    private UserRole role;
    private boolean enabled = true;
    private boolean emailVerified = false;
    
    // Profile information
    private String phoneNumber;
    private String department;
    private String territory;
    private String jobTitle;
    
    // Manager hierarchy
    private String managerId; // Keycloak ID of assigned manager
    private List<String> reportIds; // Keycloak IDs of direct reports
    
    // Authentication tracking
    private LocalDateTime lastLogin;
    private LocalDateTime passwordChangedAt;
    private boolean mustChangePassword = false;
    private int failedLoginAttempts = 0;
    private LocalDateTime lastFailedLogin;
    private boolean accountLocked = false;
    private LocalDateTime accountLockedAt;
    
    // Invitation tracking
    private String invitationToken;
    private boolean invitationUsed = false;
    private LocalDateTime invitationUsedAt;
    
    public String getFullName() {

        return firstName + " " + lastName;
    }
    
    public boolean isManager() {
        return role == UserRole.MANAGER || role == UserRole.ADMIN;
    }
    
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
    
    public void lockAccount() {
        this.accountLocked = true;
        this.accountLockedAt = LocalDateTime.now();
    }
    
    public void unlockAccount() {
        this.accountLocked = false;
        this.accountLockedAt = null;
        this.failedLoginAttempts = 0;
    }
    
    public void recordFailedLogin() {
        this.failedLoginAttempts++;
        this.lastFailedLogin = LocalDateTime.now();
    }
    
    public void recordSuccessfulLogin() {
        this.lastLogin = LocalDateTime.now();
        this.failedLoginAttempts = 0;
        this.lastFailedLogin = null;
    }
}
