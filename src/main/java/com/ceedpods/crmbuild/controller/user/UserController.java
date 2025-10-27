package com.ceedpods.crmbuild.controller.user;

import com.ceedpods.crmbuild.dto.UserDTO;
import com.ceedpods.crmbuild.dto.request.AssignManagerRequest;
import com.ceedpods.crmbuild.dto.request.CreateUserRequest;
import com.ceedpods.crmbuild.dto.response.ApiResponse;
import com.ceedpods.crmbuild.entity.user.User;
import com.ceedpods.crmbuild.enums.UserRole;
import com.ceedpods.crmbuild.mapper.UserMapper;
import com.ceedpods.crmbuild.security.CustomPermissionEvaluator;
import com.ceedpods.crmbuild.security.RequireAnyPermission;
import com.ceedpods.crmbuild.security.RequirePermission;
import com.ceedpods.crmbuild.service.user.UserHierarchyService;
import com.ceedpods.crmbuild.service.user.UserService;
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
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    private final UserHierarchyService userHierarchyService;
    private final UserMapper userMapper;
    private final CustomPermissionEvaluator permissionEvaluator;
    
    @PostMapping
    @RequirePermission("USER_CREATE")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication) {
        try {
            String createdBy = permissionEvaluator.getCurrentKeycloakId(authentication);
            
            // Validate password confirmation if needed
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Password is required"));
            }
            
            User user = userService.createUser(
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getRole(),
                request.getPassword(),
                createdBy
            );
            
            // Update additional fields
            if (request.getPhoneNumber() != null || request.getDepartment() != null || 
                request.getTerritory() != null || request.getJobTitle() != null) {
                
                user.setPhoneNumber(request.getPhoneNumber());
                user.setDepartment(request.getDepartment());
                user.setTerritory(request.getTerritory());
                user.setJobTitle(request.getJobTitle());
                user = userService.save(user);
            }
            
            // Assign to manager if specified and user is sales rep
            if (request.getRole() == UserRole.SALES_REP && request.getAssignedManagerId() != null) {
                try {
                    userHierarchyService.assignSalesRepToManager(
                        createdBy,
                        user.getKeycloakId(),
                        request.getAssignedManagerId(),
                        request.getTerritory(),
                        "Assigned during user creation"
                    );
                } catch (Exception e) {
                    log.warn("Failed to assign manager during user creation: {}", e.getMessage());
                }
            }
            
            UserDTO userDTO = userMapper.toDTO(user);
            return ResponseEntity.ok(ApiResponse.success("User created successfully", userDTO));
            
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to create user: " + e.getMessage()));
        }
    }
    
    @GetMapping
    @RequireAnyPermission({"USER_VIEW_ALL", "USER_VIEW_TEAM"})
    public ResponseEntity<ApiResponse<List<UserDTO>>> getUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) String search,
            Authentication authentication) {
        try {
            String currentUserId = permissionEvaluator.getCurrentKeycloakId(authentication);
            
            List<User> users;
            
            if (permissionEvaluator.hasPermission(authentication, "USER_VIEW_ALL")) {
                // Admin can see all users
                if (role != null) {
                    users = userService.getUsersByRole(role);
                } else if (search != null && !search.trim().isEmpty()) {
                    users = userService.searchUsers(search.trim());
                } else {
                    users = userService.getAllUsers();
                }
            } else {
                // Manager can only see their team
                users = userHierarchyService.getManagerReports(currentUserId);
                
                // Filter by role if specified
                if (role != null) {
                    users = users.stream()
                        .filter(user -> user.getRole() == role)
                        .collect(Collectors.toList());
                }
                
                // Filter by search if specified
                if (search != null && !search.trim().isEmpty()) {
                    String searchLower = search.toLowerCase();
                    users = users.stream()
                        .filter(user -> 
                            user.getFirstName().toLowerCase().contains(searchLower) ||
                            user.getLastName().toLowerCase().contains(searchLower) ||
                            user.getEmail().toLowerCase().contains(searchLower))
                        .collect(Collectors.toList());
                }
            }
            
            List<UserDTO> userDTOs = users.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(userDTOs));
            
        } catch (Exception e) {
            log.error("Error fetching users: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fetch users"));
        }
    }
    
    @GetMapping("/{userId}")
    @RequireAnyPermission({"USER_VIEW_ALL", "USER_VIEW_TEAM"})
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable String userId) {
        try {
            User user = userService.findByKeycloakId(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            UserDTO userDTO = userMapper.toDTO(user);
            return ResponseEntity.ok(ApiResponse.success(userDTO));
            
        } catch (Exception e) {
            log.error("Error fetching user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fetch user"));
        }
    }
    
    @PutMapping("/{userId}")
    @RequirePermission("USER_EDIT")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication) {
        try {
            String updatedBy = permissionEvaluator.getCurrentKeycloakId(authentication);
            
            User existingUser = userService.findByKeycloakId(userId);
            if (existingUser == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Create updated user object
            User updatedUser = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .department(request.getDepartment())
                .territory(request.getTerritory())
                .jobTitle(request.getJobTitle())
                .enabled(existingUser.isEnabled()) // Keep current status
                .build();
            
            User savedUser = userService.updateUser(existingUser.getId(), updatedUser, updatedBy);
            UserDTO userDTO = userMapper.toDTO(savedUser);
            
            return ResponseEntity.ok(ApiResponse.success("User updated successfully", userDTO));
            
        } catch (Exception e) {
            log.error("Error updating user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to update user: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{userId}")
    @RequirePermission("USER_DELETE")
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @PathVariable String userId,
            Authentication authentication) {
        try {
            String deletedBy = permissionEvaluator.getCurrentKeycloakId(authentication);
            
            User user = userService.findByKeycloakId(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            userService.deleteUser(user.getId(), deletedBy);
            
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
            
        } catch (Exception e) {
            log.error("Error deleting user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to delete user: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{userId}/enable")
    @RequirePermission("USER_EDIT")
    public ResponseEntity<ApiResponse<String>> enableUser(
            @PathVariable String userId,
            Authentication authentication) {
        try {
            String enabledBy = permissionEvaluator.getCurrentKeycloakId(authentication);
            
            User user = userService.findByKeycloakId(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            userService.enableUser(user.getId(), enabledBy);
            
            return ResponseEntity.ok(ApiResponse.success("User enabled successfully"));
            
        } catch (Exception e) {
            log.error("Error enabling user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to enable user: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{userId}/disable")
    @RequirePermission("USER_EDIT")
    public ResponseEntity<ApiResponse<String>> disableUser(
            @PathVariable String userId,
            Authentication authentication) {
        try {
            String disabledBy = permissionEvaluator.getCurrentKeycloakId(authentication);
            
            User user = userService.findByKeycloakId(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            userService.disableUser(user.getId(), disabledBy);
            
            return ResponseEntity.ok(ApiResponse.success("User disabled successfully"));
            
        } catch (Exception e) {
            log.error("Error disabling user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to disable user: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{userId}/assign-manager")
    @RequirePermission("USER_ASSIGN")
    public ResponseEntity<ApiResponse<String>> assignManager(
            @PathVariable String userId,
            @Valid @RequestBody AssignManagerRequest request,
            Authentication authentication) {
        try {
            String assignedBy = permissionEvaluator.getCurrentKeycloakId(authentication);
            
            userHierarchyService.assignSalesRepToManager(
                assignedBy,
                userId,
                request.getManagerId(),
                request.getTerritory(),
                request.getNotes()
            );
            
            return ResponseEntity.ok(ApiResponse.success("Manager assigned successfully"));
            
        } catch (Exception e) {
            log.error("Error assigning manager to user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to assign manager: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{userId}/remove-manager")
    @RequirePermission("USER_ASSIGN")
    public ResponseEntity<ApiResponse<String>> removeManager(
            @PathVariable String userId,
            @RequestParam(defaultValue = "Manager assignment removed") String reason,
            Authentication authentication) {
        try {
            String removedBy = permissionEvaluator.getCurrentKeycloakId(authentication);
            
            userHierarchyService.removeSalesRepFromManager(removedBy, userId, reason);
            
            return ResponseEntity.ok(ApiResponse.success("Manager assignment removed successfully"));
            
        } catch (Exception e) {
            log.error("Error removing manager from user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to remove manager assignment: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{userId}/reports")
    @RequireAnyPermission({"USER_VIEW_ALL", "USER_VIEW_TEAM"})
    public ResponseEntity<ApiResponse<List<UserDTO>>> getManagerReports(@PathVariable String userId) {
        try {
            List<User> reports = userHierarchyService.getManagerReports(userId);
            List<UserDTO> reportDTOs = reports.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(reportDTOs));
            
        } catch (Exception e) {
            log.error("Error fetching reports for manager {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fetch manager reports"));
        }
    }
    
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUserProfile(Authentication authentication) {
        try {
            String userId = permissionEvaluator.getCurrentKeycloakId(authentication);
            User user = userService.findByKeycloakId(userId);
            
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            UserDTO userDTO = userMapper.toDTO(user);
            return ResponseEntity.ok(ApiResponse.success(userDTO));
            
        } catch (Exception e) {
            log.error("Error fetching current user profile: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fetch user profile"));
        }
    }
    
    @GetMapping("/stats")
    @RequirePermission("USER_VIEW_ALL")
    public ResponseEntity<ApiResponse<UserStatsDTO>> getUserStats() {
        try {
            UserStatsDTO stats = UserStatsDTO.builder()
                .totalUsers(userService.getTotalUsersCount())
                .activeUsers(userService.getActiveUsersCount())
                .adminUsers(userService.getUsersByRoleCount(UserRole.ADMIN))
                .managerUsers(userService.getUsersByRoleCount(UserRole.MANAGER))
                .salesRepUsers(userService.getUsersByRoleCount(UserRole.SALES_REP))
                .build();
            
            return ResponseEntity.ok(ApiResponse.success(stats));
            
        } catch (Exception e) {
            log.error("Error fetching user statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fetch user statistics"));
        }
    }
    
    // Inner DTO class for statistics
    public static class UserStatsDTO {
        public long totalUsers;
        public long activeUsers;
        public long adminUsers;
        public long managerUsers;
        public long salesRepUsers;
        
        public static UserStatsDTO.UserStatsDTOBuilder builder() {
            return new UserStatsDTO.UserStatsDTOBuilder();
        }
        
        public static class UserStatsDTOBuilder {
            private long totalUsers;
            private long activeUsers;
            private long adminUsers;
            private long managerUsers;
            private long salesRepUsers;
            
            public UserStatsDTOBuilder totalUsers(long totalUsers) {
                this.totalUsers = totalUsers;
                return this;
            }
            
            public UserStatsDTOBuilder activeUsers(long activeUsers) {
                this.activeUsers = activeUsers;
                return this;
            }
            
            public UserStatsDTOBuilder adminUsers(long adminUsers) {
                this.adminUsers = adminUsers;
                return this;
            }
            
            public UserStatsDTOBuilder managerUsers(long managerUsers) {
                this.managerUsers = managerUsers;
                return this;
            }
            
            public UserStatsDTOBuilder salesRepUsers(long salesRepUsers) {
                this.salesRepUsers = salesRepUsers;
                return this;
            }
            
            public UserStatsDTO build() {
                UserStatsDTO stats = new UserStatsDTO();
                stats.totalUsers = this.totalUsers;
                stats.activeUsers = this.activeUsers;
                stats.adminUsers = this.adminUsers;
                stats.managerUsers = this.managerUsers;
                stats.salesRepUsers = this.salesRepUsers;
                return stats;
            }
        }
    }
}