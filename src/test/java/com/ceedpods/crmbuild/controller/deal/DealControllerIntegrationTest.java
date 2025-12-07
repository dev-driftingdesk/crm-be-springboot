package com.ceedpods.crmbuild.controller.deal;

import com.ceedpods.crmbuild.dto.deal.DealDTO;
import com.ceedpods.crmbuild.dto.request.CreateDealRequest;
import com.ceedpods.crmbuild.dto.request.UpdateDealRequest;
import com.ceedpods.crmbuild.security.CustomPermissionEvaluator;
import com.ceedpods.crmbuild.service.deal.DealService;
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
 * Integration tests for DealController.
 * Tests the full request/response cycle with Spring Security context.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("DealController Integration Tests")
class DealControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DealService dealService;

    @MockBean
    private CustomPermissionEvaluator permissionEvaluator;

    private DealDTO testDealDTO;
    private CreateDealRequest createRequest;
    private UpdateDealRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Allow all permissions for tests - method signature: hasPermission(Authentication, String)
        when(permissionEvaluator.hasPermission(any(), anyString())).thenReturn(true);

        testDealDTO = TestDataFactory.createTestDealDTO();
        createRequest = TestDataFactory.createTestCreateDealRequest();
        updateRequest = TestDataFactory.createTestUpdateDealRequest();
    }

    // ===================== createDeal Integration Tests =====================

    @Nested
    @DisplayName("POST /deals - createDeal Integration")
    class CreateDealIntegrationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should create deal when authenticated as admin")
        void createDeal_WithAdminUser_ReturnsCreated() throws Exception {
            // Given
            when(dealService.createDeal(any(CreateDealRequest.class), any(Authentication.class)))
                .thenReturn(testDealDTO);

            // When & Then
            mockMvc.perform(post("/deals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists());

            verify(dealService).createDeal(any(CreateDealRequest.class), any(Authentication.class));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void createDeal_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(post("/deals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());

            verify(dealService, never()).createDeal(any(), any());
        }
    }

    // ===================== getAllDeals Integration Tests =====================

    @Nested
    @DisplayName("GET /deals - getAllDeals Integration")
    class GetAllDealsIntegrationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return all deals when authenticated")
        void getAllDeals_WithAuthenticatedUser_ReturnsOk() throws Exception {
            // Given
            List<DealDTO> deals = Arrays.asList(testDealDTO);
            when(dealService.getAllDeals()).thenReturn(deals);

            // When & Then
            mockMvc.perform(get("/deals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(testDealDTO.getId()));

            verify(dealService).getAllDeals();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return empty list when no deals exist")
        void getAllDeals_NoDeals_ReturnsEmptyList() throws Exception {
            // Given
            when(dealService.getAllDeals()).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/deals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getAllDeals_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/deals"))
                .andExpect(status().isUnauthorized());

            verify(dealService, never()).getAllDeals();
        }
    }

    // ===================== getDealById Integration Tests =====================

    @Nested
    @DisplayName("GET /deals/{id} - getDealById Integration")
    class GetDealByIdIntegrationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return deal when valid ID provided")
        void getDealById_WithValidId_ReturnsDeal() throws Exception {
            // Given
            String dealId = TestDataFactory.TEST_DEAL_ID;
            when(dealService.getDealById(dealId)).thenReturn(testDealDTO);

            // When & Then
            mockMvc.perform(get("/deals/{id}", dealId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testDealDTO.getId()));

            verify(dealService).getDealById(dealId);
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getDealById_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/deals/{id}", TestDataFactory.TEST_DEAL_ID))
                .andExpect(status().isUnauthorized());

            verify(dealService, never()).getDealById(any());
        }
    }

    // ===================== updateDeal Integration Tests =====================

    @Nested
    @DisplayName("PUT /deals/{id} - updateDeal Integration")
    class UpdateDealIntegrationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should update deal when authenticated as admin")
        void updateDeal_WithAdminUser_ReturnsOk() throws Exception {
            // Given
            String dealId = TestDataFactory.TEST_DEAL_ID;
            when(dealService.updateDeal(eq(dealId), any(UpdateDealRequest.class), any(Authentication.class)))
                .thenReturn(testDealDTO);

            // When & Then
            mockMvc.perform(put("/deals/{id}", dealId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

            verify(dealService).updateDeal(eq(dealId), any(UpdateDealRequest.class), any(Authentication.class));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void updateDeal_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(put("/deals/{id}", TestDataFactory.TEST_DEAL_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());

            verify(dealService, never()).updateDeal(any(), any(), any());
        }
    }

    // ===================== deleteDeal Integration Tests =====================

    @Nested
    @DisplayName("DELETE /deals/{id} - deleteDeal Integration")
    class DeleteDealIntegrationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should delete deal when authenticated as admin")
        void deleteDeal_WithAdminUser_ReturnsOk() throws Exception {
            // Given
            String dealId = TestDataFactory.TEST_DEAL_ID;
            doNothing().when(dealService).deleteDeal(eq(dealId), any(Authentication.class));

            // When & Then
            mockMvc.perform(delete("/deals/{id}", dealId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

            verify(dealService).deleteDeal(eq(dealId), any(Authentication.class));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void deleteDeal_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(delete("/deals/{id}", TestDataFactory.TEST_DEAL_ID))
                .andExpect(status().isUnauthorized());

            verify(dealService, never()).deleteDeal(any(), any());
        }
    }

    // ===================== searchDeals Integration Tests =====================

    @Nested
    @DisplayName("GET /deals/search - searchDeals Integration")
    class SearchDealsIntegrationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should search deals when authenticated")
        void searchDeals_WithAuthenticatedUser_ReturnsResults() throws Exception {
            // Given
            String query = "test";
            List<DealDTO> deals = Arrays.asList(testDealDTO);
            when(dealService.searchDeals(query)).thenReturn(deals);

            // When & Then
            mockMvc.perform(get("/deals/search")
                    .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

            verify(dealService).searchDeals(query);
        }
    }

    // ===================== getDealCount Integration Tests =====================

    @Nested
    @DisplayName("GET /deals/count - getDealCount Integration")
    class GetDealCountIntegrationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return deal count when authenticated")
        void getDealCount_WithAuthenticatedUser_ReturnsCount() throws Exception {
            // Given
            when(dealService.getTotalDealCount()).thenReturn(10L);

            // When & Then
            mockMvc.perform(get("/deals/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(10));

            verify(dealService).getTotalDealCount();
        }
    }

    // ===================== getDealsByLeadId Integration Tests =====================

    @Nested
    @DisplayName("GET /deals/lead/{leadId} - getDealsByLeadId Integration")
    class GetDealsByLeadIdIntegrationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return deals for lead when authenticated")
        void getDealsByLeadId_WithAuthenticatedUser_ReturnsDeals() throws Exception {
            // Given
            String leadId = TestDataFactory.TEST_LEAD_ID;
            List<DealDTO> deals = Arrays.asList(testDealDTO);
            when(dealService.getDealsByLeadId(leadId)).thenReturn(deals);

            // When & Then
            mockMvc.perform(get("/deals/lead/{leadId}", leadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

            verify(dealService).getDealsByLeadId(leadId);
        }
    }

    // ===================== getDealsByProductId Integration Tests =====================

    @Nested
    @DisplayName("GET /deals/product/{productId} - getDealsByProductId Integration")
    class GetDealsByProductIdIntegrationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return deals for product when authenticated")
        void getDealsByProductId_WithAuthenticatedUser_ReturnsDeals() throws Exception {
            // Given
            String productId = TestDataFactory.TEST_PRODUCT_ID;
            List<DealDTO> deals = Arrays.asList(testDealDTO);
            when(dealService.getDealsByProductId(productId)).thenReturn(deals);

            // When & Then
            mockMvc.perform(get("/deals/product/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

            verify(dealService).getDealsByProductId(productId);
        }
    }

    // ===================== getDealsBySalesRepId Integration Tests =====================

    @Nested
    @DisplayName("GET /deals/salesrep/{salesRepId} - getDealsBySalesRepId Integration")
    class GetDealsBySalesRepIdIntegrationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return deals for sales rep when authenticated")
        void getDealsBySalesRepId_WithAuthenticatedUser_ReturnsDeals() throws Exception {
            // Given
            String salesRepId = TestDataFactory.TEST_SALES_REP_ID;
            List<DealDTO> deals = Arrays.asList(testDealDTO);
            when(dealService.getDealsBySalesRepId(salesRepId)).thenReturn(deals);

            // When & Then
            mockMvc.perform(get("/deals/salesrep/{salesRepId}", salesRepId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

            verify(dealService).getDealsBySalesRepId(salesRepId);
        }
    }
}
