package com.ceedpods.crmbuild.mapper;

import com.ceedpods.crmbuild.dto.lead.LeadDTO;
import com.ceedpods.crmbuild.entity.lead.Lead;
import com.ceedpods.crmbuild.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LeadMapper Unit Tests")
class LeadMapperTest {

    private LeadMapper leadMapper;

    @BeforeEach
    void setUp() {
        leadMapper = new LeadMapper();
    }

    // ===================== toDTO Tests =====================

    @Nested
    @DisplayName("toDTO Tests")
    class ToDTOTests {

        @Test
        @DisplayName("Should convert entity to DTO correctly")
        void toDTO_WithValidEntity_ReturnsCorrectDTO() {
            // Given
            Lead lead = TestDataFactory.createTestLead();

            // When
            LeadDTO result = leadMapper.toDTO(lead);

            // Then
            assertNotNull(result);
            assertEquals(lead.getId(), result.getId());
            assertEquals(lead.getLeadName(), result.getLeadName());
            assertEquals(lead.getCompany(), result.getCompany());
            assertEquals(lead.getCompanyAddress(), result.getCompanyAddress());
            assertEquals(lead.getCompanyWebsite(), result.getCompanyWebsite());
            assertEquals(lead.getOriginatedFrom(), result.getOriginatedFrom());
            assertEquals(lead.getPlatform(), result.getPlatform());
            assertEquals(lead.getContactNumber(), result.getContactNumber());
            assertEquals(lead.getDealId(), result.getDealId());
            assertEquals(lead.getCommunication(), result.getCommunication());
        }

        @Test
        @DisplayName("Should return null when entity is null")
        void toDTO_WithNullEntity_ReturnsNull() {
            // When
            LeadDTO result = leadMapper.toDTO((Lead) null);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Should map audit fields correctly")
        void toDTO_WithAuditFields_MapsAuditFieldsCorrectly() {
            // Given
            Lead lead = TestDataFactory.createTestLead();

            // When
            LeadDTO result = leadMapper.toDTO(lead);

            // Then
            assertNotNull(result);
            assertEquals(lead.getCreatedAt(), result.getCreatedAt());
            assertEquals(lead.getUpdatedAt(), result.getUpdatedAt());
            assertEquals(lead.getCreatedBy(), result.getCreatedBy());
            assertEquals(lead.getUpdatedBy(), result.getUpdatedBy());
            assertEquals(lead.isDeleted(), result.isDeleted());
            assertEquals(lead.getDeletedAt(), result.getDeletedAt());
            assertEquals(lead.getDeletedBy(), result.getDeletedBy());
        }

        @Test
        @DisplayName("Should handle entity with null optional fields")
        void toDTO_WithNullOptionalFields_HandlesNullsGracefully() {
            // Given
            Lead lead = Lead.builder()
                .id(TestDataFactory.TEST_LEAD_ID)
                .leadName(TestDataFactory.TEST_LEAD_NAME)
                .originatedFrom(null)
                .company(null)
                .dealId(null)
                .communication(null)
                .build();

            // When
            LeadDTO result = leadMapper.toDTO(lead);

            // Then
            assertNotNull(result);
            assertEquals(lead.getId(), result.getId());
            assertEquals(lead.getLeadName(), result.getLeadName());
            assertNull(result.getOriginatedFrom());
            assertNull(result.getCompany());
            assertNull(result.getDealId());
            assertNull(result.getCommunication());
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
            LeadDTO dto = TestDataFactory.createTestLeadDTO();

            // When
            Lead result = leadMapper.toEntity(dto);

            // Then
            assertNotNull(result);
            assertEquals(dto.getId(), result.getId());
            assertEquals(dto.getLeadName(), result.getLeadName());
            assertEquals(dto.getCompany(), result.getCompany());
            assertEquals(dto.getCompanyAddress(), result.getCompanyAddress());
            assertEquals(dto.getCompanyWebsite(), result.getCompanyWebsite());
            assertEquals(dto.getOriginatedFrom(), result.getOriginatedFrom());
            assertEquals(dto.getPlatform(), result.getPlatform());
            assertEquals(dto.getContactNumber(), result.getContactNumber());
            assertEquals(dto.getDealId(), result.getDealId());
        }

        @Test
        @DisplayName("Should return null when DTO is null")
        void toEntity_WithNullDTO_ReturnsNull() {
            // When
            Lead result = leadMapper.toEntity((LeadDTO) null);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Should map audit fields correctly")
        void toEntity_WithAuditFields_MapsAuditFieldsCorrectly() {
            // Given
            LeadDTO dto = TestDataFactory.createTestLeadDTO();

            // When
            Lead result = leadMapper.toEntity(dto);

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
    }

    // ===================== toDTO List Tests =====================

    @Nested
    @DisplayName("toDTO List Tests")
    class ToDTOListTests {

        @Test
        @DisplayName("Should convert entity list to DTO list correctly")
        void toDTOList_WithValidEntities_ReturnsCorrectDTOList() {
            // Given
            List<Lead> leads = TestDataFactory.createTestLeadList(3);

            // When
            List<LeadDTO> result = leadMapper.toDTO(leads);

            // Then
            assertNotNull(result);
            assertEquals(3, result.size());
            for (int i = 0; i < leads.size(); i++) {
                assertEquals(leads.get(i).getId(), result.get(i).getId());
                assertEquals(leads.get(i).getLeadName(), result.get(i).getLeadName());
            }
        }

        @Test
        @DisplayName("Should return null when list is null")
        void toDTOList_WithNullList_ReturnsNull() {
            // When
            List<LeadDTO> result = leadMapper.toDTO((List<Lead>) null);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Should return empty list when input list is empty")
        void toDTOList_WithEmptyList_ReturnsEmptyList() {
            // Given
            List<Lead> emptyList = Collections.emptyList();

            // When
            List<LeadDTO> result = leadMapper.toDTO(emptyList);

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
            List<LeadDTO> dtos = TestDataFactory.createTestLeadDTOList(3);

            // When
            List<Lead> result = leadMapper.toEntity(dtos);

            // Then
            assertNotNull(result);
            assertEquals(3, result.size());
            for (int i = 0; i < dtos.size(); i++) {
                assertEquals(dtos.get(i).getId(), result.get(i).getId());
                assertEquals(dtos.get(i).getLeadName(), result.get(i).getLeadName());
            }
        }

        @Test
        @DisplayName("Should return null when list is null")
        void toEntityList_WithNullList_ReturnsNull() {
            // When
            List<Lead> result = leadMapper.toEntity((List<LeadDTO>) null);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Should return empty list when input list is empty")
        void toEntityList_WithEmptyList_ReturnsEmptyList() {
            // Given
            List<LeadDTO> emptyList = Collections.emptyList();

            // When
            List<Lead> result = leadMapper.toEntity(emptyList);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }
}
