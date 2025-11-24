package com.ceedpods.crmbuild.service.dealnote;

import com.ceedpods.crmbuild.dto.dealnote.DealNoteDTO;
import com.ceedpods.crmbuild.dto.request.CreateDealNoteRequest;
import com.ceedpods.crmbuild.dto.request.UpdateDealNoteRequest;
import com.ceedpods.crmbuild.dto.response.PaginatedResponse;
import com.ceedpods.crmbuild.entity.dealnote.DealNote;
import com.ceedpods.crmbuild.exception.BadRequestException;
import com.ceedpods.crmbuild.exception.ResourceNotFoundException;
import com.ceedpods.crmbuild.mapper.DealNoteMapper;
import com.ceedpods.crmbuild.repository.DealRepository;
import com.ceedpods.crmbuild.repository.DealNoteRepository;
import com.ceedpods.crmbuild.service.auditLogService.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service layer for DealNote management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DealNoteService {

    private final DealNoteRepository dealNoteRepository;
    private final DealRepository dealRepository;
    private final DealNoteMapper dealNoteMapper;
    private final AuditLogService auditLogService;

    // Pagination constants (following project convention)
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    /**
     * Create a new note for a deal
     *
     * @param request Request containing note details
     * @param authentication Current user authentication
     * @return Created note DTO
     */
    @Transactional
    public DealNoteDTO createDealNote(CreateDealNoteRequest request, Authentication authentication) {
        log.info("Creating new note for deal ID: {}", request.getDealId());

        // Validate that the deal exists
        validateDealExists(request.getDealId());

        // Create note entity
        DealNote dealNote = DealNote.builder()
                .id(UUID.randomUUID().toString())
                .dealId(request.getDealId())
                .noteTitle(request.getNoteTitle())
                .noteContent(request.getNoteContent())
                .build();

        // Spring Data Auditing will automatically set createdBy, updatedBy, createdAt, updatedAt
        dealNote.setDeleted(false);

        // Save note
        DealNote savedDealNote = dealNoteRepository.save(dealNote);
        log.info("Deal note created successfully with ID: {}", savedDealNote.getId());

        // Log audit event
        auditLogService.logDealNoteCreated(authentication, savedDealNote.getId(), savedDealNote.getNoteTitle());

        return dealNoteMapper.toDTO(savedDealNote);
    }

    /**
     * Get all notes for a specific deal
     *
     * @param dealId Deal ID to get notes for
     * @return List of note DTOs
     */
    public List<DealNoteDTO> getDealNotesByDealId(String dealId) {
        log.info("Fetching all notes for deal ID: {}", dealId);

        // Validate that the deal exists
        validateDealExists(dealId);

        List<DealNote> dealNotes = dealNoteRepository.findByDealIdAndDeletedFalse(dealId);
        log.info("Found {} notes for deal ID: {}", dealNotes.size(), dealId);

        return dealNoteMapper.toDTO(dealNotes);
    }

    /**
     * Get a specific note by ID
     *
     * @param id Deal Note ID
     * @return Note DTO
     */
    public DealNoteDTO getDealNoteById(String id) {
        log.info("Fetching deal note with ID: {}", id);

        DealNote dealNote = dealNoteRepository.findById(id)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Deal note not found with ID: " + id));

        return dealNoteMapper.toDTO(dealNote);
    }

    /**
     * Update an existing deal note
     *
     * @param id Deal Note ID
     * @param request Request containing updated note details
     * @param authentication Current user authentication
     * @return Updated note DTO
     */
    @Transactional
    public DealNoteDTO updateDealNote(String id, UpdateDealNoteRequest request, Authentication authentication) {
        log.info("Updating deal note with ID: {}", id);

        // Find existing note
        DealNote dealNote = dealNoteRepository.findById(id)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Deal note not found with ID: " + id));

        // Update fields if provided
        if (request.getNoteTitle() != null) {
            dealNote.setNoteTitle(request.getNoteTitle());
        }

        if (request.getNoteContent() != null) {
            dealNote.setNoteContent(request.getNoteContent());
        }

        // Spring Data Auditing will automatically update updatedBy and updatedAt
        DealNote updatedDealNote = dealNoteRepository.save(dealNote);
        log.info("Deal note updated successfully with ID: {}", updatedDealNote.getId());

        // Log audit event
        auditLogService.logDealNoteUpdated(authentication, updatedDealNote.getId(), updatedDealNote.getNoteTitle());

        return dealNoteMapper.toDTO(updatedDealNote);
    }

    /**
     * Delete a deal note (hard delete)
     *
     * @param id Deal Note ID
     * @param authentication Current user authentication
     */
    @Transactional
    public void deleteDealNote(String id, Authentication authentication) {
        log.info("Deleting deal note with ID: {}", id);

        // Find existing note
        DealNote dealNote = dealNoteRepository.findById(id)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Deal note not found with ID: " + id));

        // Store note title before deletion for audit log
        String noteTitle = dealNote.getNoteTitle();

        // Hard delete the note
        dealNoteRepository.delete(dealNote);
        log.info("Deal note deleted successfully with ID: {}", id);

        // Log audit event
        auditLogService.logDealNoteDeleted(authentication, id, noteTitle);
    }

    /**
     * Get all deal notes (for admin purposes)
     *
     * @return List of all note DTOs
     */
    public List<DealNoteDTO> getAllDealNotes() {
        log.info("Fetching all deal notes");
        List<DealNote> dealNotes = dealNoteRepository.findByDeletedFalse();
        return dealNoteMapper.toDTO(dealNotes);
    }

    /**
     * Search deal notes by keyword
     *
     * @param searchTerm Search term
     * @return List of matching note DTOs
     */
    public List<DealNoteDTO> searchDealNotes(String searchTerm) {
        log.info("Searching deal notes with term: {}", searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new BadRequestException("Search term cannot be empty");
        }

        List<DealNote> dealNotes = dealNoteRepository.searchDealNotes(searchTerm);
        log.info("Found {} deal notes matching search term: {}", dealNotes.size(), searchTerm);

        return dealNoteMapper.toDTO(dealNotes);
    }

    /**
     * Search deal notes by keyword for a specific deal
     *
     * @param dealId Deal ID
     * @param searchTerm Search term
     * @return List of matching note DTOs
     */
    public List<DealNoteDTO> searchDealNotesByDealId(String dealId, String searchTerm) {
        log.info("Searching notes for deal ID: {} with term: {}", dealId, searchTerm);

        // Validate that the deal exists
        validateDealExists(dealId);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new BadRequestException("Search term cannot be empty");
        }

        List<DealNote> dealNotes = dealNoteRepository.searchDealNotesByDealId(dealId, searchTerm);
        log.info("Found {} notes for deal ID: {} matching search term: {}", dealNotes.size(), dealId, searchTerm);

        return dealNoteMapper.toDTO(dealNotes);
    }

    /**
     * Get total deal note count
     *
     * @return Total note count
     */
    public long getTotalDealNoteCount() {
        return dealNoteRepository.countByDeletedFalse();
    }

    /**
     * Get note count for a specific deal
     *
     * @param dealId Deal ID
     * @return Note count for the deal
     */
    public long getDealNoteCountByDealId(String dealId) {
        // Validate that the deal exists
        validateDealExists(dealId);

        return dealNoteRepository.countByDealIdAndDeletedFalse(dealId);
    }

    // ==================== PAGINATED METHODS ====================

    /**
     * Get all deal notes with pagination
     *
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Paginated response with note DTOs
     */
    public PaginatedResponse<DealNoteDTO> getAllDealNotesPaginated(Integer page, Integer size) {
        log.info("Fetching all deal notes with pagination - page: {}, size: {}", page, size);

        // Validate and set pagination parameters
        int pageNumber = (page != null && page >= 0) ? page : DEFAULT_PAGE;
        int pageSize = (size != null && size > 0) ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE;

        // Create pageable request with descending sort by createdAt
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Fetch paginated notes
        Page<DealNote> dealNotePage = dealNoteRepository.findByDeletedFalse(pageable);

        // Convert to DTOs
        List<DealNoteDTO> dealNoteDTOs = dealNoteMapper.toDTO(dealNotePage.getContent());

        log.info("Found {} deal notes on page {} of {}", dealNoteDTOs.size(), pageNumber, dealNotePage.getTotalPages());

        // Build paginated response
        return PaginatedResponse.of(
                dealNoteDTOs,
                dealNotePage.getTotalElements(),
                dealNotePage.getTotalPages(),
                pageNumber,
                pageSize,
                dealNotePage.hasNext(),
                dealNotePage.hasPrevious()
        );
    }

    /**
     * Get all notes for a specific deal with pagination
     *
     * @param dealId Deal ID
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Paginated response with note DTOs
     */
    public PaginatedResponse<DealNoteDTO> getDealNotesByDealIdPaginated(String dealId, Integer page, Integer size) {
        log.info("Fetching notes for deal ID: {} with pagination - page: {}, size: {}", dealId, page, size);

        // Validate that the deal exists
        validateDealExists(dealId);

        // Validate and set pagination parameters
        int pageNumber = (page != null && page >= 0) ? page : DEFAULT_PAGE;
        int pageSize = (size != null && size > 0) ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE;

        // Create pageable request with descending sort by createdAt
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Fetch paginated notes for the deal
        Page<DealNote> dealNotePage = dealNoteRepository.findByDealIdAndDeletedFalse(dealId, pageable);

        // Convert to DTOs
        List<DealNoteDTO> dealNoteDTOs = dealNoteMapper.toDTO(dealNotePage.getContent());

        log.info("Found {} notes for deal ID: {} on page {} of {}",
                 dealNoteDTOs.size(), dealId, pageNumber, dealNotePage.getTotalPages());

        // Build paginated response
        return PaginatedResponse.of(
                dealNoteDTOs,
                dealNotePage.getTotalElements(),
                dealNotePage.getTotalPages(),
                pageNumber,
                pageSize,
                dealNotePage.hasNext(),
                dealNotePage.hasPrevious()
        );
    }

    /**
     * Search deal notes by keyword with pagination
     *
     * @param searchTerm Search term
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Paginated response with matching note DTOs
     */
    public PaginatedResponse<DealNoteDTO> searchDealNotesPaginated(String searchTerm, Integer page, Integer size) {
        log.info("Searching deal notes with term: {} - page: {}, size: {}", searchTerm, page, size);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new BadRequestException("Search term cannot be empty");
        }

        // Validate and set pagination parameters
        int pageNumber = (page != null && page >= 0) ? page : DEFAULT_PAGE;
        int pageSize = (size != null && size > 0) ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE;

        // Create pageable request with descending sort by createdAt
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Search notes with pagination
        Page<DealNote> dealNotePage = dealNoteRepository.searchDealNotes(searchTerm, pageable);

        // Convert to DTOs
        List<DealNoteDTO> dealNoteDTOs = dealNoteMapper.toDTO(dealNotePage.getContent());

        log.info("Found {} deal notes matching search term: {} on page {} of {}",
                 dealNoteDTOs.size(), searchTerm, pageNumber, dealNotePage.getTotalPages());

        // Build paginated response
        return PaginatedResponse.of(
                dealNoteDTOs,
                dealNotePage.getTotalElements(),
                dealNotePage.getTotalPages(),
                pageNumber,
                pageSize,
                dealNotePage.hasNext(),
                dealNotePage.hasPrevious()
        );
    }

    /**
     * Search deal notes by keyword for a specific deal with pagination
     *
     * @param dealId Deal ID
     * @param searchTerm Search term
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Paginated response with matching note DTOs
     */
    public PaginatedResponse<DealNoteDTO> searchDealNotesByDealIdPaginated(String dealId, String searchTerm,
                                                                             Integer page, Integer size) {
        log.info("Searching notes for deal ID: {} with term: {} - page: {}, size: {}",
                 dealId, searchTerm, page, size);

        // Validate that the deal exists
        validateDealExists(dealId);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new BadRequestException("Search term cannot be empty");
        }

        // Validate and set pagination parameters
        int pageNumber = (page != null && page >= 0) ? page : DEFAULT_PAGE;
        int pageSize = (size != null && size > 0) ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE;

        // Create pageable request with descending sort by createdAt
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Search notes for the deal with pagination
        Page<DealNote> dealNotePage = dealNoteRepository.searchDealNotesByDealId(dealId, searchTerm, pageable);

        // Convert to DTOs
        List<DealNoteDTO> dealNoteDTOs = dealNoteMapper.toDTO(dealNotePage.getContent());

        log.info("Found {} notes for deal ID: {} matching search term: {} on page {} of {}",
                 dealNoteDTOs.size(), dealId, searchTerm, pageNumber, dealNotePage.getTotalPages());

        // Build paginated response
        return PaginatedResponse.of(
                dealNoteDTOs,
                dealNotePage.getTotalElements(),
                dealNotePage.getTotalPages(),
                pageNumber,
                pageSize,
                dealNotePage.hasNext(),
                dealNotePage.hasPrevious()
        );
    }

    /**
     * Validate that a deal exists and is not deleted
     *
     * @param dealId Deal ID
     */
    private void validateDealExists(String dealId) {
        boolean exists = dealRepository.findById(dealId)
                .filter(deal -> !deal.isDeleted())
                .isPresent();

        if (!exists) {
            throw new BadRequestException("Deal not found with ID: " + dealId);
        }
    }
}
