package com.ceedpods.crmbuild.controller.user;

import com.ceedpods.crmbuild.dto.UserPermissionDTO;
import com.ceedpods.crmbuild.dto.request.AssignPermissionRequest;
import com.ceedpods.crmbuild.dto.request.RevokePermissionRequest;
import com.ceedpods.crmbuild.dto.response.ApiResponse;
import com.ceedpods.crmbuild.entity.UserPermission;
import com.ceedpods.crmbuild.mapper.UserPermissionMapper;
import com.ceedpods.crmbuild.security.CustomPermissionEvaluator;
import com.ceedpods.crmbuild.security.RequirePermission;
import com.ceedpods.crmbuild.service.user.UserPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user-permissions")
@RequiredArgsConstructor
@Slf4j
public class UserPermissionController {
    
    private final UserPermissionService userPermissionService;
    private final UserPermissionMapper userPermissionMapper;
    private final CustomPermissionEvaluator permissionEvaluator;
    
    @PostMapping("/assign")
    @RequirePermission("USER_MANAGE_PERMISSIONS")
    public ResponseEntity<ApiResponse<UserPermissionDTO>> assignPermission(
            @Valid @RequestBody AssignPermissionRequest request,
            Authentication authentication) {
        try {
            String adminUserId = permissionEvaluator.getCurrentKeycloakId(authentication);
            
            UserPermission userPermission = userPermissionService.assignPermission(
                adminUserId,
                request.getUserId(),
                request.getPermissionCode(),
                request.getNotes()
            );
            
            // Update expiry if provided
            if (request.getExpiresAt() != null) {
                userPermission = userPermissionService.updatePermissionExpiry(
                    userPermission.getId(),
                    request.getExpiresAt(),
                    adminUserId
                );
            }
            
            UserPermissionDTO dto = userPermissionMapper.toDTO(userPermission);
            return ResponseEntity.ok(ApiResponse.success("Permission assigned successfully", dto));
            
        } catch (Exception e) {
            log.error("Error assigning permission: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to assign permission: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/revoke")
    @RequirePermission("USER_MANAGE_PERMISSIONS")
    public ResponseEntity<ApiResponse<String>> revokePermission(
            @Valid @RequestBody RevokePermissionRequest request,
            Authentication authentication) {
        try {
            String adminUserId = permissionEvaluator.getCurrentKeycloakId(authentication);
            
            userPermissionService.revokePermission(
                adminUserId,
                request.getUserId(),
                request.getPermissionCode(),
                request.getReason()
            );
            
            return ResponseEntity.ok(ApiResponse.success("Permission revoked successfully"));
            
        } catch (Exception e) {
            log.error("Error revoking permission: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to revoke permission: " + e.getMessage()));
        }
    }
    
    @GetMapping("/user/{userId}")
    @RequirePermission("USER_VIEW_ALL")
    public ResponseEntity<ApiResponse<List<UserPermissionDTO>>> getUserPermissions(
            @PathVariable String userId,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        try {
            List<UserPermission> permissions = includeInactive 
                ? userPermissionService.getAllUserPermissions(userId)
                : userPermissionService.getUserPermissions(userId);
            
            List<UserPermissionDTO> permissionDTOs = permissions.stream()
                .map(userPermissionMapper::toDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(permissionDTOs));
            
        } catch (Exception e) {
            log.error("Error fetching user permissions for {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fetch user permissions"));
        }
    }
    
