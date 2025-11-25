package com.ceedpods.crmbuild.service.deal;

import com.ceedpods.crmbuild.dto.deal.DealDTO;
import com.ceedpods.crmbuild.dto.deal.SalesRepAssignment;
import com.ceedpods.crmbuild.dto.request.CreateDealRequest;
import com.ceedpods.crmbuild.dto.request.UpdateDealRequest;
import com.ceedpods.crmbuild.entity.deal.Deal;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealService {

    private final DealRepository dealRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;
    private final DealMapper dealMapper;
    private final CustomPermissionEvaluator permissionEvaluator;
    private final AuditLogService auditLogService;

    /**
     * Get all deals (excluding soft-deleted)
     */
    public List<DealDTO> getAllDeals() {
        log.info("Fetching all deals");
        List<Deal> deals = dealRepository.findByDeletedFalse();
        return dealMapper.toDTO(deals);
    }

    /**
     * Get deal by ID (UUID)
     */
    public DealDTO getDealById(String id) {
        log.info("Fetching deal with UUID: {}", id);
        Deal deal = dealRepository.findById(id)
            .filter(d -> !d.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException("Deal not found with UUID: " + id));
        return dealMapper.toDTO(deal);
    }

    /**
     * Create new deal (Admin only)
     */
    @Transactional
    public DealDTO createDeal(CreateDealRequest request, Authentication authentication) {
        log.info("Creating new deal: {}", request.getDealName());

        // Check if user is admin - only admins can create deals
        if (!permissionEvaluator.isAdmin(authentication)) {
            log.warn("Non-admin user attempted to create deal: {}", request.getDealName());
            throw new ForbiddenException("Only admin users can create deals");
        }

        // Validate deal name is unique
        if (dealRepository.countByDealNameAndDeletedFalse(request.getDealName()) > 0) {
            log.error("Deal name already exists: {}", request.getDealName());
            throw new BadRequestException("Deal with name '" + request.getDealName() + "' already exists");
        }

        // Validate lead ID exists (if provided)
        if (request.getLeadId() != null && !request.getLeadId().trim().isEmpty()) {
            validateLeadExists(request.getLeadId());
        }

        // Validate all product IDs exist
        validateProductsExist(request.getProductIds());

        // Validate all sales rep IDs exist (if provided)
        if (request.getSalesReps() != null && !request.getSalesReps().isEmpty()) {
            List<String> salesRepIds = request.getSalesReps().stream()
                .map(SalesRepAssignment::getId)
                .collect(Collectors.toList());
            validateUsersExist(salesRepIds);
        }

        // Generate UUID for the deal _id
        String uuid = java.util.UUID.randomUUID().toString();
        log.debug("Generated UUID for deal: {}", uuid);

        // Create deal entity with all fields
        Deal deal = Deal.builder()
            .id(uuid) // Set UUID as the _id
            .dealName(request.getDealName())
            .productIds(request.getProductIds())
            .salesReps(request.getSalesReps())
            .leadId(request.getLeadId())
            .build();

        // Save deal
        // Audit fields (createdAt, createdBy, updatedAt, updatedBy) are automatically set by Spring Data Auditing
        Deal savedDeal = dealRepository.save(deal);
        log.info("Successfully created deal with UUID: {} by user: {}", savedDeal.getId(), savedDeal.getCreatedBy());

        // Log audit event
        auditLogService.logDealCreated(authentication, savedDeal.getId(), savedDeal.getDealName());

        return dealMapper.toDTO(savedDeal);
    }

    /**
     * Update existing deal (Admin only)
     */
    @Transactional
    public DealDTO updateDeal(String id, UpdateDealRequest request, Authentication authentication) {
        log.info("Updating deal with ID: {}", id);

        // Check if user is admin - only admins can update deals
        if (!permissionEvaluator.isAdmin(authentication)) {
            log.warn("Non-admin user attempted to update deal with ID: {}", id);
            throw new ForbiddenException("Only admin users can update deals");
        }

        // Find existing deal
        Deal deal = dealRepository.findById(id)
            .filter(d -> !d.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException("Deal not found with ID: " + id));

        // Update fields if provided and validate
        if (request.getDealName() != null) {
            // Validate deal name is unique (excluding current deal)
            if (dealRepository.countByDealNameAndIdNotAndDeletedFalse(request.getDealName(), id) > 0) {
                log.error("Deal name already exists: {}", request.getDealName());
                throw new BadRequestException("Deal with name '" + request.getDealName() + "' already exists");
            }
            deal.setDealName(request.getDealName());
        }

        if (request.getLeadId() != null && !request.getLeadId().trim().isEmpty()) {
            validateLeadExists(request.getLeadId());
            deal.setLeadId(request.getLeadId());
        }

        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            validateProductsExist(request.getProductIds());
            deal.setProductIds(request.getProductIds());
        }

        if (request.getSalesReps() != null) {
            if (!request.getSalesReps().isEmpty()) {
                List<String> salesRepIds = request.getSalesReps().stream()
                    .map(SalesRepAssignment::getId)
                    .collect(Collectors.toList());
                validateUsersExist(salesRepIds);
            }
            deal.setSalesReps(request.getSalesReps());
        }

        // Save updated deal
        // Audit fields are automatically updated by Spring Data Auditing
        Deal updatedDeal = dealRepository.save(deal);
        log.info("Successfully updated deal with ID: {} by user: {}", updatedDeal.getId(), updatedDeal.getUpdatedBy());

        // Log audit event
        auditLogService.logDealUpdated(authentication, updatedDeal.getId(), updatedDeal.getDealName());

        return dealMapper.toDTO(updatedDeal);
    }

    /**
     * Delete deal (hard delete - removes from database) (Admin only)
     */
    @Transactional
    public void deleteDeal(String id, Authentication authentication) {
        log.info("Deleting deal with ID: {}", id);

        // Check if user is admin - only admins can delete deals
        if (!permissionEvaluator.isAdmin(authentication)) {
            log.warn("Non-admin user attempted to delete deal with ID: {}", id);
            throw new ForbiddenException("Only admin users can delete deals");
        }

        // Find existing deal
        Deal deal = dealRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Deal not found with ID: " + id));

        // Store deal name before deletion for audit log
        String dealName = deal.getDealName();

        // Hard delete - actually remove from database
        dealRepository.delete(deal);

        log.info("Successfully deleted deal with ID: {} from database by admin user", id);

        // Log audit event
        auditLogService.logDealDeleted(authentication, id, dealName);
    }

    /**
     * Search deals by keyword
     */
    public List<DealDTO> searchDeals(String searchTerm) {
        log.info("Searching deals with term: {}", searchTerm);
        List<Deal> deals = dealRepository.searchDeals(searchTerm);
        return dealMapper.toDTO(deals);
    }

    /**
     * Get total deal count (excluding soft-deleted)
     */
    public long getTotalDealCount() {
        return dealRepository.countByDeletedFalse();
    }

    /**
     * Get deals by Lead ID
     */
    public List<DealDTO> getDealsByLeadId(String leadId) {
        log.info("Fetching deals with Lead ID: {}", leadId);
        List<Deal> deals = dealRepository.findByLeadIdAndDeletedFalse(leadId);
        return dealMapper.toDTO(deals);
    }

    /**
     * Get deals by Product ID
     */
    public List<DealDTO> getDealsByProductId(String productId) {
        log.info("Fetching deals with Product ID: {}", productId);
        List<Deal> deals = dealRepository.findByProductIdAndDeletedFalse(productId);
        return dealMapper.toDTO(deals);
    }

    /**
     * Get deals by Sales Rep ID
     */
    public List<DealDTO> getDealsBySalesRepId(String salesRepId) {
        log.info("Fetching deals with Sales Rep ID: {}", salesRepId);
        List<Deal> deals = dealRepository.findBySalesRepAndDeletedFalse(salesRepId);
        return dealMapper.toDTO(deals);
    }

    /**
     * Validate that a lead exists in the database
     */
    private void validateLeadExists(String leadId) {
        log.debug("Validating lead exists: {}", leadId);
        boolean exists = leadRepository.findById(leadId)
            .filter(l -> !l.isDeleted())
            .isPresent();

        if (!exists) {
            log.error("Lead not found with ID: {}", leadId);
            throw new BadRequestException("Lead not found with ID: " + leadId);
        }
    }

    /**
     * Validate that all product IDs exist in the database
     */
    private void validateProductsExist(List<String> productIds) {
        log.debug("Validating products exist: {}", productIds);
        List<String> invalidProductIds = new ArrayList<>();

        for (String productId : productIds) {
            boolean exists = productRepository.findById(productId)
                .filter(p -> !p.isDeleted())
                .isPresent();

            if (!exists) {
                invalidProductIds.add(productId);
            }
        }

        if (!invalidProductIds.isEmpty()) {
            log.error("Product(s) not found with IDs: {}", invalidProductIds);
            throw new BadRequestException("Product(s) not found with IDs: " + invalidProductIds);
        }
    }

    /**
     * Validate that all user IDs exist in the database
     */
    private void validateUsersExist(List<String> userIds) {
        log.debug("Validating users exist: {}", userIds);
        List<String> invalidUserIds = new ArrayList<>();

        for (String userId : userIds) {
            boolean exists = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .isPresent();

            if (!exists) {
                invalidUserIds.add(userId);
            }
        }

        if (!invalidUserIds.isEmpty()) {
            log.error("User(s) not found with IDs: {}", invalidUserIds);
            throw new BadRequestException("User(s) not found with IDs: " + invalidUserIds);
        }
    }
}
