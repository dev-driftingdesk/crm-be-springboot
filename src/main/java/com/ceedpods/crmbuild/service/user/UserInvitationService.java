package com.ceedpods.crmbuild.service.user;

import com.ceedpods.crmbuild.entity.user.User;
import com.ceedpods.crmbuild.entity.UserInvitation;
import com.ceedpods.crmbuild.enums.UserRole;
import com.ceedpods.crmbuild.repository.UserInvitationRepository;
import com.ceedpods.crmbuild.service.auditLogService.AuditService;
import com.ceedpods.crmbuild.service.emailService.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserInvitationService {
    
    private final UserInvitationRepository userInvitationRepository;
    private final UserService userService;
    private final UserPermissionService userPermissionService;
    private final UserHierarchyService userHierarchyService;
    private final EmailService emailService;
    private final AuditService auditService;
    
    @Value("${app.invitation.expiry-hours:168}") // Default 7 days
    private int invitationExpiryHours;
    
    @Value("${app.invitation.base-url:https://black-glacier-015cd510f.3.azurestaticapps.net}")
    private String frontendBaseUrl;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Transactional
    public UserInvitation createInvitation(String adminUserId, String email, String firstName, 
                                         String lastName, UserRole role, String assignedManagerId, 
                                         List<String> permissionCodes, String notes) {
        // Validate admin
        User admin = userService.findByKeycloakId(adminUserId);
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only admins can create user invitations");
        }
        
        // Check if user already exists
        if (userService.existsByEmail(email)) {
            throw new RuntimeException("User with this email already exists");
        }
        
        // Check if there's already a pending invitation
        if (userInvitationRepository.existsByEmailAndUsedFalse(email)) {
            throw new RuntimeException("Pending invitation already exists for this email");
        }
        
        // Validate manager assignment for sales reps
        if (role == UserRole.SALES_EXECUTIVE && assignedManagerId != null) {
            User manager = userService.findByKeycloakId(assignedManagerId);
            if (manager == null || !manager.isManager()) {
                throw new RuntimeException("Invalid manager assignment");
            }
        }
        
        // Generate secure invitation token
        String invitationToken = generateInvitationToken();
        
        // Create invitation
        UserInvitation invitation = UserInvitation.builder()
            .invitationToken(invitationToken)
            .email(email)
            .firstName(firstName)
            .lastName(lastName)
            .role(role)
            .assignedManagerId(assignedManagerId)
            .permissionCodes(permissionCodes)
            .invitedBy(adminUserId)
            .invitedAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusHours(invitationExpiryHours))
            .build();
        
        // Audit fields automatically handled by Spring Data Auditing
        UserInvitation saved = userInvitationRepository.save(invitation);
        
        // Send invitation email
        try {
            sendInvitationEmail(saved);
            saved.markEmailSent();
            userInvitationRepository.save(saved);
        } catch (Exception e) {
            log.error("Failed to send invitation email to {}: {}", email, e.getMessage());
            // Don't throw exception, invitation is created but email failed
        }
        
        // Audit log
        auditService.logUserInvitation(adminUserId, email, role.name(), invitationToken);
        
        log.info("Created invitation for {} ({}) by admin {}", email, role, adminUserId);
        return saved;
    }
    
    @Transactional
    public User completeInvitation(String invitationToken, String password) {
        // Find and validate invitation
        Optional<UserInvitation> invitationOpt = 
            userInvitationRepository.findValidInvitationByToken(invitationToken, LocalDateTime.now());
        
        if (invitationOpt.isEmpty()) {
            throw new RuntimeException("Invalid or expired invitation token");
        }
        
        UserInvitation invitation = invitationOpt.get();
        
        // Check if invitation is still valid
        if (!invitation.isValid()) {
            throw new RuntimeException("Invitation is not valid");
        }
        
        // Create user in Keycloak and MongoDB
        User newUser = userService.createUserFromInvitation(invitation, password);
        
        // Mark invitation as used
        invitation.markAsUsed(newUser.getKeycloakId());
        // Audit fields automatically handled by Spring Data Auditing
        userInvitationRepository.save(invitation);
        
        // Assign default permissions
        if (invitation.getPermissionCodes() != null && !invitation.getPermissionCodes().isEmpty()) {
            userPermissionService.assignMultiplePermissions(
                invitation.getInvitedBy(), 
                newUser.getKeycloakId(), 
                invitation.getPermissionCodes(),
                "Pre-assigned permissions from invitation"
            );
        } else {
            // Assign default role permissions
            userPermissionService.assignDefaultPermissions(invitation.getInvitedBy(), newUser.getKeycloakId());
        }
        
        // Assign to manager if specified
        if (invitation.getRole() == UserRole.SALES_EXECUTIVE && invitation.getAssignedManagerId() != null) {
            try {
                userHierarchyService.assignSalesRepToManager(
                    invitation.getInvitedBy(),
                    newUser.getKeycloakId(),
                    invitation.getAssignedManagerId(),
                    null, // territory to be set later
                    "Assigned from invitation"
                );
            } catch (Exception e) {
                log.error("Failed to assign sales rep to manager during invitation completion: {}", e.getMessage());
                // Don't fail the invitation completion for this
            }
        }
        
        // Audit log
        auditService.logInvitationUsed(invitationToken, invitation.getEmail(), newUser.getKeycloakId());
        auditService.logUserRegistration(newUser.getKeycloakId(), newUser.getEmail(), newUser.getRole().name());
        
        log.info("Completed invitation for {} - User created: {}", invitation.getEmail(), newUser.getKeycloakId());
        return newUser;
    }
    
    public Optional<UserInvitation> getInvitationByToken(String token) {
        return userInvitationRepository.findByInvitationToken(token);
    }
    
    public Optional<UserInvitation> getValidInvitationByToken(String token) {
        return userInvitationRepository.findValidInvitationByToken(token, LocalDateTime.now());
    }
    
    public List<UserInvitation> getInvitationsByAdmin(String adminUserId) {
        return userInvitationRepository.findByInvitedBy(adminUserId);
    }
    
    public List<UserInvitation> getPendingInvitations() {
        return userInvitationRepository.findPendingInvitations(LocalDateTime.now());
    }
    
    public List<UserInvitation> getExpiredInvitations() {
        return userInvitationRepository.findExpiredInvitations(LocalDateTime.now());
    }
    
    @Transactional
    public void resendInvitation(String invitationId, String adminUserId) {
        UserInvitation invitation = userInvitationRepository.findById(invitationId)
            .orElseThrow(() -> new RuntimeException("Invitation not found"));
        
        if (invitation.isUsed()) {
            throw new RuntimeException("Invitation has already been used");
        }
        
        if (invitation.isExpired()) {
            // Extend expiry
            invitation.setExpiresAt(LocalDateTime.now().plusHours(invitationExpiryHours));
        }
        
        try {
            sendInvitationEmail(invitation);
            invitation.markEmailSent();
            // Audit fields automatically handled by Spring Data Auditing
            userInvitationRepository.save(invitation);
            
            log.info("Resent invitation to {} by admin {}", invitation.getEmail(), adminUserId);
        } catch (Exception e) {
            log.error("Failed to resend invitation email to {}: {}", invitation.getEmail(), e.getMessage());
            throw new RuntimeException("Failed to resend invitation email: " + e.getMessage());
        }
    }
    
    @Transactional
    public void sendReminders() {
        LocalDateTime reminderThreshold = LocalDateTime.now().minusDays(3); // Send reminder after 3 days
        List<UserInvitation> invitationsNeedingReminder = 
            userInvitationRepository.findInvitationsNeedingReminder(reminderThreshold, 2, LocalDateTime.now());
        
        for (UserInvitation invitation : invitationsNeedingReminder) {
            try {
                sendReminderEmail(invitation);
                invitation.incrementReminderCount();
                userInvitationRepository.save(invitation);
                
                log.info("Sent reminder email to {} (attempt {})", 
                    invitation.getEmail(), invitation.getReminderCount());
            } catch (Exception e) {
                log.error("Failed to send reminder email to {}: {}", invitation.getEmail(), e.getMessage());
            }
        }
    }
    
    @Transactional
    public void revokeInvitation(String invitationId, String adminUserId, String reason) {
        UserInvitation invitation = userInvitationRepository.findById(invitationId)
            .orElseThrow(() -> new RuntimeException("Invitation not found"));
        
        if (invitation.isUsed()) {
            throw new RuntimeException("Cannot revoke used invitation");
        }
        
        invitation.markAsDeleted(adminUserId);
        // Audit fields automatically handled by Spring Data Auditing
        userInvitationRepository.save(invitation);
        
        log.info("Revoked invitation {} for {} by admin {} - Reason: {}", 
            invitationId, invitation.getEmail(), adminUserId, reason);
    }
    
    @Transactional
    public void cleanupExpiredInvitations() {
        List<UserInvitation> expiredInvitations = getExpiredInvitations();
        
        for (UserInvitation invitation : expiredInvitations) {
            if (!invitation.isUsed()) {
                invitation.markAsDeleted("SYSTEM");
                // Audit fields automatically handled by Spring Data Auditing
            }
        }
        
        if (!expiredInvitations.isEmpty()) {
            userInvitationRepository.saveAll(expiredInvitations);
            log.info("Cleaned up {} expired invitations", expiredInvitations.size());
        }
    }
    
    private String generateInvitationToken() {
        // Generate a secure random token
        String uuid = UUID.randomUUID().toString().replace("-", "");
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);
        
        StringBuilder token = new StringBuilder(uuid);
        for (byte b : randomBytes) {
            token.append(String.format("%02x", b));
        }
        
        return token.toString();
    }
    
    private void sendInvitationEmail(UserInvitation invitation) {
        String registrationUrl = frontendBaseUrl + "/auth/register/" + invitation.getInvitationToken();
        
        String subject = "Invitation to join CRM System";
        String body = buildInvitationEmailBody(invitation, registrationUrl);
        
        emailService.sendEmail(invitation.getEmail(), subject, body);
    }
    
    private void sendReminderEmail(UserInvitation invitation) {
        String registrationUrl = frontendBaseUrl + "/auth/register/" + invitation.getInvitationToken();
        
        String subject = "Reminder: Complete your CRM System registration";
        String body = buildReminderEmailBody(invitation, registrationUrl);
        
        emailService.sendEmail(invitation.getEmail(), subject, body);
    }
    
    private String buildInvitationEmailBody(UserInvitation invitation, String registrationUrl) {
        return String.format(
            "Dear %s,\n\n" +
            "You have been invited to join our CRM System as a %s.\n\n" +
            "Please click the following link to complete your registration:\n" +
            "%s\n\n" +
            "This invitation will expire on %s.\n\n" +
            "If you have any questions, please contact your administrator.\n\n" +
            "Best regards,\n" +
            "CRM System Team",
            invitation.getFirstName(),
            invitation.getRole().getDisplayName(),
            registrationUrl,
            invitation.getExpiresAt()
        );
    }
    
    private String buildReminderEmailBody(UserInvitation invitation, String registrationUrl) {
        return String.format(
            "Dear %s,\n\n" +
            "This is a reminder that you have a pending invitation to join our CRM System.\n\n" +
            "Please click the following link to complete your registration:\n" +
            "%s\n\n" +
            "This invitation will expire on %s.\n\n" +
            "If you no longer wish to join or have any questions, please contact your administrator.\n\n" +
            "Best regards,\n" +
            "CRM System Team",
            invitation.getFirstName(),
            registrationUrl,
            invitation.getExpiresAt()
        );
    }
    
    public long getTotalInvitationsCount() {
        return userInvitationRepository.count();
    }
    
    public long getPendingInvitationsCount() {
        return userInvitationRepository.findPendingInvitations(LocalDateTime.now()).size();
    }
    
    public long getExpiredInvitationsCount() {
        return userInvitationRepository.findExpiredInvitations(LocalDateTime.now()).size();
    }
}