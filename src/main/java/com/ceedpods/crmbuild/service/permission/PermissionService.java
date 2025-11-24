package com.ceedpods.crmbuild.service.permission;

import com.ceedpods.crmbuild.entity.PermissionEntity;
import com.ceedpods.crmbuild.entity.user.User;
import com.ceedpods.crmbuild.enums.Permission;
import com.ceedpods.crmbuild.enums.PermissionCategory;
import com.ceedpods.crmbuild.enums.UserRole;
import com.ceedpods.crmbuild.repository.PermissionRepository;
import com.ceedpods.crmbuild.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {
    
    private final PermissionRepository permissionRepository;
    private final UserService userService;
    
    @PostConstruct
    public void initializePermissions() {
        log.info("Initializing system permissions...");
        
        for (Permission permission : Permission.values()) {
            if (!permissionRepository.existsByPermissionCode(permission.getCode())) {
                PermissionEntity permissionEntity = PermissionEntity.builder()
                    .permissionCode(permission.getCode())
                    .displayName(permission.getDescription())
                    .description(permission.getDescription())
                    .category(permission.getCategory())
                    .assignable(permission.isAssignable())
                    .active(true)
                    .build();
                
                // Audit fields automatically handled by Spring Data Auditing
                permissionRepository.save(permissionEntity);
                
                log.debug("Created permission: {}", permission.getCode());
            }
        }
        
        log.info("Permission initialization completed");
    }
    
    public List<PermissionEntity> getAllPermissions() {
        return permissionRepository.findAllActive();
    }
    
    public List<PermissionEntity> getAssignablePermissions() {
        return permissionRepository.findAllAssignablePermissions();
    }
    
    public List<PermissionEntity> getAssignablePermissionsForRole(UserRole targetRole) {
        // Role-based permission management is now handled through roles collection
        return getAssignablePermissions();
    }
    
    public List<PermissionEntity> getPermissionsByCategory(PermissionCategory category) {
        return permissionRepository.findByCategory(category);
    }
    
    public Optional<PermissionEntity> getPermissionByCode(String permissionCode) {
        return permissionRepository.findByPermissionCode(permissionCode);
    }
    
    public boolean canAssignPermission(String adminUserId, String targetUserId, String permissionCode) {
        try {
            // Rule 1: Only ADMIN can assign permissions
            User admin = userService.findByKeycloakId(adminUserId);
            if (admin == null || admin.getRole() != UserRole.ADMIN) {
                log.warn("Non-admin user {} attempted to assign permission", adminUserId);
                return false;
            }
            
            // Rule 2: Target user must exist
            User targetUser = userService.findByKeycloakId(targetUserId);
            if (targetUser == null) {
                log.warn("Attempted to assign permission to non-existent user: {}", targetUserId);
                return false;
            }
            
            // Rule 3: Permission must exist and be valid
            Optional<PermissionEntity> permissionOpt = getPermissionByCode(permissionCode);
            if (permissionOpt.isEmpty()) {
                log.warn("Attempted to assign non-existent permission: {}", permissionCode);
                return false;
            }
            
            PermissionEntity permission = permissionOpt.get();
            
            // Rule 4: Permission must be assignable
            if (!permission.isAssignable()) {
                log.warn("Attempted to assign non-assignable permission: {}", permissionCode);
                return false;
            }
            
            // Rule 5: Permission must be active
            if (!permission.isActive()) {
                log.warn("Attempted to assign inactive permission: {}", permissionCode);
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Error checking permission assignment eligibility", e);
            return false;
        }
    }
    
    public boolean isPermissionValid(String permissionCode) {
        return permissionRepository.findByPermissionCode(permissionCode)
            .map(permission -> permission.isActive() && !permission.isDeleted())
            .orElse(false);
    }
    
    public List<String> getDefaultPermissionsForRole(UserRole role) {
        // Define default permissions for each role
        switch (role) {
            case ADMIN:
                // Admins get all permissions by default (handled separately)
                return Arrays.asList();
                
            case MANAGER:
                return Arrays.asList(
                    "ANALYTICS_TEAM",
                    "USER_VIEW_TEAM",
                    "LEAD_VIEW_TEAM",
                    "CUSTOMER_VIEW_TEAM",
                    "CALL_RECORDINGS_VIEW"
                );
                
            case SALES_REP:
                return Arrays.asList(
                    "ANALYTICS_PERSONAL",
                    "LEAD_CREATE",
                    "LEAD_EDIT",
                    "CUSTOMER_CREATE",
                    "CUSTOMER_EDIT",
                    "COMMUNICATION_SEND_EMAIL",
                    "SALES_CREATE_OPPORTUNITY"
                );
                
            default:
                return Arrays.asList();
        }
    }
    
    @Transactional
    public PermissionEntity createPermission(PermissionEntity permission, String createdBy) {
        // Audit fields automatically handled by Spring Data Auditing
        return permissionRepository.save(permission);
    }
    
    @Transactional
    public PermissionEntity updatePermission(String permissionId, PermissionEntity updatedPermission, String updatedBy) {
        PermissionEntity existingPermission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionId));
        
        existingPermission.setDisplayName(updatedPermission.getDisplayName());
        existingPermission.setDescription(updatedPermission.getDescription());
        existingPermission.setCategory(updatedPermission.getCategory());
        existingPermission.setAssignable(updatedPermission.isAssignable());
        existingPermission.setActive(updatedPermission.isActive());
        // Audit fields automatically handled by Spring Data Auditing
        
        return permissionRepository.save(existingPermission);
    }
    
    @Transactional
    public void deactivatePermission(String permissionId, String deactivatedBy) {
        PermissionEntity permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionId));
        
        permission.setActive(false);
        // Audit fields automatically handled by Spring Data Auditing
        permissionRepository.save(permission);
        
        log.info("Permission {} deactivated by {}", permissionId, deactivatedBy);
    }
    
    public long getTotalPermissionsCount() {
        return permissionRepository.count();
    }
    
    public long getActivePermissionsCount() {
        return permissionRepository.findAllActive().size();
    }
    
    public long getAssignablePermissionsCount() {
        return permissionRepository.findAllAssignablePermissions().size();
    }
}