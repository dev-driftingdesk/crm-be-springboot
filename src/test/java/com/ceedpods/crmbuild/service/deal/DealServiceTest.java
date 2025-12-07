package com.ceedpods.crmbuild.service.deal;

import com.ceedpods.crmbuild.dto.deal.DealDTO;
import com.ceedpods.crmbuild.dto.deal.SalesRepAssignment;
import com.ceedpods.crmbuild.dto.request.CreateDealRequest;
import com.ceedpods.crmbuild.dto.request.UpdateDealRequest;
import com.ceedpods.crmbuild.entity.user.User;
import com.ceedpods.crmbuild.entity.deal.Deal;
import com.ceedpods.crmbuild.entity.lead.Lead;
import com.ceedpods.crmbuild.entity.product.Product;
import com.ceedpods.crmbuild.exception.BadRequestException;
import com.ceedpods.crmbuild.exception.ForbiddenException;
import com.ceedpods.crmbuild.exception.ResourceNotFoundException;
import com.ceedpods.crmbuild.mapper.DealMapper;
import com.ceedpods.crmbuild.repository.DealRepository;
import com.ceedpods.crmbuild.repository.LeadRepository;
import com.ceedpods.crmbuild.repository.ProductRepository;
import com.ceedpods.crmbuild.repository.UserRepository;
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
@DisplayName("DealService Unit Tests")
class DealServiceTest {

    @Mock
    private DealRepository dealRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private DealMapper dealMapper;

    @Mock
    private CustomPermissionEvaluator permissionEvaluator;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private DealService dealService;

    private Deal testDeal;
    private DealDTO testDealDTO;
    private CreateDealRequest createRequest;
    private UpdateDealRequest updateRequest;
    private Authentication adminAuth;
    private Authentication nonAdminAuth;

    @BeforeEach
    void setUp() {
        testDeal = TestDataFactory.createTestDeal();
        testDealDTO = TestDataFactory.createTestDealDTO();
        createRequest = TestDataFactory.createTestCreateDealRequest();
        updateRequest = TestDataFactory.createTestUpdateDealRequest();
        adminAuth = MockAuthenticationFactory.createAdminAuthentication();
        nonAdminAuth = MockAuthenticationFactory.createNonAdminAuthentication();
    }

    // ===================== getAllDeals Tests =====================

    @Nested
    @DisplayName("getAllDeals Tests")
    class GetAllDealsTests {

        @Test
        @DisplayName("Should return all non-deleted deals")
        void getAllDeals_ReturnsAllNonDeletedDeals() {
            // Given
            List<Deal> deals = TestDataFactory.createTestDealList(3);
            List<DealDTO> expectedDTOs = TestDataFactory.createTestDealDTOList(3);

            when(dealRepository.findByDeletedFalse()).thenReturn(deals);
            when(dealMapper.toDTO(deals)).thenReturn(expectedDTOs);

            // When
            List<DealDTO> result = dealService.getAllDeals();

            // Then
            assertNotNull(result);
            assertEquals(3, result.size());
            verify(dealRepository).findByDeletedFalse();
            verify(dealMapper).toDTO(deals);
        }

        @Test
        @DisplayName("Should return empty list when no deals exist")
        void getAllDeals_WhenNoDeals_ReturnsEmptyList() {
            // Given
            when(dealRepository.findByDeletedFalse()).thenReturn(Collections.emptyList());
            when(dealMapper.toDTO(Collections.emptyList())).thenReturn(Collections.emptyList());

            // When
            List<DealDTO> result = dealService.getAllDeals();

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(dealRepository).findByDeletedFalse();
        }
    }

    // ===================== getDealById Tests =====================

    @Nested
    @DisplayName("getDealById Tests")
    class GetDealByIdTests {

