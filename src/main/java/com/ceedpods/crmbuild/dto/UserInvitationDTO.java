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
public class UserInvitationDTO extends BaseDTO {
    
    private String id;
    private String invitationToken;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private UserRole role;
    private String assignedManagerId;
    private List<String> permissionCodes;
    
    private String invitedBy;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime invitedAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;
    
    private boolean used;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime usedAt;
    
    private String registeredUserId;
    
    // Invitation status tracking
    private boolean emailSent;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime emailSentAt;
    
    private int emailAttempts;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastEmailAttempt;
    
    // Reminder tracking
    private int reminderCount;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastReminderSent;
    
    // Related entities
    private UserDTO invitedByUser;
    private UserDTO assignedManager;
    private UserDTO registeredUser;
    private List<PermissionDTO> permissions;
    
    // Computed fields
    private boolean isExpired;
    private boolean isValid;
    private long daysUntilExpiry;
    private long daysSinceInvited;
    private String invitationStatus; // PENDING, EXPIRED, USED, INVALID
    private long hoursUntilExpiry;
}