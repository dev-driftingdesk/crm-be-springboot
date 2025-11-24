package com.ceedpods.crmbuild.service.user;

import com.ceedpods.crmbuild.entity.user.User;
import com.ceedpods.crmbuild.entity.UserRelationship;
import com.ceedpods.crmbuild.enums.UserRole;
import com.ceedpods.crmbuild.repository.UserRelationshipRepository;
import com.ceedpods.crmbuild.service.auditLogService.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserHierarchyService {
    
    private final UserRelationshipRepository userRelationshipRepository;
    private final UserService userService;
    private final AuditService auditService;
    
    @Transactional
    public UserRelationship assignSalesRepToManager(String adminUserId, String salesRepId, 
                                                   String managerId, String territory, String notes) {
        // Validate users
        User admin = userService.findByKeycloakId(adminUserId);
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only admins can assign sales reps to managers");
        }
        
        User salesRep = userService.findByKeycloakId(salesRepId);
        if (salesRep == null || salesRep.getRole() != UserRole.SALES_EXECUTIVE) {
            throw new RuntimeException("Invalid sales rep user");
        }
        
        User manager = userService.findByKeycloakId(managerId);
        if (manager == null || !manager.isManager()) {
            throw new RuntimeException("Invalid manager user");
        }
        
        // Check if sales rep already has an active manager
        Optional<UserRelationship> existingRelationship = 
            userRelationshipRepository.findActiveManagerBySalesRepId(salesRepId);
        
        if (existingRelationship.isPresent()) {
            UserRelationship existing = existingRelationship.get();
            if (existing.getManagerId().equals(managerId)) {
                throw new RuntimeException("Sales rep is already assigned to this manager");
            } else {
                // End existing relationship
                existing.endRelationship(adminUserId, "Reassigned to new manager");
                // Audit fields automatically handled by Spring Data Auditing
                userRelationshipRepository.save(existing);
                
                log.info("Ended existing relationship between sales rep {} and manager {}", 
                    salesRepId, existing.getManagerId());
            }
        }
        
        // Create new relationship
        UserRelationship relationship = UserRelationship.builder()
            .salesRepId(salesRepId)
            .managerId(managerId)
            .assignedBy(adminUserId)
            .assignedAt(LocalDateTime.now())
            .relationshipStartDate(LocalDateTime.now())
            .territory(territory)
            .notes(notes)
            .active(true)
            .build();
        
        // Audit fields automatically handled by Spring Data Auditing
        UserRelationship saved = userRelationshipRepository.save(relationship);
        
        // Update user entities
        salesRep.setManagerId(managerId);
        salesRep.setTerritory(territory);
        // Audit fields automatically handled by Spring Data Auditing
        userService.save(salesRep);
        
        // Update manager's report list
        manager.getReportIds().add(salesRepId);
        // Audit fields automatically handled by Spring Data Auditing
        userService.save(manager);
        
        // Audit log
        auditService.logHierarchyAssignment(adminUserId, salesRepId, managerId, territory);
        
        log.info("Assigned sales rep {} to manager {} by {} in territory {}", 
            salesRepId, managerId, adminUserId, territory);
        
        return saved;
    }
    
    @Transactional
    public void removeSalesRepFromManager(String adminUserId, String salesRepId, String reason) {
        // Validate admin
        User admin = userService.findByKeycloakId(adminUserId);
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only admins can remove sales rep assignments");
        }
        
        // Find active relationship
        Optional<UserRelationship> relationshipOpt = 
            userRelationshipRepository.findActiveManagerBySalesRepId(salesRepId);
        
        if (relationshipOpt.isEmpty()) {
            throw new RuntimeException("No active manager assignment found for sales rep");
        }
        
        UserRelationship relationship = relationshipOpt.get();
        String managerId = relationship.getManagerId();
        
        // End relationship
        relationship.endRelationship(adminUserId, reason);
        // Audit fields automatically handled by Spring Data Auditing
        userRelationshipRepository.save(relationship);
        
        // Update user entities
        User salesRep = userService.findByKeycloakId(salesRepId);
        if (salesRep != null) {
            salesRep.setManagerId(null);
            // Audit fields automatically handled by Spring Data Auditing
            userService.save(salesRep);
        }
        
        User manager = userService.findByKeycloakId(managerId);
        if (manager != null && manager.getReportIds() != null) {
            manager.getReportIds().remove(salesRepId);
            // Audit fields automatically handled by Spring Data Auditing
            userService.save(manager);
        }
        
        // Audit log
        auditService.logHierarchyRemoval(adminUserId, salesRepId, managerId, reason);
        
        log.info("Removed sales rep {} from manager {} by {} - Reason: {}", 
            salesRepId, managerId, adminUserId, reason);
    }
    
    public boolean isUserInHierarchy(String targetUserId, String managerUserId) {
        // Check if targetUserId is directly managed by managerUserId
        Optional<UserRelationship> relationship = 
            userRelationshipRepository.findActiveManagerBySalesRepId(targetUserId);
        
        return relationship.isPresent() && relationship.get().getManagerId().equals(managerUserId);
    }
    
    public boolean isOwnRecord(String targetUserId, String currentUserId) {
        return targetUserId.equals(currentUserId);
    }
    
    public List<User> getManagerReports(String managerId) {
        List<UserRelationship> relationships = 
            userRelationshipRepository.findActiveReportsByManagerId(managerId);
        
        return relationships.stream()
            .map(relationship -> userService.findByKeycloakId(relationship.getSalesRepId()))
            .filter(user -> user != null)
            .collect(Collectors.toList());
    }
    
    public Optional<User> getSalesRepManager(String salesRepId) {
        Optional<UserRelationship> relationship = 
            userRelationshipRepository.findActiveManagerBySalesRepId(salesRepId);
        
        if (relationship.isPresent()) {
            return Optional.ofNullable(userService.findByKeycloakId(relationship.get().getManagerId()));
        }
        
        return Optional.empty();
    }
    
    public List<UserRelationship> getManagerRelationships(String managerId) {
        return userRelationshipRepository.findActiveReportsByManagerId(managerId);
    }
    
    public List<UserRelationship> getSalesRepRelationshipHistory(String salesRepId) {
        return userRelationshipRepository.findAllRelationshipsBySalesRepId(salesRepId);
    }
    
    public List<UserRelationship> getRelationshipsByTerritory(String territory) {
        return userRelationshipRepository.findActiveRelationshipsByTerritory(territory);
    }
    
    public long getManagerReportCount(String managerId) {
        return userRelationshipRepository.countActiveReportsByManagerId(managerId);
    }
    
    public boolean hasActiveRelationship(String salesRepId, String managerId) {
        return userRelationshipRepository.existsBySalesRepIdAndManagerIdAndActiveTrue(salesRepId, managerId);
    }
    
    @Transactional
    public UserRelationship updateRelationshipTerritory(String relationshipId, String newTerritory, String updatedBy) {
        UserRelationship relationship = userRelationshipRepository.findById(relationshipId)
            .orElseThrow(() -> new RuntimeException("Relationship not found: " + relationshipId));
        
        String oldTerritory = relationship.getTerritory();
        relationship.setTerritory(newTerritory);
        // Audit fields automatically handled by Spring Data Auditing
        
        UserRelationship saved = userRelationshipRepository.save(relationship);
        
        // Update sales rep territory
        User salesRep = userService.findByKeycloakId(relationship.getSalesRepId());
        if (salesRep != null) {
            salesRep.setTerritory(newTerritory);
            // Audit fields automatically handled by Spring Data Auditing
            userService.save(salesRep);
        }
        
        auditService.logTerritoryUpdate(updatedBy, relationship.getSalesRepId(), 
            relationship.getManagerId(), oldTerritory, newTerritory);
        
        log.info("Updated territory for relationship {} from {} to {} by {}", 
            relationshipId, oldTerritory, newTerritory, updatedBy);
        
        return saved;
    }
    
    public List<User> getUnassignedSalesReps() {
        List<User> allSalesReps = userService.getUsersByRole(UserRole.SALES_EXECUTIVE);
        List<String> assignedSalesRepIds = userRelationshipRepository
            .findAll()
            .stream()
            .filter(UserRelationship::isActive)
            .map(UserRelationship::getSalesRepId)
            .collect(Collectors.toList());
        
        return allSalesReps.stream()
            .filter(user -> !assignedSalesRepIds.contains(user.getKeycloakId()))
            .collect(Collectors.toList());
    }
    
    public List<User> getAvailableManagers() {
        return userService.getUsersByRole(UserRole.MANAGER)
            .stream()
            .filter(User::isEnabled)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public void cleanupInactiveRelationships() {
        // This method can be used to clean up relationships for deleted/inactive users
        List<UserRelationship> activeRelationships = userRelationshipRepository
            .findAll()
            .stream()
            .filter(UserRelationship::isActive)
            .collect(Collectors.toList());
        
        for (UserRelationship relationship : activeRelationships) {
            User manager = userService.findByKeycloakId(relationship.getManagerId());
            User salesRep = userService.findByKeycloakId(relationship.getSalesRepId());
            
            if (manager == null || !manager.isEnabled() || salesRep == null || !salesRep.isEnabled()) {
                relationship.endRelationship("SYSTEM", "User account inactive or deleted");
                // Audit fields automatically handled by Spring Data Auditing
                userRelationshipRepository.save(relationship);
                
                log.info("Cleaned up inactive relationship: {}", relationship.getId());
            }
        }
    }
}