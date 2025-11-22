package com.ceedpods.crmbuild.controller.dealnote;

import com.ceedpods.crmbuild.dto.dealnote.DealNoteDTO;
import com.ceedpods.crmbuild.dto.request.CreateDealNoteRequest;
import com.ceedpods.crmbuild.dto.request.UpdateDealNoteRequest;
import com.ceedpods.crmbuild.dto.response.ApiResponse;
import com.ceedpods.crmbuild.dto.response.PaginatedResponse;
import com.ceedpods.crmbuild.exception.ResourceNotFoundException;
import com.ceedpods.crmbuild.security.RequirePermission;
import com.ceedpods.crmbuild.service.dealnote.DealNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for DealNote management
 */
@RestController
@RequestMapping("/deal-notes")
@RequiredArgsConstructor
@Slf4j
public class DealNoteController {

    private final DealNoteService dealNoteService;

    /**
     * Create a new note for a deal
     *
     * @param request Request containing note details
     * @param authentication Current user authentication
     * @return Created note DTO
     */
    @PostMapping
    @RequirePermission("DEAL_NOTE_CREATE")
    public ResponseEntity<ApiResponse<DealNoteDTO>> createDealNote(
            @Valid @RequestBody CreateDealNoteRequest request,
            Authentication authentication) {
        try {
            log.info("Creating new note for deal ID: {}", request.getDealId());
            DealNoteDTO dealNote = dealNoteService.createDealNote(request, authentication);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Deal note created successfully", dealNote));
        } catch (Exception e) {
            log.error("Error creating deal note: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create deal note: " + e.getMessage()));
        }
    }

    /**
     * Get all notes for a specific deal (paginated by default)
     *
     * @param dealId Deal ID
     * @param page Page number (0-indexed, default: 0)
     * @param size Page size (default: 10, max: 100)
     * @return Paginated response with note DTOs
     */
    @GetMapping("/{dealId}")
    @RequirePermission("DEAL_NOTE_VIEW_ALL")
    public ResponseEntity<ApiResponse<PaginatedResponse<DealNoteDTO>>> getDealNotesByDealId(
            @PathVariable String dealId,
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

            log.info("Fetching notes for deal ID: {} with pagination - page: {}, size: {}", dealId, page, size);
            PaginatedResponse<DealNoteDTO> paginatedDealNotes = dealNoteService.getDealNotesByDealIdPaginated(dealId, page, size);
            return ResponseEntity.ok(ApiResponse.success(paginatedDealNotes));
        } catch (Exception e) {
            log.error("Error fetching notes for deal ID {}: {}", dealId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error fetching notes: " + e.getMessage()));
        }
    }

    /**
     * Get a specific note by ID
     *
     * @param id Deal Note ID
     * @return Note DTO
     */
    @GetMapping("/note/{id}")
    @RequirePermission("DEAL_NOTE_VIEW_ALL")
    public ResponseEntity<ApiResponse<DealNoteDTO>> getDealNoteById(@PathVariable String id) {
        try {
            log.info("Fetching deal note with ID: {}", id);
            DealNoteDTO dealNote = dealNoteService.getDealNoteById(id);
            return ResponseEntity.ok(ApiResponse.success(dealNote));
        } catch (ResourceNotFoundException e) {
            log.error("Deal note not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching deal note with ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error fetching deal note: " + e.getMessage()));
        }
    }

    /**
     * Update an existing deal note
     *
     * @param id Deal Note ID
     * @param request Request containing updated note details
     * @param authentication Current user authentication
     * @return Updated note DTO
     */
    @PutMapping("/{id}")
    @RequirePermission("DEAL_NOTE_EDIT")
    public ResponseEntity<ApiResponse<DealNoteDTO>> updateDealNote(
            @PathVariable String id,
            @Valid @RequestBody UpdateDealNoteRequest request,
            Authentication authentication) {
        try {
            log.info("Updating deal note with ID: {}", id);
            DealNoteDTO dealNote = dealNoteService.updateDealNote(id, request, authentication);
            return ResponseEntity.ok(ApiResponse.success("Deal note updated successfully", dealNote));
        } catch (ResourceNotFoundException e) {
            log.error("Deal note not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating deal note with ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error updating deal note: " + e.getMessage()));
        }
    }

    /**
     * Delete a deal note
     *
     * @param id Deal Note ID
     * @param authentication Current user authentication
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @RequirePermission("DEAL_NOTE_DELETE")
    public ResponseEntity<ApiResponse<Void>> deleteDealNote(
            @PathVariable String id,
            Authentication authentication) {
        try {
            log.info("Deleting deal note with ID: {}", id);
            dealNoteService.deleteDealNote(id, authentication);
            return ResponseEntity.ok(ApiResponse.success("Deal note deleted successfully"));
        } catch (ResourceNotFoundException e) {
            log.error("Deal note not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting deal note with ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error deleting deal note: " + e.getMessage()));
        }
    }

    /**
     * Get all deal notes (paginated by default)
     *
     * @param page Page number (0-indexed, default: 0)
     * @param size Page size (default: 10, max: 100)
     * @return Paginated response with note DTOs
     */
    @GetMapping
    @RequirePermission("DEAL_NOTE_VIEW_ALL")
    public ResponseEntity<ApiResponse<PaginatedResponse<DealNoteDTO>>> getAllDealNotes(
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

            log.info("Fetching all deal notes with pagination - page: {}, size: {}", page, size);
            PaginatedResponse<DealNoteDTO> paginatedDealNotes = dealNoteService.getAllDealNotesPaginated(page, size);
            return ResponseEntity.ok(ApiResponse.success(paginatedDealNotes));
        } catch (Exception e) {
            log.error("Error fetching all deal notes: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error fetching deal notes: " + e.getMessage()));
        }
    }

    /**
     * Search deal notes by keyword (paginated by default)
     *
     * @param query Search term
     * @param page Page number (0-indexed, default: 0)
     * @param size Page size (default: 10, max: 100)
     * @return Paginated response with matching note DTOs
     */
    @GetMapping("/search")
    @RequirePermission("DEAL_NOTE_VIEW_ALL")
    public ResponseEntity<ApiResponse<PaginatedResponse<DealNoteDTO>>> searchDealNotes(
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

            log.info("Searching deal notes with term: {} - page: {}, size: {}", query, page, size);
            PaginatedResponse<DealNoteDTO> paginatedDealNotes = dealNoteService.searchDealNotesPaginated(query, page, size);
            return ResponseEntity.ok(ApiResponse.success(paginatedDealNotes));
        } catch (Exception e) {
            log.error("Error searching deal notes: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error searching deal notes: " + e.getMessage()));
        }
    }

    /**
     * Search deal notes by keyword for a specific deal (paginated by default)
     *
     * @param dealId Deal ID
     * @param query Search term
     * @param page Page number (0-indexed, default: 0)
     * @param size Page size (default: 10, max: 100)
     * @return Paginated response with matching note DTOs
     */
    @GetMapping("/{dealId}/search")
    @RequirePermission("DEAL_NOTE_VIEW_ALL")
    public ResponseEntity<ApiResponse<PaginatedResponse<DealNoteDTO>>> searchDealNotesByDealId(
            @PathVariable String dealId,
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

            log.info("Searching notes for deal ID: {} with term: {} - page: {}, size: {}",
                     dealId, query, page, size);
            PaginatedResponse<DealNoteDTO> paginatedDealNotes =
                    dealNoteService.searchDealNotesByDealIdPaginated(dealId, query, page, size);
            return ResponseEntity.ok(ApiResponse.success(paginatedDealNotes));
        } catch (Exception e) {
            log.error("Error searching notes for deal ID {}: {}", dealId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error searching deal notes: " + e.getMessage()));
        }
    }

    /**
     * Get total deal note count
     *
     * @return Total note count
     */
    @GetMapping("/count")
    @RequirePermission("DEAL_NOTE_VIEW_ALL")
    public ResponseEntity<ApiResponse<Long>> getDealNoteCount() {
        try {
            log.info("Fetching total deal note count");
            long count = dealNoteService.getTotalDealNoteCount();
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            log.error("Error fetching deal note count: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error fetching deal note count: " + e.getMessage()));
        }
    }

    /**
     * Get note count for a specific deal
     *
     * @param dealId Deal ID
     * @return Note count for the deal
     */
    @GetMapping("/{dealId}/count")
    @RequirePermission("DEAL_NOTE_VIEW_ALL")
    public ResponseEntity<ApiResponse<Long>> getDealNoteCountByDealId(@PathVariable String dealId) {
        try {
            log.info("Fetching note count for deal ID: {}", dealId);
            long count = dealNoteService.getDealNoteCountByDealId(dealId);
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            log.error("Error fetching note count for deal ID {}: {}", dealId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error fetching deal note count: " + e.getMessage()));
        }
    }
}
