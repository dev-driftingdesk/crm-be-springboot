package com.ceedpods.crmbuild.service.permission;

import com.ceedpods.crmbuild.entity.UserRolePermission;
import com.ceedpods.crmbuild.enums.Permission;
import com.ceedpods.crmbuild.enums.PermissionScope;
import com.ceedpods.crmbuild.enums.UserRole;
import com.ceedpods.crmbuild.repository.UserRolePermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RBACServiceTest {

    @Mock
    private UserRolePermissionRepository rolePermissionRepository;

    @InjectMocks
    private RBACService rbacService;

    private UserRolePermission adminLeadViewPermission;
    private UserRolePermission managerLeadViewPermission;
    private UserRolePermission salesRepLeadViewPermission;
    private UserRolePermission viewerLeadViewPermission;

    @BeforeEach
    void setUp() {
        // Admin has ALL scope for LEAD_VIEW
        adminLeadViewPermission = UserRolePermission.builder()
            .role(UserRole.ADMIN)
            .permission(Permission.LEAD_VIEW)
            .scope(PermissionScope.ALL)
            .enabled(true)
            .build();

        // Manager has TEAM scope for LEAD_VIEW
        managerLeadViewPermission = UserRolePermission.builder()
            .role(UserRole.MANAGER)
            .permission(Permission.LEAD_VIEW)
            .scope(PermissionScope.TEAM)
            .enabled(true)
            .build();

        // Sales Rep has OWN scope for LEAD_VIEW
        salesRepLeadViewPermission = UserRolePermission.builder()
            .role(UserRole.SALES_REP)
            .permission(Permission.LEAD_VIEW)
            .scope(PermissionScope.OWN)
            .enabled(true)
            .build();

        // Viewer has AS_PERMITTED scope for LEAD_VIEW
        viewerLeadViewPermission = UserRolePermission.builder()
            .role(UserRole.VIEWER)
            .permission(Permission.LEAD_VIEW)
            .scope(PermissionScope.AS_PERMITTED)
            .enabled(true)
            .build();
    }

    @Test
    void testAdminHasAllScopePermissions() {
        // Given
        when(rolePermissionRepository.findEnabledByRoleAndPermission(UserRole.ADMIN, Permission.LEAD_VIEW))
            .thenReturn(Optional.of(adminLeadViewPermission));

        // When & Then
        assertTrue(rbacService.hasPermission(UserRole.ADMIN, Permission.LEAD_VIEW, PermissionScope.ALL));
        assertTrue(rbacService.hasPermission(UserRole.ADMIN, Permission.LEAD_VIEW, PermissionScope.TEAM));
        assertTrue(rbacService.hasPermission(UserRole.ADMIN, Permission.LEAD_VIEW, PermissionScope.OWN));
    }

    @Test
    void testManagerHasTeamScopePermissions() {
        // Given
        when(rolePermissionRepository.findEnabledByRoleAndPermission(UserRole.MANAGER, Permission.LEAD_VIEW))
            .thenReturn(Optional.of(managerLeadViewPermission));

        // When & Then
        assertFalse(rbacService.hasPermission(UserRole.MANAGER, Permission.LEAD_VIEW, PermissionScope.ALL));
        assertTrue(rbacService.hasPermission(UserRole.MANAGER, Permission.LEAD_VIEW, PermissionScope.TEAM));
        assertTrue(rbacService.hasPermission(UserRole.MANAGER, Permission.LEAD_VIEW, PermissionScope.OWN));
    }

    @Test
    void testSalesRepHasOwnScopePermissions() {
        // Given
        when(rolePermissionRepository.findEnabledByRoleAndPermission(UserRole.SALES_REP, Permission.LEAD_VIEW))
            .thenReturn(Optional.of(salesRepLeadViewPermission));

        // When & Then
        assertFalse(rbacService.hasPermission(UserRole.SALES_REP, Permission.LEAD_VIEW, PermissionScope.ALL));
        assertFalse(rbacService.hasPermission(UserRole.SALES_REP, Permission.LEAD_VIEW, PermissionScope.TEAM));
        assertTrue(rbacService.hasPermission(UserRole.SALES_REP, Permission.LEAD_VIEW, PermissionScope.OWN));
    }

    @Test
    void testViewerHasAsPermittedScope() {
        // Given
        when(rolePermissionRepository.findEnabledByRoleAndPermission(UserRole.VIEWER, Permission.LEAD_VIEW))
            .thenReturn(Optional.of(viewerLeadViewPermission));

        // When & Then
        assertFalse(rbacService.hasPermission(UserRole.VIEWER, Permission.LEAD_VIEW, PermissionScope.ALL));
        assertFalse(rbacService.hasPermission(UserRole.VIEWER, Permission.LEAD_VIEW, PermissionScope.TEAM));
        assertFalse(rbacService.hasPermission(UserRole.VIEWER, Permission.LEAD_VIEW, PermissionScope.OWN));
        // AS_PERMITTED scope needs special handling, so for now it should be false for basic scope checks
    }

    @Test
    void testGetPermissionScopeReturnsCorrectScope() {
        // Given
        when(rolePermissionRepository.findEnabledByRoleAndPermission(UserRole.MANAGER, Permission.LEAD_VIEW))
            .thenReturn(Optional.of(managerLeadViewPermission));

        // When
        PermissionScope scope = rbacService.getPermissionScope(UserRole.MANAGER, Permission.LEAD_VIEW);

        // Then
        assertEquals(PermissionScope.TEAM, scope);
    }

    @Test
    void testGetPermissionScopeReturnsNoneForMissingPermission() {
        // Given
        when(rolePermissionRepository.findEnabledByRoleAndPermission(any(), any()))
            .thenReturn(Optional.empty());

        // When
        PermissionScope scope = rbacService.getPermissionScope(UserRole.SALES_REP, Permission.USER_DELETE);

        // Then
        assertEquals(PermissionScope.NONE, scope);
    }

    @Test
    void testUpdateRolePermissionScope() {
        // Given
        UserRolePermission existingPermission = UserRolePermission.builder()
            .role(UserRole.MANAGER)
            .permission(Permission.LEAD_EDIT)
            .scope(PermissionScope.OWN)
            .enabled(true)
            .build();

        when(rolePermissionRepository.findByRoleAndPermission(UserRole.MANAGER, Permission.LEAD_EDIT))
            .thenReturn(Optional.of(existingPermission));
        when(rolePermissionRepository.save(any())).thenReturn(existingPermission);

        // When
        UserRolePermission updated = rbacService.updateRolePermissionScope(
            UserRole.MANAGER, Permission.LEAD_EDIT, PermissionScope.TEAM, true);

        // Then
        assertNotNull(updated);
        assertEquals(PermissionScope.TEAM, updated.getScope());
        assertTrue(updated.isEnabled());
        verify(rolePermissionRepository).save(existingPermission);
    }

    @Test
    void testRoleHierarchy() {
        // Test role hierarchy methods
        assertTrue(UserRole.ADMIN.canManage(UserRole.MANAGER));
        assertTrue(UserRole.ADMIN.canManage(UserRole.SALES_REP));
        assertTrue(UserRole.ADMIN.canManage(UserRole.VIEWER));
        
        assertTrue(UserRole.MANAGER.canManage(UserRole.SALES_REP));
        assertTrue(UserRole.MANAGER.canManage(UserRole.VIEWER));
        assertFalse(UserRole.MANAGER.canManage(UserRole.ADMIN));
        
        assertFalse(UserRole.SALES_REP.canManage(UserRole.ADMIN));
        assertFalse(UserRole.SALES_REP.canManage(UserRole.MANAGER));
        assertFalse(UserRole.SALES_REP.canManage(UserRole.VIEWER));
        
        assertFalse(UserRole.VIEWER.canManage(UserRole.ADMIN));
        assertFalse(UserRole.VIEWER.canManage(UserRole.MANAGER));
        assertFalse(UserRole.VIEWER.canManage(UserRole.SALES_REP));
    }

    @Test
    void testPermissionScopeInclusion() {
        // Test scope inclusion logic
        assertTrue(PermissionScope.ALL.includes(PermissionScope.ALL));
        assertTrue(PermissionScope.ALL.includes(PermissionScope.TEAM));
        assertTrue(PermissionScope.ALL.includes(PermissionScope.OWN));
        
        assertFalse(PermissionScope.TEAM.includes(PermissionScope.ALL));
        assertTrue(PermissionScope.TEAM.includes(PermissionScope.TEAM));
        assertTrue(PermissionScope.TEAM.includes(PermissionScope.OWN));
        
        assertFalse(PermissionScope.OWN.includes(PermissionScope.ALL));
        assertFalse(PermissionScope.OWN.includes(PermissionScope.TEAM));
        assertTrue(PermissionScope.OWN.includes(PermissionScope.OWN));
        
        assertFalse(PermissionScope.NONE.includes(PermissionScope.ALL));
        assertFalse(PermissionScope.NONE.includes(PermissionScope.TEAM));
        assertFalse(PermissionScope.NONE.includes(PermissionScope.OWN));
        
        // AS_PERMITTED needs explicit configuration
        assertFalse(PermissionScope.AS_PERMITTED.includes(PermissionScope.ALL));
        assertFalse(PermissionScope.AS_PERMITTED.includes(PermissionScope.TEAM));
        assertFalse(PermissionScope.AS_PERMITTED.includes(PermissionScope.OWN));
    }
}