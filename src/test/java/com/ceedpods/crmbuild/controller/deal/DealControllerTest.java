package com.ceedpods.crmbuild.controller.deal;

import com.ceedpods.crmbuild.dto.deal.DealDTO;
import com.ceedpods.crmbuild.dto.request.CreateDealRequest;
import com.ceedpods.crmbuild.dto.request.UpdateDealRequest;
import com.ceedpods.crmbuild.exception.BadRequestException;
import com.ceedpods.crmbuild.exception.ForbiddenException;
import com.ceedpods.crmbuild.exception.ResourceNotFoundException;
import com.ceedpods.crmbuild.security.CustomPermissionEvaluator;
import com.ceedpods.crmbuild.service.deal.DealService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DealController Unit Tests")
class DealControllerTest {

    @Mock
    private DealService dealService;

    @Mock
    private CustomPermissionEvaluator permissionEvaluator;

    @InjectMocks
    private DealController dealController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private DealDTO testDealDTO;
    private CreateDealRequest createRequest;
    private UpdateDealRequest updateRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(dealController)
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();

        testDealDTO = TestDataFactory.createTestDealDTO();
        createRequest = TestDataFactory.createTestCreateDealRequest();
        updateRequest = TestDataFactory.createTestUpdateDealRequest();
    }

    // ===================== createDeal Endpoint Tests =====================

    @Nested
    @DisplayName("POST /deals - createDeal")
    class CreateDealEndpointTests {

        @Test
        @DisplayName("Should return 201 Created when deal created successfully")
        void createDeal_WithValidRequest_Returns201Created() throws Exception {
            // Given
            when(dealService.createDeal(any(CreateDealRequest.class), any()))
                .thenReturn(testDealDTO);

            // When & Then
            mockMvc.perform(post("/deals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Deal created successfully"))
                .andExpect(jsonPath("$.data.id").value(testDealDTO.getId()));

            verify(dealService).createDeal(any(CreateDealRequest.class), any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when service throws exception")
        void createDeal_ServiceThrowsException_Returns400BadRequest() throws Exception {
            // Given
            when(dealService.createDeal(any(CreateDealRequest.class), any()))
                .thenThrow(new BadRequestException("Deal name already exists"));

            // When & Then
            mockMvc.perform(post("/deals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

            verify(dealService).createDeal(any(CreateDealRequest.class), any());
        }

        @Test
        @DisplayName("Should return 400 when forbidden exception occurs")
        void createDeal_ForbiddenException_Returns400() throws Exception {
            // Given
            when(dealService.createDeal(any(CreateDealRequest.class), any()))
                .thenThrow(new ForbiddenException("Only admin users can create deals"));

            // When & Then
            mockMvc.perform(post("/deals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ===================== getAllDeals Endpoint Tests =====================

    @Nested
    @DisplayName("GET /deals - getAllDeals")
    class GetAllDealsEndpointTests {

        @Test
        @DisplayName("Should return 200 OK with deal list")
        void getAllDeals_ReturnsOkWithDealList() throws Exception {
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
        @DisplayName("Should return 200 OK with empty list when no deals")
        void getAllDeals_NoDeals_ReturnsEmptyList() throws Exception {
            // Given
            when(dealService.getAllDeals()).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/deals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());

            verify(dealService).getAllDeals();
        }

        @Test
        @DisplayName("Should return 400 Bad Request when service throws exception")
        void getAllDeals_ServiceThrowsException_Returns400BadRequest() throws Exception {
            // Given
            when(dealService.getAllDeals()).thenThrow(new RuntimeException("Database error"));

            // When & Then
            mockMvc.perform(get("/deals"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

            verify(dealService).getAllDeals();
        }
    }

    // ===================== getDealById Endpoint Tests =====================

    @Nested
    @DisplayName("GET /deals/{id} - getDealById")
    class GetDealByIdEndpointTests {

        @Test
        @DisplayName("Should return 200 OK with deal when valid ID")
        void getDealById_WithValidId_ReturnsOkWithDeal() throws Exception {
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
        @DisplayName("Should return 400 Bad Request when deal not found")
        void getDealById_WithInvalidId_Returns400BadRequest() throws Exception {
            // Given
            String invalidId = "invalid-id";
            when(dealService.getDealById(invalidId))
                .thenThrow(new ResourceNotFoundException("Deal not found with UUID: " + invalidId));

            // When & Then
            mockMvc.perform(get("/deals/{id}", invalidId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

            verify(dealService).getDealById(invalidId);
        }
    }

    // ===================== updateDeal Endpoint Tests =====================

    @Nested
    @DisplayName("PUT /deals/{id} - updateDeal")
    class UpdateDealEndpointTests {

        @Test
        @DisplayName("Should return 200 OK with updated deal")
        void updateDeal_WithValidRequest_ReturnsOkWithUpdatedDeal() throws Exception {
            // Given
            String dealId = TestDataFactory.TEST_DEAL_ID;
            when(dealService.updateDeal(eq(dealId), any(UpdateDealRequest.class), any()))
                .thenReturn(testDealDTO);

            // When & Then
            mockMvc.perform(put("/deals/{id}", dealId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Deal updated successfully"));

            verify(dealService).updateDeal(eq(dealId), any(UpdateDealRequest.class), any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when service throws exception")
        void updateDeal_ServiceThrowsException_Returns400BadRequest() throws Exception {
            // Given
            String dealId = TestDataFactory.TEST_DEAL_ID;
            when(dealService.updateDeal(eq(dealId), any(UpdateDealRequest.class), any()))
                .thenThrow(new BadRequestException("Deal name already exists"));

            // When & Then
            mockMvc.perform(put("/deals/{id}", dealId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

            verify(dealService).updateDeal(eq(dealId), any(UpdateDealRequest.class), any());
        }

        @Test
        @DisplayName("Should return 400 when deal not found")
        void updateDeal_DealNotFound_Returns400() throws Exception {
            // Given
            String invalidId = "invalid-id";
            when(dealService.updateDeal(eq(invalidId), any(UpdateDealRequest.class), any()))
                .thenThrow(new ResourceNotFoundException("Deal not found"));

            // When & Then
            mockMvc.perform(put("/deals/{id}", invalidId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ===================== deleteDeal Endpoint Tests =====================

    @Nested
    @DisplayName("DELETE /deals/{id} - deleteDeal")
    class DeleteDealEndpointTests {

        @Test
        @DisplayName("Should return 200 OK with success message")
        void deleteDeal_WithValidId_ReturnsOkWithSuccessMessage() throws Exception {
            // Given
            String dealId = TestDataFactory.TEST_DEAL_ID;
            doNothing().when(dealService).deleteDeal(eq(dealId), any());

            // When & Then
            mockMvc.perform(delete("/deals/{id}", dealId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Deal deleted successfully"));

            verify(dealService).deleteDeal(eq(dealId), any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when service throws exception")
        void deleteDeal_ServiceThrowsException_Returns400BadRequest() throws Exception {
            // Given
            String dealId = TestDataFactory.TEST_DEAL_ID;
            doThrow(new ResourceNotFoundException("Deal not found"))
                .when(dealService).deleteDeal(eq(dealId), any());

            // When & Then
            mockMvc.perform(delete("/deals/{id}", dealId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

            verify(dealService).deleteDeal(eq(dealId), any());
        }
    }

    // ===================== searchDeals Endpoint Tests =====================

    @Nested
    @DisplayName("GET /deals/search - searchDeals")
    class SearchDealsEndpointTests {

        @Test
        @DisplayName("Should return 200 OK with matching deals")
        void searchDeals_WithValidQuery_ReturnsOkWithResults() throws Exception {
            // Given
            String query = "test";
            List<DealDTO> deals = Arrays.asList(testDealDTO);
            when(dealService.searchDeals(query)).thenReturn(deals);

            // When & Then
            mockMvc.perform(get("/deals/search")
                    .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(testDealDTO.getId()));

            verify(dealService).searchDeals(query);
        }

        @Test
        @DisplayName("Should return 200 OK with empty list when no matches")
        void searchDeals_NoMatches_ReturnsEmptyList() throws Exception {
            // Given
            String query = "nonexistent";
            when(dealService.searchDeals(query)).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/deals/search")
                    .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());

            verify(dealService).searchDeals(query);
        }

        @Test
        @DisplayName("Should return 400 Bad Request when service throws exception")
        void searchDeals_ServiceThrowsException_Returns400BadRequest() throws Exception {
            // Given
            String query = "test";
            when(dealService.searchDeals(query)).thenThrow(new RuntimeException("Search error"));

            // When & Then
            mockMvc.perform(get("/deals/search")
                    .param("query", query))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

            verify(dealService).searchDeals(query);
        }
    }

    // ===================== getDealCount Endpoint Tests =====================

    @Nested
    @DisplayName("GET /deals/count - getDealCount")
    class GetDealCountEndpointTests {

        @Test
        @DisplayName("Should return 200 OK with count")
        void getDealCount_ReturnsOkWithCount() throws Exception {
            // Given
            long count = 10L;
            when(dealService.getTotalDealCount()).thenReturn(count);

            // When & Then
            mockMvc.perform(get("/deals/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(10));

            verify(dealService).getTotalDealCount();
        }

        @Test
        @DisplayName("Should return 400 Bad Request when service throws exception")
        void getDealCount_ServiceThrowsException_Returns400BadRequest() throws Exception {
            // Given
            when(dealService.getTotalDealCount()).thenThrow(new RuntimeException("Count error"));

            // When & Then
            mockMvc.perform(get("/deals/count"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

            verify(dealService).getTotalDealCount();
        }
    }

    // ===================== getDealsByLeadId Endpoint Tests =====================

    @Nested
    @DisplayName("GET /deals/lead/{leadId} - getDealsByLeadId")
    class GetDealsByLeadIdEndpointTests {

        @Test
        @DisplayName("Should return 200 OK with deals for lead")
        void getDealsByLeadId_WithValidLeadId_ReturnsOkWithDeals() throws Exception {
            // Given
            String leadId = TestDataFactory.TEST_LEAD_ID;
            List<DealDTO> deals = Arrays.asList(testDealDTO);
            when(dealService.getDealsByLeadId(leadId)).thenReturn(deals);

            // When & Then
            mockMvc.perform(get("/deals/lead/{leadId}", leadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(testDealDTO.getId()));

            verify(dealService).getDealsByLeadId(leadId);
        }

        @Test
        @DisplayName("Should return 400 Bad Request when service throws exception")
        void getDealsByLeadId_ServiceThrowsException_Returns400BadRequest() throws Exception {
            // Given
            String leadId = TestDataFactory.TEST_LEAD_ID;
            when(dealService.getDealsByLeadId(leadId)).thenThrow(new RuntimeException("Error"));

            // When & Then
            mockMvc.perform(get("/deals/lead/{leadId}", leadId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

            verify(dealService).getDealsByLeadId(leadId);
        }
    }

    // ===================== getDealsByProductId Endpoint Tests =====================

    @Nested
    @DisplayName("GET /deals/product/{productId} - getDealsByProductId")
    class GetDealsByProductIdEndpointTests {

        @Test
        @DisplayName("Should return 200 OK with deals for product")
        void getDealsByProductId_WithValidProductId_ReturnsOkWithDeals() throws Exception {
            // Given
            String productId = TestDataFactory.TEST_PRODUCT_ID;
            List<DealDTO> deals = Arrays.asList(testDealDTO);
            when(dealService.getDealsByProductId(productId)).thenReturn(deals);

            // When & Then
            mockMvc.perform(get("/deals/product/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(testDealDTO.getId()));

            verify(dealService).getDealsByProductId(productId);
        }

        @Test
        @DisplayName("Should return 400 Bad Request when service throws exception")
        void getDealsByProductId_ServiceThrowsException_Returns400BadRequest() throws Exception {
            // Given
            String productId = TestDataFactory.TEST_PRODUCT_ID;
            when(dealService.getDealsByProductId(productId)).thenThrow(new RuntimeException("Error"));

            // When & Then
            mockMvc.perform(get("/deals/product/{productId}", productId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

            verify(dealService).getDealsByProductId(productId);
        }
    }

    // ===================== getDealsBySalesRepId Endpoint Tests =====================

    @Nested
    @DisplayName("GET /deals/salesrep/{salesRepId} - getDealsBySalesRepId")
    class GetDealsBySalesRepIdEndpointTests {

        @Test
        @DisplayName("Should return 200 OK with deals for sales rep")
        void getDealsBySalesRepId_WithValidSalesRepId_ReturnsOkWithDeals() throws Exception {
            // Given
            String salesRepId = TestDataFactory.TEST_SALES_REP_ID;
            List<DealDTO> deals = Arrays.asList(testDealDTO);
            when(dealService.getDealsBySalesRepId(salesRepId)).thenReturn(deals);

            // When & Then
            mockMvc.perform(get("/deals/salesrep/{salesRepId}", salesRepId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(testDealDTO.getId()));

            verify(dealService).getDealsBySalesRepId(salesRepId);
        }

        @Test
        @DisplayName("Should return 400 Bad Request when service throws exception")
        void getDealsBySalesRepId_ServiceThrowsException_Returns400BadRequest() throws Exception {
            // Given
            String salesRepId = TestDataFactory.TEST_SALES_REP_ID;
            when(dealService.getDealsBySalesRepId(salesRepId)).thenThrow(new RuntimeException("Error"));

            // When & Then
            mockMvc.perform(get("/deals/salesrep/{salesRepId}", salesRepId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

            verify(dealService).getDealsBySalesRepId(salesRepId);
        }
    }
}
