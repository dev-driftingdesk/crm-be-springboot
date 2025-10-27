package com.ceedpods.crmbuild.security;

import com.ceedpods.crmbuild.entity.user.User;
import com.ceedpods.crmbuild.enums.UserRole;
import com.ceedpods.crmbuild.service.user.UserHierarchyService;
import com.ceedpods.crmbuild.service.user.UserPermissionService;
import com.ceedpods.crmbuild.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component("permissionEvaluator")
@RequiredArgsConstructor
@Slf4j
public class CustomPermissionEvaluator {
    
    private final UserPermissionService userPermissionService;
    private final UserService userService;
    private final UserHierarchyService userHierarchyService;
    
    public boolean hasPermission(Authentication authentication, String permissionCode) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("Authentication is null or not authenticated");
            return false;
        }
        
        try {
            String keycloakId = getKeycloakId(authentication);
            if (keycloakId == null) {
                log.debug("Could not extract Keycloak ID from authentication");
                return false;
            }
            
            User user = userService.findByKeycloakId(keycloakId);
            if (user == null) {
                log.debug("User not found for Keycloak ID: {}", keycloakId);
                return false;
            }
            
            // ADMIN has all permissions by default
            if (user.getRole() == UserRole.ADMIN) {
                log.debug("Admin user {} has permission {} by default", keycloakId, permissionCode);
                return true;
            }
            
            // Check specific permission assignment
            boolean hasPermission = userPermissionService.hasPermission(keycloakId, permissionCode);
            log.debug("User {} has permission {}: {}", keycloakId, permissionCode, hasPermission);
            
            return hasPermission;
            
        } catch (Exception e) {
            log.error("Error checking permission {} for authentication: {}", permissionCode, e.getMessage());
            return false;
        }
    }
    
    public boolean hasAnyPermission(Authentication authentication, String[] permissionCodes) {
        if (permissionCodes == null || permissionCodes.length == 0) {
            return false;
        }
        
        return Arrays.stream(permissionCodes)
            .anyMatch(permission -> hasPermission(authentication, permission));
    }
    
    public boolean hasPermissionForUser(Authentication authentication, String permissionCode, String targetUserId) {
        // Check basic permission first
        if (!hasPermission(authentication, permissionCode)) {
            return false;
        }
        
        try {
            String currentUserId = getKeycloakId(authentication);
            User currentUser = userService.findByKeycloakId(currentUserId);
            
            if (currentUser == null) {
                return false;
            }
            
            // Admin can operate on anyone
            if (currentUser.getRole() == UserRole.ADMIN) {
                return true;
            }
            
            // Users can always operate on themselves
            if (currentUserId.equals(targetUserId)) {
                return true;
            }
            
            // Managers can only operate on their assigned sales reps
            if (currentUser.getRole() == UserRole.MANAGER) {
                return userHierarchyService.isUserInHierarchy(targetUserId, currentUserId);
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("Error checking hierarchical permission: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean canViewUserData(Authentication authentication, String targetUserId) {
        return hasPermissionForUser(authentication, "USER_VIEW_ALL", targetUserId) ||
               hasPermissionForUser(authentication, "USER_VIEW_TEAM", targetUserId);
    }
    
    public boolean canEditUserData(Authentication authentication, String targetUserId) {
        return hasPermissionForUser(authentication, "USER_EDIT", targetUserId);
    }
    
    public boolean canViewLeadData(Authentication authentication, String leadOwnerId) {
        return hasPermissionForUser(authentication, "LEAD_VIEW_ALL", leadOwnerId) ||
               hasPermissionForUser(authentication, "LEAD_VIEW_TEAM", leadOwnerId);
    }
    
    public boolean canViewAnalytics(Authentication authentication, String scope) {
        String currentUserId = getKeycloakId(authentication);
        User currentUser = userService.findByKeycloakId(currentUserId);
        
        if (currentUser == null) {
            return false;
        }
        
        // Admin can view all analytics
        if (currentUser.getRole() == UserRole.ADMIN) {
            return hasPermission(authentication, "ANALYTICS_FULL");
        }
        
        // Check based on scope
        switch (scope.toLowerCase()) {
            case "full":
                return hasPermission(authentication, "ANALYTICS_FULL");
            case "team":
                return hasPermission(authentication, "ANALYTICS_TEAM") || 
                       hasPermission(authentication, "ANALYTICS_FULL");
            case "personal":
                return hasPermission(authentication, "ANALYTICS_PERSONAL") ||
                       hasPermission(authentication, "ANALYTICS_TEAM") ||
                       hasPermission(authentication, "ANALYTICS_FULL");
            default:
                return false;
        }
    }
    
    private String getKeycloakId(Authentication authentication) {
        try {
            if (authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                return jwt.getClaimAsString("sub");
            }
            
            // Fallback for other authentication types
            return authentication.getName();
            
        } catch (Exception e) {
            log.error("Error extracting Keycloak ID from authentication: {}", e.getMessage());
            return null;
        }
    }
    
    public String getCurrentKeycloakId(Authentication authentication) {
        return getKeycloakId(authentication);
    }
    
    public User getCurrentUser(Authentication authentication) {
        String keycloakId = getKeycloakId(authentication);
        return keycloakId != null ? userService.findByKeycloakId(keycloakId) : null;
    }
}