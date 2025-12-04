package com.ceedpods.crmbuild.controller.rbac;

import com.ceedpods.crmbuild.enums.UserRole;
import com.ceedpods.crmbuild.enums.Permission;
import com.ceedpods.crmbuild.enums.PermissionScope;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for RBAC endpoints
 * Note: These tests assume authentication bypass for testing purposes
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class RBACControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testGetAllPermissions() throws Exception {
        setUp();
        
        mockMvc.perform(get("/rbac/permissions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(Permission.values().length));
    }

    @Test
    void testGetAllScopes() throws Exception {
        setUp();
        
        mockMvc.perform(get("/rbac/scopes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(PermissionScope.values().length));
    }

    @Test 
    void testGetRolePermissions() throws Exception {
        setUp();
        
        mockMvc.perform(get("/rbac/roles/{role}/permissions", UserRole.ADMIN.name())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testCheckRolePermission() throws Exception {
        setUp();
        
        mockMvc.perform(get("/rbac/roles/{role}/permissions/check", UserRole.ADMIN.name())
                .param("permission", Permission.LEAD_VIEW.getCode())
                .param("scope", PermissionScope.ALL.name())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasPermission").value(true))
                .andExpect(jsonPath("$.grantedScope").value(PermissionScope.ALL.getCode()));
    }


    @Test
    void testUpdateRolePermission() throws Exception {
        setUp();
        
        RBACController.UpdateRolePermissionRequest request = new RBACController.UpdateRolePermissionRequest();
        request.setScope(PermissionScope.TEAM);
        request.setEnabled(true);
        
        String requestJson = objectMapper.writeValueAsString(request);
        
        mockMvc.perform(put("/rbac/roles/{role}/permissions/{permission}", 
                        UserRole.MANAGER.name(), Permission.LEAD_VIEW.getCode())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value(PermissionScope.TEAM.getCode()))
                .andExpect(jsonPath("$.enabled").value(true));
    }
}