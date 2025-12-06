package com.ceedpods.crmbuild.security;

import com.ceedpods.crmbuild.entity.user.User;
import com.ceedpods.crmbuild.enums.Permission;
import com.ceedpods.crmbuild.enums.PermissionScope;
import com.ceedpods.crmbuild.enums.UserRole;
import com.ceedpods.crmbuild.service.permission.RBACService;
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
    private final RBACService rbacService;
    
    /**
     * Check if user has a permission (uses PermissionScope.OWN as default required scope)
     */
    public boolean hasPermission(Authentication authentication, String permissionCode) {
        // Handle "ADMIN" as a special role check, not a permission
        if ("ADMIN".equals(permissionCode)) {
            return isAdmin(authentication);
        }
        return hasPermissionWithScope(authentication, permissionCode, PermissionScope.OWN);
    }
    
    /**
     * Check if user has a permission with specific scope requirement
     */
    public boolean hasPermissionWithScope(Authentication authentication, String permissionCode, PermissionScope requiredScope) {
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
            
            // Convert permission code to Permission enum
            Permission permission;
            try {
                permission = Permission.fromCode(permissionCode);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown permission code: {}", permissionCode);
                return false;
            }
            
            // Use RBAC service to check permission
            boolean hasPermission = rbacService.hasPermission(user.getRole(), permission, requiredScope);
            
            log.debug("User {} (role: {}) has permission {} with scope {}: {}", 
                keycloakId, user.getRole(), permissionCode, requiredScope, hasPermission);
            
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
            .anyMatch(permissionOrRole -> {
                // Handle "ADMIN" as a special role check, not a permission
                if ("ADMIN".equals(permissionOrRole)) {
                    return isAdmin(authentication);
                }
                return hasPermission(authentication, permissionOrRole);
            });
    }
    
    /**
     * Check if user can perform action on specific target user based on data visibility scope
     */
    public boolean hasPermissionForUser(Authentication authentication, String permissionCode, String targetUserId) {
        try {
            String currentUserId = getKeycloakId(authentication);
            User currentUser = userService.findByKeycloakId(currentUserId);
            User targetUser = userService.findByKeycloakId(targetUserId);
            
            if (currentUser == null || targetUser == null) {
                return false;
            }
            
            // Convert permission code to enum
            Permission permission = Permission.fromCode(permissionCode);
            PermissionScope grantedScope = rbacService.getPermissionScope(currentUser.getRole(), permission);
            
            // Check based on granted scope
            switch (grantedScope) {
                case ALL:
                    return true; // Can operate on anyone
                    
                case TEAM:
                    // Same user or user is in current user's team hierarchy
                    return currentUserId.equals(targetUserId) || 
                           isInSameTeamHierarchy(currentUser, targetUser);
                    
                case OWN:
                    // Only on themselves
                    return currentUserId.equals(targetUserId);
                    
                case AS_PERMITTED:
                    // TODO: Implement admin-configured permissions for viewers
                    return false;
                    
                case NONE:
                default:
                    return false;
            }
            
        } catch (Exception e) {
            log.error("Error checking user-specific permission: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if two users are in the same team hierarchy
     */
    private boolean isInSameTeamHierarchy(User currentUser, User targetUser) {
        try {
            // If current user is a manager, check if target is in their team
            if (currentUser.getRole() == UserRole.MANAGER) {
                return userHierarchyService.isUserInHierarchy(targetUser.getKeycloakId(), currentUser.getKeycloakId());
            }
            
            // If same level (both sales reps), check if they have the same manager
            if (currentUser.getRole() == targetUser.getRole() && 
                currentUser.getManagerId() != null && 
                currentUser.getManagerId().equals(targetUser.getManagerId())) {
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("Error checking team hierarchy: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean canViewUserData(Authentication authentication, String targetUserId) {
        return hasPermissionForUser(authentication, "USER_VIEW", targetUserId);
    }
    
    public boolean canEditUserData(Authentication authentication, String targetUserId) {
        return hasPermissionForUser(authentication, "USER_EDIT", targetUserId);
    }
    
    public boolean canViewLeadData(Authentication authentication, String leadOwnerId) {
        return hasPermissionForUser(authentication, "LEAD_VIEW", leadOwnerId);
    }
    
    public boolean canEditLeadData(Authentication authentication, String leadOwnerId) {
        return hasPermissionForUser(authentication, "LEAD_EDIT", leadOwnerId);
    }
    
    public boolean canViewDealData(Authentication authentication, String dealOwnerId) {
        return hasPermissionForUser(authentication, "DEAL_VIEW", dealOwnerId);
    }
    
    public boolean canEditDealData(Authentication authentication, String dealOwnerId) {
        return hasPermissionForUser(authentication, "DEAL_EDIT", dealOwnerId);
    }
    
    /**
     * Check analytics access based on requested dashboard type
     */
    public boolean canViewAnalytics(Authentication authentication, String dashboardType) {
        try {
            String currentUserId = getKeycloakId(authentication);
            User currentUser = userService.findByKeycloakId(currentUserId);
            
            if (currentUser == null) {
                return false;
            }
            
            // Check based on dashboard type
            switch (dashboardType.toLowerCase()) {
                case "company":
                    return hasPermission(authentication, "ANALYTICS_VIEW_COMPANY_DASHBOARD");
                case "team":
                    return hasPermission(authentication, "ANALYTICS_VIEW_TEAM_DASHBOARD");
                case "individual":
                case "personal":
                    return hasPermission(authentication, "ANALYTICS_VIEW_INDIVIDUAL_DASHBOARD");
                default:
                    return false;
            }
        } catch (Exception e) {
            log.error("Error checking analytics permission: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if user can view call recordings based on data scope
     */
    public boolean canViewCallRecordings(Authentication authentication, String callOwnerId) {
        return hasPermissionForUser(authentication, "CALL_LISTEN_RECORDINGS", callOwnerId);
    }
    
    /**
     * Check if user can view email history based on data scope
     */
    public boolean canViewEmailHistory(Authentication authentication, String emailOwnerId) {
        return hasPermissionForUser(authentication, "EMAIL_VIEW_HISTORY", emailOwnerId);
    }
    
    /**
     * Get the effective scope for a user's permission
     */
    public PermissionScope getPermissionScope(Authentication authentication, String permissionCode) {
        try {
            String currentUserId = getKeycloakId(authentication);
            User currentUser = userService.findByKeycloakId(currentUserId);
            
            if (currentUser == null) {
                return PermissionScope.NONE;
            }
            
            Permission permission = Permission.fromCode(permissionCode);
            return rbacService.getPermissionScope(currentUser.getRole(), permission);
            
        } catch (Exception e) {
            log.error("Error getting permission scope: {}", e.getMessage());
            return PermissionScope.NONE;
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

    /**
     * Check if the current user is an admin
     */
    public boolean isAdmin(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            if (currentUser == null) {
                log.debug("User not found for authentication");
                return false;
            }

            boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
            log.debug("User {} is admin: {}", currentUser.getId(), isAdmin);
            return isAdmin;

        } catch (Exception e) {
            log.error("Error checking if user is admin: {}", e.getMessage());
            return false;
        }
    }
}