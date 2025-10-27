package com.ceedpods.crmbuild.controller.user;

import com.ceedpods.crmbuild.dto.UserDTO;
import com.ceedpods.crmbuild.dto.UserInvitationDTO;
import com.ceedpods.crmbuild.dto.request.CompleteInvitationRequest;
import com.ceedpods.crmbuild.dto.request.CreateInvitationRequest;
import com.ceedpods.crmbuild.dto.response.ApiResponse;
import com.ceedpods.crmbuild.entity.user.User;
import com.ceedpods.crmbuild.entity.UserInvitation;
import com.ceedpods.crmbuild.mapper.UserInvitationMapper;
import com.ceedpods.crmbuild.mapper.UserMapper;
import com.ceedpods.crmbuild.security.CustomPermissionEvaluator;
import com.ceedpods.crmbuild.security.RequirePermission;
import com.ceedpods.crmbuild.service.user.UserInvitationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
@Slf4j
public class UserInvitationController {
    
    private final UserInvitationService userInvitationService;
    private final UserInvitationMapper userInvitationMapper;
    private final UserMapper userMapper;
    private final CustomPermissionEvaluator permissionEvaluator;
    
    @PostMapping
    @RequirePermission("USER_CREATE")
    public ResponseEntity<ApiResponse<UserInvitationDTO>> createInvitation(
            @Valid @RequestBody CreateInvitationRequest request,
            Authentication authentication) {
        try {
            String adminUserId = permissionEvaluator.getCurrentKeycloakId(authentication);
            
            UserInvitation invitation = userInvitationService.createInvitation(
                adminUserId,
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getRole(),
                request.getAssignedManagerId(),
                request.getPermissionCodes(),
                request.getNotes()
            );
            
            UserInvitationDTO invitationDTO = userInvitationMapper.toDTO(invitation);
            return ResponseEntity.ok(ApiResponse.success("Invitation created successfully", invitationDTO));
            
        } catch (Exception e) {
            log.error("Error creating invitation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to create invitation: " + e.getMessage()));
        }
    }
    
    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<UserDTO>> completeInvitation(
            @Valid @RequestBody CompleteInvitationRequest request) {
        try {
            // Validate password confirmation
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Password and confirm password do not match"));
            }
            
            User user = userInvitationService.completeInvitation(
                request.getInvitationToken(),
                request.getPassword()
            );
            
            UserDTO userDTO = userMapper.toDTO(user);
            return ResponseEntity.ok(ApiResponse.success("Registration completed successfully", userDTO));
            
        } catch (Exception e) {
            log.error("Error completing invitation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to complete invitation: " + e.getMessage()));
        }
    }
    
    @GetMapping
    @RequirePermission("USER_CREATE")
    public ResponseEntity<ApiResponse<List<UserInvitationDTO>>> getAllInvitations(
            @RequestParam(defaultValue = "false") boolean includeUsed,
            @RequestParam(defaultValue = "false") boolean includeExpired,
            Authentication authentication) {
        try {
            List<UserInvitation> invitations;
            
            if (includeUsed && includeExpired) {
                // Get invitations created by this admin
                String adminUserId = permissionEvaluator.getCurrentKeycloakId(authentication);
                invitations = userInvitationService.getInvitationsByAdmin(adminUserId);
            } else if (!includeExpired) {
                invitations = userInvitationService.getPendingInvitations();
            } else {
                // This would need a custom method to get all non-expired
                String adminUserId = permissionEvaluator.getCurrentKeycloakId(authentication);
                invitations = userInvitationService.getInvitationsByAdmin(adminUserId);
            }
            
            List<UserInvitationDTO> invitationDTOs = invitations.stream()
                .map(userInvitationMapper::toDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(invitationDTOs));
            
        } catch (Exception e) {
            log.error("Error fetching invitations: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fetch invitations"));
        }
    }
    
    @GetMapping("/pending")
    @RequirePermission("USER_CREATE")
    public ResponseEntity<ApiResponse<List<UserInvitationDTO>>> getPendingInvitations() {
        try {
            List<UserInvitation> invitations = userInvitationService.getPendingInvitations();
            List<UserInvitationDTO> invitationDTOs = invitations.stream()
                .map(userInvitationMapper::toDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(invitationDTOs));
            
        } catch (Exception e) {
            log.error("Error fetching pending invitations: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fetch pending invitations"));
        }
    }
    
    @GetMapping("/expired")
    @RequirePermission("USER_CREATE")
    public ResponseEntity<ApiResponse<List<UserInvitationDTO>>> getExpiredInvitations() {
        try {
            List<UserInvitation> invitations = userInvitationService.getExpiredInvitations();
            List<UserInvitationDTO> invitationDTOs = invitations.stream()
                .map(userInvitationMapper::toDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(invitationDTOs));
            
        } catch (Exception e) {
            log.error("Error fetching expired invitations: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fetch expired invitations"));
        }
    }
    
    @GetMapping("/{invitationId}")
    @RequirePermission("USER_CREATE")
    public ResponseEntity<ApiResponse<UserInvitationDTO>> getInvitationById(@PathVariable String invitationId) {
        try {
            return userInvitationService.getInvitationByToken(invitationId)
                .map(invitation -> ResponseEntity.ok(ApiResponse.success(userInvitationMapper.toDTO(invitation))))
                .orElse(ResponseEntity.notFound().build());
                
        } catch (Exception e) {
            log.error("Error fetching invitation {}: {}", invitationId, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fetch invitation"));
        }
    }
    
    @GetMapping("/validate/{token}")
    public ResponseEntity<ApiResponse<UserInvitationDTO>> validateInvitationToken(@PathVariable String token) {
        try {
            return userInvitationService.getValidInvitationByToken(token)
                .map(invitation -> {
                    UserInvitationDTO dto = userInvitationMapper.toDTO(invitation);
                    // Remove sensitive information for public validation
                    dto.setInvitationToken(null);
                    return ResponseEntity.ok(ApiResponse.success(dto));
                })
                .orElse(ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid or expired invitation token")));
                
        } catch (Exception e) {
            log.error("Error validating invitation token: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid invitation token"));
        }
    }
    
    @PostMapping("/{invitationId}/resend")
    @RequirePermission("USER_CREATE")
    public ResponseEntity<ApiResponse<String>> resendInvitation(
            @PathVariable String invitationId,
            Authentication authentication) {
        try {
            String adminUserId = permissionEvaluator.getCurrentKeycloakId(authentication);
            userInvitationService.resendInvitation(invitationId, adminUserId);
            
            return ResponseEntity.ok(ApiResponse.success("Invitation resent successfully"));
            
        } catch (Exception e) {
            log.error("Error resending invitation {}: {}", invitationId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to resend invitation: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{invitationId}")
    @RequirePermission("USER_CREATE")
    public ResponseEntity<ApiResponse<String>> revokeInvitation(
            @PathVariable String invitationId,
            @RequestParam(defaultValue = "Invitation revoked by admin") String reason,
            Authentication authentication) {
        try {
            String adminUserId = permissionEvaluator.getCurrentKeycloakId(authentication);
            userInvitationService.revokeInvitation(invitationId, adminUserId, reason);
            
            return ResponseEntity.ok(ApiResponse.success("Invitation revoked successfully"));
            
        } catch (Exception e) {
            log.error("Error revoking invitation {}: {}", invitationId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to revoke invitation: " + e.getMessage()));
        }
    }
    
    @PostMapping("/send-reminders")
    @RequirePermission("USER_CREATE")
    public ResponseEntity<ApiResponse<String>> sendReminders() {
        try {
            userInvitationService.sendReminders();
            return ResponseEntity.ok(ApiResponse.success("Reminders sent successfully"));
            
        } catch (Exception e) {
            log.error("Error sending reminders: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to send reminders"));
        }
    }
    
    @PostMapping("/cleanup/expired")
    @RequirePermission("USER_CREATE")
    public ResponseEntity<ApiResponse<String>> cleanupExpiredInvitations() {
        try {
            userInvitationService.cleanupExpiredInvitations();
            return ResponseEntity.ok(ApiResponse.success("Expired invitations cleaned up successfully"));
            
        } catch (Exception e) {
            log.error("Error cleaning up expired invitations: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to cleanup expired invitations"));
        }
    }
    
    @GetMapping("/stats")
    @RequirePermission("USER_CREATE")
    public ResponseEntity<ApiResponse<InvitationStatsDTO>> getInvitationStats() {
        try {
            InvitationStatsDTO stats = InvitationStatsDTO.builder()
                .totalInvitations(userInvitationService.getTotalInvitationsCount())
                .pendingInvitations(userInvitationService.getPendingInvitationsCount())
                .expiredInvitations(userInvitationService.getExpiredInvitationsCount())
                .build();
            
            return ResponseEntity.ok(ApiResponse.success(stats));
            
        } catch (Exception e) {
            log.error("Error fetching invitation statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fetch invitation statistics"));
        }
    }
    
    // Inner DTO class for statistics
    public static class InvitationStatsDTO {
        public long totalInvitations;
        public long pendingInvitations;
        public long expiredInvitations;
        
        public static InvitationStatsDTO.InvitationStatsDTOBuilder builder() {
            return new InvitationStatsDTO.InvitationStatsDTOBuilder();
        }
        
        public static class InvitationStatsDTOBuilder {
            private long totalInvitations;
            private long pendingInvitations;
            private long expiredInvitations;
            
            public InvitationStatsDTOBuilder totalInvitations(long totalInvitations) {
                this.totalInvitations = totalInvitations;
                return this;
            }
            
            public InvitationStatsDTOBuilder pendingInvitations(long pendingInvitations) {
                this.pendingInvitations = pendingInvitations;
                return this;
            }
            
            public InvitationStatsDTOBuilder expiredInvitations(long expiredInvitations) {
                this.expiredInvitations = expiredInvitations;
                return this;
            }
            
            public InvitationStatsDTO build() {
                InvitationStatsDTO stats = new InvitationStatsDTO();
                stats.totalInvitations = this.totalInvitations;
                stats.pendingInvitations = this.pendingInvitations;
                stats.expiredInvitations = this.expiredInvitations;
                return stats;
            }
        }
    }
}