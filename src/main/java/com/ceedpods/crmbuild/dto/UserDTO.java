package com.ceedpods.crmbuild.dto;

import com.ceedpods.crmbuild.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "User data transfer object containing user profile and authentication information")
public class UserDTO extends BaseDTO {

    @Schema(description = "User's internal database ID", example = "507f1f77bcf86cd799439011")
    private String id;

    @Schema(description = "User's Keycloak UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String keycloakId;

    @Schema(description = "User's email address", example = "user@example.com")
    private String email;

    @Schema(description = "User's first name", example = "John")
    private String firstName;

    @Schema(description = "User's last name", example = "Doe")
    private String lastName;

    @Schema(description = "User's full name", example = "John Doe")
    private String fullName;

    @Schema(description = "User's role in the system", example = "SALES_REP")
    private UserRole role;

    @Schema(description = "Whether the user account is enabled", example = "true")
    private boolean enabled;

    @Schema(description = "Whether the user's email has been verified", example = "true")
    private boolean emailVerified;
    
    // Profile information
    private String phoneNumber;
    private String department;
    private String territory;
    private String jobTitle;

    @Schema(description = "User's profile picture as Base64 encoded string", example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA...")
    private String profilePicture;
    
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