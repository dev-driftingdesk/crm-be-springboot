package com.ceedpods.crmbuild.service.user;

import com.ceedpods.crmbuild.entity.user.User;
import com.ceedpods.crmbuild.entity.UserPermission;
import com.ceedpods.crmbuild.enums.UserRole;
import com.ceedpods.crmbuild.repository.UserPermissionRepository;
import com.ceedpods.crmbuild.service.auditLogService.AuditService;
import com.ceedpods.crmbuild.service.permission.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPermissionService {
    
    private final UserPermissionRepository userPermissionRepository;
    private final PermissionService permissionService;
    private final UserService userService;
    private final AuditService auditService;
    
    @Transactional
    public UserPermission assignPermission(String adminUserId, String targetUserId, 
                                         String permissionCode, String notes) {
        // Validate assignment
        if (!permissionService.canAssignPermission(adminUserId, targetUserId, permissionCode)) {
            throw new RuntimeException("Cannot assign permission " + permissionCode + " to user " + targetUserId);
        }
        
        // Check if already assigned and active
        Optional<UserPermission> existingPermission = 
            userPermissionRepository.findActivePermissionByUserIdAndCode(targetUserId, permissionCode);
        
        if (existingPermission.isPresent()) {
            if (existingPermission.get().isEffective()) {
                throw new RuntimeException("Permission already assigned and active");
            } else {
                // Reactivate expired permission
                UserPermission permission = existingPermission.get();
                permission.setActive(true);
                permission.setExpiresAt(null);
                permission.setAssignedBy(adminUserId);
                permission.setAssignedAt(LocalDateTime.now());
                permission.setNotes(notes);
                // Audit fields automatically handled by Spring Data Auditing
                
                UserPermission saved = userPermissionRepository.save(permission);
                auditService.logPermissionAssignment(adminUserId, targetUserId, permissionCode, "REACTIVATED");
                
                log.info("Reactivated permission {} for user {} by {}", permissionCode, targetUserId, adminUserId);
                return saved;
            }
        }
        
        // Create new permission assignment
        UserPermission userPermission = UserPermission.builder()
            .userId(targetUserId)
            .permissionCode(permissionCode)
            .assignedBy(adminUserId)
            .assignedAt(LocalDateTime.now())
            .active(true)
            .notes(notes)
            .build();
        
        // Audit fields automatically handled by Spring Data Auditing
        UserPermission saved = userPermissionRepository.save(userPermission);
        
        // Audit log
        auditService.logPermissionAssignment(adminUserId, targetUserId, permissionCode, "ASSIGNED");
        
        log.info("Assigned permission {} to user {} by {}", permissionCode, targetUserId, adminUserId);
        return saved;
    }
    
    @Transactional
    public void revokePermission(String adminUserId, String targetUserId, String permissionCode, String reason) {
        Optional<UserPermission> userPermissionOpt = 
            userPermissionRepository.findActivePermissionByUserIdAndCode(targetUserId, permissionCode);
        
        if (userPermissionOpt.isEmpty()) {
            throw new RuntimeException("Permission assignment not found or not active");
        }
        
        UserPermission userPermission = userPermissionOpt.get();
        userPermission.revoke(adminUserId, reason);
        // Audit fields automatically handled by Spring Data Auditing
        
        userPermissionRepository.save(userPermission);
        
        // Audit log
        auditService.logPermissionRevocation(adminUserId, targetUserId, permissionCode, reason);
        
        log.info("Revoked permission {} from user {} by {} - Reason: {}", 
            permissionCode, targetUserId, adminUserId, reason);
    }
    
    @Transactional
    public void assignMultiplePermissions(String adminUserId, String targetUserId, 
                                        List<String> permissionCodes, String notes) {
        for (String permissionCode : permissionCodes) {
            try {
                assignPermission(adminUserId, targetUserId, permissionCode, notes);
            } catch (Exception e) {
                log.error("Failed to assign permission {} to user {}: {}", 
                    permissionCode, targetUserId, e.getMessage());
                // Continue with other permissions
            }
        }
    }
    
    @Transactional
    public void assignDefaultPermissions(String adminUserId, String targetUserId) {
        User targetUser = userService.findByKeycloakId(targetUserId);
        if (targetUser == null) {
            throw new RuntimeException("Target user not found: " + targetUserId);
        }
        
        List<String> defaultPermissions = permissionService.getDefaultPermissionsForRole(targetUser.getRole());
        if (!defaultPermissions.isEmpty()) {
            assignMultiplePermissions(adminUserId, targetUserId, defaultPermissions, 
                "Default permissions for role: " + targetUser.getRole());
            
            log.info("Assigned {} default permissions to {} user {}", 
                defaultPermissions.size(), targetUser.getRole(), targetUserId);
        }
    }
    
    public boolean hasPermission(String userId, String permissionCode) {
        // Admins have all permissions by default
        User user = userService.findByKeycloakId(userId);
        if (user != null && user.getRole() == UserRole.ADMIN) {
            return true;
        }
        
        // Check specific permission assignment
        Optional<UserPermission> permission = 
            userPermissionRepository.findActivePermissionByUserIdAndCode(userId, permissionCode);
        
        return permission.isPresent() && permission.get().isEffective();
    }
    
    public boolean hasAnyPermission(String userId, List<String> permissionCodes) {
        return permissionCodes.stream()
            .anyMatch(permissionCode -> hasPermission(userId, permissionCode));
    }
    
    public List<UserPermission> getUserPermissions(String userId) {
        return userPermissionRepository.findActivePermissionsByUserId(userId);
    }
    
    public List<UserPermission> getEffectiveUserPermissions(String userId) {
        return userPermissionRepository.findEffectivePermissionsByUserId(userId, LocalDateTime.now());
    }
    
    public Set<String> getUserPermissionCodes(String userId) {
        return getUserPermissions(userId).stream()
            .filter(UserPermission::isEffective)
            .map(UserPermission::getPermissionCode)
            .collect(Collectors.toSet());
    }
    
    public List<UserPermission> getAllUserPermissions(String userId) {
        return userPermissionRepository.findByUserId(userId);
    }
    
    public List<UserPermission> getPermissionsByCode(String permissionCode) {
        return userPermissionRepository.findByPermissionCode(permissionCode);
    }
    
    public List<UserPermission> getPermissionsAssignedBy(String adminUserId) {
        return userPermissionRepository.findByAssignedBy(adminUserId);
    }
    
    @Transactional
    public void cleanupExpiredPermissions() {
        List<UserPermission> expiredPermissions = 
            userPermissionRepository.findExpiredPermissions(LocalDateTime.now());
        
        for (UserPermission permission : expiredPermissions) {
            permission.setActive(false);
            // Audit fields automatically handled by Spring Data Auditing
        }
        
        if (!expiredPermissions.isEmpty()) {
            userPermissionRepository.saveAll(expiredPermissions);
            log.info("Cleaned up {} expired permissions", expiredPermissions.size());
        }
    }
    
    @Transactional
    public void removeAllUserPermissions(String userId, String removedBy) {
        List<UserPermission> userPermissions = userPermissionRepository.findActivePermissionsByUserId(userId);
        
        for (UserPermission permission : userPermissions) {
            permission.revoke(removedBy, "User account removed");
            // Audit fields automatically handled by Spring Data Auditing
        }
        
        if (!userPermissions.isEmpty()) {
            userPermissionRepository.saveAll(userPermissions);
            auditService.logBulkPermissionRevocation(removedBy, userId, 
                userPermissions.stream().map(UserPermission::getPermissionCode).collect(Collectors.toList()));
            
            log.info("Removed {} permissions from user {} by {}", 
                userPermissions.size(), userId, removedBy);
        }
    }
    
    public long getUserPermissionCount(String userId) {
        return userPermissionRepository.countActivePermissionsByUserId(userId);
    }
    
    public long getPermissionsAssignedByCount(String adminUserId) {
        return userPermissionRepository.countPermissionsAssignedBy(adminUserId);
    }
    
    @Transactional
    public UserPermission updatePermissionExpiry(String permissionId, LocalDateTime expiresAt, String updatedBy) {
        UserPermission permission = userPermissionRepository.findById(permissionId)
            .orElseThrow(() -> new RuntimeException("Permission assignment not found: " + permissionId));
        
        LocalDateTime previousExpiry = permission.getExpiresAt();
        permission.setExpiresAt(expiresAt);
        // Audit fields automatically handled by Spring Data Auditing
        
        UserPermission saved = userPermissionRepository.save(permission);
        
        auditService.logPermissionExpiryUpdate(updatedBy, permission.getUserId(), 
            permission.getPermissionCode(), previousExpiry, expiresAt);
        
        log.info("Updated expiry for permission {} of user {} by {}", 
            permission.getPermissionCode(), permission.getUserId(), updatedBy);
        
        return saved;
    }
}