    @GetMapping("/user/{userId}/codes")
    @RequirePermission("USER_VIEW_ALL")
    public ResponseEntity<ApiResponse<Set<String>>> getUserPermissionCodes(@PathVariable String userId) {
        try {
            Set<String> permissionCodes = userPermissionService.getUserPermissionCodes(userId);
            return ResponseEntity.ok(ApiResponse.success(permissionCodes));
            
        } catch (Exception e) {
            log.error("Error fetching user permission codes for {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fetch user permission codes"));
        }
    }
    
    @GetMapping("/user/{userId}/effective")
    @RequirePermission("USER_VIEW_ALL")
    public ResponseEntity<ApiResponse<List<UserPermissionDTO>>> getEffectiveUserPermissions(
            @PathVariable String userId) {
        try {
            List<UserPermission> permissions = userPermissionService.getEffectiveUserPermissions(userId);
            List<UserPermissionDTO> permissionDTOs = permissions.stream()
                .map(userPermissionMapper::toDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(permissionDTOs));
            
        } catch (Exception e) {
            log.error("Error fetching effective permissions for {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fetch effective permissions"));
        }
    }
    
    @GetMapping("/permission/{permissionCode}")
    @RequirePermission("USER_MANAGE_PERMISSIONS")
    public ResponseEntity<ApiResponse<List<UserPermissionDTO>>> getPermissionAssignments(
            @PathVariable String permissionCode) {
        try {
            List<UserPermission> permissions = userPermissionService.getPermissionsByCode(permissionCode);
            List<UserPermissionDTO> permissionDTOs = permissions.stream()
                .map(userPermissionMapper::toDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(permissionDTOs));
            
        } catch (Exception e) {
            log.error("Error fetching assignments for permission {}: {}", permissionCode, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fetch permission assignments"));
        }
    }
    
    @GetMapping("/assigned-by/{adminUserId}")
    @RequirePermission("USER_MANAGE_PERMISSIONS")
    public ResponseEntity<ApiResponse<List<UserPermissionDTO>>> getPermissionsAssignedBy(
            @PathVariable String adminUserId) {
        try {
            List<UserPermission> permissions = userPermissionService.getPermissionsAssignedBy(adminUserId);
            List<UserPermissionDTO> permissionDTOs = permissions.stream()
                .map(userPermissionMapper::toDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(permissionDTOs));
            
        } catch (Exception e) {
            log.error("Error fetching permissions assigned by {}: {}", adminUserId, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fetch assigned permissions"));
        }
    }
    
    @PutMapping("/{permissionId}/expiry")
    @RequirePermission("USER_MANAGE_PERMISSIONS")
    public ResponseEntity<ApiResponse<UserPermissionDTO>> updatePermissionExpiry(
            @PathVariable String permissionId,
            @RequestParam(required = false) LocalDateTime expiresAt,
            Authentication authentication) {
        try {
            String adminUserId = permissionEvaluator.getCurrentKeycloakId(authentication);
            
            UserPermission updatedPermission = userPermissionService.updatePermissionExpiry(
                permissionId,
                expiresAt,
                adminUserId
            );
            
            UserPermissionDTO dto = userPermissionMapper.toDTO(updatedPermission);
            return ResponseEntity.ok(ApiResponse.success("Permission expiry updated successfully", dto));
            
        } catch (Exception e) {
            log.error("Error updating permission expiry: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to update permission expiry: " + e.getMessage()));
        }
    }
    
    @PostMapping("/user/{userId}/default")
    @RequirePermission("USER_MANAGE_PERMISSIONS")
    public ResponseEntity<ApiResponse<String>> assignDefaultPermissions(
            @PathVariable String userId,
            Authentication authentication) {
        try {
            String adminUserId = permissionEvaluator.getCurrentKeycloakId(authentication);
            userPermissionService.assignDefaultPermissions(adminUserId, userId);
            
            return ResponseEntity.ok(ApiResponse.success("Default permissions assigned successfully"));
            
        } catch (Exception e) {
            log.error("Error assigning default permissions to {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to assign default permissions: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/user/{userId}/all")
    @RequirePermission("USER_MANAGE_PERMISSIONS")
    public ResponseEntity<ApiResponse<String>> removeAllUserPermissions(
            @PathVariable String userId,
            Authentication authentication) {
        try {
            String adminUserId = permissionEvaluator.getCurrentKeycloakId(authentication);
            userPermissionService.removeAllUserPermissions(userId, adminUserId);
            
            return ResponseEntity.ok(ApiResponse.success("All user permissions removed successfully"));
            
        } catch (Exception e) {
            log.error("Error removing all permissions for {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to remove user permissions: " + e.getMessage()));
        }
    }
    
    @PostMapping("/cleanup/expired")
    @RequirePermission("USER_MANAGE_PERMISSIONS")
    public ResponseEntity<ApiResponse<String>> cleanupExpiredPermissions() {
        try {
            userPermissionService.cleanupExpiredPermissions();
            return ResponseEntity.ok(ApiResponse.success("Expired permissions cleaned up successfully"));
            
        } catch (Exception e) {
            log.error("Error cleaning up expired permissions: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to cleanup expired permissions"));
        }
    }
    
    @GetMapping("/user/{userId}/count")
    @RequirePermission("USER_VIEW_ALL")
    public ResponseEntity<ApiResponse<Long>> getUserPermissionCount(@PathVariable String userId) {
        try {
            long count = userPermissionService.getUserPermissionCount(userId);
            return ResponseEntity.ok(ApiResponse.success(count));
            
        } catch (Exception e) {
            log.error("Error fetching permission count for {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fetch permission count"));
        }
    }
    
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> checkPermission(
            @RequestParam String permissionCode,
            Authentication authentication) {
        try {
            String userId = permissionEvaluator.getCurrentKeycloakId(authentication);
            boolean hasPermission = userPermissionService.hasPermission(userId, permissionCode);
            
            return ResponseEntity.ok(ApiResponse.success(hasPermission));
            
        } catch (Exception e) {
            log.error("Error checking permission {}: {}", permissionCode, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to check permission"));
        }
    }
}