package com.ceedpods.crmbuild.service.auditLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    
    public void logPermissionAssignment(String adminUserId, String targetUserId, String permissionCode, String action) {
        log.info("AUDIT: Permission {} - Admin: {}, Target: {}, Permission: {}, Time: {}", 
            action, adminUserId, targetUserId, permissionCode, LocalDateTime.now());
    }
    
    public void logPermissionRevocation(String adminUserId, String targetUserId, String permissionCode, String reason) {
        log.info("AUDIT: Permission REVOKED - Admin: {}, Target: {}, Permission: {}, Reason: {}, Time: {}", 
            adminUserId, targetUserId, permissionCode, reason, LocalDateTime.now());
    }
    
    public void logBulkPermissionRevocation(String adminUserId, String targetUserId, List<String> permissionCodes) {
        log.info("AUDIT: Bulk Permission REVOKED - Admin: {}, Target: {}, Permissions: {}, Time: {}", 
            adminUserId, targetUserId, permissionCodes, LocalDateTime.now());
    }
    
    public void logPermissionExpiryUpdate(String adminUserId, String targetUserId, String permissionCode, 
                                        LocalDateTime previousExpiry, LocalDateTime newExpiry) {
        log.info("AUDIT: Permission Expiry UPDATED - Admin: {}, Target: {}, Permission: {}, Previous: {}, New: {}, Time: {}", 
            adminUserId, targetUserId, permissionCode, previousExpiry, newExpiry, LocalDateTime.now());
    }
    
    public void logHierarchyAssignment(String adminUserId, String salesRepId, String managerId, String territory) {
        log.info("AUDIT: Hierarchy ASSIGNED - Admin: {}, SalesRep: {}, Manager: {}, Territory: {}, Time: {}", 
            adminUserId, salesRepId, managerId, territory, LocalDateTime.now());
    }
    
    public void logHierarchyRemoval(String adminUserId, String salesRepId, String managerId, String reason) {
        log.info("AUDIT: Hierarchy REMOVED - Admin: {}, SalesRep: {}, Manager: {}, Reason: {}, Time: {}", 
            adminUserId, salesRepId, managerId, reason, LocalDateTime.now());
    }
    
    public void logTerritoryUpdate(String adminUserId, String salesRepId, String managerId, 
                                 String oldTerritory, String newTerritory) {
        log.info("AUDIT: Territory UPDATED - Admin: {}, SalesRep: {}, Manager: {}, Old: {}, New: {}, Time: {}", 
            adminUserId, salesRepId, managerId, oldTerritory, newTerritory, LocalDateTime.now());
    }
    
    public void logUserInvitation(String adminUserId, String email, String role, String token) {
        log.info("AUDIT: User INVITED - Admin: {}, Email: {}, Role: {}, Token: {}, Time: {}", 
            adminUserId, email, role, token, LocalDateTime.now());
    }
    
    public void logInvitationUsed(String token, String email, String registeredUserId) {
        log.info("AUDIT: Invitation USED - Token: {}, Email: {}, RegisteredUser: {}, Time: {}", 
            token, email, registeredUserId, LocalDateTime.now());
    }
    
    public void logUserRegistration(String userId, String email, String role) {
        log.info("AUDIT: User REGISTERED - User: {}, Email: {}, Role: {}, Time: {}", 
            userId, email, role, LocalDateTime.now());
    }
    
    public void logUserLogin(String userId, String email, boolean success) {
        log.info("AUDIT: User LOGIN {} - User: {}, Email: {}, Time: {}", 
            success ? "SUCCESS" : "FAILED", userId, email, LocalDateTime.now());
    }
    
    public void logAccountLocked(String userId, String email, int failedAttempts) {
        log.warn("AUDIT: Account LOCKED - User: {}, Email: {}, FailedAttempts: {}, Time: {}", 
            userId, email, failedAttempts, LocalDateTime.now());
    }
    
    public void logPasswordChange(String userId, String email) {
        log.info("AUDIT: Password CHANGED - User: {}, Email: {}, Time: {}", 
            userId, email, LocalDateTime.now());
    }
}