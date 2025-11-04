package com.ceedpods.crmbuild.service.lead;

import com.ceedpods.crmbuild.dto.lead.LeadDTO;
import com.ceedpods.crmbuild.dto.request.CreateLeadRequest;
import com.ceedpods.crmbuild.dto.request.UpdateLeadRequest;
import com.ceedpods.crmbuild.entity.lead.Lead;
import com.ceedpods.crmbuild.exception.BadRequestException;
import com.ceedpods.crmbuild.exception.ForbiddenException;
import com.ceedpods.crmbuild.exception.ResourceNotFoundException;
import com.ceedpods.crmbuild.mapper.LeadMapper;
import com.ceedpods.crmbuild.repository.DealRepository;
import com.ceedpods.crmbuild.repository.LeadRepository;
import com.ceedpods.crmbuild.security.CustomPermissionEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadService {

    private final LeadRepository leadRepository;
    private final DealRepository dealRepository;
    private final LeadMapper leadMapper;
    private final CustomPermissionEvaluator permissionEvaluator;

    /**
     * Get all leads (excluding soft-deleted)
     */
    public List<LeadDTO> getAllLeads() {
        log.info("Fetching all leads");
        List<Lead> leads = leadRepository.findByDeletedFalse();
        return leadMapper.toDTO(leads);
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

        // Validate deal ID exists (now mandatory)
        validateDealExists(request.getDealId());

        // Check if dealId is already linked to another lead
        if (leadRepository.existsByDealIdAndDeletedFalse(request.getDealId())) {
            log.warn("Attempted to create lead with duplicate dealId: {}", request.getDealId());
            throw new BadRequestException("A lead is already linked to this deal. Please choose a different deal.");
        }

        // Generate UUID for the lead _id
        String uuid = java.util.UUID.randomUUID().toString();
        log.debug("Generated UUID for lead: {}", uuid);

        // Create lead entity with all fields
        Lead lead = Lead.builder()
            .id(uuid) // Set UUID as the _id
            .originatedFrom(request.getOriginatedFrom())
            .leadName(request.getLeadName())
            .company(request.getCompany())
            .companyAddress(request.getCompanyAddress())
            .companyWebsite(request.getCompanyWebsite())
            .communication(request.getCommunication())
            .platform(request.getPlatform())
            .contactNumber(request.getContactNumber())
            .dealId(request.getDealId()) // Required field
            .build();

        // Save lead
        // Audit fields (createdAt, createdBy, updatedAt, updatedBy) are automatically set by Spring Data Auditing:
        // - createdAt: Current timestamp
        // - createdBy: Current user's MongoDB ID from JWT token
        // - updatedAt: Current timestamp
        // - updatedBy: Current user's MongoDB ID from JWT token
        Lead savedLead = leadRepository.save(lead);
        log.info("Successfully created lead with UUID: {} by user: {}", savedLead.getId(), savedLead.getCreatedBy());

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

        // Validate deal ID exists (now mandatory)
        validateDealExists(request.getDealId());

        // Check if dealId is being changed to a different deal that's already linked to another lead
        if (!request.getDealId().equals(lead.getDealId())) {
            if (leadRepository.existsByDealIdAndDeletedFalse(request.getDealId())) {
                log.warn("Attempted to update lead to duplicate dealId: {}", request.getDealId());
                throw new BadRequestException("A lead is already linked to this deal. Please choose a different deal.");
            }
        }

        // Update fields if provided
        if (request.getOriginatedFrom() != null) {
            lead.setOriginatedFrom(request.getOriginatedFrom());
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
        // DealId is now mandatory and already validated
        lead.setDealId(request.getDealId());

        // Save updated lead
        // Audit fields are automatically updated by Spring Data Auditing:
        // - updatedAt: Set to current timestamp
        // - updatedBy: Set to current user's MongoDB ID from JWT token
        // (createdAt and createdBy remain unchanged)
        Lead updatedLead = leadRepository.save(lead);
        log.info("Successfully updated lead with ID: {} by user: {}", updatedLead.getId(), updatedLead.getUpdatedBy());

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

        // Hard delete - actually remove from database
        leadRepository.delete(lead);

        log.info("Successfully deleted lead with ID: {} from database by admin user", id);
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
        List<Lead> leads = leadRepository.findByDealIdAndDeletedFalse(dealId);
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
