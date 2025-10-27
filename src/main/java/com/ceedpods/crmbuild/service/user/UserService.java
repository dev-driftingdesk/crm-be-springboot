package com.ceedpods.crmbuild.service.user;

import com.ceedpods.crmbuild.entity.user.User;
import com.ceedpods.crmbuild.entity.UserInvitation;
import com.ceedpods.crmbuild.enums.UserRole;
import com.ceedpods.crmbuild.repository.UserRepository;
import com.ceedpods.crmbuild.service.auditLogService.AuditService;
import com.ceedpods.crmbuild.service.keycloakService.KeycloakAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final AuditService auditService;
    
    public User findByKeycloakId(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId).orElse(null);
    }
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public boolean existsByKeycloakId(String keycloakId) {
        return userRepository.existsByKeycloakId(keycloakId);
    }
    
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findByDeletedFalse();
    }
    
    public List<User> getEnabledUsers() {
        return userRepository.findByEnabledTrueAndDeletedFalse();
    }
    
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }
    
    @Transactional
    public User createUser(String email, String firstName, String lastName, UserRole role, 
                          String password, String createdBy) {
        // Check if user already exists
        if (existsByEmail(email)) {
            throw new RuntimeException("User with this email already exists");
        }
        
        try {
            // Ensure all roles exist in Keycloak
            keycloakAdminService.ensureAllRolesExist();
            
            // Create user in Keycloak first
            String keycloakUserId = keycloakAdminService.createUser(email, firstName, lastName, password);
            
            // Assign role in Keycloak
            keycloakAdminService.assignRealmRoleToUser(keycloakUserId, role.name());
            
            // Create user in MongoDB
            User user = User.builder()
                .keycloakId(keycloakUserId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .enabled(true)
                .emailVerified(false)
                .mustChangePassword(true)
                .build();
            
            // Audit fields automatically handled by Spring Data Auditing
            User savedUser = userRepository.save(user);
            
            // Audit log
            auditService.logUserRegistration(keycloakUserId, email, role.name());
            
            log.info("Created user {} with role {} by {}", email, role, createdBy);
            return savedUser;
            
        } catch (Exception e) {
            log.error("Failed to create user {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to create user: " + e.getMessage());
        }
    }
    
    @Transactional
    public User createUserFromInvitation(UserInvitation invitation, String password) {
        try {
            // Ensure all roles exist in Keycloak
            keycloakAdminService.ensureAllRolesExist();
            
            // Create user in Keycloak
            String keycloakUserId = keycloakAdminService.createUser(
                invitation.getEmail(), 
                invitation.getFirstName(), 
                invitation.getLastName(), 
                password
            );
            
            // Assign role in Keycloak
            keycloakAdminService.assignRealmRoleToUser(keycloakUserId, invitation.getRole().name());
            
            // Create user in MongoDB
            User user = User.builder()
                .keycloakId(keycloakUserId)
                .email(invitation.getEmail())
                .firstName(invitation.getFirstName())
                .lastName(invitation.getLastName())
                .role(invitation.getRole())
                .enabled(true)
                .emailVerified(false)
                .mustChangePassword(false) // User just set password
                .invitationToken(invitation.getInvitationToken())
                .invitationUsed(true)
                .invitationUsedAt(LocalDateTime.now())
                .managerId(invitation.getAssignedManagerId())
                .build();
            
            // Audit fields automatically handled by Spring Data Auditing
            User savedUser = userRepository.save(user);
            
            log.info("Created user from invitation: {} with role {}", 
                invitation.getEmail(), invitation.getRole());
            
            return savedUser;
            
        } catch (Exception e) {
            log.error("Failed to create user from invitation for {}: {}", 
                invitation.getEmail(), e.getMessage());
            throw new RuntimeException("Failed to create user from invitation: " + e.getMessage());
        }
    }
    
    @Transactional
    public User updateUser(String userId, User updatedUser, String updatedBy) {
        User existingUser = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        // Update allowed fields
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        existingUser.setDepartment(updatedUser.getDepartment());
        existingUser.setJobTitle(updatedUser.getJobTitle());
        existingUser.setEnabled(updatedUser.isEnabled());
        // Audit fields automatically handled by Spring Data Auditing
        
        User saved = userRepository.save(existingUser);
        
        log.info("Updated user {} by {}", existingUser.getEmail(), updatedBy);
        return saved;
    }
    
    @Transactional
    public void deleteUser(String userId, String deletedBy) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        // Soft delete
        user.markAsDeleted(deletedBy);
        user.setEnabled(false);
        userRepository.save(user);
        
        // TODO: Disable user in Keycloak as well
        
        log.info("Deleted user {} by {}", user.getEmail(), deletedBy);
    }
    
    @Transactional
    public void enableUser(String userId, String enabledBy) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        user.setEnabled(true);
        // Audit fields automatically handled by Spring Data Auditing
        userRepository.save(user);
        
        log.info("Enabled user {} by {}", user.getEmail(), enabledBy);
    }
    
    @Transactional
    public void disableUser(String userId, String disabledBy) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        user.setEnabled(false);
        // Audit fields automatically handled by Spring Data Auditing
        userRepository.save(user);
        
        log.info("Disabled user {} by {}", user.getEmail(), disabledBy);
    }
    
    @Transactional
    public void recordSuccessfulLogin(String keycloakId) {
        Optional<User> userOpt = userRepository.findByKeycloakId(keycloakId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.recordSuccessfulLogin();
            userRepository.save(user);
            
            auditService.logUserLogin(keycloakId, user.getEmail(), true);
        }
    }
    
    @Transactional
    public void recordFailedLogin(String keycloakId) {
        Optional<User> userOpt = userRepository.findByKeycloakId(keycloakId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.recordFailedLogin();
            
            // Lock account after 5 failed attempts
            if (user.getFailedLoginAttempts() >= 5) {
                user.lockAccount();
                auditService.logAccountLocked(keycloakId, user.getEmail(), user.getFailedLoginAttempts());
            }
            
            userRepository.save(user);
            auditService.logUserLogin(keycloakId, user.getEmail(), false);
        }
    }
    
    @Transactional
    public void unlockAccount(String userId, String unlockedBy) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        user.unlockAccount();
        // Audit fields automatically handled by Spring Data Auditing
        userRepository.save(user);
        
        log.info("Unlocked account for user {} by {}", user.getEmail(), unlockedBy);
    }
    
    @Transactional
    public void changePassword(String keycloakId, String newPassword, String changedBy) {
        User user = userRepository.findByKeycloakId(keycloakId)
            .orElseThrow(() -> new RuntimeException("User not found: " + keycloakId));
        
        try {
            // Update password in Keycloak
            keycloakAdminService.updateUserPassword(keycloakId, newPassword);
            
            // Update user record
            user.setPasswordChangedAt(LocalDateTime.now());
            user.setMustChangePassword(false);
            // Audit fields automatically handled by Spring Data Auditing
            userRepository.save(user);
            
            auditService.logPasswordChange(keycloakId, user.getEmail());
            
            log.info("Password changed for user {} by {}", user.getEmail(), changedBy);
            
        } catch (Exception e) {
            log.error("Failed to change password for user {}: {}", user.getEmail(), e.getMessage());
            throw new RuntimeException("Failed to change password: " + e.getMessage());
        }
    }
    
    public long getTotalUsersCount() {
        return userRepository.countByDeletedFalse();
    }
    
    public long getActiveUsersCount() {
        return userRepository.countByEnabledTrueAndDeletedFalse();
    }
    
    public long getUsersByRoleCount(UserRole role) {
        return userRepository.countByRoleAndDeletedFalse(role);
    }
    
    public List<User> searchUsers(String searchTerm) {
        return userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            searchTerm, searchTerm, searchTerm
        );
    }
}