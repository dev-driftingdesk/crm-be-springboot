package com.ceedpods.crmbuild.service.role;

import com.ceedpods.crmbuild.entity.RoleEntity;
import com.ceedpods.crmbuild.enums.UserRole;
import com.ceedpods.crmbuild.repository.RoleRepository;
import com.ceedpods.crmbuild.service.permission.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {
    
    private final RoleRepository roleRepository;
    private final PermissionService permissionService;
    
    @PostConstruct
    @Transactional
    public void initializeSystemRoles() {
        log.info("Initializing system roles...");
        
        // Define the four system roles
        createSystemRoleIfNotExists(UserRole.ADMIN, "Administrator", 
            "Full system access with all administrative privileges", 
            getAllPermissionsForAdmin());
            
        createSystemRoleIfNotExists(UserRole.MANAGER, "Manager", 
            "Team management access with oversight capabilities",
            getDefaultPermissionsForManager());
            
        createSystemRoleIfNotExists(UserRole.SALES_REP, "Sales Representative",
            "Sales-focused access for customer and lead management",
            getDefaultPermissionsForSalesRep());
            
        createSystemRoleIfNotExists(UserRole.VIEWER, "Viewer",
            "Read-only access with limited viewing permissions as permitted by admin",
            getDefaultPermissionsForViewer());
        
        log.info("System roles initialization completed");
    }
    
    private void createSystemRoleIfNotExists(UserRole roleName, String displayName, 
                                           String description, List<String> permissions) {
        if (!roleRepository.existsByRoleName(roleName)) {
            RoleEntity role = RoleEntity.builder()
                .id(UUID.randomUUID().toString()) // Use UUID as requested
                .roleName(roleName)
                .displayName(displayName)
                .description(description)
                .permissionCodes(permissions)
                .active(true)
                .isSystemRole(true)
                .build();
                
            roleRepository.save(role);
            log.info("Created system role: {} with ID: {}", roleName, role.getId());
        } else {
            log.debug("System role already exists: {}", roleName);
        }
    }
    
    private List<String> getAllPermissionsForAdmin() {
        // Admin gets all available permissions
        return permissionService.getAllPermissions()
            .stream()
            .map(permission -> permission.getPermissionCode())
            .toList();
    }
    
    private List<String> getDefaultPermissionsForManager() {
        return Arrays.asList(
            "ANALYTICS_TEAM",
            "USER_VIEW_TEAM",
            "USER_CREATE",
            "USER_EDIT",
            "LEAD_VIEW_TEAM",
            "LEAD_CREATE",
            "LEAD_EDIT",
            "CUSTOMER_VIEW_TEAM",
            "CUSTOMER_CREATE",
            "CUSTOMER_EDIT",
            "CALL_RECORDINGS_VIEW",
            "DEAL_VIEW_TEAM",
            "DEAL_CREATE",
            "DEAL_EDIT"
        );
    }
    
    private List<String> getDefaultPermissionsForSalesRep() {
        return Arrays.asList(
            "ANALYTICS_PERSONAL",
            "LEAD_CREATE",
            "LEAD_EDIT",
            "LEAD_VIEW_PERSONAL",
            "CUSTOMER_CREATE",
            "CUSTOMER_EDIT",
            "CUSTOMER_VIEW_PERSONAL",
            "COMMUNICATION_SEND_EMAIL",
            "COMMUNICATION_SEND_SMS",
            "SALES_CREATE_OPPORTUNITY",
            "DEAL_CREATE",
            "DEAL_EDIT",
            "DEAL_VIEW_PERSONAL"
        );
    }
    
    private List<String> getDefaultPermissionsForViewer() {
        return Arrays.asList(
            // Authentication & Account - Basic access
            "AUTH_LOGIN_MOBILE",
            "AUTH_LOGIN_WEB", 
            "AUTH_CHANGE_PASSWORD",
            "AUTH_UPDATE_PROFILE",
            "AUTH_ENABLE_MFA",
            "AUTH_VIEW_LOGIN_HISTORY",
            // Data Access - As permitted
            "LEAD_VIEW",
            "DEAL_VIEW", 
            "LEAD_VIEW_HISTORY",
            "DEAL_VIEW_HISTORY",
            // Analytics - As permitted
            "ANALYTICS_VIEW_INDIVIDUAL_DASHBOARD",
            "ANALYTICS_VIEW_LEADERBOARDS",
            "ANALYTICS_VIEW_PERFORMANCE",
            "ANALYTICS_VIEW_PIPELINE_HEALTH",
            "ANALYTICS_VIEW_ACTIVITY",
            "ANALYTICS_VIEW_TRENDS",
            // Team hierarchy - Own only  
            "TEAM_VIEW_HIERARCHY",
            // Products - View only
            "PRODUCT_VIEW_CATALOG",
            // Calendar - Own only
            "CALENDAR_VIEW_OWN",
            "CALENDAR_SET_AVAILABILITY",
            // Gamification - View only
            "GAMIFICATION_VIEW_LEADERBOARDS", 
            "GAMIFICATION_VIEW_ACHIEVEMENTS",
            // Notifications - Own scope
            "NOTIFICATION_RECEIVE_PUSH",
            "NOTIFICATION_CONFIGURE_PREFERENCES",
            "NOTIFICATION_VIEW_HISTORY"
        );
    }
    
    public List<RoleEntity> getAllActiveRoles() {
        return roleRepository.findAllActive();
    }
    
    public List<RoleEntity> getSystemRoles() {
        return roleRepository.findAllSystemRoles();
    }
    
    public List<RoleEntity> getCustomRoles() {
        return roleRepository.findAllCustomRoles();
    }
    
    public Optional<RoleEntity> getRoleByName(UserRole roleName) {
        return roleRepository.findByRoleName(roleName);
    }
    
    public Optional<RoleEntity> getRoleById(String roleId) {
        return roleRepository.findById(roleId);
    }
    
    @Transactional
    public RoleEntity updateRolePermissions(String roleId, List<String> permissionCodes, String updatedBy) {
        RoleEntity role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));
        
        // Validate that all permission codes exist
        for (String permissionCode : permissionCodes) {
            if (!permissionService.isPermissionValid(permissionCode)) {
                throw new IllegalArgumentException("Invalid permission code: " + permissionCode);
            }
        }
        
        role.setPermissionCodes(permissionCodes);
        RoleEntity savedRole = roleRepository.save(role);
        
        log.info("Updated permissions for role {} by {}", role.getRoleName(), updatedBy);
        return savedRole;
    }
    
    @Transactional
    public RoleEntity addPermissionToRole(String roleId, String permissionCode, String updatedBy) {
        RoleEntity role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));
            
        if (!permissionService.isPermissionValid(permissionCode)) {
            throw new IllegalArgumentException("Invalid permission code: " + permissionCode);
        }
        
        if (!role.getPermissionCodes().contains(permissionCode)) {
            role.getPermissionCodes().add(permissionCode);
            RoleEntity savedRole = roleRepository.save(role);
            
            log.info("Added permission {} to role {} by {}", permissionCode, role.getRoleName(), updatedBy);
            return savedRole;
        }
        
        return role; // Permission already exists
    }
    
    @Transactional
    public RoleEntity removePermissionFromRole(String roleId, String permissionCode, String updatedBy) {
        RoleEntity role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));
        
        if (role.getPermissionCodes().remove(permissionCode)) {
            RoleEntity savedRole = roleRepository.save(role);
            
            log.info("Removed permission {} from role {} by {}", permissionCode, role.getRoleName(), updatedBy);
            return savedRole;
        }
        
        return role; // Permission didn't exist
    }
    
    public boolean hasPermission(UserRole userRole, String permissionCode) {
        return roleRepository.findByRoleName(userRole)
            .map(role -> role.getPermissionCodes().contains(permissionCode))
            .orElse(false);
    }
}