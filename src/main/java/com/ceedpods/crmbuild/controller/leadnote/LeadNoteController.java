package com.ceedpods.crmbuild.controller.leadnote;

import com.ceedpods.crmbuild.dto.leadnote.LeadNoteDTO;
import com.ceedpods.crmbuild.dto.request.CreateLeadNoteRequest;
import com.ceedpods.crmbuild.dto.request.UpdateLeadNoteRequest;
import com.ceedpods.crmbuild.dto.response.ApiResponse;
import com.ceedpods.crmbuild.dto.response.PaginatedResponse;
import com.ceedpods.crmbuild.exception.ResourceNotFoundException;
import com.ceedpods.crmbuild.security.RequirePermission;
import com.ceedpods.crmbuild.service.leadnote.LeadNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for LeadNote management
 */
@RestController
@RequestMapping("/lead-notes")
@RequiredArgsConstructor
@Slf4j
public class LeadNoteController {

    private final LeadNoteService leadNoteService;

    /**
     * Create a new note for a lead
     *
     * @param request Request containing note details
     * @param authentication Current user authentication
     * @return Created note DTO
     */
    @PostMapping
    @RequirePermission("LEAD_NOTE_CREATE")
    public ResponseEntity<ApiResponse<LeadNoteDTO>> createLeadNote(
            @Valid @RequestBody CreateLeadNoteRequest request,
            Authentication authentication) {
        try {
            log.info("Creating new note for lead ID: {}", request.getLeadId());
            LeadNoteDTO leadNote = leadNoteService.createLeadNote(request, authentication);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Lead note created successfully", leadNote));
        } catch (Exception e) {
            log.error("Error creating lead note: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create lead note: " + e.getMessage()));
        }
    }

    /**
     * Get all notes for a specific lead (paginated by default)
     *
     * @param leadId Lead ID
     * @param page Page number (0-indexed, default: 0)
     * @param size Page size (default: 10, max: 100)
     * @return Paginated response with note DTOs
     */
    @GetMapping("/lead/{leadId}")
    @RequirePermission("LEAD_NOTE_VIEW_ALL")
    public ResponseEntity<ApiResponse<PaginatedResponse<LeadNoteDTO>>> getLeadNotesByLeadId(
            @PathVariable String leadId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        try {
            // Validate pagination parameters
            if (page < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Page number must be 0 or greater"));
            }

            if (size <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Page size must be greater than 0"));
            }

            log.info("Fetching notes for lead ID: {} with pagination - page: {}, size: {}", leadId, page, size);
            PaginatedResponse<LeadNoteDTO> paginatedLeadNotes = leadNoteService.getLeadNotesByLeadIdPaginated(leadId, page, size);
            return ResponseEntity.ok(ApiResponse.success(paginatedLeadNotes));
        } catch (Exception e) {
            log.error("Error fetching notes for lead ID {}: {}", leadId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error fetching notes: " + e.getMessage()));
        }
    }

    /**
     * Get a specific note by ID
     *
     * @param id Lead Note ID
     * @return Note DTO
     */
    @GetMapping("/{id}")
    @RequirePermission("LEAD_NOTE_VIEW_ALL")
    public ResponseEntity<ApiResponse<LeadNoteDTO>> getLeadNoteById(@PathVariable String id) {
        try {
            log.info("Fetching lead note with ID: {}", id);
            LeadNoteDTO leadNote = leadNoteService.getLeadNoteById(id);
            return ResponseEntity.ok(ApiResponse.success(leadNote));
        } catch (ResourceNotFoundException e) {
            log.error("Lead note not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching lead note with ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error fetching lead note: " + e.getMessage()));
        }
    }

    /**
     * Update an existing lead note
     *
     * @param id Lead Note ID
     * @param request Request containing updated note details
     * @param authentication Current user authentication
     * @return Updated note DTO
     */
    @PutMapping("/{id}")
    @RequirePermission("LEAD_NOTE_EDIT")
    public ResponseEntity<ApiResponse<LeadNoteDTO>> updateLeadNote(
            @PathVariable String id,
            @Valid @RequestBody UpdateLeadNoteRequest request,
            Authentication authentication) {
        try {
            log.info("Updating lead note with ID: {}", id);
            LeadNoteDTO leadNote = leadNoteService.updateLeadNote(id, request, authentication);
            return ResponseEntity.ok(ApiResponse.success("Lead note updated successfully", leadNote));
        } catch (ResourceNotFoundException e) {
            log.error("Lead note not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating lead note with ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error updating lead note: " + e.getMessage()));
        }
    }

    /**
     * Delete a lead note
     *
     * @param id Lead Note ID
     * @param authentication Current user authentication
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @RequirePermission("LEAD_NOTE_DELETE")
    public ResponseEntity<ApiResponse<Void>> deleteLeadNote(
            @PathVariable String id,
            Authentication authentication) {
        try {
            log.info("Deleting lead note with ID: {}", id);
            leadNoteService.deleteLeadNote(id, authentication);
            return ResponseEntity.ok(ApiResponse.success("Lead note deleted successfully"));
        } catch (ResourceNotFoundException e) {
            log.error("Lead note not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting lead note with ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error deleting lead note: " + e.getMessage()));
        }
    }

    /**
     * Get all lead notes (paginated by default)
     *
     * @param page Page number (0-indexed, default: 0)
     * @param size Page size (default: 10, max: 100)
     * @return Paginated response with note DTOs
     */
    @GetMapping
    @RequirePermission("LEAD_NOTE_VIEW_ALL")
    public ResponseEntity<ApiResponse<PaginatedResponse<LeadNoteDTO>>> getAllLeadNotes(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        try {
            // Validate pagination parameters
            if (page < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Page number must be 0 or greater"));
            }

            if (size <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Page size must be greater than 0"));
            }

            log.info("Fetching all lead notes with pagination - page: {}, size: {}", page, size);
            PaginatedResponse<LeadNoteDTO> paginatedLeadNotes = leadNoteService.getAllLeadNotesPaginated(page, size);
            return ResponseEntity.ok(ApiResponse.success(paginatedLeadNotes));
        } catch (Exception e) {
            log.error("Error fetching all lead notes: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error fetching lead notes: " + e.getMessage()));
        }
    }

    /**
     * Search lead notes by keyword (paginated by default)
     *
     * @param query Search term
     * @param page Page number (0-indexed, default: 0)
     * @param size Page size (default: 10, max: 100)
     * @return Paginated response with matching note DTOs
     */
    @GetMapping("/search")
    @RequirePermission("LEAD_NOTE_VIEW_ALL")
    public ResponseEntity<ApiResponse<PaginatedResponse<LeadNoteDTO>>> searchLeadNotes(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        try {
            // Validate pagination parameters
            if (page < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Page number must be 0 or greater"));
            }

            if (size <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Page size must be greater than 0"));
            }

            log.info("Searching lead notes with term: {} - page: {}, size: {}", query, page, size);
            PaginatedResponse<LeadNoteDTO> paginatedLeadNotes = leadNoteService.searchLeadNotesPaginated(query, page, size);
            return ResponseEntity.ok(ApiResponse.success(paginatedLeadNotes));
        } catch (Exception e) {
            log.error("Error searching lead notes: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error searching lead notes: " + e.getMessage()));
        }
    }

    /**
     * Search lead notes by keyword for a specific lead (paginated by default)
     *
     * @param leadId Lead ID
     * @param query Search term
     * @param page Page number (0-indexed, default: 0)
     * @param size Page size (default: 10, max: 100)
     * @return Paginated response with matching note DTOs
     */
    @GetMapping("/lead/{leadId}/search")
    @RequirePermission("LEAD_NOTE_VIEW_ALL")
    public ResponseEntity<ApiResponse<PaginatedResponse<LeadNoteDTO>>> searchLeadNotesByLeadId(
            @PathVariable String leadId,
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        try {
            // Validate pagination parameters
            if (page < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Page number must be 0 or greater"));
            }

            if (size <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Page size must be greater than 0"));
            }

            log.info("Searching notes for lead ID: {} with term: {} - page: {}, size: {}",
                     leadId, query, page, size);
            PaginatedResponse<LeadNoteDTO> paginatedLeadNotes =
                    leadNoteService.searchLeadNotesByLeadIdPaginated(leadId, query, page, size);
            return ResponseEntity.ok(ApiResponse.success(paginatedLeadNotes));
        } catch (Exception e) {
            log.error("Error searching notes for lead ID {}: {}", leadId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error searching lead notes: " + e.getMessage()));
        }
    }

    /**
     * Get total lead note count
     *
     * @return Total note count
     */
    @GetMapping("/count")
    @RequirePermission("LEAD_NOTE_VIEW_ALL")
    public ResponseEntity<ApiResponse<Long>> getLeadNoteCount() {
        try {
            log.info("Fetching total lead note count");
            long count = leadNoteService.getTotalLeadNoteCount();
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            log.error("Error fetching lead note count: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error fetching lead note count: " + e.getMessage()));
        }
    }

    /**
     * Get note count for a specific lead
     *
     * @param leadId Lead ID
     * @return Note count for the lead
     */
    @GetMapping("/lead/{leadId}/count")
    @RequirePermission("LEAD_NOTE_VIEW_ALL")
    public ResponseEntity<ApiResponse<Long>> getLeadNoteCountByLeadId(@PathVariable String leadId) {
        try {
            log.info("Fetching note count for lead ID: {}", leadId);
            long count = leadNoteService.getLeadNoteCountByLeadId(leadId);
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            log.error("Error fetching note count for lead ID {}: {}", leadId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error fetching lead note count: " + e.getMessage()));
        }
    }
}
