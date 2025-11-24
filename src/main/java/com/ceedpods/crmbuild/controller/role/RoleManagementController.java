package com.ceedpods.crmbuild.controller.role;

import com.ceedpods.crmbuild.dto.response.ApiResponse;
import com.ceedpods.crmbuild.entity.RoleEntity;
import com.ceedpods.crmbuild.enums.UserRole;
import com.ceedpods.crmbuild.service.role.RoleService;
import com.ceedpods.crmbuild.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Role Management", description = "Admin-only role and permission management")
@SecurityRequirement(name = "bearer-jwt")
public class RoleManagementController {
    
    private final RoleService roleService;
    private final UserService userService;
    
    @GetMapping
    @Operation(summary = "Get all roles", description = "Retrieve all active roles (Admin only)")
    public ResponseEntity<ApiResponse<List<RoleEntity>>> getAllRoles(
            @AuthenticationPrincipal Jwt jwt) {
        
        if (!isAdmin(jwt)) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Access denied. Admin privileges required."));
        }
        
        try {
            List<RoleEntity> roles = roleService.getAllActiveRoles();
            return ResponseEntity.ok(ApiResponse.success("Roles retrieved successfully", roles));
        } catch (Exception e) {
            log.error("Error retrieving roles", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to retrieve roles"));
        }
    }
    
    @GetMapping("/{roleId}")
    @Operation(summary = "Get role by ID", description = "Retrieve a specific role by its UUID (Admin only)")
    public ResponseEntity<ApiResponse<RoleEntity>> getRoleById(
            @PathVariable String roleId,
            @AuthenticationPrincipal Jwt jwt) {
        
        if (!isAdmin(jwt)) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Access denied. Admin privileges required."));
        }
        
        try {
            return roleService.getRoleById(roleId)
                .map(role -> ResponseEntity.ok(ApiResponse.success("Role retrieved successfully", role)))
                .orElse(ResponseEntity.status(404)
                    .body(ApiResponse.error("Role not found")));
        } catch (Exception e) {
            log.error("Error retrieving role: {}", roleId, e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to retrieve role"));
        }
    }
    
    @GetMapping("/name/{roleName}")
    @Operation(summary = "Get role by name", description = "Retrieve a specific role by name (Admin only)")
    public ResponseEntity<ApiResponse<RoleEntity>> getRoleByName(
            @PathVariable UserRole roleName,
            @AuthenticationPrincipal Jwt jwt) {
        
        if (!isAdmin(jwt)) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Access denied. Admin privileges required."));
        }
        
        try {
            return roleService.getRoleByName(roleName)
                .map(role -> ResponseEntity.ok(ApiResponse.success("Role retrieved successfully", role)))
                .orElse(ResponseEntity.status(404)
                    .body(ApiResponse.error("Role not found")));
        } catch (Exception e) {
            log.error("Error retrieving role: {}", roleName, e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to retrieve role"));
        }
    }
    
    @PutMapping("/{roleId}/permissions")
    @Operation(summary = "Update role permissions", description = "Update the complete list of permissions for a role (Admin only)")
    public ResponseEntity<ApiResponse<RoleEntity>> updateRolePermissions(
            @PathVariable String roleId,
            @Valid @RequestBody UpdateRolePermissionsRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        if (!isAdmin(jwt)) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Access denied. Admin privileges required."));
        }
        
        try {
            String adminUserId = jwt.getSubject();
            RoleEntity updatedRole = roleService.updateRolePermissions(
                roleId, request.getPermissionCodes(), adminUserId);
            
            return ResponseEntity.ok(ApiResponse.success("Role permissions updated successfully", updatedRole));
        } catch (IllegalArgumentException e) {
            log.error("Invalid permission code in request: {}", roleId, e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Error updating role permissions: {}", roleId, e);
            return ResponseEntity.status(404)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating role permissions: {}", roleId, e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to update role permissions"));
        }
    }
    
    @PostMapping("/{roleId}/permissions/{permissionCode}")
    @Operation(summary = "Add permission to role", description = "Add a single permission to a role (Admin only)")
    public ResponseEntity<ApiResponse<RoleEntity>> addPermissionToRole(
            @PathVariable String roleId,
            @PathVariable String permissionCode,
            @AuthenticationPrincipal Jwt jwt) {
        
        if (!isAdmin(jwt)) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Access denied. Admin privileges required."));
        }
        
        try {
            String adminUserId = jwt.getSubject();
            RoleEntity updatedRole = roleService.addPermissionToRole(roleId, permissionCode, adminUserId);
            
            return ResponseEntity.ok(ApiResponse.success("Permission added to role successfully", updatedRole));
        } catch (IllegalArgumentException e) {
            log.error("Invalid permission code: {}", permissionCode, e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Error adding permission to role: {}", roleId, e);
            return ResponseEntity.status(404)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error adding permission to role: {}", roleId, e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to add permission to role"));
        }
    }
    
    @DeleteMapping("/{roleId}/permissions/{permissionCode}")
    @Operation(summary = "Remove permission from role", description = "Remove a single permission from a role (Admin only)")
    public ResponseEntity<ApiResponse<RoleEntity>> removePermissionFromRole(
            @PathVariable String roleId,
            @PathVariable String permissionCode,
            @AuthenticationPrincipal Jwt jwt) {
        
        if (!isAdmin(jwt)) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Access denied. Admin privileges required."));
        }
        
        try {
            String adminUserId = jwt.getSubject();
            RoleEntity updatedRole = roleService.removePermissionFromRole(roleId, permissionCode, adminUserId);
            
            return ResponseEntity.ok(ApiResponse.success("Permission removed from role successfully", updatedRole));
        } catch (RuntimeException e) {
            log.error("Error removing permission from role: {}", roleId, e);
            return ResponseEntity.status(404)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error removing permission from role: {}", roleId, e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to remove permission from role"));
        }
    }
    
    private boolean isAdmin(Jwt jwt) {
        try {
            String keycloakId = jwt.getSubject();
            var user = userService.findByKeycloakId(keycloakId);
            return user != null && user.getRole() == UserRole.ADMIN;
        } catch (Exception e) {
            log.error("Error checking admin status", e);
            return false;
        }
    }
    
    // Request DTOs
    public static class UpdateRolePermissionsRequest {
        private List<String> permissionCodes;
        
        public List<String> getPermissionCodes() {
            return permissionCodes;
        }
        
        public void setPermissionCodes(List<String> permissionCodes) {
            this.permissionCodes = permissionCodes;
        }
    }
}