package com.ceedpods.crmbuild.util;

import com.ceedpods.crmbuild.dto.deal.DealDTO;
import com.ceedpods.crmbuild.dto.deal.SalesRepAssignment;
import com.ceedpods.crmbuild.dto.lead.LeadDTO;
import com.ceedpods.crmbuild.dto.request.CreateDealRequest;
import com.ceedpods.crmbuild.dto.request.CreateLeadRequest;
import com.ceedpods.crmbuild.dto.request.UpdateDealRequest;
import com.ceedpods.crmbuild.dto.request.UpdateLeadRequest;
import com.ceedpods.crmbuild.entity.deal.Deal;
import com.ceedpods.crmbuild.entity.lead.Lead;
import com.ceedpods.crmbuild.enums.LeadOriginatedFrom;
import com.ceedpods.crmbuild.enums.SalesRepPosition;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Factory class for creating test data objects used across unit tests.
 */
public class TestDataFactory {

    // ===================== Common Test Constants =====================

    public static final String TEST_LEAD_ID = UUID.randomUUID().toString();
    public static final String TEST_LEAD_NAME = "Test Lead";
    public static final String TEST_COMPANY = "Test Company";
    public static final String TEST_COMPANY_ADDRESS = "123 Test Street";
    public static final String TEST_COMPANY_WEBSITE = "https://test.com";
    public static final String TEST_PLATFORM = "Web";
    public static final String TEST_CONTACT_NUMBER = "+1234567890";

    public static final String TEST_DEAL_ID = UUID.randomUUID().toString();
    public static final String TEST_DEAL_NAME = "Test Deal";
    public static final String TEST_PRODUCT_ID = UUID.randomUUID().toString();
    public static final String TEST_SALES_REP_ID = UUID.randomUUID().toString();

    public static final String TEST_USER_ID = "test-user-123";
    public static final String ADMIN_USER_ID = "admin-user-456";

    // ===================== Lead Test Data =====================

    /**
     * Creates a test Lead entity with default values
     */
    public static Lead createTestLead() {
        Lead lead = Lead.builder()
            .id(TEST_LEAD_ID)
            .leadName(TEST_LEAD_NAME)
            .company(TEST_COMPANY)
            .companyAddress(TEST_COMPANY_ADDRESS)
            .companyWebsite(TEST_COMPANY_WEBSITE)
            .originatedFrom(LeadOriginatedFrom.EMAIL)
            .platform(TEST_PLATFORM)
            .contactNumber(TEST_CONTACT_NUMBER)
            .communication(createCommunicationList())
            .dealId(TEST_DEAL_ID)
            .build();

        // Set audit fields
        lead.setCreatedAt(LocalDateTime.now());
        lead.setUpdatedAt(LocalDateTime.now());
        lead.setCreatedBy(ADMIN_USER_ID);
        lead.setUpdatedBy(ADMIN_USER_ID);
        lead.setDeleted(false);

        return lead;
    }

    /**
     * Creates a test Lead entity with a specific ID
     */
    public static Lead createTestLeadWithId(String id) {
        Lead lead = createTestLead();
        lead.setId(id);
        return lead;
    }

    /**
     * Creates a test Lead entity with a specific name
     */
    public static Lead createTestLeadWithName(String name) {
        Lead lead = createTestLead();
        lead.setLeadName(name);
        return lead;
    }

    /**
     * Creates a soft-deleted Lead entity
     */
    public static Lead createDeletedLead() {
        Lead lead = createTestLead();
        lead.setDeleted(true);
        lead.setDeletedAt(LocalDateTime.now());
        lead.setDeletedBy(ADMIN_USER_ID);
        return lead;
    }

