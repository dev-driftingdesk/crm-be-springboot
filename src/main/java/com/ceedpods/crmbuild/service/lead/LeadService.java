package com.ceedpods.crmbuild.service.lead;

import com.ceedpods.crmbuild.dto.deal.SalesRepAssignment;
import com.ceedpods.crmbuild.dto.lead.DealDetailDTO;
import com.ceedpods.crmbuild.dto.lead.LeadDTO;
import com.ceedpods.crmbuild.dto.lead.LeadDetailDTO;
import com.ceedpods.crmbuild.dto.lead.LeadStatisticsDTO;
import com.ceedpods.crmbuild.dto.request.CreateLeadRequest;
import com.ceedpods.crmbuild.dto.request.UpdateLeadRequest;
import com.ceedpods.crmbuild.entity.deal.Deal;
import com.ceedpods.crmbuild.entity.lead.Lead;
import com.ceedpods.crmbuild.entity.product.Product;
import com.ceedpods.crmbuild.entity.user.User;
import com.ceedpods.crmbuild.enums.DealStatus;
import com.ceedpods.crmbuild.enums.LeadStatus;
import com.ceedpods.crmbuild.exception.BadRequestException;
import com.ceedpods.crmbuild.exception.ForbiddenException;
import com.ceedpods.crmbuild.exception.ResourceNotFoundException;
import com.ceedpods.crmbuild.mapper.LeadMapper;
import com.ceedpods.crmbuild.repository.DealRepository;
import com.ceedpods.crmbuild.repository.LeadRepository;
import com.ceedpods.crmbuild.repository.ProductRepository;
import com.ceedpods.crmbuild.repository.UserRepository;
import com.ceedpods.crmbuild.security.CustomPermissionEvaluator;
import com.ceedpods.crmbuild.service.auditLogService.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadService {

    private final LeadRepository leadRepository;
    private final DealRepository dealRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final LeadMapper leadMapper;
    private final CustomPermissionEvaluator permissionEvaluator;
    private final AuditLogService auditLogService;

    /**
     * Get all leads with summary information including:
     * - Lead Name
     * - Created User Name
     * - Source (originatedFrom)
     * - Status
     * - Total Value (sum of product values from associated deals)
     * - Total Deals (count of deals under this lead)
     *
     * This method is optimized to minimize database queries by batch fetching
     * related entities (users, deals, products) instead of querying per lead.
     */
    public List<LeadDTO> getAllLeads() {
        log.info("Fetching all leads");

        // 1. Fetch all non-deleted leads
        List<Lead> leads = leadRepository.findByDeletedFalse();
        if (leads.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. Collect all unique createdBy (keycloakIds) for batch user lookup
        Set<String> createdByIds = leads.stream()
                .map(Lead::getCreatedBy)
                .filter(id -> id != null && !id.isEmpty())
                .collect(Collectors.toSet());

        // 3. Batch fetch users by keycloakId and create a map for quick lookup
        Map<String, User> userMap = userRepository.findByDeletedFalse().stream()
                .filter(user -> createdByIds.contains(user.getKeycloakId()))
                .collect(Collectors.toMap(User::getKeycloakId, Function.identity(), (u1, u2) -> u1));

        // 4. Collect all deal IDs from all leads for batch deal lookup
        Set<String> allDealIds = leads.stream()
                .filter(lead -> lead.getDealIds() != null)
                .flatMap(lead -> lead.getDealIds().stream())
                .collect(Collectors.toSet());

        // 5. Batch fetch all deals and create a map by deal ID
        Map<String, Deal> dealMap = dealRepository.findByDeletedFalse().stream()
                .filter(deal -> allDealIds.contains(deal.getId()))
                .collect(Collectors.toMap(Deal::getId, Function.identity(), (d1, d2) -> d1));

        // 6. Collect all unique product IDs from deals for batch product lookup
        Set<String> allProductIds = dealMap.values().stream()
                .filter(deal -> deal.getProductIds() != null)
                .flatMap(deal -> deal.getProductIds().stream())
                .collect(Collectors.toSet());

        // 7. Batch fetch all products and create a map for quick lookup
        Map<String, Product> productMap = productRepository.findByDeletedFalse().stream()
                .filter(product -> allProductIds.contains(product.getId()))
                .collect(Collectors.toMap(Product::getId, Function.identity(), (p1, p2) -> p1));

        // 8. Build DTOs for each lead
        List<LeadDTO> leadDTOs = new ArrayList<>();
        for (Lead lead : leads) {
            // Get created user name
            String createdUserName = null;
            if (lead.getCreatedBy() != null) {
                User user = userMap.get(lead.getCreatedBy());
                if (user != null) {
                    createdUserName = user.getFullName();
                }
            }

            // Get deals for this lead using lead.dealIds
            List<String> leadDealIds = lead.getDealIds() != null ? lead.getDealIds() : new ArrayList<>();
            long totalDeals = leadDealIds.size();

            // Calculate total value from products in all deals for this lead
            BigDecimal totalValue = BigDecimal.ZERO;
            for (String dealId : leadDealIds) {
                Deal deal = dealMap.get(dealId);
                if (deal != null && deal.getProductIds() != null) {
                    for (String productId : deal.getProductIds()) {
                        Product product = productMap.get(productId);
                        if (product != null && product.getBasePrice() != null) {
                            totalValue = totalValue.add(product.getBasePrice());
                        }
                    }
                }
            }

            // Build DTO with all lead fields
            LeadDTO leadDTO = LeadDTO.builder()
                    .id(lead.getId())
                    .leadName(lead.getLeadName())
                    .createdUserName(createdUserName)
                    .originatedFrom(lead.getOriginatedFrom())
                    .status(lead.getStatus())
                    .company(lead.getCompany())
                    .companyAddress(lead.getCompanyAddress())
                    .companyWebsite(lead.getCompanyWebsite())
                    .communication(lead.getCommunication())
                    .platform(lead.getPlatform())
                    .contactNumber(lead.getContactNumber())
                    .dealIds(leadDealIds)
                    .totalValue(totalValue)
                    .totalDeals(totalDeals)
                    .build();

            // Set audit fields from BaseDTO
            leadDTO.setCreatedAt(lead.getCreatedAt());
            leadDTO.setUpdatedAt(lead.getUpdatedAt());
            leadDTO.setCreatedBy(lead.getCreatedBy());
            leadDTO.setUpdatedBy(lead.getUpdatedBy());
            leadDTO.setDeleted(lead.isDeleted());
            leadDTO.setDeletedAt(lead.getDeletedAt());
            leadDTO.setDeletedBy(lead.getDeletedBy());

            leadDTOs.add(leadDTO);
        }

        log.info("Successfully fetched {} leads", leadDTOs.size());
        return leadDTOs;
    }

    /**
     * Get lead by ID (UUID)
     */
    public LeadDTO getLeadById(String id) {
        log.info("Fetching lead with UUID: {}", id);
        Lead lead = leadRepository.findById(id)
            .filter(l -> !l.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException("Lead not found with UUID: " + id));
        return leadMapper.toDTO(lead);
    }

    /**
     * Get detailed lead information by ID including:
     * - Lead details: personal info (name, contact person), company name
     * - Communication: phone, email, WhatsApp number, etc.
     * - Company details: company name, address, website, industry
     * - Lead statistics: total lead value, total commission, average deal size,
     *                    conversion probability, open deals count, closed deals count, critical items
     * - All deals under the lead: full list of related deals with relevant fields
     */
    public LeadDetailDTO getLeadDetails(String id) {
        log.info("Fetching detailed lead information for UUID: {}", id);

        // 1. Fetch the lead
        Lead lead = leadRepository.findById(id)
            .filter(l -> !l.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException("Lead not found with UUID: " + id));

        // 2. Get deal IDs from the lead
        List<String> leadDealIds = lead.getDealIds() != null ? lead.getDealIds() : new ArrayList<>();

        // 3. Batch fetch all deals for this lead
        Map<String, Deal> dealMap = dealRepository.findByDeletedFalse().stream()
            .filter(deal -> leadDealIds.contains(deal.getId()))
            .collect(Collectors.toMap(Deal::getId, Function.identity(), (d1, d2) -> d1));

        // 4. Collect all product IDs and sales rep IDs from deals
        Set<String> allProductIds = new HashSet<>();
        Set<String> allSalesRepIds = new HashSet<>();
        for (Deal deal : dealMap.values()) {
            if (deal.getProductIds() != null) {
                allProductIds.addAll(deal.getProductIds());
            }
            if (deal.getSalesReps() != null) {
                deal.getSalesReps().forEach(sr -> allSalesRepIds.add(sr.getId()));
            }
        }

        // 5. Batch fetch all products
        Map<String, Product> productMap = productRepository.findByDeletedFalse().stream()
            .filter(product -> allProductIds.contains(product.getId()))
            .collect(Collectors.toMap(Product::getId, Function.identity(), (p1, p2) -> p1));

        // 6. Batch fetch all users (for sales reps and audit info)
        Set<String> allUserIds = new HashSet<>(allSalesRepIds);
        if (lead.getCreatedBy() != null) allUserIds.add(lead.getCreatedBy());
        if (lead.getUpdatedBy() != null) allUserIds.add(lead.getUpdatedBy());

        Map<String, User> userMap = userRepository.findByDeletedFalse().stream()
            .filter(user -> allUserIds.contains(user.getId()) || allUserIds.contains(user.getKeycloakId()))
            .collect(Collectors.toMap(
                user -> user.getId() != null ? user.getId() : user.getKeycloakId(),
                Function.identity(),
                (u1, u2) -> u1
            ));

        // Also create a map by keycloakId for audit lookups
        Map<String, User> userByKeycloakIdMap = userRepository.findByDeletedFalse().stream()
            .filter(user -> user.getKeycloakId() != null)
            .collect(Collectors.toMap(User::getKeycloakId, Function.identity(), (u1, u2) -> u1));

        // 7. Build deal details list and calculate statistics
        List<DealDetailDTO> dealDetails = new ArrayList<>();
        BigDecimal totalLeadValue = BigDecimal.ZERO;
        BigDecimal totalCommission = BigDecimal.ZERO;
        long openDealsCount = 0;
        long closedDealsCount = 0;
        long wonDealsCount = 0;
        long lostDealsCount = 0;
        List<String> criticalItems = new ArrayList<>();

        for (String dealId : leadDealIds) {
            Deal deal = dealMap.get(dealId);
            if (deal == null) continue;

            // Calculate deal value from products
            BigDecimal dealValue = BigDecimal.ZERO;
            List<DealDetailDTO.ProductSummaryDTO> productSummaries = new ArrayList<>();

            if (deal.getProductIds() != null) {
                for (String productId : deal.getProductIds()) {
                    Product product = productMap.get(productId);
                    if (product != null) {
                        if (product.getBasePrice() != null) {
                            dealValue = dealValue.add(product.getBasePrice());
                        }
                        productSummaries.add(DealDetailDTO.ProductSummaryDTO.builder()
                            .id(product.getId())
                            .productName(product.getProductName())
                            .productValue(product.getBasePrice())
                            .productStatus(product.getProductStatus() != null ? product.getProductStatus().getDisplayName() : null)
                            .build());
                    }
                }
            }

            // Build sales rep details
            List<DealDetailDTO.SalesRepDetailDTO> salesRepDetails = new ArrayList<>();
            if (deal.getSalesReps() != null) {
                for (SalesRepAssignment sr : deal.getSalesReps()) {
                    User user = userMap.get(sr.getId());
                    salesRepDetails.add(DealDetailDTO.SalesRepDetailDTO.builder()
                        .id(sr.getId())
                        .fullName(user != null ? user.getFullName() : null)
                        .email(user != null ? user.getEmail() : null)
                        .position(sr.getPosition() != null ? sr.getPosition().getDisplayName() : null)
                        .build());
                }
            }

            // Count deals by status
            DealStatus dealStatus = deal.getStatus();
            if (dealStatus != null) {
                switch (dealStatus) {
                    case OPEN, PENDING, NEGOTIATION -> openDealsCount++;
                    case WON -> {
                        closedDealsCount++;
                        wonDealsCount++;
                    }
                    case LOST -> {
                        closedDealsCount++;
                        lostDealsCount++;
                    }
                }
            } else {
                openDealsCount++; // Default to open if no status
            }

            // Add to totals
            totalLeadValue = totalLeadValue.add(dealValue);
            if (deal.getCommission() != null) {
                totalCommission = totalCommission.add(deal.getCommission());
            }

            // Check for critical items (deals without status or products)
            if (dealStatus == null) {
                criticalItems.add("Deal '" + deal.getDealName() + "' has no status assigned");
            }
            if (deal.getProductIds() == null || deal.getProductIds().isEmpty()) {
                criticalItems.add("Deal '" + deal.getDealName() + "' has no products");
            }

            // Build deal detail DTO
            dealDetails.add(DealDetailDTO.builder()
                .id(deal.getId())
                .dealName(deal.getDealName())
                .status(deal.getStatus())
                .commission(deal.getCommission())
                .dealValue(dealValue)
                .products(productSummaries)
                .salesReps(salesRepDetails)
                .createdAt(deal.getCreatedAt())
                .updatedAt(deal.getUpdatedAt())
                .build());
        }

        // 8. Calculate statistics
        long totalDeals = leadDealIds.size();
        BigDecimal averageDealSize = totalDeals > 0
            ? totalLeadValue.divide(BigDecimal.valueOf(totalDeals), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        // Calculate conversion probability based on lead status
        Double conversionProbability = calculateConversionProbability(lead.getStatus());

        LeadStatisticsDTO statistics = LeadStatisticsDTO.builder()
            .totalLeadValue(totalLeadValue)
            .totalCommission(totalCommission)
            .averageDealSize(averageDealSize)
            .conversionProbability(conversionProbability)
            .openDealsCount(openDealsCount)
            .closedDealsCount(closedDealsCount)
            .wonDealsCount(wonDealsCount)
            .lostDealsCount(lostDealsCount)
            .criticalItems(criticalItems.isEmpty() ? null : criticalItems)
            .build();

        // 9. Get user names for audit fields
        String createdByName = null;
        String updatedByName = null;
        if (lead.getCreatedBy() != null) {
            User createdByUser = userByKeycloakIdMap.get(lead.getCreatedBy());
            if (createdByUser != null) {
                createdByName = createdByUser.getFullName();
            }
        }
        if (lead.getUpdatedBy() != null) {
            User updatedByUser = userByKeycloakIdMap.get(lead.getUpdatedBy());
            if (updatedByUser != null) {
                updatedByName = updatedByUser.getFullName();
            }
        }

        // 10. Build and return the detailed response
        return LeadDetailDTO.builder()
            .id(lead.getId())
            .leadName(lead.getLeadName())
            .status(lead.getStatus())
            .originatedFrom(lead.getOriginatedFrom())
            .personalInfo(LeadDetailDTO.PersonalInfoDTO.builder()
                .contactPersonName(lead.getLeadName())
                .contactNumber(lead.getContactNumber())
                .platform(lead.getPlatform())
                .build())
            .communication(lead.getCommunication())
            .companyDetails(LeadDetailDTO.CompanyDetailsDTO.builder()
                .companyName(lead.getCompany())
                .companyAddress(lead.getCompanyAddress())
                .companyWebsite(lead.getCompanyWebsite())
                .industry(null) // Industry field not available in Lead entity
                .build())
            .statistics(statistics)
            .deals(dealDetails)
            .createdAt(lead.getCreatedAt())
            .updatedAt(lead.getUpdatedAt())
            .createdBy(lead.getCreatedBy())
            .createdByName(createdByName)
            .updatedBy(lead.getUpdatedBy())
            .updatedByName(updatedByName)
            .build();
    }

    /**
     * Calculate conversion probability based on lead status
     */
    private Double calculateConversionProbability(LeadStatus status) {
        if (status == null) {
            return 0.0;
        }
        return switch (status) {
            case NEW -> 10.0;
            case CONTACTED -> 25.0;
            case QUALIFIED -> 50.0;
            case CONVERTED -> 100.0;
            case LOST -> 0.0;
        };
    }

    /**
     * Create new lead (Admin only)
     */
    @Transactional
    public LeadDTO createLead(CreateLeadRequest request, Authentication authentication) {
        log.info("Creating new lead for: {}", request.getLeadName());

        // Check if user is admin - only admins can create leads
        if (!permissionEvaluator.isAdmin(authentication)) {
            log.warn("Non-admin user attempted to create lead: {}", request.getLeadName());
            throw new ForbiddenException("Only admin users can create leads");
        }

        // Check if lead name already exists
        if (leadRepository.existsByLeadNameAndDeletedFalse(request.getLeadName())) {
            log.warn("Attempted to create lead with duplicate name: {}", request.getLeadName());
            throw new BadRequestException("A lead with this name already exists. Please use a different name.");
        }

        // Validate deal IDs exist (only if dealIds is provided)
        if (request.getDealIds() != null && !request.getDealIds().isEmpty()) {
            for (String dealId : request.getDealIds()) {
                if (dealId != null && !dealId.trim().isEmpty()) {
                    validateDealExists(dealId);
                }
            }
        }

        // Generate UUID for the lead _id
        String uuid = java.util.UUID.randomUUID().toString();
        log.debug("Generated UUID for lead: {}", uuid);

        // Create lead entity with all fields
        Lead lead = Lead.builder()
            .id(uuid) // Set UUID as the _id
            .originatedFrom(request.getOriginatedFrom())
            .status(request.getStatus() != null ? request.getStatus() : LeadStatus.NEW) // Default to NEW if not provided
            .leadName(request.getLeadName())
            .company(request.getCompany())
            .companyAddress(request.getCompanyAddress())
            .companyWebsite(request.getCompanyWebsite())
            .communication(request.getCommunication())
            .platform(request.getPlatform())
            .contactNumber(request.getContactNumber())
            .dealIds(request.getDealIds() != null ? request.getDealIds() : new ArrayList<>()) // Default to empty list if not provided
            .build();

        // Save lead
        // Audit fields (createdAt, createdBy, updatedAt, updatedBy) are automatically set by Spring Data Auditing:
        // - createdAt: Current timestamp
        // - createdBy: Current user's MongoDB ID from JWT token
        // - updatedAt: Current timestamp
        // - updatedBy: Current user's MongoDB ID from JWT token
        Lead savedLead = leadRepository.save(lead);
        log.info("Successfully created lead with UUID: {} by user: {}", savedLead.getId(), savedLead.getCreatedBy());

        // Log audit event
        auditLogService.logLeadCreated(authentication, savedLead.getId(), savedLead.getLeadName());

        return leadMapper.toDTO(savedLead);
    }

    /**
     * Update existing lead (Admin only)
     */
    @Transactional
    public LeadDTO updateLead(String id, UpdateLeadRequest request, Authentication authentication) {
        log.info("Updating lead with ID: {}", id);

        // Check if user is admin - only admins can update leads
        if (!permissionEvaluator.isAdmin(authentication)) {
            log.warn("Non-admin user attempted to update lead with ID: {}", id);
            throw new ForbiddenException("Only admin users can update leads");
        }

        // Find existing lead
        Lead lead = leadRepository.findById(id)
            .filter(l -> !l.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + id));

        // Note: leadId (UUID) cannot be updated once created

        // Validate deal IDs exist (only if dealIds is provided)
        if (request.getDealIds() != null && !request.getDealIds().isEmpty()) {
            for (String dealId : request.getDealIds()) {
                if (dealId != null && !dealId.trim().isEmpty()) {
                    validateDealExists(dealId);
                }
            }
        }

        // Update fields if provided
        if (request.getOriginatedFrom() != null) {
            lead.setOriginatedFrom(request.getOriginatedFrom());
        }
        if (request.getStatus() != null) {
            lead.setStatus(request.getStatus());
        }
        if (request.getLeadName() != null) {
            // Check if the new lead name already exists (excluding current lead)
            // Only check if the name is actually being changed
            if (!request.getLeadName().equals(lead.getLeadName())) {
                if (leadRepository.existsByLeadNameAndDeletedFalse(request.getLeadName())) {
                    log.warn("Attempted to update lead to duplicate name: {}", request.getLeadName());
                    throw new BadRequestException("A lead with this name already exists. Please use a different name.");
                }
            }
            lead.setLeadName(request.getLeadName());
        }
        if (request.getCompany() != null) {
            lead.setCompany(request.getCompany());
        }
        if (request.getCompanyAddress() != null) {
            lead.setCompanyAddress(request.getCompanyAddress());
        }
        if (request.getCompanyWebsite() != null) {
            lead.setCompanyWebsite(request.getCompanyWebsite());
        }
        if (request.getCommunication() != null) {
            lead.setCommunication(request.getCommunication());
        }
        if (request.getPlatform() != null) {
            lead.setPlatform(request.getPlatform());
        }
        if (request.getContactNumber() != null) {
            lead.setContactNumber(request.getContactNumber());
        }
        // DealIds - update if provided, keep existing if not
        if (request.getDealIds() != null) {
            lead.setDealIds(request.getDealIds());
        } else if (lead.getDealIds() == null) {
            lead.setDealIds(new ArrayList<>());
        }

        // Save updated lead
        // Audit fields are automatically updated by Spring Data Auditing:
        // - updatedAt: Set to current timestamp
        // - updatedBy: Set to current user's MongoDB ID from JWT token
        // (createdAt and createdBy remain unchanged)
        Lead updatedLead = leadRepository.save(lead);
        log.info("Successfully updated lead with ID: {} by user: {}", updatedLead.getId(), updatedLead.getUpdatedBy());

        // Log audit event
        auditLogService.logLeadUpdated(authentication, updatedLead.getId(), updatedLead.getLeadName());

        return leadMapper.toDTO(updatedLead);
    }

    /**
     * Delete lead (hard delete - removes from database) (Admin only)
     */
    @Transactional
    public void deleteLead(String id, Authentication authentication) {
        log.info("Deleting lead with ID: {}", id);

        // Check if user is admin - only admins can delete leads
        if (!permissionEvaluator.isAdmin(authentication)) {
            log.warn("Non-admin user attempted to delete lead with ID: {}", id);
            throw new ForbiddenException("Only admin users can delete leads");
        }

        // Find existing lead (check both active and soft-deleted leads)
        Lead lead = leadRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + id));

        // Store lead name before deletion for audit log
        String leadName = lead.getLeadName();

        // Hard delete - actually remove from database
        leadRepository.delete(lead);

        log.info("Successfully deleted lead with ID: {} from database by admin user", id);

        // Log audit event
        auditLogService.logLeadDeleted(authentication, id, leadName);
    }

    /**
     * Search leads by keyword
     */
    public List<LeadDTO> searchLeads(String searchTerm) {
        log.info("Searching leads with term: {}", searchTerm);
        List<Lead> leads = leadRepository.searchLeads(searchTerm);
        return leadMapper.toDTO(leads);
    }

    /**
     * Get total lead count (excluding soft-deleted)
     */
    public long getTotalLeadCount() {
        return leadRepository.countByDeletedFalse();
    }

    /**
     * Get leads by Deal ID
     */
    public List<LeadDTO> getLeadsByDealId(String dealId) {
        log.info("Fetching leads with Deal ID: {}", dealId);
        List<Lead> leads = leadRepository.findByDealIdsContainingAndDeletedFalse(dealId);
        return leadMapper.toDTO(leads);
    }

    /**
     * Validate that a deal exists in the database
     */
    private void validateDealExists(String dealId) {
        log.debug("Validating deal exists: {}", dealId);
        boolean exists = dealRepository.findById(dealId)
            .filter(d -> !d.isDeleted())
            .isPresent();

        if (!exists) {
            log.error("Deal not found with ID: {}", dealId);
            throw new BadRequestException("Deal not found with ID: " + dealId);
        }
    }
}
