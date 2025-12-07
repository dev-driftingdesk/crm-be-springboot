package com.ceedpods.crmbuild.service.lead;

import com.ceedpods.crmbuild.dto.lead.LeadDTO;
import com.ceedpods.crmbuild.dto.request.CreateLeadRequest;
import com.ceedpods.crmbuild.dto.request.UpdateLeadRequest;
import com.ceedpods.crmbuild.entity.deal.Deal;
import com.ceedpods.crmbuild.entity.lead.Lead;
import com.ceedpods.crmbuild.exception.BadRequestException;
import com.ceedpods.crmbuild.exception.ForbiddenException;
import com.ceedpods.crmbuild.exception.ResourceNotFoundException;
import com.ceedpods.crmbuild.mapper.LeadMapper;
import com.ceedpods.crmbuild.repository.DealRepository;
import com.ceedpods.crmbuild.repository.LeadRepository;
import com.ceedpods.crmbuild.security.CustomPermissionEvaluator;
import com.ceedpods.crmbuild.service.auditLogService.AuditLogService;
import com.ceedpods.crmbuild.util.MockAuthenticationFactory;
import com.ceedpods.crmbuild.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeadService Unit Tests")
class LeadServiceTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private DealRepository dealRepository;

    @Mock
    private LeadMapper leadMapper;

    @Mock
    private CustomPermissionEvaluator permissionEvaluator;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private LeadService leadService;

    private Lead testLead;
    private LeadDTO testLeadDTO;
    private CreateLeadRequest createRequest;
    private UpdateLeadRequest updateRequest;
    private Authentication adminAuth;
    private Authentication nonAdminAuth;

    @BeforeEach
    void setUp() {
        testLead = TestDataFactory.createTestLead();
        testLeadDTO = TestDataFactory.createTestLeadDTO();
        createRequest = TestDataFactory.createTestCreateLeadRequest();
        updateRequest = TestDataFactory.createTestUpdateLeadRequest();
        adminAuth = MockAuthenticationFactory.createAdminAuthentication();
        nonAdminAuth = MockAuthenticationFactory.createNonAdminAuthentication();
    }

    // ===================== getAllLeads Tests =====================

    @Nested
    @DisplayName("getAllLeads Tests")
    class GetAllLeadsTests {

        @Test
        @DisplayName("Should return all non-deleted leads")
        void getAllLeads_ReturnsAllNonDeletedLeads() {
            // Given
            List<Lead> leads = TestDataFactory.createTestLeadList(3);
            List<LeadDTO> expectedDTOs = TestDataFactory.createTestLeadDTOList(3);

            when(leadRepository.findByDeletedFalse()).thenReturn(leads);
            when(leadMapper.toDTO(leads)).thenReturn(expectedDTOs);

            // When
            List<LeadDTO> result = leadService.getAllLeads();

            // Then
            assertNotNull(result);
            assertEquals(3, result.size());
            verify(leadRepository).findByDeletedFalse();
            verify(leadMapper).toDTO(leads);
        }

        @Test
        @DisplayName("Should return empty list when no leads exist")
        void getAllLeads_WhenNoLeads_ReturnsEmptyList() {
            // Given
            when(leadRepository.findByDeletedFalse()).thenReturn(Collections.emptyList());
            when(leadMapper.toDTO(Collections.emptyList())).thenReturn(Collections.emptyList());

            // When
            List<LeadDTO> result = leadService.getAllLeads();

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(leadRepository).findByDeletedFalse();
        }
    }

    // ===================== getLeadById Tests =====================

    @Nested
    @DisplayName("getLeadById Tests")
    class GetLeadByIdTests {

        @Test
        @DisplayName("Should return lead when valid ID provided")
        void getLeadById_WithValidId_ReturnsLead() {
            // Given
            String leadId = TestDataFactory.TEST_LEAD_ID;
            when(leadRepository.findById(leadId)).thenReturn(Optional.of(testLead));
            when(leadMapper.toDTO(testLead)).thenReturn(testLeadDTO);

            // When
            LeadDTO result = leadService.getLeadById(leadId);

            // Then
            assertNotNull(result);
            assertEquals(testLeadDTO.getId(), result.getId());
            assertEquals(testLeadDTO.getLeadName(), result.getLeadName());
            verify(leadRepository).findById(leadId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when lead not found")
        void getLeadById_WithInvalidId_ThrowsResourceNotFoundException() {
            // Given
            String invalidId = "invalid-id";
            when(leadRepository.findById(invalidId)).thenReturn(Optional.empty());

            // When & Then
            ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> leadService.getLeadById(invalidId)
            );
            assertTrue(exception.getMessage().contains(invalidId));
            verify(leadRepository).findById(invalidId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when lead is soft-deleted")
        void getLeadById_WithDeletedLead_ThrowsResourceNotFoundException() {
            // Given
            Lead deletedLead = TestDataFactory.createDeletedLead();
            String leadId = deletedLead.getId();
            when(leadRepository.findById(leadId)).thenReturn(Optional.of(deletedLead));

            // When & Then
            assertThrows(
                ResourceNotFoundException.class,
                () -> leadService.getLeadById(leadId)
            );
            verify(leadRepository).findById(leadId);
        }
    }

    // ===================== createLead Tests =====================

    @Nested
    @DisplayName("createLead Tests")
    class CreateLeadTests {

        @Test
        @DisplayName("Should create lead when valid request from admin")
        void createLead_WithValidRequest_ReturnsCreatedLead() {
            // Given
            Deal testDeal = TestDataFactory.createTestDeal();

            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(leadRepository.existsByLeadNameAndDeletedFalse(createRequest.getLeadName())).thenReturn(false);
            when(dealRepository.findById(createRequest.getDealId())).thenReturn(Optional.of(testDeal));
            when(leadRepository.existsByDealIdAndDeletedFalse(createRequest.getDealId())).thenReturn(false);
            when(leadRepository.save(any(Lead.class))).thenReturn(testLead);
            when(leadMapper.toDTO(testLead)).thenReturn(testLeadDTO);

            // When
            LeadDTO result = leadService.createLead(createRequest, adminAuth);

            // Then
            assertNotNull(result);
            assertEquals(testLeadDTO.getLeadName(), result.getLeadName());
            verify(leadRepository).save(any(Lead.class));
            verify(auditLogService).logLeadCreated(eq(adminAuth), anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw ForbiddenException when non-admin tries to create lead")
        void createLead_WithNonAdminUser_ThrowsForbiddenException() {
            // Given
            when(permissionEvaluator.isAdmin(nonAdminAuth)).thenReturn(false);

            // When & Then
            ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> leadService.createLead(createRequest, nonAdminAuth)
            );
            assertTrue(exception.getMessage().toLowerCase().contains("admin"));
            verify(leadRepository, never()).save(any(Lead.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when lead name already exists")
        void createLead_WithDuplicateName_ThrowsBadRequestException() {
            // Given
            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(leadRepository.existsByLeadNameAndDeletedFalse(createRequest.getLeadName())).thenReturn(true);

            // When & Then
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> leadService.createLead(createRequest, adminAuth)
            );
            assertTrue(exception.getMessage().toLowerCase().contains("already exists"));
            verify(leadRepository, never()).save(any(Lead.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when deal ID does not exist")
        void createLead_WithInvalidDealId_ThrowsBadRequestException() {
            // Given
            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(leadRepository.existsByLeadNameAndDeletedFalse(createRequest.getLeadName())).thenReturn(false);
            when(dealRepository.findById(createRequest.getDealId())).thenReturn(Optional.empty());

            // When & Then
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> leadService.createLead(createRequest, adminAuth)
            );
            assertTrue(exception.getMessage().contains("Deal not found"));
            verify(leadRepository, never()).save(any(Lead.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when deal ID is already linked to another lead")
        void createLead_WithDuplicateDealId_ThrowsBadRequestException() {
            // Given
            Deal testDeal = TestDataFactory.createTestDeal();

            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(leadRepository.existsByLeadNameAndDeletedFalse(createRequest.getLeadName())).thenReturn(false);
            when(dealRepository.findById(createRequest.getDealId())).thenReturn(Optional.of(testDeal));
            when(leadRepository.existsByDealIdAndDeletedFalse(createRequest.getDealId())).thenReturn(true);

            // When & Then
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> leadService.createLead(createRequest, adminAuth)
            );
            assertTrue(exception.getMessage().toLowerCase().contains("already linked"));
            verify(leadRepository, never()).save(any(Lead.class));
        }

        @Test
        @DisplayName("Should succeed when deal ID is null")
        void createLead_WithNullDealId_SucceedsWithNullDealId() {
            // Given
            CreateLeadRequest requestWithoutDeal = TestDataFactory.createTestCreateLeadRequestWithoutDealId();
            Lead leadWithoutDeal = TestDataFactory.createTestLead();
            leadWithoutDeal.setDealId(null);
            LeadDTO dtoWithoutDeal = TestDataFactory.createTestLeadDTO();
            dtoWithoutDeal.setDealId(null);

            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(leadRepository.existsByLeadNameAndDeletedFalse(requestWithoutDeal.getLeadName())).thenReturn(false);
            when(leadRepository.save(any(Lead.class))).thenReturn(leadWithoutDeal);
            when(leadMapper.toDTO(leadWithoutDeal)).thenReturn(dtoWithoutDeal);

            // When
            LeadDTO result = leadService.createLead(requestWithoutDeal, adminAuth);

            // Then
            assertNotNull(result);
            verify(dealRepository, never()).findById(anyString());
            verify(leadRepository).save(any(Lead.class));
        }
    }

    // ===================== updateLead Tests =====================

    @Nested
    @DisplayName("updateLead Tests")
    class UpdateLeadTests {

        @Test
        @DisplayName("Should update lead when valid request from admin")
        void updateLead_WithValidRequest_ReturnsUpdatedLead() {
            // Given
            String leadId = TestDataFactory.TEST_LEAD_ID;

            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(leadRepository.findById(leadId)).thenReturn(Optional.of(testLead));
            when(leadRepository.existsByLeadNameAndDeletedFalse(updateRequest.getLeadName())).thenReturn(false);
            when(leadRepository.save(any(Lead.class))).thenReturn(testLead);
            when(leadMapper.toDTO(testLead)).thenReturn(testLeadDTO);

            // When
            LeadDTO result = leadService.updateLead(leadId, updateRequest, adminAuth);

            // Then
            assertNotNull(result);
            verify(leadRepository).save(any(Lead.class));
            verify(auditLogService).logLeadUpdated(eq(adminAuth), anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw ForbiddenException when non-admin tries to update")
        void updateLead_WithNonAdminUser_ThrowsForbiddenException() {
            // Given
            when(permissionEvaluator.isAdmin(nonAdminAuth)).thenReturn(false);

            // When & Then
            assertThrows(
                ForbiddenException.class,
                () -> leadService.updateLead(TestDataFactory.TEST_LEAD_ID, updateRequest, nonAdminAuth)
            );
            verify(leadRepository, never()).save(any(Lead.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when lead not found")
        void updateLead_WithInvalidId_ThrowsResourceNotFoundException() {
            // Given
            String invalidId = "invalid-id";
            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(leadRepository.findById(invalidId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(
                ResourceNotFoundException.class,
                () -> leadService.updateLead(invalidId, updateRequest, adminAuth)
            );
            verify(leadRepository, never()).save(any(Lead.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when updating to duplicate name")
        void updateLead_WithDuplicateName_ThrowsBadRequestException() {
            // Given
            String leadId = TestDataFactory.TEST_LEAD_ID;

            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(leadRepository.findById(leadId)).thenReturn(Optional.of(testLead));
            when(leadRepository.existsByLeadNameAndDeletedFalse(updateRequest.getLeadName())).thenReturn(true);

            // When & Then
            assertThrows(
                BadRequestException.class,
                () -> leadService.updateLead(leadId, updateRequest, adminAuth)
            );
            verify(leadRepository, never()).save(any(Lead.class));
        }

        @Test
        @DisplayName("Should allow same name without duplicate check")
        void updateLead_WithSameName_SucceedsWithoutDuplicateCheck() {
            // Given
            String leadId = TestDataFactory.TEST_LEAD_ID;
            UpdateLeadRequest sameNameRequest = UpdateLeadRequest.builder()
                .leadName(testLead.getLeadName()) // Same name as existing
                .build();

            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(leadRepository.findById(leadId)).thenReturn(Optional.of(testLead));
            when(leadRepository.save(any(Lead.class))).thenReturn(testLead);
            when(leadMapper.toDTO(testLead)).thenReturn(testLeadDTO);

            // When
            LeadDTO result = leadService.updateLead(leadId, sameNameRequest, adminAuth);

            // Then
            assertNotNull(result);
            verify(leadRepository, never()).existsByLeadNameAndDeletedFalse(anyString());
        }

        @Test
        @DisplayName("Should validate and update when new deal ID is provided")
        void updateLead_WithNewDealId_ValidatesAndUpdates() {
            // Given
            String leadId = TestDataFactory.TEST_LEAD_ID;
            String newDealId = "new-deal-id";
            Deal newDeal = TestDataFactory.createTestDealWithId(newDealId);
            UpdateLeadRequest requestWithNewDeal = UpdateLeadRequest.builder()
                .dealId(newDealId)
                .build();

            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(leadRepository.findById(leadId)).thenReturn(Optional.of(testLead));
            when(dealRepository.findById(newDealId)).thenReturn(Optional.of(newDeal));
            when(leadRepository.existsByDealIdAndDeletedFalse(newDealId)).thenReturn(false);
            when(leadRepository.save(any(Lead.class))).thenReturn(testLead);
            when(leadMapper.toDTO(testLead)).thenReturn(testLeadDTO);

            // When
            LeadDTO result = leadService.updateLead(leadId, requestWithNewDeal, adminAuth);

            // Then
            assertNotNull(result);
            verify(dealRepository).findById(newDealId);
            verify(leadRepository).save(any(Lead.class));
        }
    }

    // ===================== deleteLead Tests =====================

    @Nested
    @DisplayName("deleteLead Tests")
    class DeleteLeadTests {

        @Test
        @DisplayName("Should delete lead when valid ID from admin")
        void deleteLead_WithValidId_DeletesLead() {
            // Given
            String leadId = TestDataFactory.TEST_LEAD_ID;

            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(leadRepository.findById(leadId)).thenReturn(Optional.of(testLead));

            // When
            leadService.deleteLead(leadId, adminAuth);

            // Then
            verify(leadRepository).delete(testLead);
            verify(auditLogService).logLeadDeleted(eq(adminAuth), anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw ForbiddenException when non-admin tries to delete")
        void deleteLead_WithNonAdminUser_ThrowsForbiddenException() {
            // Given
            when(permissionEvaluator.isAdmin(nonAdminAuth)).thenReturn(false);

            // When & Then
            assertThrows(
                ForbiddenException.class,
                () -> leadService.deleteLead(TestDataFactory.TEST_LEAD_ID, nonAdminAuth)
            );
            verify(leadRepository, never()).delete(any(Lead.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when lead not found")
        void deleteLead_WithInvalidId_ThrowsResourceNotFoundException() {
            // Given
            String invalidId = "invalid-id";
            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(leadRepository.findById(invalidId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(
                ResourceNotFoundException.class,
                () -> leadService.deleteLead(invalidId, adminAuth)
            );
            verify(leadRepository, never()).delete(any(Lead.class));
        }
    }

    // ===================== searchLeads Tests =====================

    @Nested
    @DisplayName("searchLeads Tests")
    class SearchLeadsTests {

        @Test
        @DisplayName("Should return matching leads")
        void searchLeads_WithMatchingTerm_ReturnsMatchingLeads() {
            // Given
            String searchTerm = "Test";
            List<Lead> matchingLeads = Arrays.asList(testLead);
            List<LeadDTO> expectedDTOs = Arrays.asList(testLeadDTO);

            when(leadRepository.searchLeads(searchTerm)).thenReturn(matchingLeads);
            when(leadMapper.toDTO(matchingLeads)).thenReturn(expectedDTOs);

            // When
            List<LeadDTO> result = leadService.searchLeads(searchTerm);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(leadRepository).searchLeads(searchTerm);
        }

        @Test
        @DisplayName("Should return empty list when no matches")
        void searchLeads_WithNoMatches_ReturnsEmptyList() {
            // Given
            String searchTerm = "NonExistent";
            when(leadRepository.searchLeads(searchTerm)).thenReturn(Collections.emptyList());
            when(leadMapper.toDTO(Collections.emptyList())).thenReturn(Collections.emptyList());

            // When
            List<LeadDTO> result = leadService.searchLeads(searchTerm);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ===================== getTotalLeadCount Tests =====================

    @Nested
    @DisplayName("getTotalLeadCount Tests")
    class GetTotalLeadCountTests {

        @Test
        @DisplayName("Should return correct count")
        void getTotalLeadCount_ReturnsCorrectCount() {
            // Given
            long expectedCount = 10L;
            when(leadRepository.countByDeletedFalse()).thenReturn(expectedCount);

            // When
            long result = leadService.getTotalLeadCount();

            // Then
            assertEquals(expectedCount, result);
            verify(leadRepository).countByDeletedFalse();
        }

        @Test
        @DisplayName("Should return zero when no leads")
        void getTotalLeadCount_WhenNoLeads_ReturnsZero() {
            // Given
            when(leadRepository.countByDeletedFalse()).thenReturn(0L);

            // When
            long result = leadService.getTotalLeadCount();

            // Then
            assertEquals(0L, result);
        }
    }

    // ===================== getLeadsByDealId Tests =====================

    @Nested
    @DisplayName("getLeadsByDealId Tests")
    class GetLeadsByDealIdTests {

        @Test
        @DisplayName("Should return leads for valid deal ID")
        void getLeadsByDealId_WithValidDealId_ReturnsLeads() {
            // Given
            String dealId = TestDataFactory.TEST_DEAL_ID;
            List<Lead> leads = Arrays.asList(testLead);
            List<LeadDTO> expectedDTOs = Arrays.asList(testLeadDTO);

            when(leadRepository.findByDealIdAndDeletedFalse(dealId)).thenReturn(leads);
            when(leadMapper.toDTO(leads)).thenReturn(expectedDTOs);

            // When
            List<LeadDTO> result = leadService.getLeadsByDealId(dealId);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(leadRepository).findByDealIdAndDeletedFalse(dealId);
        }

        @Test
        @DisplayName("Should return empty list when no matching leads")
        void getLeadsByDealId_WithNoMatchingDeals_ReturnsEmptyList() {
            // Given
            String dealId = "non-existent-deal";
            when(leadRepository.findByDealIdAndDeletedFalse(dealId)).thenReturn(Collections.emptyList());
            when(leadMapper.toDTO(Collections.emptyList())).thenReturn(Collections.emptyList());

            // When
            List<LeadDTO> result = leadService.getLeadsByDealId(dealId);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }
}
