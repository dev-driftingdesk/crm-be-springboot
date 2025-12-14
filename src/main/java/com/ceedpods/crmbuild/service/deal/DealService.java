package com.ceedpods.crmbuild.service.deal;

import com.ceedpods.crmbuild.dto.deal.CreateDealResponse;
import com.ceedpods.crmbuild.dto.deal.DealDTO;
import com.ceedpods.crmbuild.dto.deal.DealListItem;
import com.ceedpods.crmbuild.dto.deal.DealProduct;
import com.ceedpods.crmbuild.dto.deal.DeleteDealResponse;
import com.ceedpods.crmbuild.dto.deal.GetDealResponse;
import com.ceedpods.crmbuild.dto.deal.SalesRepAssignment;
import com.ceedpods.crmbuild.dto.deal.UpdateDealResponse;
import com.ceedpods.crmbuild.dto.request.CreateDealRequest;
import com.ceedpods.crmbuild.dto.request.UpdateDealRequest;
import com.ceedpods.crmbuild.entity.deal.Deal;
import com.ceedpods.crmbuild.entity.dealnote.DealNote;
import com.ceedpods.crmbuild.entity.lead.Lead;
import com.ceedpods.crmbuild.entity.product.PricingPackage;
import com.ceedpods.crmbuild.entity.product.Product;
import com.ceedpods.crmbuild.entity.user.User;
import com.ceedpods.crmbuild.enums.DealStatus;
import com.ceedpods.crmbuild.enums.PackageType;
import com.ceedpods.crmbuild.exception.BadRequestException;
import com.ceedpods.crmbuild.exception.ForbiddenException;
import com.ceedpods.crmbuild.exception.ResourceNotFoundException;
import com.ceedpods.crmbuild.mapper.DealMapper;
import com.ceedpods.crmbuild.repository.DealNoteRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealService {

    private final DealRepository dealRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;
    private final DealNoteRepository dealNoteRepository;
    private final DealMapper dealMapper;
    private final CustomPermissionEvaluator permissionEvaluator;
    private final AuditLogService auditLogService;

    /**
     * Get all deals (excluding soft-deleted)
     * Returns summary list with linkedLead and assignedAgents info
     */
    public List<DealListItem> getAllDeals() {
        log.info("Fetching all deals");
        List<Deal> deals = dealRepository.findByDeletedFalse();

        if (deals.isEmpty()) {
            return new ArrayList<>();
        }

        // Batch fetch all lead IDs
        List<String> leadIds = deals.stream()
            .map(Deal::getLeadId)
            .filter(id -> id != null && !id.isEmpty())
            .distinct()
            .collect(Collectors.toList());

        Map<String, Lead> leadMap = new java.util.HashMap<>();
        if (!leadIds.isEmpty()) {
            leadRepository.findAllById(leadIds).stream()
                .filter(l -> !l.isDeleted())
                .forEach(l -> leadMap.put(l.getId(), l));
        }

        // Batch fetch all user IDs from sales representatives
        List<String> userIds = deals.stream()
            .filter(d -> d.getSalesRepresentatives() != null)
            .flatMap(d -> d.getSalesRepresentatives().stream())
            .map(SalesRepAssignment::getUserId)
            .filter(id -> id != null && !id.isEmpty())
            .distinct()
            .collect(Collectors.toList());

        Map<String, User> userMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            userRepository.findAllById(userIds).stream()
                .filter(u -> !u.isDeleted())
                .forEach(u -> userMap.put(u.getId(), u));
        }

        // Map deals to DealListItem
        return deals.stream().map(deal -> {
            // Build linked lead info
            DealListItem.LinkedLeadInfo linkedLead = null;
            if (deal.getLeadId() != null) {
                Lead lead = leadMap.get(deal.getLeadId());
                if (lead != null) {
                    linkedLead = DealListItem.LinkedLeadInfo.builder()
                        .leadId(lead.getId())
                        .leadName(lead.getLeadName())
                        .build();
                }
            }

            // Build assigned agents info
            List<DealListItem.AssignedAgentInfo> assignedAgents = new ArrayList<>();
            if (deal.getSalesRepresentatives() != null) {
                for (SalesRepAssignment sr : deal.getSalesRepresentatives()) {
                    User user = userMap.get(sr.getUserId());
                    assignedAgents.add(DealListItem.AssignedAgentInfo.builder()
                        .userId(sr.getUserId())
                        .fullName(user != null ? user.getFullName() : null)
                        .role(sr.getRole())
                        .build());
                }
            }

            return DealListItem.builder()
                .dealId(deal.getId())
                .dealName(deal.getDealName())
                .dealValue(deal.getDealValue())
                .status(deal.getStatus() != null ? deal.getStatus().name().toLowerCase() : null)
                .linkedLead(linkedLead)
                .assignedAgents(assignedAgents)
                .createdAt(deal.getCreatedAt())
                .updatedAt(deal.getUpdatedAt())
                .build();
        }).collect(Collectors.toList());
    }

    /**
     * Get deal by ID (UUID) with detailed response
     */
    public GetDealResponse getDealById(String id) {
        log.info("Fetching deal with UUID: {}", id);
        Deal deal = dealRepository.findById(id)
            .filter(d -> !d.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException("Deal not found with UUID: " + id));

        // Fetch related data
        // 1. Get created by user info
        GetDealResponse.CreatedByInfo createdByInfo = null;
        if (deal.getCreatedBy() != null) {
            User createdByUser = userRepository.findByKeycloakIdAndDeletedFalse(deal.getCreatedBy()).orElse(null);
            if (createdByUser != null) {
                createdByInfo = GetDealResponse.CreatedByInfo.builder()
                    .userId(createdByUser.getId())
                    .fullName(createdByUser.getFullName())
                    .build();
            }
        }

        // 2. Get linked lead info
        GetDealResponse.LinkedLeadInfo linkedLeadInfo = null;
        if (deal.getLeadId() != null) {
            Lead lead = leadRepository.findById(deal.getLeadId())
                .filter(l -> !l.isDeleted())
                .orElse(null);
            if (lead != null) {
                linkedLeadInfo = GetDealResponse.LinkedLeadInfo.builder()
                    .leadId(lead.getId())
                    .leadName(lead.getLeadName())
                    .build();
            }
        }

        // 3. Get products info with deal value and commission
        List<GetDealResponse.ProductInfo> productInfoList = new ArrayList<>();
        if (deal.getProducts() != null && !deal.getProducts().isEmpty()) {
            List<String> productIds = deal.getProducts().stream()
                .map(DealProduct::getProductId)
                .distinct()
                .collect(Collectors.toList());

            Map<String, Product> productMap = productRepository.findAllById(productIds).stream()
                .filter(p -> !p.isDeleted())
                .collect(Collectors.toMap(Product::getId, p -> p));

            for (DealProduct dealProduct : deal.getProducts()) {
                Product product = productMap.get(dealProduct.getProductId());
                if (product != null) {
                    int quantity = dealProduct.getQuantity() != null ? dealProduct.getQuantity() : 1;
                    BigDecimal unitPrice = getProductPrice(product, dealProduct.getPackageType());
                    BigDecimal productDealValue = unitPrice.multiply(BigDecimal.valueOf(quantity));
                    // Calculate commission as 10% of deal value (or use commission rate from pricing package)
                    BigDecimal commission = calculateProductCommission(product, dealProduct.getPackageType(), productDealValue);

                    productInfoList.add(GetDealResponse.ProductInfo.builder()
                        .productId(product.getId())
                        .productName(product.getProductName())
                        .description(product.getKeyLearningOutcomes())
                        .dealValue(productDealValue)
                        .commission(commission)
                        .build());
                }
            }
        }

        // 4. Get assigned agents info
        List<GetDealResponse.AssignedAgentInfo> assignedAgents = new ArrayList<>();
        if (deal.getSalesRepresentatives() != null && !deal.getSalesRepresentatives().isEmpty()) {
            List<String> userIds = deal.getSalesRepresentatives().stream()
                .map(SalesRepAssignment::getUserId)
                .distinct()
                .collect(Collectors.toList());

            Map<String, User> userMap = userRepository.findAllById(userIds).stream()
                .filter(u -> !u.isDeleted())
                .collect(Collectors.toMap(User::getId, u -> u));

            for (SalesRepAssignment sr : deal.getSalesRepresentatives()) {
                User user = userMap.get(sr.getUserId());
                assignedAgents.add(GetDealResponse.AssignedAgentInfo.builder()
                    .userId(sr.getUserId())
                    .fullName(user != null ? user.getFullName() : null)
                    .role(sr.getRole())
                    .profilePicture(null) // Will be populated after Minio deployment
                    .build());
            }
        }

        // 5. Get notes for this deal
        List<GetDealResponse.NoteInfo> notesList = new ArrayList<>();
        List<DealNote> dealNotes = dealNoteRepository.findByDealIdAndDeletedFalse(id);
        if (dealNotes != null && !dealNotes.isEmpty()) {
            // Get all note creator user IDs
            List<String> noteCreatorIds = dealNotes.stream()
                .map(DealNote::getCreatedBy)
                .filter(createdBy -> createdBy != null && !createdBy.isEmpty())
                .distinct()
                .collect(Collectors.toList());

            Map<String, User> noteCreatorMap = new java.util.HashMap<>();
            if (!noteCreatorIds.isEmpty()) {
                userRepository.findByKeycloakIdInAndDeletedFalse(noteCreatorIds)
                    .forEach(u -> noteCreatorMap.put(u.getKeycloakId(), u));
            }

            for (DealNote note : dealNotes) {
                User noteCreator = noteCreatorMap.get(note.getCreatedBy());
                notesList.add(GetDealResponse.NoteInfo.builder()
                    .noteId(note.getId())
                    .content(note.getNoteContent())
                    .createdBy(noteCreator != null ? noteCreator.getFullName() : note.getCreatedBy())
                    .createdAt(note.getCreatedAt())
                    .build());
            }
        }

        // 6. Calculate total commission from products
        BigDecimal totalCommission = productInfoList.stream()
            .map(GetDealResponse.ProductInfo::getCommission)
            .filter(c -> c != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Build and return the response
        return GetDealResponse.builder()
            .dealId(deal.getId())
            .dealName(deal.getDealName())
            .createdBy(createdByInfo)
            .dealValue(deal.getDealValue())
            .totalCommission(totalCommission)
            .linkedLead(linkedLeadInfo)
            .products(productInfoList)
            .activities(new ArrayList<>()) // Not implemented yet
            .actionItems(new ArrayList<>()) // Not implemented yet
            .notes(notesList)
            .assignedAgents(assignedAgents)
            .status(deal.getStatus() != null ? deal.getStatus().name().toLowerCase() : null)
            .createdAt(deal.getCreatedAt())
            .updatedAt(deal.getUpdatedAt())
            .build();
    }

    /**
     * Calculate commission for a product based on package type
     */
    private BigDecimal calculateProductCommission(Product product, PackageType packageType, BigDecimal dealValue) {
        // If pricing packages are enabled and have commission rate, use it
        if (packageType != null && product.getPricingPackages() != null
            && Boolean.TRUE.equals(product.getPricingPackages().getEnabled())
            && product.getPricingPackages().getPackages() != null) {

            for (PricingPackage pkg : product.getPricingPackages().getPackages()) {
                if (pkg.getPackageType() == packageType && pkg.getCommissionRate() != null) {
                    return dealValue.multiply(pkg.getCommissionRate()).divide(BigDecimal.valueOf(100));
                }
            }
        }
        // Default: 10% commission
        return dealValue.multiply(BigDecimal.valueOf(0.10));
    }

    /**
     * Create new deal (Admin only)
     * Returns simplified response with dealId, dealName, dealValue, createdAt
     */
    @Transactional
    public CreateDealResponse createDeal(CreateDealRequest request, Authentication authentication) {
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

        // Validate lead ID exists (required)
        validateLeadExists(request.getLeadId());

        // Validate all product IDs exist and get products for price calculation
        Map<String, Product> productMap = validateAndGetProducts(request.getProducts());

        // Validate all sales rep user IDs exist
        List<String> salesRepUserIds = request.getSalesRepresentatives().stream()
            .map(SalesRepAssignment::getUserId)
            .collect(Collectors.toList());
        validateUsersExist(salesRepUserIds);

        // Calculate deal value from products
        BigDecimal dealValue = calculateDealValue(request.getProducts(), productMap);

        // Generate UUID for the deal _id
        String uuid = java.util.UUID.randomUUID().toString();
        log.debug("Generated UUID for deal: {}", uuid);

        // Create deal entity with all fields
        Deal deal = Deal.builder()
            .id(uuid) // Set UUID as the _id
            .dealName(request.getDealName())
            .status(DealStatus.OPEN) // Default to OPEN
            .dealValue(dealValue)
            .products(request.getProducts())
            .salesRepresentatives(request.getSalesRepresentatives())
            .leadId(request.getLeadId())
            .build();

        // Save deal
        // Audit fields (createdAt, createdBy, updatedAt, updatedBy) are automatically set by Spring Data Auditing
        Deal savedDeal = dealRepository.save(deal);
        log.info("Successfully created deal with UUID: {} by user: {}", savedDeal.getId(), savedDeal.getCreatedBy());

        // Log audit event
        auditLogService.logDealCreated(authentication, savedDeal.getId(), savedDeal.getDealName());

        // Return simplified response
        return CreateDealResponse.builder()
            .dealId(savedDeal.getId())
            .dealName(savedDeal.getDealName())
            .dealValue(savedDeal.getDealValue())
            .createdAt(savedDeal.getCreatedAt())
            .build();
    }

    /**
     * Update existing deal (Admin only)
     * Returns simplified response with dealId, dealName, dealValue, updatedAt
     */
    @Transactional
    public UpdateDealResponse updateDeal(String id, UpdateDealRequest request, Authentication authentication) {
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

        if (request.getStatus() != null) {
            deal.setStatus(request.getStatus());
        }

        if (request.getCommission() != null) {
            deal.setCommission(request.getCommission());
        }

        if (request.getLeadId() != null && !request.getLeadId().trim().isEmpty()) {
            validateLeadExists(request.getLeadId());
            deal.setLeadId(request.getLeadId());
        }

        if (request.getProducts() != null && !request.getProducts().isEmpty()) {
            Map<String, Product> productMap = validateAndGetProducts(request.getProducts());
            deal.setProducts(request.getProducts());
            // Recalculate deal value when products change
            deal.setDealValue(calculateDealValue(request.getProducts(), productMap));
        }

        if (request.getAssignedAgents() != null) {
            if (!request.getAssignedAgents().isEmpty()) {
                List<String> agentUserIds = request.getAssignedAgents().stream()
                    .map(SalesRepAssignment::getUserId)
                    .collect(Collectors.toList());
                validateUsersExist(agentUserIds);
            }
            deal.setSalesRepresentatives(request.getAssignedAgents());
        }

        // Save updated deal
        // Audit fields are automatically updated by Spring Data Auditing
        Deal updatedDeal = dealRepository.save(deal);
        log.info("Successfully updated deal with ID: {} by user: {}", updatedDeal.getId(), updatedDeal.getUpdatedBy());

        // Log audit event
        auditLogService.logDealUpdated(authentication, updatedDeal.getId(), updatedDeal.getDealName());

        // Return simplified response
        return UpdateDealResponse.builder()
            .dealId(updatedDeal.getId())
            .dealName(updatedDeal.getDealName())
            .dealValue(updatedDeal.getDealValue())
            .updatedAt(updatedDeal.getUpdatedAt())
            .build();
    }

    /**
     * Delete deal (hard delete - removes from database) (Admin only)
     * Returns response with dealId and deletedAt timestamp
     */
    @Transactional
    public DeleteDealResponse deleteDeal(String id, Authentication authentication) {
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

        // Capture deletion timestamp
        java.time.LocalDateTime deletedAt = java.time.LocalDateTime.now();

        log.info("Successfully deleted deal with ID: {} from database by admin user", id);

        // Log audit event
        auditLogService.logDealDeleted(authentication, id, dealName);

        // Return response with dealId and deletedAt
        return DeleteDealResponse.builder()
            .dealId(id)
            .deletedAt(deletedAt)
            .build();
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
     * Validate that all product IDs exist in the database and return them as a map
     * for price calculation
     */
    private Map<String, Product> validateAndGetProducts(List<DealProduct> dealProducts) {
        List<String> productIds = dealProducts.stream()
            .map(DealProduct::getProductId)
            .distinct()
            .collect(Collectors.toList());

        log.debug("Validating products exist: {}", productIds);

        // Fetch all products in batch
        List<Product> products = productRepository.findAllById(productIds);
        Map<String, Product> productMap = products.stream()
            .filter(p -> !p.isDeleted())
            .collect(Collectors.toMap(Product::getId, p -> p));

        // Check for missing products
        List<String> invalidProductIds = productIds.stream()
            .filter(id -> !productMap.containsKey(id))
            .collect(Collectors.toList());

        if (!invalidProductIds.isEmpty()) {
            log.error("Product(s) not found with IDs: {}", invalidProductIds);
            throw new BadRequestException("Product(s) not found with IDs: " + invalidProductIds);
        }

        return productMap;
    }

    /**
     * Calculate the total deal value from products
     * Uses package price if packageType is specified, otherwise uses basePrice
     */
    private BigDecimal calculateDealValue(List<DealProduct> dealProducts, Map<String, Product> productMap) {
        BigDecimal totalValue = BigDecimal.ZERO;

        for (DealProduct dealProduct : dealProducts) {
            Product product = productMap.get(dealProduct.getProductId());
            if (product == null) {
                continue; // Skip if product not found (already validated)
            }

            BigDecimal unitPrice = getProductPrice(product, dealProduct.getPackageType());
            int quantity = dealProduct.getQuantity() != null ? dealProduct.getQuantity() : 1;

            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            totalValue = totalValue.add(lineTotal);
        }

        log.debug("Calculated deal value: {}", totalValue);
        return totalValue;
    }

    /**
     * Get the price for a product based on package type
     * If packageType is specified and pricing packages are enabled, use package price
     * Otherwise, use basePrice
     */
    private BigDecimal getProductPrice(Product product, PackageType packageType) {
        // If no package type specified, use base price
        if (packageType == null) {
            return product.getBasePrice() != null ? product.getBasePrice() : BigDecimal.ZERO;
        }

        // If pricing packages are enabled and available, find matching package
        if (product.getPricingPackages() != null
            && Boolean.TRUE.equals(product.getPricingPackages().getEnabled())
            && product.getPricingPackages().getPackages() != null) {

            for (PricingPackage pkg : product.getPricingPackages().getPackages()) {
                if (pkg.getPackageType() == packageType && pkg.getPrice() != null) {
                    return pkg.getPrice();
                }
            }
        }

        // Fall back to base price if package not found
        return product.getBasePrice() != null ? product.getBasePrice() : BigDecimal.ZERO;
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
