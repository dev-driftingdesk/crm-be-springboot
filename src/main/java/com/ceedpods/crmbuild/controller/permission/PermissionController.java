package com.ceedpods.crmbuild.controller.permission;

import com.ceedpods.crmbuild.dto.PermissionDTO;
import com.ceedpods.crmbuild.dto.response.ApiResponse;
import com.ceedpods.crmbuild.entity.PermissionEntity;
import com.ceedpods.crmbuild.enums.PermissionCategory;
import com.ceedpods.crmbuild.enums.UserRole;
import com.ceedpods.crmbuild.mapper.PermissionMapper;
import com.ceedpods.crmbuild.security.RequirePermission;
import com.ceedpods.crmbuild.service.permission.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@Slf4j
public class PermissionController {
    
    private final PermissionService permissionService;
    private final PermissionMapper permissionMapper;
    
    @GetMapping
    @RequirePermission("USER_MANAGE_PERMISSIONS")
    public ResponseEntity<ApiResponse<List<PermissionDTO>>> getAllPermissions() {
        try {
            List<PermissionEntity> permissions = permissionService.getAllPermissions();
            List<PermissionDTO> permissionDTOs = permissions.stream()
                .map(permissionMapper::toDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(permissionDTOs));
            
        } catch (Exception e) {
            log.error("Error fetching all permissions: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fetch permissions"));
        }
    }
    
    @GetMapping("/assignable")
    @RequirePermission("USER_MANAGE_PERMISSIONS")
    public ResponseEntity<ApiResponse<List<PermissionDTO>>> getAssignablePermissions() {
        try {
            List<PermissionEntity> permissions = permissionService.getAssignablePermissions();
            List<PermissionDTO> permissionDTOs = permissions.stream()
                .map(permissionMapper::toDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(permissionDTOs));
            
        } catch (Exception e) {
            log.error("Error fetching assignable permissions: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fetch assignable permissions"));
        }
    }
    
    @GetMapping("/assignable/{role}")
    @RequirePermission("USER_MANAGE_PERMISSIONS")
    public ResponseEntity<ApiResponse<List<PermissionDTO>>> getAssignablePermissionsForRole(
            @PathVariable UserRole role) {
        try {
            List<PermissionEntity> permissions = permissionService.getAssignablePermissionsForRole(role);
            List<PermissionDTO> permissionDTOs = permissions.stream()
                .map(permissionMapper::toDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(permissionDTOs));
            
        } catch (Exception e) {
            log.error("Error fetching assignable permissions for role {}: {}", role, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fetch permissions for role"));
        }
    }
    
    @GetMapping("/category/{category}")
    @RequirePermission("USER_MANAGE_PERMISSIONS")
    public ResponseEntity<ApiResponse<List<PermissionDTO>>> getPermissionsByCategory(
            @PathVariable PermissionCategory category) {
        try {
            List<PermissionEntity> permissions = permissionService.getPermissionsByCategory(category);
            List<PermissionDTO> permissionDTOs = permissions.stream()
                .map(permissionMapper::toDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(permissionDTOs));
            
        } catch (Exception e) {
            log.error("Error fetching permissions for category {}: {}", category, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fetch permissions for category"));
        }
    }
    
    @GetMapping("/{permissionCode}")
    @RequirePermission("USER_MANAGE_PERMISSIONS")
    public ResponseEntity<ApiResponse<PermissionDTO>> getPermissionByCode(
            @PathVariable String permissionCode) {
        try {
            return permissionService.getPermissionByCode(permissionCode)
                .map(permission -> ResponseEntity.ok(ApiResponse.success(permissionMapper.toDTO(permission))))
                .orElse(ResponseEntity.notFound().build());
                
        } catch (Exception e) {
            log.error("Error fetching permission {}: {}", permissionCode, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fetch permission"));
        }
    }
    
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<PermissionCategory[]>> getPermissionCategories() {
        return ResponseEntity.ok(ApiResponse.success(PermissionCategory.values()));
    }
    
    @GetMapping("/stats")
    @RequirePermission("USER_MANAGE_PERMISSIONS")
    public ResponseEntity<ApiResponse<PermissionStatsDTO>> getPermissionStats() {
        try {
            PermissionStatsDTO stats = PermissionStatsDTO.builder()
                .totalPermissions(permissionService.getTotalPermissionsCount())
                .activePermissions(permissionService.getActivePermissionsCount())
                .assignablePermissions(permissionService.getAssignablePermissionsCount())
                .build();
            
            return ResponseEntity.ok(ApiResponse.success(stats));
            
        } catch (Exception e) {
            log.error("Error fetching permission statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fetch permission statistics"));
        }
    }
    
    @GetMapping("/validate")
    @RequirePermission("USER_MANAGE_PERMISSIONS")
    public ResponseEntity<ApiResponse<Boolean>> validatePermission(
            @RequestParam String permissionCode) {
        try {
            boolean isValid = permissionService.isPermissionValid(permissionCode);
            return ResponseEntity.ok(ApiResponse.success(isValid));
            
        } catch (Exception e) {
            log.error("Error validating permission {}: {}", permissionCode, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to validate permission"));
        }
    }
    
    // Inner DTO class for statistics
    public static class PermissionStatsDTO {
        public long totalPermissions;
        public long activePermissions;
        public long assignablePermissions;
        
        public static PermissionStatsDTO.PermissionStatsDTOBuilder builder() {
            return new PermissionStatsDTO.PermissionStatsDTOBuilder();
        }
        
        public static class PermissionStatsDTOBuilder {
            private long totalPermissions;
            private long activePermissions;
            private long assignablePermissions;
            
            public PermissionStatsDTOBuilder totalPermissions(long totalPermissions) {
                this.totalPermissions = totalPermissions;
                return this;
            }
            
            public PermissionStatsDTOBuilder activePermissions(long activePermissions) {
                this.activePermissions = activePermissions;
                return this;
            }
            
            public PermissionStatsDTOBuilder assignablePermissions(long assignablePermissions) {
                this.assignablePermissions = assignablePermissions;
                return this;
            }
            
            public PermissionStatsDTO build() {
                PermissionStatsDTO stats = new PermissionStatsDTO();
                stats.totalPermissions = this.totalPermissions;
                stats.activePermissions = this.activePermissions;
                stats.assignablePermissions = this.assignablePermissions;
                return stats;
            }
        }
    }
}