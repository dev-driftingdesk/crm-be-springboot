package com.ceedpods.crmbuild.controller.lead;

import com.ceedpods.crmbuild.dto.lead.LeadDTO;
import com.ceedpods.crmbuild.dto.request.CreateLeadRequest;
import com.ceedpods.crmbuild.dto.request.UpdateLeadRequest;
import com.ceedpods.crmbuild.exception.BadRequestException;
import com.ceedpods.crmbuild.exception.ForbiddenException;
import com.ceedpods.crmbuild.exception.ResourceNotFoundException;
import com.ceedpods.crmbuild.security.CustomPermissionEvaluator;
import com.ceedpods.crmbuild.service.lead.LeadService;
import com.ceedpods.crmbuild.util.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeadController Unit Tests")
class LeadControllerTest {

    @Mock
    private LeadService leadService;

    @Mock
    private CustomPermissionEvaluator permissionEvaluator;

    @InjectMocks
    private LeadController leadController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private LeadDTO testLeadDTO;
    private CreateLeadRequest createRequest;
    private UpdateLeadRequest updateRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(leadController)
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();

        testLeadDTO = TestDataFactory.createTestLeadDTO();
        createRequest = TestDataFactory.createTestCreateLeadRequest();
        updateRequest = TestDataFactory.createTestUpdateLeadRequest();
    }

    // ===================== createLead Endpoint Tests =====================

    @Nested
    @DisplayName("POST /leads - createLead")
    class CreateLeadEndpointTests {

        @Test
        @DisplayName("Should return 201 Created when lead created successfully")
        void createLead_WithValidRequest_Returns201Created() throws Exception {
            // Given
            when(leadService.createLead(any(CreateLeadRequest.class), any()))
                .thenReturn(testLeadDTO);

            // When & Then
            mockMvc.perform(post("/leads")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lead created successfully"))
                .andExpect(jsonPath("$.data.id").value(testLeadDTO.getId()));

            verify(leadService).createLead(any(CreateLeadRequest.class), any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when service throws exception")
        void createLead_ServiceThrowsException_Returns400BadRequest() throws Exception {
            // Given
            when(leadService.createLead(any(CreateLeadRequest.class), any()))
                .thenThrow(new BadRequestException("Lead name already exists"));

            // When & Then
            mockMvc.perform(post("/leads")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

            verify(leadService).createLead(any(CreateLeadRequest.class), any());
        }

        @Test
        @DisplayName("Should return 400 when forbidden exception occurs")
        void createLead_ForbiddenException_Returns400() throws Exception {
            // Given
            when(leadService.createLead(any(CreateLeadRequest.class), any()))
                .thenThrow(new ForbiddenException("Only admin users can create leads"));

            // When & Then
            mockMvc.perform(post("/leads")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ===================== getAllLeads Endpoint Tests =====================

    @Nested
    @DisplayName("GET /leads - getAllLeads")
    class GetAllLeadsEndpointTests {

        @Test
        @DisplayName("Should return 200 OK with lead list")
        void getAllLeads_ReturnsOkWithLeadList() throws Exception {
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
        @DisplayName("Should return 200 OK with empty list when no leads")
        void getAllLeads_NoLeads_ReturnsEmptyList() throws Exception {
            // Given
            when(leadService.getAllLeads()).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/leads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());

            verify(leadService).getAllLeads();
        }

        @Test
        @DisplayName("Should return 400 Bad Request when service throws exception")
        void getAllLeads_ServiceThrowsException_Returns400BadRequest() throws Exception {
            // Given
            when(leadService.getAllLeads()).thenThrow(new RuntimeException("Database error"));

            // When & Then
            mockMvc.perform(get("/leads"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

            verify(leadService).getAllLeads();
        }
    }

    // ===================== getLeadById Endpoint Tests =====================

    @Nested
    @DisplayName("GET /leads/{id} - getLeadById")
    class GetLeadByIdEndpointTests {

        @Test
        @DisplayName("Should return 200 OK with lead when valid ID")
        void getLeadById_WithValidId_ReturnsOkWithLead() throws Exception {
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
        @DisplayName("Should return 400 Bad Request when lead not found")
        void getLeadById_WithInvalidId_Returns400BadRequest() throws Exception {
            // Given
            String invalidId = "invalid-id";
            when(leadService.getLeadById(invalidId))
                .thenThrow(new ResourceNotFoundException("Lead not found with UUID: " + invalidId));

            // When & Then
            mockMvc.perform(get("/leads/{id}", invalidId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

            verify(leadService).getLeadById(invalidId);
        }
    }

    // ===================== updateLead Endpoint Tests =====================

    @Nested
    @DisplayName("PUT /leads/{id} - updateLead")
    class UpdateLeadEndpointTests {

        @Test
        @DisplayName("Should return 200 OK with updated lead")
        void updateLead_WithValidRequest_ReturnsOkWithUpdatedLead() throws Exception {
            // Given
            String leadId = TestDataFactory.TEST_LEAD_ID;
            when(leadService.updateLead(eq(leadId), any(UpdateLeadRequest.class), any()))
                .thenReturn(testLeadDTO);

            // When & Then
            mockMvc.perform(put("/leads/{id}", leadId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lead updated successfully"));

            verify(leadService).updateLead(eq(leadId), any(UpdateLeadRequest.class), any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when service throws exception")
        void updateLead_ServiceThrowsException_Returns400BadRequest() throws Exception {
            // Given
            String leadId = TestDataFactory.TEST_LEAD_ID;
            when(leadService.updateLead(eq(leadId), any(UpdateLeadRequest.class), any()))
                .thenThrow(new BadRequestException("Lead name already exists"));

            // When & Then
            mockMvc.perform(put("/leads/{id}", leadId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

            verify(leadService).updateLead(eq(leadId), any(UpdateLeadRequest.class), any());
        }

        @Test
        @DisplayName("Should return 400 when lead not found")
        void updateLead_LeadNotFound_Returns400() throws Exception {
            // Given
            String invalidId = "invalid-id";
            when(leadService.updateLead(eq(invalidId), any(UpdateLeadRequest.class), any()))
                .thenThrow(new ResourceNotFoundException("Lead not found"));

            // When & Then
            mockMvc.perform(put("/leads/{id}", invalidId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ===================== deleteLead Endpoint Tests =====================

    @Nested
    @DisplayName("DELETE /leads/{id} - deleteLead")
    class DeleteLeadEndpointTests {

        @Test
        @DisplayName("Should return 200 OK with success message")
        void deleteLead_WithValidId_ReturnsOkWithSuccessMessage() throws Exception {
            // Given
            String leadId = TestDataFactory.TEST_LEAD_ID;
            doNothing().when(leadService).deleteLead(eq(leadId), any());

            // When & Then
            mockMvc.perform(delete("/leads/{id}", leadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lead deleted successfully"));

            verify(leadService).deleteLead(eq(leadId), any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when service throws exception")
        void deleteLead_ServiceThrowsException_Returns400BadRequest() throws Exception {
            // Given
            String leadId = TestDataFactory.TEST_LEAD_ID;
            doThrow(new ResourceNotFoundException("Lead not found"))
                .when(leadService).deleteLead(eq(leadId), any());

            // When & Then
            mockMvc.perform(delete("/leads/{id}", leadId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

            verify(leadService).deleteLead(eq(leadId), any());
        }
    }

    // ===================== searchLeads Endpoint Tests =====================

    @Nested
    @DisplayName("GET /leads/search - searchLeads")
    class SearchLeadsEndpointTests {

        @Test
        @DisplayName("Should return 200 OK with matching leads")
        void searchLeads_WithValidQuery_ReturnsOkWithResults() throws Exception {
            // Given
            String query = "test";
            List<LeadDTO> leads = Arrays.asList(testLeadDTO);
            when(leadService.searchLeads(query)).thenReturn(leads);

            // When & Then
            mockMvc.perform(get("/leads/search")
                    .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(testLeadDTO.getId()));

            verify(leadService).searchLeads(query);
        }

        @Test
        @DisplayName("Should return 200 OK with empty list when no matches")
        void searchLeads_NoMatches_ReturnsEmptyList() throws Exception {
            // Given
            String query = "nonexistent";
            when(leadService.searchLeads(query)).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/leads/search")
                    .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());

            verify(leadService).searchLeads(query);
        }

        @Test
        @DisplayName("Should return 400 Bad Request when service throws exception")
        void searchLeads_ServiceThrowsException_Returns400BadRequest() throws Exception {
            // Given
            String query = "test";
            when(leadService.searchLeads(query)).thenThrow(new RuntimeException("Search error"));

            // When & Then
            mockMvc.perform(get("/leads/search")
                    .param("query", query))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

            verify(leadService).searchLeads(query);
        }
    }

    // ===================== getLeadCount Endpoint Tests =====================

    @Nested
    @DisplayName("GET /leads/count - getLeadCount")
    class GetLeadCountEndpointTests {

        @Test
        @DisplayName("Should return 200 OK with count")
        void getLeadCount_ReturnsOkWithCount() throws Exception {
            // Given
            long count = 10L;
            when(leadService.getTotalLeadCount()).thenReturn(count);

            // When & Then
            mockMvc.perform(get("/leads/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(10));

            verify(leadService).getTotalLeadCount();
        }

        @Test
        @DisplayName("Should return 400 Bad Request when service throws exception")
        void getLeadCount_ServiceThrowsException_Returns400BadRequest() throws Exception {
            // Given
            when(leadService.getTotalLeadCount()).thenThrow(new RuntimeException("Count error"));

            // When & Then
            mockMvc.perform(get("/leads/count"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

            verify(leadService).getTotalLeadCount();
        }
    }

    // ===================== getLeadsByDealId Endpoint Tests =====================

    @Nested
    @DisplayName("GET /leads/deal/{dealId} - getLeadsByDealId")
    class GetLeadsByDealIdEndpointTests {

        @Test
        @DisplayName("Should return 200 OK with leads for deal")
        void getLeadsByDealId_WithValidDealId_ReturnsOkWithLeads() throws Exception {
            // Given
            String dealId = TestDataFactory.TEST_DEAL_ID;
            List<LeadDTO> leads = Arrays.asList(testLeadDTO);
            when(leadService.getLeadsByDealId(dealId)).thenReturn(leads);

            // When & Then
            mockMvc.perform(get("/leads/deal/{dealId}", dealId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(testLeadDTO.getId()));

            verify(leadService).getLeadsByDealId(dealId);
        }

        @Test
        @DisplayName("Should return 400 Bad Request when service throws exception")
        void getLeadsByDealId_ServiceThrowsException_Returns400BadRequest() throws Exception {
            // Given
            String dealId = TestDataFactory.TEST_DEAL_ID;
            when(leadService.getLeadsByDealId(dealId)).thenThrow(new RuntimeException("Error"));

            // When & Then
            mockMvc.perform(get("/leads/deal/{dealId}", dealId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

            verify(leadService).getLeadsByDealId(dealId);
        }
    }
}
