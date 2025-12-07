package com.ceedpods.crmbuild.controller.lead;

import com.ceedpods.crmbuild.dto.lead.LeadDTO;
import com.ceedpods.crmbuild.dto.request.CreateLeadRequest;
import com.ceedpods.crmbuild.dto.request.UpdateLeadRequest;
import com.ceedpods.crmbuild.dto.response.ApiResponse;
import com.ceedpods.crmbuild.security.CustomPermissionEvaluator;
import com.ceedpods.crmbuild.security.RequirePermission;
import com.ceedpods.crmbuild.service.lead.LeadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leads")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Lead Management", description = "Operations for managing sales leads and prospects")
@SecurityRequirement(name = "Bearer Authentication")
public class LeadController {

    private final LeadService leadService;
    private final CustomPermissionEvaluator permissionEvaluator;

    /**
     * Create a new lead (Admin only)
     */
    @Operation(
        summary = "Create Lead",
        description = "Creates a new sales lead in the system. Requires LEAD_CREATE permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Lead created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "success": true,
                      "message": "Lead created successfully",
                      "data": {
                        "id": "lead-uuid",
                        "firstName": "John",
                        "lastName": "Doe",
                        "email": "john.doe@example.com",
                        "phone": "+1234567890",
                        "company": "Acme Corp",
                        "status": "NEW",
                        "source": "WEBSITE",
                        "assignedTo": "sales-rep-uuid"
                      }
                    }
                    """)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request data"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        )
    })
    @PostMapping
    @RequirePermission("LEAD_CREATE")
    public ResponseEntity<ApiResponse<LeadDTO>> createLead(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Lead details",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = CreateLeadRequest.class),
                    examples = @ExampleObject(value = """
                        {
                          "firstName": "John",
                          "lastName": "Doe",
                          "email": "john.doe@example.com",
                          "phone": "+1234567890",
                          "company": "Acme Corp",
                          "jobTitle": "CEO",
                          "source": "WEBSITE",
                          "assignedTo": "sales-rep-uuid",
                          "notes": "Interested in product demo"
                        }
                        """)
                )
            )
            @Valid @RequestBody CreateLeadRequest request,
            @Parameter(hidden = true) Authentication authentication) {
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
    @Operation(
        summary = "Get All Leads",
        description = "Retrieves all leads in the system. Requires LEAD_VIEW_ALL permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Leads retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        )
    })
    @GetMapping
    @RequirePermission("LEAD_VIEW")
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
    @Operation(
        summary = "Get Lead by ID",
        description = "Retrieves a specific lead by its UUID. Requires LEAD_VIEW_ALL permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lead found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Lead not found"
        )
    })
    @GetMapping("/{id}")
    @RequirePermission("LEAD_VIEW_ALL")
    public ResponseEntity<ApiResponse<LeadDTO>> getLeadById(
            @Parameter(description = "Lead UUID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String id) {
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
    @Operation(
        summary = "Update Lead",
        description = "Updates an existing lead. Requires LEAD_EDIT permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lead updated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Lead not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        )
    })
    @PutMapping("/{id}")
    @RequirePermission("LEAD_EDIT")
    public ResponseEntity<ApiResponse<LeadDTO>> updateLead(
            @Parameter(description = "Lead UUID", required = true)
            @PathVariable String id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Updated lead details",
                required = true,
                content = @Content(schema = @Schema(implementation = UpdateLeadRequest.class))
            )
            @Valid @RequestBody UpdateLeadRequest request,
            @Parameter(hidden = true) Authentication authentication) {
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
    @Operation(
        summary = "Delete Lead",
        description = "Soft deletes a lead (marks as deleted without removing from database). Requires LEAD_DELETE permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lead deleted successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Lead not found"
        )
    })
    @DeleteMapping("/{id}")
    @RequirePermission("LEAD_DELETE")
    public ResponseEntity<ApiResponse<Void>> deleteLead(
            @Parameter(description = "Lead UUID", required = true)
            @PathVariable String id,
            @Parameter(hidden = true) Authentication authentication) {
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
    @Operation(
        summary = "Search Leads",
        description = "Searches leads by name, email, company, or other fields"
    )
    @GetMapping("/search")
    @RequirePermission("LEAD_VIEW_ALL")
    public ResponseEntity<ApiResponse<List<LeadDTO>>> searchLeads(
            @Parameter(description = "Search query", required = true, example = "john")
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
