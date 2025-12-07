package com.ceedpods.crmbuild.mapper;

import com.ceedpods.crmbuild.dto.deal.DealDTO;
import com.ceedpods.crmbuild.dto.deal.SalesRepAssignment;
import com.ceedpods.crmbuild.entity.deal.Deal;
import com.ceedpods.crmbuild.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DealMapper Unit Tests")
class DealMapperTest {

    private DealMapper dealMapper;

    @BeforeEach
    void setUp() {
        dealMapper = new DealMapper();
    }

    // ===================== toDTO Tests =====================

    @Nested
    @DisplayName("toDTO Tests")
    class ToDTOTests {

        @Test
        @DisplayName("Should convert entity to DTO correctly")
        void toDTO_WithValidEntity_ReturnsCorrectDTO() {
            // Given
            Deal deal = TestDataFactory.createTestDeal();

            // When
            DealDTO result = dealMapper.toDTO(deal);

            // Then
            assertNotNull(result);
            assertEquals(deal.getId(), result.getId());
            assertEquals(deal.getDealName(), result.getDealName());
            assertEquals(deal.getLeadId(), result.getLeadId());
            assertEquals(deal.getProductIds().size(), result.getProductIds().size());
            assertEquals(deal.getSalesReps().size(), result.getSalesReps().size());
        }

        @Test
        @DisplayName("Should return null when entity is null")
        void toDTO_WithNullEntity_ReturnsNull() {
            // When
            DealDTO result = dealMapper.toDTO((Deal) null);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Should map audit fields correctly")
        void toDTO_WithAuditFields_MapsAuditFieldsCorrectly() {
            // Given
            Deal deal = TestDataFactory.createTestDeal();

            // When
            DealDTO result = dealMapper.toDTO(deal);

            // Then
            assertNotNull(result);
            assertEquals(deal.getCreatedAt(), result.getCreatedAt());
            assertEquals(deal.getUpdatedAt(), result.getUpdatedAt());
            assertEquals(deal.getCreatedBy(), result.getCreatedBy());
            assertEquals(deal.getUpdatedBy(), result.getUpdatedBy());
            assertEquals(deal.isDeleted(), result.isDeleted());
            assertEquals(deal.getDeletedAt(), result.getDeletedAt());
            assertEquals(deal.getDeletedBy(), result.getDeletedBy());
        }

        @Test
        @DisplayName("Should copy sales reps list correctly (defensive copy)")
        void toDTO_WithSalesReps_CopiesListCorrectly() {
            // Given
            Deal deal = TestDataFactory.createTestDeal();
            int originalSize = deal.getSalesReps().size();

            // When
            DealDTO result = dealMapper.toDTO(deal);

            // Then
            assertNotNull(result.getSalesReps());
            assertEquals(originalSize, result.getSalesReps().size());

            // Verify it's a defensive copy (modifying result shouldn't affect original)
            result.getSalesReps().clear();
            assertEquals(originalSize, deal.getSalesReps().size());
        }

        @Test
        @DisplayName("Should copy product IDs list correctly (defensive copy)")
        void toDTO_WithProductIds_CopiesListCorrectly() {
            // Given
            Deal deal = TestDataFactory.createTestDeal();
            int originalSize = deal.getProductIds().size();

            // When
            DealDTO result = dealMapper.toDTO(deal);

            // Then
            assertNotNull(result.getProductIds());
            assertEquals(originalSize, result.getProductIds().size());

            // Verify it's a defensive copy
            result.getProductIds().clear();
            assertEquals(originalSize, deal.getProductIds().size());
        }

        @Test
        @DisplayName("Should handle entity with null optional fields")
        void toDTO_WithNullOptionalFields_HandlesNullsGracefully() {
            // Given
            Deal deal = Deal.builder()
                .id(TestDataFactory.TEST_DEAL_ID)
                .dealName(TestDataFactory.TEST_DEAL_NAME)
                .productIds(null)
                .salesReps(null)
                .leadId(null)
                .build();

            // When
            DealDTO result = dealMapper.toDTO(deal);

            // Then
            assertNotNull(result);
            assertEquals(deal.getId(), result.getId());
            assertEquals(deal.getDealName(), result.getDealName());
            assertNull(result.getProductIds());
            assertNull(result.getSalesReps());
            assertNull(result.getLeadId());
        }
    }

    // ===================== toEntity Tests =====================

    @Nested
    @DisplayName("toEntity Tests")
    class ToEntityTests {