        @Test
        @DisplayName("Should return deal when valid ID provided")
        void getDealById_WithValidId_ReturnsDeal() {
            // Given
            String dealId = TestDataFactory.TEST_DEAL_ID;
            when(dealRepository.findById(dealId)).thenReturn(Optional.of(testDeal));
            when(dealMapper.toDTO(testDeal)).thenReturn(testDealDTO);

            // When
            DealDTO result = dealService.getDealById(dealId);

            // Then
            assertNotNull(result);
            assertEquals(testDealDTO.getId(), result.getId());
            assertEquals(testDealDTO.getDealName(), result.getDealName());
            verify(dealRepository).findById(dealId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deal not found")
        void getDealById_WithInvalidId_ThrowsResourceNotFoundException() {
            // Given
            String invalidId = "invalid-id";
            when(dealRepository.findById(invalidId)).thenReturn(Optional.empty());

            // When & Then
            ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> dealService.getDealById(invalidId)
            );
            assertTrue(exception.getMessage().contains(invalidId));
            verify(dealRepository).findById(invalidId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deal is soft-deleted")
        void getDealById_WithDeletedDeal_ThrowsResourceNotFoundException() {
            // Given
            Deal deletedDeal = TestDataFactory.createDeletedDeal();
            String dealId = deletedDeal.getId();
            when(dealRepository.findById(dealId)).thenReturn(Optional.of(deletedDeal));

            // When & Then
            assertThrows(
                ResourceNotFoundException.class,
                () -> dealService.getDealById(dealId)
            );
            verify(dealRepository).findById(dealId);
        }
    }

    // ===================== createDeal Tests =====================

    @Nested
    @DisplayName("createDeal Tests")
    class CreateDealTests {

        @Test
        @DisplayName("Should create deal when valid request from admin")
        void createDeal_WithValidRequest_ReturnsCreatedDeal() {
            // Given
            Lead testLead = TestDataFactory.createTestLead();
            Product mockProduct = mock(Product.class);
            User mockUser = mock(User.class);

            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(dealRepository.countByDealNameAndDeletedFalse(createRequest.getDealName())).thenReturn(0L);
            when(leadRepository.findById(createRequest.getLeadId())).thenReturn(Optional.of(testLead));
            when(productRepository.findById(anyString())).thenReturn(Optional.of(mockProduct));
            when(mockProduct.isDeleted()).thenReturn(false);
            when(userRepository.findById(anyString())).thenReturn(Optional.of(mockUser));
            when(mockUser.isDeleted()).thenReturn(false);
            when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);
            when(dealMapper.toDTO(testDeal)).thenReturn(testDealDTO);

            // When
            DealDTO result = dealService.createDeal(createRequest, adminAuth);

            // Then
            assertNotNull(result);
            assertEquals(testDealDTO.getDealName(), result.getDealName());
            verify(dealRepository).save(any(Deal.class));
            verify(auditLogService).logDealCreated(eq(adminAuth), anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw ForbiddenException when non-admin tries to create deal")
        void createDeal_WithNonAdminUser_ThrowsForbiddenException() {
            // Given
            when(permissionEvaluator.isAdmin(nonAdminAuth)).thenReturn(false);

            // When & Then
            ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> dealService.createDeal(createRequest, nonAdminAuth)
            );
            assertTrue(exception.getMessage().toLowerCase().contains("admin"));
            verify(dealRepository, never()).save(any(Deal.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when deal name already exists")
        void createDeal_WithDuplicateName_ThrowsBadRequestException() {
            // Given
            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(dealRepository.countByDealNameAndDeletedFalse(createRequest.getDealName())).thenReturn(1L);

            // When & Then
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> dealService.createDeal(createRequest, adminAuth)
            );
            assertTrue(exception.getMessage().toLowerCase().contains("already exists"));
            verify(dealRepository, never()).save(any(Deal.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when lead ID does not exist")
        void createDeal_WithInvalidLeadId_ThrowsBadRequestException() {
            // Given
            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(dealRepository.countByDealNameAndDeletedFalse(createRequest.getDealName())).thenReturn(0L);
            when(leadRepository.findById(createRequest.getLeadId())).thenReturn(Optional.empty());

            // When & Then
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> dealService.createDeal(createRequest, adminAuth)
            );
            assertTrue(exception.getMessage().contains("Lead not found"));
            verify(dealRepository, never()).save(any(Deal.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when product IDs do not exist")
        void createDeal_WithInvalidProductIds_ThrowsBadRequestException() {
            // Given
            Lead testLead = TestDataFactory.createTestLead();

            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(dealRepository.countByDealNameAndDeletedFalse(createRequest.getDealName())).thenReturn(0L);
            when(leadRepository.findById(createRequest.getLeadId())).thenReturn(Optional.of(testLead));
            when(productRepository.findById(anyString())).thenReturn(Optional.empty());

            // When & Then
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> dealService.createDeal(createRequest, adminAuth)
            );
            assertTrue(exception.getMessage().contains("Product"));
            verify(dealRepository, never()).save(any(Deal.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when sales rep IDs do not exist")
        void createDeal_WithInvalidSalesRepIds_ThrowsBadRequestException() {
            // Given
            Lead testLead = TestDataFactory.createTestLead();
            Product mockProduct = mock(Product.class);

            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(dealRepository.countByDealNameAndDeletedFalse(createRequest.getDealName())).thenReturn(0L);
            when(leadRepository.findById(createRequest.getLeadId())).thenReturn(Optional.of(testLead));
            when(productRepository.findById(anyString())).thenReturn(Optional.of(mockProduct));
            when(mockProduct.isDeleted()).thenReturn(false);
            when(userRepository.findById(anyString())).thenReturn(Optional.empty());

            // When & Then
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> dealService.createDeal(createRequest, adminAuth)
            );
            assertTrue(exception.getMessage().contains("User"));
            verify(dealRepository, never()).save(any(Deal.class));
        }

        @Test
        @DisplayName("Should succeed when lead ID is null")
        void createDeal_WithNullLeadId_SucceedsWithNullLeadId() {
            // Given
            CreateDealRequest requestWithoutLead = TestDataFactory.createTestCreateDealRequestWithoutLeadId();
            Deal dealWithoutLead = TestDataFactory.createTestDeal();
            dealWithoutLead.setLeadId(null);
            DealDTO dtoWithoutLead = TestDataFactory.createTestDealDTO();
            dtoWithoutLead.setLeadId(null);
            Product mockProduct = mock(Product.class);
            User mockUser = mock(User.class);

            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(dealRepository.countByDealNameAndDeletedFalse(requestWithoutLead.getDealName())).thenReturn(0L);
            when(productRepository.findById(anyString())).thenReturn(Optional.of(mockProduct));
            when(mockProduct.isDeleted()).thenReturn(false);
            when(userRepository.findById(anyString())).thenReturn(Optional.of(mockUser));
            when(mockUser.isDeleted()).thenReturn(false);
            when(dealRepository.save(any(Deal.class))).thenReturn(dealWithoutLead);
            when(dealMapper.toDTO(dealWithoutLead)).thenReturn(dtoWithoutLead);

            // When
            DealDTO result = dealService.createDeal(requestWithoutLead, adminAuth);

            // Then
            assertNotNull(result);
            verify(leadRepository, never()).findById(anyString());
            verify(dealRepository).save(any(Deal.class));
        }

        @Test
        @DisplayName("Should succeed when sales reps list is empty")
        void createDeal_WithEmptySalesReps_SucceedsWithEmptyList() {
            // Given
            CreateDealRequest requestWithEmptySalesReps = CreateDealRequest.builder()
                .dealName(TestDataFactory.TEST_DEAL_NAME)
                .productIds(Arrays.asList(TestDataFactory.TEST_PRODUCT_ID))
                .salesReps(Collections.emptyList())
                .leadId(TestDataFactory.TEST_LEAD_ID)
                .build();
            Lead testLead = TestDataFactory.createTestLead();
            Product mockProduct = mock(Product.class);

            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(dealRepository.countByDealNameAndDeletedFalse(requestWithEmptySalesReps.getDealName())).thenReturn(0L);
            when(leadRepository.findById(requestWithEmptySalesReps.getLeadId())).thenReturn(Optional.of(testLead));
            when(productRepository.findById(anyString())).thenReturn(Optional.of(mockProduct));
            when(mockProduct.isDeleted()).thenReturn(false);
            when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);
            when(dealMapper.toDTO(testDeal)).thenReturn(testDealDTO);

            // When
            DealDTO result = dealService.createDeal(requestWithEmptySalesReps, adminAuth);

            // Then
            assertNotNull(result);
            verify(userRepository, never()).findById(anyString());
            verify(dealRepository).save(any(Deal.class));
        }
    }

    // ===================== updateDeal Tests =====================

    @Nested
    @DisplayName("updateDeal Tests")
    class UpdateDealTests {

        @Test
        @DisplayName("Should update deal when valid request from admin")
        void updateDeal_WithValidRequest_ReturnsUpdatedDeal() {
            // Given
            String dealId = TestDataFactory.TEST_DEAL_ID;
            Product mockProduct = mock(Product.class);

            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(dealRepository.findById(dealId)).thenReturn(Optional.of(testDeal));
            when(dealRepository.countByDealNameAndIdNotAndDeletedFalse(updateRequest.getDealName(), dealId)).thenReturn(0L);
            when(productRepository.findById(anyString())).thenReturn(Optional.of(mockProduct));
            when(mockProduct.isDeleted()).thenReturn(false);
            when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);
            when(dealMapper.toDTO(testDeal)).thenReturn(testDealDTO);

            // When
            DealDTO result = dealService.updateDeal(dealId, updateRequest, adminAuth);

            // Then
            assertNotNull(result);
            verify(dealRepository).save(any(Deal.class));
            verify(auditLogService).logDealUpdated(eq(adminAuth), anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw ForbiddenException when non-admin tries to update")
        void updateDeal_WithNonAdminUser_ThrowsForbiddenException() {
            // Given
            when(permissionEvaluator.isAdmin(nonAdminAuth)).thenReturn(false);

            // When & Then
            assertThrows(
                ForbiddenException.class,
                () -> dealService.updateDeal(TestDataFactory.TEST_DEAL_ID, updateRequest, nonAdminAuth)
            );
            verify(dealRepository, never()).save(any(Deal.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deal not found")
        void updateDeal_WithInvalidId_ThrowsResourceNotFoundException() {
            // Given
            String invalidId = "invalid-id";
            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(dealRepository.findById(invalidId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(
                ResourceNotFoundException.class,
                () -> dealService.updateDeal(invalidId, updateRequest, adminAuth)
            );
            verify(dealRepository, never()).save(any(Deal.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when updating to duplicate name")
        void updateDeal_WithDuplicateName_ThrowsBadRequestException() {
            // Given
            String dealId = TestDataFactory.TEST_DEAL_ID;

            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(dealRepository.findById(dealId)).thenReturn(Optional.of(testDeal));
            when(dealRepository.countByDealNameAndIdNotAndDeletedFalse(updateRequest.getDealName(), dealId)).thenReturn(1L);

            // When & Then
            assertThrows(
                BadRequestException.class,
                () -> dealService.updateDeal(dealId, updateRequest, adminAuth)
            );
            verify(dealRepository, never()).save(any(Deal.class));
        }

        @Test
        @DisplayName("Should validate and update when new lead ID is provided")
        void updateDeal_WithNewLeadId_ValidatesAndUpdates() {
            // Given
            String dealId = TestDataFactory.TEST_DEAL_ID;
            String newLeadId = "new-lead-id";
            Lead newLead = TestDataFactory.createTestLeadWithId(newLeadId);
            UpdateDealRequest requestWithNewLead = UpdateDealRequest.builder()
                .leadId(newLeadId)
                .build();

            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(dealRepository.findById(dealId)).thenReturn(Optional.of(testDeal));
            when(leadRepository.findById(newLeadId)).thenReturn(Optional.of(newLead));
            when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);
            when(dealMapper.toDTO(testDeal)).thenReturn(testDealDTO);

            // When
            DealDTO result = dealService.updateDeal(dealId, requestWithNewLead, adminAuth);

            // Then
            assertNotNull(result);
            verify(leadRepository).findById(newLeadId);
            verify(dealRepository).save(any(Deal.class));
        }

        @Test
        @DisplayName("Should validate and update when new product IDs are provided")
        void updateDeal_WithNewProductIds_ValidatesAndUpdates() {
            // Given
            String dealId = TestDataFactory.TEST_DEAL_ID;
            String newProductId = "new-product-id";
            Product mockProduct = mock(Product.class);
            UpdateDealRequest requestWithNewProducts = UpdateDealRequest.builder()
                .productIds(Arrays.asList(newProductId))
                .build();

            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(dealRepository.findById(dealId)).thenReturn(Optional.of(testDeal));
            when(productRepository.findById(newProductId)).thenReturn(Optional.of(mockProduct));
            when(mockProduct.isDeleted()).thenReturn(false);
            when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);
            when(dealMapper.toDTO(testDeal)).thenReturn(testDealDTO);

            // When
            DealDTO result = dealService.updateDeal(dealId, requestWithNewProducts, adminAuth);

            // Then
            assertNotNull(result);
            verify(productRepository).findById(newProductId);
            verify(dealRepository).save(any(Deal.class));
        }

        @Test
        @DisplayName("Should validate and update when new sales reps are provided")
        void updateDeal_WithNewSalesReps_ValidatesAndUpdates() {
            // Given
            String dealId = TestDataFactory.TEST_DEAL_ID;
            User mockUser = mock(User.class);
            List<SalesRepAssignment> newSalesReps = TestDataFactory.createSalesRepAssignments();
            UpdateDealRequest requestWithNewSalesReps = UpdateDealRequest.builder()
                .salesReps(newSalesReps)
                .build();

            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(dealRepository.findById(dealId)).thenReturn(Optional.of(testDeal));
            when(userRepository.findById(anyString())).thenReturn(Optional.of(mockUser));
            when(mockUser.isDeleted()).thenReturn(false);
            when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);
            when(dealMapper.toDTO(testDeal)).thenReturn(testDealDTO);

            // When
            DealDTO result = dealService.updateDeal(dealId, requestWithNewSalesReps, adminAuth);

            // Then
            assertNotNull(result);
            verify(userRepository, atLeastOnce()).findById(anyString());
            verify(dealRepository).save(any(Deal.class));
        }
    }

    // ===================== deleteDeal Tests =====================

    @Nested
    @DisplayName("deleteDeal Tests")
    class DeleteDealTests {

        @Test
        @DisplayName("Should delete deal when valid ID from admin")
        void deleteDeal_WithValidId_DeletesDeal() {
            // Given
            String dealId = TestDataFactory.TEST_DEAL_ID;

            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(dealRepository.findById(dealId)).thenReturn(Optional.of(testDeal));

            // When
            dealService.deleteDeal(dealId, adminAuth);

            // Then
            verify(dealRepository).delete(testDeal);
            verify(auditLogService).logDealDeleted(eq(adminAuth), anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw ForbiddenException when non-admin tries to delete")
        void deleteDeal_WithNonAdminUser_ThrowsForbiddenException() {
            // Given
            when(permissionEvaluator.isAdmin(nonAdminAuth)).thenReturn(false);

            // When & Then
            assertThrows(
                ForbiddenException.class,
                () -> dealService.deleteDeal(TestDataFactory.TEST_DEAL_ID, nonAdminAuth)
            );
            verify(dealRepository, never()).delete(any(Deal.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deal not found")
        void deleteDeal_WithInvalidId_ThrowsResourceNotFoundException() {
            // Given
            String invalidId = "invalid-id";
            when(permissionEvaluator.isAdmin(adminAuth)).thenReturn(true);
            when(dealRepository.findById(invalidId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(
                ResourceNotFoundException.class,
                () -> dealService.deleteDeal(invalidId, adminAuth)
            );
            verify(dealRepository, never()).delete(any(Deal.class));
        }
    }

    // ===================== searchDeals Tests =====================

    @Nested
    @DisplayName("searchDeals Tests")
    class SearchDealsTests {

        @Test
        @DisplayName("Should return matching deals")
        void searchDeals_WithMatchingTerm_ReturnsMatchingDeals() {
            // Given
            String searchTerm = "Test";
            List<Deal> matchingDeals = Arrays.asList(testDeal);
            List<DealDTO> expectedDTOs = Arrays.asList(testDealDTO);

            when(dealRepository.searchDeals(searchTerm)).thenReturn(matchingDeals);
            when(dealMapper.toDTO(matchingDeals)).thenReturn(expectedDTOs);

            // When
            List<DealDTO> result = dealService.searchDeals(searchTerm);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(dealRepository).searchDeals(searchTerm);
        }

        @Test
        @DisplayName("Should return empty list when no matches")
        void searchDeals_WithNoMatches_ReturnsEmptyList() {
            // Given
            String searchTerm = "NonExistent";
            when(dealRepository.searchDeals(searchTerm)).thenReturn(Collections.emptyList());
            when(dealMapper.toDTO(Collections.emptyList())).thenReturn(Collections.emptyList());

            // When
            List<DealDTO> result = dealService.searchDeals(searchTerm);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ===================== getTotalDealCount Tests =====================

    @Nested
    @DisplayName("getTotalDealCount Tests")
    class GetTotalDealCountTests {

        @Test
        @DisplayName("Should return correct count")
        void getTotalDealCount_ReturnsCorrectCount() {
            // Given
            long expectedCount = 10L;
            when(dealRepository.countByDeletedFalse()).thenReturn(expectedCount);

            // When
            long result = dealService.getTotalDealCount();

            // Then
            assertEquals(expectedCount, result);
            verify(dealRepository).countByDeletedFalse();
        }

        @Test
        @DisplayName("Should return zero when no deals")
        void getTotalDealCount_WhenNoDeals_ReturnsZero() {
            // Given
            when(dealRepository.countByDeletedFalse()).thenReturn(0L);

            // When
            long result = dealService.getTotalDealCount();

            // Then
            assertEquals(0L, result);
        }
    }

    // ===================== getDealsByLeadId Tests =====================

    @Nested
    @DisplayName("getDealsByLeadId Tests")
    class GetDealsByLeadIdTests {

        @Test
        @DisplayName("Should return deals for valid lead ID")
        void getDealsByLeadId_WithValidLeadId_ReturnsDeals() {
            // Given
            String leadId = TestDataFactory.TEST_LEAD_ID;
            List<Deal> deals = Arrays.asList(testDeal);
            List<DealDTO> expectedDTOs = Arrays.asList(testDealDTO);

            when(dealRepository.findByLeadIdAndDeletedFalse(leadId)).thenReturn(deals);
            when(dealMapper.toDTO(deals)).thenReturn(expectedDTOs);

            // When
            List<DealDTO> result = dealService.getDealsByLeadId(leadId);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(dealRepository).findByLeadIdAndDeletedFalse(leadId);
        }

        @Test
        @DisplayName("Should return empty list when no matching leads")
        void getDealsByLeadId_WithNoMatchingLeads_ReturnsEmptyList() {
            // Given
            String leadId = "non-existent-lead";
            when(dealRepository.findByLeadIdAndDeletedFalse(leadId)).thenReturn(Collections.emptyList());
            when(dealMapper.toDTO(Collections.emptyList())).thenReturn(Collections.emptyList());

            // When
            List<DealDTO> result = dealService.getDealsByLeadId(leadId);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ===================== getDealsByProductId Tests =====================

    @Nested
    @DisplayName("getDealsByProductId Tests")
    class GetDealsByProductIdTests {

        @Test
        @DisplayName("Should return deals for valid product ID")
        void getDealsByProductId_WithValidProductId_ReturnsDeals() {
            // Given
            String productId = TestDataFactory.TEST_PRODUCT_ID;
            List<Deal> deals = Arrays.asList(testDeal);
            List<DealDTO> expectedDTOs = Arrays.asList(testDealDTO);

            when(dealRepository.findByProductIdAndDeletedFalse(productId)).thenReturn(deals);
            when(dealMapper.toDTO(deals)).thenReturn(expectedDTOs);

            // When
            List<DealDTO> result = dealService.getDealsByProductId(productId);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(dealRepository).findByProductIdAndDeletedFalse(productId);
        }

        @Test
        @DisplayName("Should return empty list when no matching products")
        void getDealsByProductId_WithNoMatchingProducts_ReturnsEmptyList() {
            // Given
            String productId = "non-existent-product";
            when(dealRepository.findByProductIdAndDeletedFalse(productId)).thenReturn(Collections.emptyList());
            when(dealMapper.toDTO(Collections.emptyList())).thenReturn(Collections.emptyList());

            // When
            List<DealDTO> result = dealService.getDealsByProductId(productId);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ===================== getDealsBySalesRepId Tests =====================

    @Nested
    @DisplayName("getDealsBySalesRepId Tests")
    class GetDealsBySalesRepIdTests {

        @Test
        @DisplayName("Should return deals for valid sales rep ID")
        void getDealsBySalesRepId_WithValidSalesRepId_ReturnsDeals() {
            // Given
            String salesRepId = TestDataFactory.TEST_SALES_REP_ID;
            List<Deal> deals = Arrays.asList(testDeal);
            List<DealDTO> expectedDTOs = Arrays.asList(testDealDTO);

            when(dealRepository.findBySalesRepAndDeletedFalse(salesRepId)).thenReturn(deals);
            when(dealMapper.toDTO(deals)).thenReturn(expectedDTOs);

            // When
            List<DealDTO> result = dealService.getDealsBySalesRepId(salesRepId);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(dealRepository).findBySalesRepAndDeletedFalse(salesRepId);
        }

        @Test
        @DisplayName("Should return empty list when no matching sales reps")
        void getDealsBySalesRepId_WithNoMatchingSalesReps_ReturnsEmptyList() {
            // Given
            String salesRepId = "non-existent-sales-rep";
            when(dealRepository.findBySalesRepAndDeletedFalse(salesRepId)).thenReturn(Collections.emptyList());
            when(dealMapper.toDTO(Collections.emptyList())).thenReturn(Collections.emptyList());

            // When
            List<DealDTO> result = dealService.getDealsBySalesRepId(salesRepId);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }
}
