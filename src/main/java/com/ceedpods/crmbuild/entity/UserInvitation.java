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

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = AppConstants.MongoDB.COLLECTION_USER_INVITATIONS)
public class UserInvitation extends BaseEntity {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String invitationToken;
    
    @Indexed
    private String email;
    
    private String firstName;
    private String lastName;
    private UserRole role;
    private String assignedManagerId; // For sales reps
    private List<String> permissionCodes; // Pre-assigned permissions
    
    private String invitedBy; // Admin who sent invitation
    private LocalDateTime invitedAt;
    private LocalDateTime expiresAt;
    private boolean used = false;
    private LocalDateTime usedAt;
    private String registeredUserId; // Keycloak ID when user registers
    
    // Invitation status tracking
    private boolean emailSent = false;
    private LocalDateTime emailSentAt;
    private int emailAttempts = 0;
    private LocalDateTime lastEmailAttempt;
    
    // Reminder tracking
    private int reminderCount = 0;
    private LocalDateTime lastReminderSent;
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isValid() {
        return !used && !isExpired() && emailSent;
    }
    
    public void markAsUsed(String userId) {
        this.used = true;
        this.usedAt = LocalDateTime.now();
        this.registeredUserId = userId;
    }
    
    public void markEmailSent() {
        this.emailSent = true;
        this.emailSentAt = LocalDateTime.now();
        this.emailAttempts++;
        this.lastEmailAttempt = LocalDateTime.now();
    }
    
    public void incrementReminderCount() {
        this.reminderCount++;
        this.lastReminderSent = LocalDateTime.now();
    }
}