package com.ceedpods.crmbuild.controller.note;

import com.ceedpods.crmbuild.dto.note.NoteDTO;
import com.ceedpods.crmbuild.dto.request.CreateNoteRequest;
import com.ceedpods.crmbuild.dto.request.UpdateNoteRequest;
import com.ceedpods.crmbuild.dto.response.ApiResponse;
import com.ceedpods.crmbuild.dto.response.PaginatedResponse;
import com.ceedpods.crmbuild.exception.ResourceNotFoundException;
import com.ceedpods.crmbuild.security.RequirePermission;
import com.ceedpods.crmbuild.service.note.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Note management
 */
@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
@Slf4j
public class NoteController {

    private final NoteService noteService;

    /**
     * Create a new note for a deal
     *
     * @param request Request containing note details
     * @param authentication Current user authentication
     * @return Created note DTO
     */
    @PostMapping
    @RequirePermission("NOTE_CREATE")
    public ResponseEntity<ApiResponse<NoteDTO>> createNote(
            @Valid @RequestBody CreateNoteRequest request,
            Authentication authentication) {
        try {
            log.info("Creating new note for deal ID: {}", request.getDealId());
            NoteDTO note = noteService.createNote(request, authentication);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Note created successfully", note));
        } catch (Exception e) {
            log.error("Error creating note: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create note: " + e.getMessage()));
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
    @GetMapping("/deal/{dealId}")
    @RequirePermission("NOTE_VIEW_ALL")
    public ResponseEntity<ApiResponse<PaginatedResponse<NoteDTO>>> getNotesByDealId(
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
            PaginatedResponse<NoteDTO> paginatedNotes = noteService.getNotesByDealIdPaginated(dealId, page, size);
            return ResponseEntity.ok(ApiResponse.success(paginatedNotes));
        } catch (Exception e) {
            log.error("Error fetching notes for deal ID {}: {}", dealId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error fetching notes: " + e.getMessage()));
        }
    }

    /**
     * Get a specific note by ID
     *
     * @param id Note ID
     * @return Note DTO
     */
    @GetMapping("/{id}")
    @RequirePermission("NOTE_VIEW_ALL")
    public ResponseEntity<ApiResponse<NoteDTO>> getNoteById(@PathVariable String id) {
        try {
            log.info("Fetching note with ID: {}", id);
            NoteDTO note = noteService.getNoteById(id);
            return ResponseEntity.ok(ApiResponse.success(note));
        } catch (ResourceNotFoundException e) {
            log.error("Note not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching note with ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error fetching note: " + e.getMessage()));
        }
    }

    /**
     * Update an existing note
     *
     * @param id Note ID
     * @param request Request containing updated note details
     * @param authentication Current user authentication
     * @return Updated note DTO
     */
    @PutMapping("/{id}")
    @RequirePermission("NOTE_EDIT")
    public ResponseEntity<ApiResponse<NoteDTO>> updateNote(
            @PathVariable String id,
            @Valid @RequestBody UpdateNoteRequest request,
            Authentication authentication) {
        try {
            log.info("Updating note with ID: {}", id);
            NoteDTO note = noteService.updateNote(id, request, authentication);
            return ResponseEntity.ok(ApiResponse.success("Note updated successfully", note));
        } catch (ResourceNotFoundException e) {
            log.error("Note not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating note with ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error updating note: " + e.getMessage()));
        }
    }

    /**
     * Delete a note
     *
     * @param id Note ID
     * @param authentication Current user authentication
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @RequirePermission("NOTE_DELETE")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @PathVariable String id,
            Authentication authentication) {
        try {
            log.info("Deleting note with ID: {}", id);
            noteService.deleteNote(id, authentication);
            return ResponseEntity.ok(ApiResponse.success("Note deleted successfully"));
        } catch (ResourceNotFoundException e) {
            log.error("Note not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting note with ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error deleting note: " + e.getMessage()));
        }
    }

    /**
     * Get all notes (paginated by default)
     *
     * @param page Page number (0-indexed, default: 0)
     * @param size Page size (default: 10, max: 100)
     * @return Paginated response with note DTOs
     */
    @GetMapping
    @RequirePermission("NOTE_VIEW_ALL")
    public ResponseEntity<ApiResponse<PaginatedResponse<NoteDTO>>> getAllNotes(
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

            log.info("Fetching all notes with pagination - page: {}, size: {}", page, size);
            PaginatedResponse<NoteDTO> paginatedNotes = noteService.getAllNotesPaginated(page, size);
            return ResponseEntity.ok(ApiResponse.success(paginatedNotes));
        } catch (Exception e) {
            log.error("Error fetching all notes: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error fetching notes: " + e.getMessage()));
        }
    }

    /**
     * Search notes by keyword (paginated by default)
     *
     * @param query Search term
     * @param page Page number (0-indexed, default: 0)
     * @param size Page size (default: 10, max: 100)
     * @return Paginated response with matching note DTOs
     */
    @GetMapping("/search")
    @RequirePermission("NOTE_VIEW_ALL")
    public ResponseEntity<ApiResponse<PaginatedResponse<NoteDTO>>> searchNotes(
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

            log.info("Searching notes with term: {} - page: {}, size: {}", query, page, size);
            PaginatedResponse<NoteDTO> paginatedNotes = noteService.searchNotesPaginated(query, page, size);
            return ResponseEntity.ok(ApiResponse.success(paginatedNotes));
        } catch (Exception e) {
            log.error("Error searching notes: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error searching notes: " + e.getMessage()));
        }
    }

    /**
     * Search notes by keyword for a specific deal (paginated by default)
     *
     * @param dealId Deal ID
     * @param query Search term
     * @param page Page number (0-indexed, default: 0)
     * @param size Page size (default: 10, max: 100)
     * @return Paginated response with matching note DTOs
     */
    @GetMapping("/deal/{dealId}/search")
    @RequirePermission("NOTE_VIEW_ALL")
    public ResponseEntity<ApiResponse<PaginatedResponse<NoteDTO>>> searchNotesByDealId(
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
            PaginatedResponse<NoteDTO> paginatedNotes =
                    noteService.searchNotesByDealIdPaginated(dealId, query, page, size);
            return ResponseEntity.ok(ApiResponse.success(paginatedNotes));
        } catch (Exception e) {
            log.error("Error searching notes for deal ID {}: {}", dealId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error searching notes: " + e.getMessage()));
        }
    }

    /**
     * Get total note count
     *
     * @return Total note count
     */
    @GetMapping("/count")
    @RequirePermission("NOTE_VIEW_ALL")
    public ResponseEntity<ApiResponse<Long>> getNoteCount() {
        try {
            log.info("Fetching total note count");
            long count = noteService.getTotalNoteCount();
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            log.error("Error fetching note count: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error fetching note count: " + e.getMessage()));
        }
    }

    /**
     * Get note count for a specific deal
     *
     * @param dealId Deal ID
     * @return Note count for the deal
     */
    @GetMapping("/deal/{dealId}/count")
    @RequirePermission("NOTE_VIEW_ALL")
    public ResponseEntity<ApiResponse<Long>> getNoteCountByDealId(@PathVariable String dealId) {
        try {
            log.info("Fetching note count for deal ID: {}", dealId);
            long count = noteService.getNoteCountByDealId(dealId);
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            log.error("Error fetching note count for deal ID {}: {}", dealId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error fetching note count: " + e.getMessage()));
        }
    }
}