    /**
     * Creates a test LeadDTO with default values
     */
    public static LeadDTO createTestLeadDTO() {
        LeadDTO dto = LeadDTO.builder()
            .id(TEST_LEAD_ID)
            .leadName(TEST_LEAD_NAME)
            .company(TEST_COMPANY)
            .companyAddress(TEST_COMPANY_ADDRESS)
            .companyWebsite(TEST_COMPANY_WEBSITE)
            .originatedFrom(LeadOriginatedFrom.EMAIL)
            .platform(TEST_PLATFORM)
            .contactNumber(TEST_CONTACT_NUMBER)
            .communication(createCommunicationList())
            .dealId(TEST_DEAL_ID)
            .build();

        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        dto.setCreatedBy(ADMIN_USER_ID);
        dto.setUpdatedBy(ADMIN_USER_ID);
        dto.setDeleted(false);

        return dto;
    }

    /**
     * Creates a test CreateLeadRequest with default values
     */
    public static CreateLeadRequest createTestCreateLeadRequest() {
        return CreateLeadRequest.builder()
            .leadName(TEST_LEAD_NAME)
            .company(TEST_COMPANY)
            .companyAddress(TEST_COMPANY_ADDRESS)
            .companyWebsite(TEST_COMPANY_WEBSITE)
            .originatedFrom(LeadOriginatedFrom.EMAIL)
            .platform(TEST_PLATFORM)
            .contactNumber(TEST_CONTACT_NUMBER)
            .communication(createCommunicationList())
            .dealId(TEST_DEAL_ID)
            .build();
    }

    /**
     * Creates a test CreateLeadRequest without dealId
     */
    public static CreateLeadRequest createTestCreateLeadRequestWithoutDealId() {
        return CreateLeadRequest.builder()
            .leadName(TEST_LEAD_NAME)
            .company(TEST_COMPANY)
            .companyAddress(TEST_COMPANY_ADDRESS)
            .companyWebsite(TEST_COMPANY_WEBSITE)
            .originatedFrom(LeadOriginatedFrom.EMAIL)
            .platform(TEST_PLATFORM)
            .contactNumber(TEST_CONTACT_NUMBER)
            .communication(createCommunicationList())
            .dealId(null)
            .build();
    }

    /**
     * Creates a test UpdateLeadRequest with default values
     */
    public static UpdateLeadRequest createTestUpdateLeadRequest() {
        return UpdateLeadRequest.builder()
            .leadName("Updated Lead Name")
            .company("Updated Company")
            .companyAddress("456 Updated Street")
            .originatedFrom(LeadOriginatedFrom.FACEBOOK)
            .build();
    }

    /**
     * Creates a list of communication key-value pairs
     */
    public static List<Map<String, String>> createCommunicationList() {
        List<Map<String, String>> communication = new ArrayList<>();

        Map<String, String> phone = new HashMap<>();
        phone.put("phone", "+1234567890");
        communication.add(phone);

        Map<String, String> email = new HashMap<>();
        email.put("email", "test@example.com");
        communication.add(email);

        return communication;
    }

