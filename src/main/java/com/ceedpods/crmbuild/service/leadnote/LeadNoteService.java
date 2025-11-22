package com.ceedpods.crmbuild.service.leadnote;

import com.ceedpods.crmbuild.dto.leadnote.LeadNoteDTO;
import com.ceedpods.crmbuild.dto.request.CreateLeadNoteRequest;
import com.ceedpods.crmbuild.dto.request.UpdateLeadNoteRequest;
import com.ceedpods.crmbuild.dto.response.PaginatedResponse;
import com.ceedpods.crmbuild.entity.leadnote.LeadNote;
import com.ceedpods.crmbuild.exception.BadRequestException;
import com.ceedpods.crmbuild.exception.ResourceNotFoundException;
import com.ceedpods.crmbuild.mapper.LeadNoteMapper;
import com.ceedpods.crmbuild.repository.LeadNoteRepository;
import com.ceedpods.crmbuild.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service layer for LeadNote management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LeadNoteService {

    private final LeadNoteRepository leadNoteRepository;
    private final LeadRepository leadRepository;
    private final LeadNoteMapper leadNoteMapper;

    // Pagination constants (following project convention)
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    /**
     * Create a new note for a lead
     *
     * @param request Request containing note details
     * @param authentication Current user authentication
     * @return Created note DTO
     */
    @Transactional
    public LeadNoteDTO createLeadNote(CreateLeadNoteRequest request, Authentication authentication) {
        log.info("Creating new note for lead ID: {}", request.getLeadId());

        // Validate that the lead exists
        validateLeadExists(request.getLeadId());

        // Create note entity
        LeadNote leadNote = LeadNote.builder()
                .id(UUID.randomUUID().toString())
                .leadId(request.getLeadId())
                .noteTitle(request.getNoteTitle())
                .noteContent(request.getNoteContent())
                .build();

        // Spring Data Auditing will automatically set createdBy, updatedBy, createdAt, updatedAt
        leadNote.setDeleted(false);

        // Save note
        LeadNote savedLeadNote = leadNoteRepository.save(leadNote);
        log.info("Lead note created successfully with ID: {}", savedLeadNote.getId());

        return leadNoteMapper.toDTO(savedLeadNote);
    }

    /**
     * Get all notes for a specific lead
     *
     * @param leadId Lead ID to get notes for
     * @return List of note DTOs
     */
    public List<LeadNoteDTO> getLeadNotesByLeadId(String leadId) {
        log.info("Fetching all notes for lead ID: {}", leadId);

        // Validate that the lead exists
        validateLeadExists(leadId);

        List<LeadNote> leadNotes = leadNoteRepository.findByLeadIdAndDeletedFalse(leadId);
        log.info("Found {} notes for lead ID: {}", leadNotes.size(), leadId);

        return leadNoteMapper.toDTO(leadNotes);
    }

    /**
     * Get a specific note by ID
     *
     * @param id Lead Note ID
     * @return Note DTO
     */
    public LeadNoteDTO getLeadNoteById(String id) {
        log.info("Fetching lead note with ID: {}", id);

        LeadNote leadNote = leadNoteRepository.findById(id)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Lead note not found with ID: " + id));

        return leadNoteMapper.toDTO(leadNote);
    }

    /**
     * Update an existing lead note
     *
     * @param id Lead Note ID
     * @param request Request containing updated note details
     * @param authentication Current user authentication
     * @return Updated note DTO
     */
    @Transactional
    public LeadNoteDTO updateLeadNote(String id, UpdateLeadNoteRequest request, Authentication authentication) {
        log.info("Updating lead note with ID: {}", id);

        // Find existing note
        LeadNote leadNote = leadNoteRepository.findById(id)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Lead note not found with ID: " + id));

        // Update fields if provided
        if (request.getNoteTitle() != null) {
            leadNote.setNoteTitle(request.getNoteTitle());
        }

        if (request.getNoteContent() != null) {
            leadNote.setNoteContent(request.getNoteContent());
        }

        // Spring Data Auditing will automatically update updatedBy and updatedAt
        LeadNote updatedLeadNote = leadNoteRepository.save(leadNote);
        log.info("Lead note updated successfully with ID: {}", updatedLeadNote.getId());

        return leadNoteMapper.toDTO(updatedLeadNote);
    }

    /**
     * Delete a lead note (hard delete)
     *
     * @param id Lead Note ID
     * @param authentication Current user authentication
     */
    @Transactional
    public void deleteLeadNote(String id, Authentication authentication) {
        log.info("Deleting lead note with ID: {}", id);

        // Find existing note
        LeadNote leadNote = leadNoteRepository.findById(id)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Lead note not found with ID: " + id));

        // Hard delete the note
        leadNoteRepository.delete(leadNote);
        log.info("Lead note deleted successfully with ID: {}", id);
    }

    /**
     * Get all lead notes (for admin purposes)
     *
     * @return List of all note DTOs
     */
    public List<LeadNoteDTO> getAllLeadNotes() {
        log.info("Fetching all lead notes");
        List<LeadNote> leadNotes = leadNoteRepository.findByDeletedFalse();
        return leadNoteMapper.toDTO(leadNotes);
    }

    /**
     * Search lead notes by keyword
     *
     * @param searchTerm Search term
     * @return List of matching note DTOs
     */
    public List<LeadNoteDTO> searchLeadNotes(String searchTerm) {
        log.info("Searching lead notes with term: {}", searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new BadRequestException("Search term cannot be empty");
        }

        List<LeadNote> leadNotes = leadNoteRepository.searchLeadNotes(searchTerm);
        log.info("Found {} lead notes matching search term: {}", leadNotes.size(), searchTerm);

        return leadNoteMapper.toDTO(leadNotes);
    }

    /**
     * Search lead notes by keyword for a specific lead
     *
     * @param leadId Lead ID
     * @param searchTerm Search term
     * @return List of matching note DTOs
     */
    public List<LeadNoteDTO> searchLeadNotesByLeadId(String leadId, String searchTerm) {
        log.info("Searching notes for lead ID: {} with term: {}", leadId, searchTerm);

        // Validate that the lead exists
        validateLeadExists(leadId);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new BadRequestException("Search term cannot be empty");
        }

        List<LeadNote> leadNotes = leadNoteRepository.searchLeadNotesByLeadId(leadId, searchTerm);
        log.info("Found {} notes for lead ID: {} matching search term: {}", leadNotes.size(), leadId, searchTerm);

        return leadNoteMapper.toDTO(leadNotes);
    }

    /**
     * Get total lead note count
     *
     * @return Total note count
     */
    public long getTotalLeadNoteCount() {
        return leadNoteRepository.countByDeletedFalse();
    }

    /**
     * Get note count for a specific lead
     *
     * @param leadId Lead ID
     * @return Note count for the lead
     */
    public long getLeadNoteCountByLeadId(String leadId) {
        // Validate that the lead exists
        validateLeadExists(leadId);

        return leadNoteRepository.countByLeadIdAndDeletedFalse(leadId);
    }

    // ==================== PAGINATED METHODS ====================

    /**
     * Get all lead notes with pagination
     *
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Paginated response with note DTOs
     */
    public PaginatedResponse<LeadNoteDTO> getAllLeadNotesPaginated(Integer page, Integer size) {
        log.info("Fetching all lead notes with pagination - page: {}, size: {}", page, size);

        // Validate and set pagination parameters
        int pageNumber = (page != null && page >= 0) ? page : DEFAULT_PAGE;
        int pageSize = (size != null && size > 0) ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE;

        // Create pageable request
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        // Fetch paginated notes
        Page<LeadNote> leadNotePage = leadNoteRepository.findByDeletedFalse(pageable);

        // Convert to DTOs
        List<LeadNoteDTO> leadNoteDTOs = leadNoteMapper.toDTO(leadNotePage.getContent());

        log.info("Found {} lead notes on page {} of {}", leadNoteDTOs.size(), pageNumber, leadNotePage.getTotalPages());

        // Build paginated response
        return PaginatedResponse.of(
                leadNoteDTOs,
                leadNotePage.getTotalElements(),
                leadNotePage.getTotalPages(),
                pageNumber,
                pageSize,
                leadNotePage.hasNext(),
                leadNotePage.hasPrevious()
        );
    }

    /**
     * Get all notes for a specific lead with pagination
     *
     * @param leadId Lead ID
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Paginated response with note DTOs
     */
    public PaginatedResponse<LeadNoteDTO> getLeadNotesByLeadIdPaginated(String leadId, Integer page, Integer size) {
        log.info("Fetching notes for lead ID: {} with pagination - page: {}, size: {}", leadId, page, size);

        // Validate that the lead exists
        validateLeadExists(leadId);

        // Validate and set pagination parameters
        int pageNumber = (page != null && page >= 0) ? page : DEFAULT_PAGE;
        int pageSize = (size != null && size > 0) ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE;

        // Create pageable request
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        // Fetch paginated notes for the lead
        Page<LeadNote> leadNotePage = leadNoteRepository.findByLeadIdAndDeletedFalse(leadId, pageable);

        // Convert to DTOs
        List<LeadNoteDTO> leadNoteDTOs = leadNoteMapper.toDTO(leadNotePage.getContent());

        log.info("Found {} notes for lead ID: {} on page {} of {}",
                 leadNoteDTOs.size(), leadId, pageNumber, leadNotePage.getTotalPages());

        // Build paginated response
        return PaginatedResponse.of(
                leadNoteDTOs,
                leadNotePage.getTotalElements(),
                leadNotePage.getTotalPages(),
                pageNumber,
                pageSize,
                leadNotePage.hasNext(),
                leadNotePage.hasPrevious()
        );
    }

    /**
     * Search lead notes by keyword with pagination
     *
     * @param searchTerm Search term
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Paginated response with matching note DTOs
     */
    public PaginatedResponse<LeadNoteDTO> searchLeadNotesPaginated(String searchTerm, Integer page, Integer size) {
        log.info("Searching lead notes with term: {} - page: {}, size: {}", searchTerm, page, size);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new BadRequestException("Search term cannot be empty");
        }

        // Validate and set pagination parameters
        int pageNumber = (page != null && page >= 0) ? page : DEFAULT_PAGE;
        int pageSize = (size != null && size > 0) ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE;

        // Create pageable request
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        // Search notes with pagination
        Page<LeadNote> leadNotePage = leadNoteRepository.searchLeadNotes(searchTerm, pageable);

        // Convert to DTOs
        List<LeadNoteDTO> leadNoteDTOs = leadNoteMapper.toDTO(leadNotePage.getContent());

        log.info("Found {} lead notes matching search term: {} on page {} of {}",
                 leadNoteDTOs.size(), searchTerm, pageNumber, leadNotePage.getTotalPages());

        // Build paginated response
        return PaginatedResponse.of(
                leadNoteDTOs,
                leadNotePage.getTotalElements(),
                leadNotePage.getTotalPages(),
                pageNumber,
                pageSize,
                leadNotePage.hasNext(),
                leadNotePage.hasPrevious()
        );
    }

    /**
     * Search lead notes by keyword for a specific lead with pagination
     *
     * @param leadId Lead ID
     * @param searchTerm Search term
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Paginated response with matching note DTOs
     */
    public PaginatedResponse<LeadNoteDTO> searchLeadNotesByLeadIdPaginated(String leadId, String searchTerm,
                                                                             Integer page, Integer size) {
        log.info("Searching notes for lead ID: {} with term: {} - page: {}, size: {}",
                 leadId, searchTerm, page, size);

        // Validate that the lead exists
        validateLeadExists(leadId);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new BadRequestException("Search term cannot be empty");
        }

        // Validate and set pagination parameters
        int pageNumber = (page != null && page >= 0) ? page : DEFAULT_PAGE;
        int pageSize = (size != null && size > 0) ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE;

        // Create pageable request
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        // Search notes for the lead with pagination
        Page<LeadNote> leadNotePage = leadNoteRepository.searchLeadNotesByLeadId(leadId, searchTerm, pageable);

        // Convert to DTOs
        List<LeadNoteDTO> leadNoteDTOs = leadNoteMapper.toDTO(leadNotePage.getContent());

        log.info("Found {} notes for lead ID: {} matching search term: {} on page {} of {}",
                 leadNoteDTOs.size(), leadId, searchTerm, pageNumber, leadNotePage.getTotalPages());

        // Build paginated response
        return PaginatedResponse.of(
                leadNoteDTOs,
                leadNotePage.getTotalElements(),
                leadNotePage.getTotalPages(),
                pageNumber,
                pageSize,
                leadNotePage.hasNext(),
                leadNotePage.hasPrevious()
        );
    }

    /**
     * Validate that a lead exists and is not deleted
     *
     * @param leadId Lead ID
     */
    private void validateLeadExists(String leadId) {
        boolean exists = leadRepository.findById(leadId)
                .filter(lead -> !lead.isDeleted())
                .isPresent();

        if (!exists) {
            throw new BadRequestException("Lead not found with ID: " + leadId);
        }
    }
}
