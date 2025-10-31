package com.ceedpods.crmbuild.controller.lead;

import com.ceedpods.crmbuild.dto.lead.LeadDTO;
import com.ceedpods.crmbuild.dto.request.CreateLeadRequest;
import com.ceedpods.crmbuild.dto.request.UpdateLeadRequest;
import com.ceedpods.crmbuild.dto.response.ApiResponse;
import com.ceedpods.crmbuild.security.CustomPermissionEvaluator;
import com.ceedpods.crmbuild.security.RequirePermission;
import com.ceedpods.crmbuild.service.lead.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
@Slf4j
public class LeadController {

    private final LeadService leadService;
    private final CustomPermissionEvaluator permissionEvaluator;

    /**
     * Create a new lead (Admin only)
     */
    @PostMapping
    @RequirePermission("LEAD_CREATE")
    public ResponseEntity<ApiResponse<LeadDTO>> createLead(
            @Valid @RequestBody CreateLeadRequest request,
            Authentication authentication) {
        try {
            log.info("Creating new lead");
            LeadDTO lead = leadService.createLead(request, authentication);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lead created successfully", lead));
        } catch (Exception e) {
            log.error("Error creating lead: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to create lead: " + e.getMessage()));
        }
    }

    /**
     * Get all leads
     */
    @GetMapping
    @RequirePermission("LEAD_VIEW_ALL")
    public ResponseEntity<ApiResponse<List<LeadDTO>>> getAllLeads() {
        try {
            log.info("Fetching all leads");
            List<LeadDTO> leads = leadService.getAllLeads();
            return ResponseEntity.ok(ApiResponse.success(leads));
        } catch (Exception e) {
            log.error("Error fetching leads: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to fetch leads: " + e.getMessage()));
        }
    }

    /**
     * Get lead by UUID
     */
    @GetMapping("/{id}")
    @RequirePermission("LEAD_VIEW_ALL")
    public ResponseEntity<ApiResponse<LeadDTO>> getLeadById(@PathVariable String id) {
        try {
            log.info("Fetching lead with UUID: {}", id);
            LeadDTO lead = leadService.getLeadById(id);
            return ResponseEntity.ok(ApiResponse.success(lead));
        } catch (Exception e) {
            log.error("Error fetching lead: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to fetch lead: " + e.getMessage()));
        }
    }

    /**
     * Update lead (Admin only)
     */
    @PutMapping("/{id}")
    @RequirePermission("LEAD_EDIT")
    public ResponseEntity<ApiResponse<LeadDTO>> updateLead(
            @PathVariable String id,
            @Valid @RequestBody UpdateLeadRequest request,
            Authentication authentication) {
        try {
            log.info("Updating lead with ID: {}", id);
            LeadDTO lead = leadService.updateLead(id, request, authentication);
            return ResponseEntity.ok(ApiResponse.success("Lead updated successfully", lead));
        } catch (Exception e) {
            log.error("Error updating lead: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to update lead: " + e.getMessage()));
        }
    }

    /**
     * Delete lead (soft delete) (Admin only)
     */
    @DeleteMapping("/{id}")
    @RequirePermission("LEAD_DELETE")
    public ResponseEntity<ApiResponse<Void>> deleteLead(
            @PathVariable String id,
            Authentication authentication) {
        try {
            log.info("Deleting lead with ID: {}", id);
            leadService.deleteLead(id, authentication);
            return ResponseEntity.ok(ApiResponse.success("Lead deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting lead: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to delete lead: " + e.getMessage()));
        }
    }

    /**
     * Search leads
     */
    @GetMapping("/search")
    @RequirePermission("LEAD_VIEW_ALL")
    public ResponseEntity<ApiResponse<List<LeadDTO>>> searchLeads(
            @RequestParam String query) {
        try {
            log.info("Searching leads with query: {}", query);
            List<LeadDTO> leads = leadService.searchLeads(query);
            return ResponseEntity.ok(ApiResponse.success(leads));
        } catch (Exception e) {
            log.error("Error searching leads: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to search leads: " + e.getMessage()));
        }
    }

    /**
     * Get total lead count
     */
    @GetMapping("/count")
    @RequirePermission("LEAD_VIEW_ALL")
    public ResponseEntity<ApiResponse<Long>> getLeadCount() {
        try {
            log.info("Fetching lead count");
            long count = leadService.getTotalLeadCount();
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            log.error("Error fetching lead count: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to fetch lead count: " + e.getMessage()));
        }
    }

    /**
     * Get leads by Deal ID
     */
    @GetMapping("/deal/{dealId}")
    @RequirePermission("LEAD_VIEW_ALL")
    public ResponseEntity<ApiResponse<List<LeadDTO>>> getLeadsByDealId(@PathVariable String dealId) {
        try {
            log.info("Fetching leads with Deal ID: {}", dealId);
            List<LeadDTO> leads = leadService.getLeadsByDealId(dealId);
            return ResponseEntity.ok(ApiResponse.success(leads));
        } catch (Exception e) {
            log.error("Error fetching leads by deal ID: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to fetch leads by deal ID: " + e.getMessage()));
        }
    }
}