    /**
     * Creates a list of test Lead entities
     */
    public static List<Lead> createTestLeadList(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> {
                Lead lead = createTestLead();
                lead.setId(UUID.randomUUID().toString());
                lead.setLeadName("Test Lead " + i);
                return lead;
            })
            .collect(Collectors.toList());
    }

    /**
     * Creates a list of test LeadDTO objects
     */
    public static List<LeadDTO> createTestLeadDTOList(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> {
                LeadDTO dto = createTestLeadDTO();
                dto.setId(UUID.randomUUID().toString());
                dto.setLeadName("Test Lead " + i);
                return dto;
            })
            .collect(Collectors.toList());
    }

    // ===================== Deal Test Data =====================

    /**
     * Creates a test Deal entity with default values
     */
    public static Deal createTestDeal() {
        Deal deal = Deal.builder()
            .id(TEST_DEAL_ID)
            .dealName(TEST_DEAL_NAME)
            .productIds(Arrays.asList(TEST_PRODUCT_ID))
            .salesReps(createSalesRepAssignments())
            .leadId(TEST_LEAD_ID)
            .build();

        // Set audit fields
        deal.setCreatedAt(LocalDateTime.now());
        deal.setUpdatedAt(LocalDateTime.now());
        deal.setCreatedBy(ADMIN_USER_ID);
        deal.setUpdatedBy(ADMIN_USER_ID);
        deal.setDeleted(false);

        return deal;
    }

    /**
     * Creates a test Deal entity with a specific ID
     */
    public static Deal createTestDealWithId(String id) {
        Deal deal = createTestDeal();
        deal.setId(id);
        return deal;
    }

    /**
     * Creates a test Deal entity with a specific name
     */
    public static Deal createTestDealWithName(String name) {
        Deal deal = createTestDeal();
        deal.setDealName(name);
        return deal;
    }

    /**
     * Creates a soft-deleted Deal entity
     */
    public static Deal createDeletedDeal() {
        Deal deal = createTestDeal();
        deal.setDeleted(true);
        deal.setDeletedAt(LocalDateTime.now());
        deal.setDeletedBy(ADMIN_USER_ID);
        return deal;
    }

    /**
     * Creates a test DealDTO with default values
     */
    public static DealDTO createTestDealDTO() {
        DealDTO dto = DealDTO.builder()
            .id(TEST_DEAL_ID)
            .dealName(TEST_DEAL_NAME)
            .productIds(Arrays.asList(TEST_PRODUCT_ID))
            .salesReps(createSalesRepAssignments())
            .leadId(TEST_LEAD_ID)
            .build();

        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        dto.setCreatedBy(ADMIN_USER_ID);
        dto.setUpdatedBy(ADMIN_USER_ID);
        dto.setDeleted(false);

        return dto;
    }

    /**
     * Creates a test CreateDealRequest with default values
     */
    public static CreateDealRequest createTestCreateDealRequest() {
        return CreateDealRequest.builder()
            .dealName(TEST_DEAL_NAME)
            .productIds(Arrays.asList(TEST_PRODUCT_ID))
            .salesReps(createSalesRepAssignments())
            .leadId(TEST_LEAD_ID)
            .build();
    }

    /**
     * Creates a test CreateDealRequest without leadId
     */
    public static CreateDealRequest createTestCreateDealRequestWithoutLeadId() {
        return CreateDealRequest.builder()
            .dealName(TEST_DEAL_NAME)
            .productIds(Arrays.asList(TEST_PRODUCT_ID))
            .salesReps(createSalesRepAssignments())
            .leadId(null)
            .build();
    }

    /**
     * Creates a test UpdateDealRequest with default values
     */
    public static UpdateDealRequest createTestUpdateDealRequest() {
        return UpdateDealRequest.builder()
            .dealName("Updated Deal Name")
            .productIds(Arrays.asList(TEST_PRODUCT_ID, UUID.randomUUID().toString()))
            .build();
    }

    /**
     * Creates a list of SalesRepAssignment objects
     */
    public static List<SalesRepAssignment> createSalesRepAssignments() {
        return Arrays.asList(
            SalesRepAssignment.builder()
                .id(TEST_SALES_REP_ID)
                .position(SalesRepPosition.PRIMARY)
                .build(),
            SalesRepAssignment.builder()
                .id(UUID.randomUUID().toString())
                .position(SalesRepPosition.CO_PRIMARY)
                .build()
        );
    }

    /**
     * Creates a list of test Deal entities
     */
    public static List<Deal> createTestDealList(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> {
                Deal deal = createTestDeal();
                deal.setId(UUID.randomUUID().toString());
                deal.setDealName("Test Deal " + i);
                return deal;
            })
            .collect(Collectors.toList());
    }

    /**
     * Creates a list of test DealDTO objects
     */
    public static List<DealDTO> createTestDealDTOList(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> {
                DealDTO dto = createTestDealDTO();
                dto.setId(UUID.randomUUID().toString());
                dto.setDealName("Test Deal " + i);
                return dto;
            })
            .collect(Collectors.toList());
    }
}
