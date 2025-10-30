package com.ceedpods.crmbuild.service.lead;

import com.ceedpods.crmbuild.dto.lead.LeadDTO;
import com.ceedpods.crmbuild.dto.request.CreateLeadRequest;
import com.ceedpods.crmbuild.dto.request.UpdateLeadRequest;
import com.ceedpods.crmbuild.entity.lead.Lead;
import com.ceedpods.crmbuild.exception.BadRequestException;
import com.ceedpods.crmbuild.exception.ForbiddenException;
import com.ceedpods.crmbuild.exception.ResourceNotFoundException;
import com.ceedpods.crmbuild.mapper.LeadMapper;
import com.ceedpods.crmbuild.repository.LeadRepository;
import com.ceedpods.crmbuild.security.CustomPermissionEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadService {

    private final LeadRepository leadRepository;
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
     * Get lead by ID
     */
    public LeadDTO getLeadById(String id) {
        log.info("Fetching lead with ID: {}", id);
        Lead lead = leadRepository.findById(id)
            .filter(l -> !l.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + id));
        return leadMapper.toDTO(lead);
    }

    /**
     * Get lead by Lead ID
     */
    public LeadDTO getLeadByLeadId(String leadId) {
        log.info("Fetching lead with Lead ID: {}", leadId);
        Lead lead = leadRepository.findByLeadIdAndDeletedFalse(leadId)
            .orElseThrow(() -> new ResourceNotFoundException("Lead not found with Lead ID: " + leadId));
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

        // Generate UUID for leadId
        String leadId = UUID.randomUUID().toString();
        log.debug("Generated UUID for lead: {}", leadId);

        // Create lead entity with all fields
        Lead lead = Lead.builder()
            .leadId(leadId) // Auto-generated UUID
            .originatedFrom(request.getOriginatedFrom())
            .leadName(request.getLeadName())
            .company(request.getCompany())
            .companyAddress(request.getCompanyAddress())
            .companyWebsite(request.getCompanyWebsite())
            .communication(request.getCommunication())
            .platform(request.getPlatform())
            .contactNumber(request.getContactNumber())
            .dealId(request.getDealId()) // Optional field
            .build();

        // Save lead (audit fields are automatically handled by Spring Data Auditing)
        Lead savedLead = leadRepository.save(lead);
        log.info("Successfully created lead with ID: {} and UUID: {} by admin user", savedLead.getId(), savedLead.getLeadId());

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

        // Update fields if provided
        if (request.getOriginatedFrom() != null) {
            lead.setOriginatedFrom(request.getOriginatedFrom());
        }
        if (request.getLeadName() != null) {
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
        if (request.getDealId() != null) {
            lead.setDealId(request.getDealId());
        }

        // Save updated lead (updatedAt and updatedBy are automatically handled by Spring Data Auditing)
        Lead updatedLead = leadRepository.save(lead);
        log.info("Successfully updated lead with ID: {} by admin user", updatedLead.getId());

        return leadMapper.toDTO(updatedLead);
    }

    /**
     * Delete lead (soft delete) (Admin only)
     */
    @Transactional
    public void deleteLead(String id, Authentication authentication) {
        log.info("Deleting lead with ID: {}", id);

        // Check if user is admin - only admins can delete leads
        if (!permissionEvaluator.isAdmin(authentication)) {
            log.warn("Non-admin user attempted to delete lead with ID: {}", id);
            throw new ForbiddenException("Only admin users can delete leads");
        }

        // Find existing lead
        Lead lead = leadRepository.findById(id)
            .filter(l -> !l.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + id));

        // Get current user ID for audit
        String deletedBy = permissionEvaluator.getCurrentKeycloakId(authentication);

        // Soft delete
        lead.markAsDeleted(deletedBy);
        leadRepository.save(lead);

        log.info("Successfully deleted lead with ID: {} by admin user", id);
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
}
