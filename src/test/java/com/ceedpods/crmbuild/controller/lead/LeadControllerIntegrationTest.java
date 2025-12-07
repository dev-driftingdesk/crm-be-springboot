package com.ceedpods.crmbuild.controller.lead;

import com.ceedpods.crmbuild.dto.lead.LeadDTO;
import com.ceedpods.crmbuild.dto.request.CreateLeadRequest;
import com.ceedpods.crmbuild.dto.request.UpdateLeadRequest;
import com.ceedpods.crmbuild.security.CustomPermissionEvaluator;
import com.ceedpods.crmbuild.service.lead.LeadService;
import com.ceedpods.crmbuild.util.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for LeadController.
 * Tests the full request/response cycle with Spring Security context.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("LeadController Integration Tests")
class LeadControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LeadService leadService;

    @MockBean
    private CustomPermissionEvaluator permissionEvaluator;

    private LeadDTO testLeadDTO;
    private CreateLeadRequest createRequest;
    private UpdateLeadRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Allow all permissions for tests - method signature: hasPermission(Authentication, String)
        when(permissionEvaluator.hasPermission(any(), anyString())).thenReturn(true);

        testLeadDTO = TestDataFactory.createTestLeadDTO();
        createRequest = TestDataFactory.createTestCreateLeadRequest();
        updateRequest = TestDataFactory.createTestUpdateLeadRequest();
    }

    // ===================== createLead Integration Tests =====================

    @Nested
    @DisplayName("POST /leads - createLead Integration")
    class CreateLeadIntegrationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should create lead when authenticated as admin")
        void createLead_WithAdminUser_ReturnsCreated() throws Exception {
            // Given
            when(leadService.createLead(any(CreateLeadRequest.class), any(Authentication.class)))
                .thenReturn(testLeadDTO);

            // When & Then
            mockMvc.perform(post("/leads")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists());

            verify(leadService).createLead(any(CreateLeadRequest.class), any(Authentication.class));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void createLead_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(post("/leads")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());

            verify(leadService, never()).createLead(any(), any());
        }
    }

    // ===================== getAllLeads Integration Tests =====================

    @Nested
    @DisplayName("GET /leads - getAllLeads Integration")
    class GetAllLeadsIntegrationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return all leads when authenticated")
        void getAllLeads_WithAuthenticatedUser_ReturnsOk() throws Exception {
            // Given
            List<LeadDTO> leads = Arrays.asList(testLeadDTO);
            when(leadService.getAllLeads()).thenReturn(leads);

            // When & Then
            mockMvc.perform(get("/leads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(testLeadDTO.getId()));

            verify(leadService).getAllLeads();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return empty list when no leads exist")
        void getAllLeads_NoLeads_ReturnsEmptyList() throws Exception {
            // Given
            when(leadService.getAllLeads()).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/leads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getAllLeads_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/leads"))
                .andExpect(status().isUnauthorized());

            verify(leadService, never()).getAllLeads();
        }
    }

    // ===================== getLeadById Integration Tests =====================

    @Nested
    @DisplayName("GET /leads/{id} - getLeadById Integration")
    class GetLeadByIdIntegrationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return lead when valid ID provided")
        void getLeadById_WithValidId_ReturnsLead() throws Exception {
            // Given
            String leadId = TestDataFactory.TEST_LEAD_ID;
            when(leadService.getLeadById(leadId)).thenReturn(testLeadDTO);

            // When & Then
            mockMvc.perform(get("/leads/{id}", leadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testLeadDTO.getId()));

            verify(leadService).getLeadById(leadId);
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getLeadById_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/leads/{id}", TestDataFactory.TEST_LEAD_ID))
                .andExpect(status().isUnauthorized());

            verify(leadService, never()).getLeadById(any());
        }
    }

    // ===================== updateLead Integration Tests =====================

    @Nested
    @DisplayName("PUT /leads/{id} - updateLead Integration")
    class UpdateLeadIntegrationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should update lead when authenticated as admin")
        void updateLead_WithAdminUser_ReturnsOk() throws Exception {
            // Given
            String leadId = TestDataFactory.TEST_LEAD_ID;
            when(leadService.updateLead(eq(leadId), any(UpdateLeadRequest.class), any(Authentication.class)))
                .thenReturn(testLeadDTO);

            // When & Then
            mockMvc.perform(put("/leads/{id}", leadId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

            verify(leadService).updateLead(eq(leadId), any(UpdateLeadRequest.class), any(Authentication.class));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void updateLead_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(put("/leads/{id}", TestDataFactory.TEST_LEAD_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());

            verify(leadService, never()).updateLead(any(), any(), any());
        }
    }

    // ===================== deleteLead Integration Tests =====================

    @Nested
    @DisplayName("DELETE /leads/{id} - deleteLead Integration")
    class DeleteLeadIntegrationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should delete lead when authenticated as admin")
        void deleteLead_WithAdminUser_ReturnsOk() throws Exception {
            // Given
            String leadId = TestDataFactory.TEST_LEAD_ID;
            doNothing().when(leadService).deleteLead(eq(leadId), any(Authentication.class));

            // When & Then
            mockMvc.perform(delete("/leads/{id}", leadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

            verify(leadService).deleteLead(eq(leadId), any(Authentication.class));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void deleteLead_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(delete("/leads/{id}", TestDataFactory.TEST_LEAD_ID))
                .andExpect(status().isUnauthorized());

            verify(leadService, never()).deleteLead(any(), any());
        }
    }

    // ===================== searchLeads Integration Tests =====================

    @Nested
    @DisplayName("GET /leads/search - searchLeads Integration")
    class SearchLeadsIntegrationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should search leads when authenticated")
        void searchLeads_WithAuthenticatedUser_ReturnsResults() throws Exception {
            // Given
            String query = "test";
            List<LeadDTO> leads = Arrays.asList(testLeadDTO);
            when(leadService.searchLeads(query)).thenReturn(leads);

            // When & Then
            mockMvc.perform(get("/leads/search")
                    .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

            verify(leadService).searchLeads(query);
        }
    }

    // ===================== getLeadCount Integration Tests =====================

    @Nested
    @DisplayName("GET /leads/count - getLeadCount Integration")
    class GetLeadCountIntegrationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return lead count when authenticated")
        void getLeadCount_WithAuthenticatedUser_ReturnsCount() throws Exception {
            // Given
            when(leadService.getTotalLeadCount()).thenReturn(10L);

            // When & Then
            mockMvc.perform(get("/leads/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(10));

            verify(leadService).getTotalLeadCount();
        }
    }
}
