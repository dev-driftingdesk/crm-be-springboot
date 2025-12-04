package com.ceedpods.crmbuild.controller.rbac;

import com.ceedpods.crmbuild.entity.UserRolePermission;
import com.ceedpods.crmbuild.enums.Permission;
import com.ceedpods.crmbuild.enums.PermissionScope;
import com.ceedpods.crmbuild.enums.UserRole;
import com.ceedpods.crmbuild.security.RequirePermission;
import com.ceedpods.crmbuild.service.permission.RBACService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for managing Role-Based Access Control (RBAC) permissions
 * Admin-only endpoints for configuring the permission matrix
 */
@RestController
@RequestMapping("/rbac")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "RBAC Management", description = "Role-Based Access Control management endpoints")
public class RBACController {
    
    private final RBACService rbacService;
    
    @GetMapping("/roles/{role}/permissions")
    @Operation(summary = "Get all permissions for a role")
    @RequirePermission("SYSTEM_ACCESS_ADMIN_SETTINGS")
    public ResponseEntity<List<UserRolePermission>> getRolePermissions(
            @Parameter(description = "User role") @PathVariable UserRole role) {
        
        List<UserRolePermission> permissions = rbacService.getRolePermissions(role);
        return ResponseEntity.ok(permissions);
    }
    
    @GetMapping("/roles/{role}/permissions/{permission}")
    @Operation(summary = "Get specific role permission")
    @RequirePermission("SYSTEM_ACCESS_ADMIN_SETTINGS")
    public ResponseEntity<UserRolePermission> getRolePermission(
            @Parameter(description = "User role") @PathVariable UserRole role,
            @Parameter(description = "Permission code") @PathVariable String permission) {
        
        try {
            Permission permissionEnum = Permission.fromCode(permission);
            return rbacService.getRolePermission(role, permissionEnum)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/roles/{role}/permissions/{permission}")
    @Operation(summary = "Update role permission scope")
    @RequirePermission("SYSTEM_ACCESS_ADMIN_SETTINGS")
    public ResponseEntity<UserRolePermission> updateRolePermission(
            @Parameter(description = "User role") @PathVariable UserRole role,
            @Parameter(description = "Permission code") @PathVariable String permission,
            @RequestBody UpdateRolePermissionRequest request) {
        
        try {
            Permission permissionEnum = Permission.fromCode(permission);
            UserRolePermission updated = rbacService.updateRolePermissionScope(
                role, permissionEnum, request.getScope(), request.isEnabled());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/permissions")
    @Operation(summary = "Get all available permissions")
    @RequirePermission("SYSTEM_ACCESS_ADMIN_SETTINGS")
    public ResponseEntity<List<PermissionInfo>> getAllPermissions() {
        List<PermissionInfo> permissions = java.util.Arrays.stream(Permission.values())
            .map(p -> new PermissionInfo(p.getCode(), p.getDescription(), p.getCategory(), p.isAssignable()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(permissions);
    }
    
    @GetMapping("/scopes")
    @Operation(summary = "Get all available permission scopes")
    @RequirePermission("SYSTEM_ACCESS_ADMIN_SETTINGS")
    public ResponseEntity<List<ScopeInfo>> getAllScopes() {
        List<ScopeInfo> scopes = java.util.Arrays.stream(PermissionScope.values())
            .map(s -> new ScopeInfo(s.getCode(), s.getDescription()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(scopes);
    }
    
    @PostMapping("/initialize")
    @Operation(summary = "Reinitialize RBAC permissions matrix")
    @RequirePermission("SYSTEM_ACCESS_ADMIN_SETTINGS")
    public ResponseEntity<Map<String, String>> initializeRBAC() {
        rbacService.initializeRolePermissions();
        return ResponseEntity.ok(Map.of("message", "RBAC permissions matrix reinitialized successfully"));
    }
    
    @GetMapping("/roles/{role}/permissions/check")
    @Operation(summary = "Check if role has specific permission with scope")
    @RequirePermission("SYSTEM_ACCESS_ADMIN_SETTINGS")
    public ResponseEntity<PermissionCheckResult> checkRolePermission(
            @Parameter(description = "User role") @PathVariable UserRole role,
            @Parameter(description = "Permission code") @RequestParam String permission,
            @Parameter(description = "Required scope") @RequestParam PermissionScope scope) {
        
        try {
            Permission permissionEnum = Permission.fromCode(permission);
            boolean hasPermission = rbacService.hasPermission(role, permissionEnum, scope);
            PermissionScope grantedScope = rbacService.getPermissionScope(role, permissionEnum);
            
            PermissionCheckResult result = new PermissionCheckResult(
                hasPermission, grantedScope, scope);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // DTO classes
    public static class UpdateRolePermissionRequest {
        private PermissionScope scope;
        private boolean enabled;
        
        // Getters and setters
        public PermissionScope getScope() { return scope; }
        public void setScope(PermissionScope scope) { this.scope = scope; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
    
    public static class PermissionInfo {
        private String code;
        private String description;
        private String category;
        private boolean assignable;
        
        public PermissionInfo(String code, String description, Object category, boolean assignable) {
            this.code = code;
            this.description = description;
            this.category = category.toString();
            this.assignable = assignable;
        }
        
        // Getters
        public String getCode() { return code; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
        public boolean isAssignable() { return assignable; }
    }
    
    public static class ScopeInfo {
        private String code;
        private String description;
        
        public ScopeInfo(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        // Getters
        public String getCode() { return code; }
        public String getDescription() { return description; }
    }
    
    public static class PermissionCheckResult {
        private boolean hasPermission;
        private PermissionScope grantedScope;
        private PermissionScope requiredScope;
        
        public PermissionCheckResult(boolean hasPermission, PermissionScope grantedScope, PermissionScope requiredScope) {
            this.hasPermission = hasPermission;
            this.grantedScope = grantedScope;
            this.requiredScope = requiredScope;
        }
        
        // Getters
        public boolean isHasPermission() { return hasPermission; }
        public PermissionScope getGrantedScope() { return grantedScope; }
        public PermissionScope getRequiredScope() { return requiredScope; }
    }
}