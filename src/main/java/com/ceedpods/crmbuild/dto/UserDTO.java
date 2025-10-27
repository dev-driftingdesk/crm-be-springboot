package com.ceedpods.crmbuild.dto;

import com.ceedpods.crmbuild.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserDTO extends BaseDTO {
    
    private String id;
    private String keycloakId;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private UserRole role;
    private boolean enabled;
    private boolean emailVerified;
    
    // Profile information
    private String phoneNumber;
    private String department;
    private String territory;
    private String jobTitle;
    
    // Manager hierarchy
    private String managerId;
    private UserDTO manager;
    private List<UserDTO> reports;
    
    // Authentication tracking
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLogin;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime passwordChangedAt;
    
    private boolean mustChangePassword;
    private int failedLoginAttempts;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastFailedLogin;
    
    private boolean accountLocked;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime accountLockedAt;
    
    // Invitation tracking
    private boolean invitationUsed;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime invitationUsedAt;
    
    // Computed fields
    private boolean isManager;
    private boolean isAdmin;
    private long permissionCount;
    private long reportsCount;
}