        @Test
        @DisplayName("Should convert DTO to entity correctly")
        void toEntity_WithValidDTO_ReturnsCorrectEntity() {
            // Given
            DealDTO dto = TestDataFactory.createTestDealDTO();

            // When
            Deal result = dealMapper.toEntity(dto);

            // Then
            assertNotNull(result);
            assertEquals(dto.getId(), result.getId());
            assertEquals(dto.getDealName(), result.getDealName());
            assertEquals(dto.getLeadId(), result.getLeadId());
            assertEquals(dto.getProductIds().size(), result.getProductIds().size());
            assertEquals(dto.getSalesReps().size(), result.getSalesReps().size());
        }

        @Test
        @DisplayName("Should return null when DTO is null")
        void toEntity_WithNullDTO_ReturnsNull() {
            // When
            Deal result = dealMapper.toEntity((DealDTO) null);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Should map audit fields correctly")
        void toEntity_WithAuditFields_MapsAuditFieldsCorrectly() {
            // Given
            DealDTO dto = TestDataFactory.createTestDealDTO();

            // When
            Deal result = dealMapper.toEntity(dto);

            // Then
            assertNotNull(result);
            assertEquals(dto.getCreatedAt(), result.getCreatedAt());
            assertEquals(dto.getUpdatedAt(), result.getUpdatedAt());
            assertEquals(dto.getCreatedBy(), result.getCreatedBy());
            assertEquals(dto.getUpdatedBy(), result.getUpdatedBy());
            assertEquals(dto.isDeleted(), result.isDeleted());
            assertEquals(dto.getDeletedAt(), result.getDeletedAt());
            assertEquals(dto.getDeletedBy(), result.getDeletedBy());
        }

        @Test
        @DisplayName("Should copy sales reps list correctly (defensive copy)")
        void toEntity_WithSalesReps_CopiesListCorrectly() {
            // Given
            DealDTO dto = TestDataFactory.createTestDealDTO();
            int originalSize = dto.getSalesReps().size();

            // When
            Deal result = dealMapper.toEntity(dto);

            // Then
            assertNotNull(result.getSalesReps());
            assertEquals(originalSize, result.getSalesReps().size());

            // Verify it's a defensive copy
            result.getSalesReps().clear();
            assertEquals(originalSize, dto.getSalesReps().size());
        }
    }

    // ===================== toDTO List Tests =====================

    @Nested
    @DisplayName("toDTO List Tests")
    class ToDTOListTests {

        @Test
        @DisplayName("Should convert entity list to DTO list correctly")
        void toDTOList_WithValidEntities_ReturnsCorrectDTOList() {
            // Given
            List<Deal> deals = TestDataFactory.createTestDealList(3);

            // When
            List<DealDTO> result = dealMapper.toDTO(deals);

            // Then
            assertNotNull(result);
            assertEquals(3, result.size());
            for (int i = 0; i < deals.size(); i++) {
                assertEquals(deals.get(i).getId(), result.get(i).getId());
                assertEquals(deals.get(i).getDealName(), result.get(i).getDealName());
            }
        }

        @Test
        @DisplayName("Should return null when list is null")
        void toDTOList_WithNullList_ReturnsNull() {
            // When
            List<DealDTO> result = dealMapper.toDTO((List<Deal>) null);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Should return empty list when input list is empty")
        void toDTOList_WithEmptyList_ReturnsEmptyList() {
            // Given
            List<Deal> emptyList = Collections.emptyList();

            // When
            List<DealDTO> result = dealMapper.toDTO(emptyList);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ===================== toEntity List Tests =====================

    @Nested
    @DisplayName("toEntity List Tests")
    class ToEntityListTests {

        @Test
        @DisplayName("Should convert DTO list to entity list correctly")
        void toEntityList_WithValidDTOs_ReturnsCorrectEntityList() {
            // Given
            List<DealDTO> dtos = TestDataFactory.createTestDealDTOList(3);

            // When
            List<Deal> result = dealMapper.toEntity(dtos);

            // Then
            assertNotNull(result);
            assertEquals(3, result.size());
            for (int i = 0; i < dtos.size(); i++) {
                assertEquals(dtos.get(i).getId(), result.get(i).getId());
                assertEquals(dtos.get(i).getDealName(), result.get(i).getDealName());
            }
        }

        @Test
        @DisplayName("Should return null when list is null")
        void toEntityList_WithNullList_ReturnsNull() {
            // When
            List<Deal> result = dealMapper.toEntity((List<DealDTO>) null);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Should return empty list when input list is empty")
        void toEntityList_WithEmptyList_ReturnsEmptyList() {
            // Given
            List<DealDTO> emptyList = Collections.emptyList();

            // When
            List<Deal> result = dealMapper.toEntity(emptyList);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }
}
