package com.ceedpods.crmbuild.service.note;

import com.ceedpods.crmbuild.dto.note.NoteDTO;
import com.ceedpods.crmbuild.dto.request.CreateNoteRequest;
import com.ceedpods.crmbuild.dto.request.UpdateNoteRequest;
import com.ceedpods.crmbuild.dto.response.PaginatedResponse;
import com.ceedpods.crmbuild.entity.note.Note;
import com.ceedpods.crmbuild.exception.BadRequestException;
import com.ceedpods.crmbuild.exception.ResourceNotFoundException;
import com.ceedpods.crmbuild.mapper.NoteMapper;
import com.ceedpods.crmbuild.repository.DealRepository;
import com.ceedpods.crmbuild.repository.NoteRepository;
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
 * Service layer for Note management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NoteService {

    private final NoteRepository noteRepository;
    private final DealRepository dealRepository;
    private final NoteMapper noteMapper;

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
    public NoteDTO createNote(CreateNoteRequest request, Authentication authentication) {
        log.info("Creating new note for deal ID: {}", request.getDealId());

        // Validate that the deal exists
        validateDealExists(request.getDealId());

        // Create note entity
        Note note = Note.builder()
                .id(UUID.randomUUID().toString())
                .dealId(request.getDealId())
                .noteTitle(request.getNoteTitle())
                .noteContent(request.getNoteContent())
                .build();

        // Spring Data Auditing will automatically set createdBy, updatedBy, createdAt, updatedAt
        note.setDeleted(false);

        // Save note
        Note savedNote = noteRepository.save(note);
        log.info("Note created successfully with ID: {}", savedNote.getId());

        return noteMapper.toDTO(savedNote);
    }

    /**
     * Get all notes for a specific deal
     *
     * @param dealId Deal ID to get notes for
     * @return List of note DTOs
     */
    public List<NoteDTO> getNotesByDealId(String dealId) {
        log.info("Fetching all notes for deal ID: {}", dealId);

        // Validate that the deal exists
        validateDealExists(dealId);

        List<Note> notes = noteRepository.findByDealIdAndDeletedFalse(dealId);
        log.info("Found {} notes for deal ID: {}", notes.size(), dealId);

        return noteMapper.toDTO(notes);
    }

    /**
     * Get a specific note by ID
     *
     * @param id Note ID
     * @return Note DTO
     */
    public NoteDTO getNoteById(String id) {
        log.info("Fetching note with ID: {}", id);

        Note note = noteRepository.findById(id)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + id));

        return noteMapper.toDTO(note);
    }

    /**
     * Update an existing note
     *
     * @param id Note ID
     * @param request Request containing updated note details
     * @param authentication Current user authentication
     * @return Updated note DTO
     */
    @Transactional
    public NoteDTO updateNote(String id, UpdateNoteRequest request, Authentication authentication) {
        log.info("Updating note with ID: {}", id);

        // Find existing note
        Note note = noteRepository.findById(id)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + id));

        // Update fields if provided
        if (request.getNoteTitle() != null) {
            note.setNoteTitle(request.getNoteTitle());
        }

        if (request.getNoteContent() != null) {
            note.setNoteContent(request.getNoteContent());
        }

        // Spring Data Auditing will automatically update updatedBy and updatedAt
        Note updatedNote = noteRepository.save(note);
        log.info("Note updated successfully with ID: {}", updatedNote.getId());

        return noteMapper.toDTO(updatedNote);
    }

    /**
     * Delete a note (hard delete)
     *
     * @param id Note ID
     * @param authentication Current user authentication
     */
    @Transactional
    public void deleteNote(String id, Authentication authentication) {
        log.info("Deleting note with ID: {}", id);

        // Find existing note
        Note note = noteRepository.findById(id)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + id));

        // Hard delete the note
        noteRepository.delete(note);
        log.info("Note deleted successfully with ID: {}", id);
    }

    /**
     * Get all notes (for admin purposes)
     *
     * @return List of all note DTOs
     */
    public List<NoteDTO> getAllNotes() {
        log.info("Fetching all notes");
        List<Note> notes = noteRepository.findByDeletedFalse();
        return noteMapper.toDTO(notes);
    }

    /**
     * Search notes by keyword
     *
     * @param searchTerm Search term
     * @return List of matching note DTOs
     */
    public List<NoteDTO> searchNotes(String searchTerm) {
        log.info("Searching notes with term: {}", searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new BadRequestException("Search term cannot be empty");
        }

        List<Note> notes = noteRepository.searchNotes(searchTerm);
        log.info("Found {} notes matching search term: {}", notes.size(), searchTerm);

        return noteMapper.toDTO(notes);
    }

    /**
     * Search notes by keyword for a specific deal
     *
     * @param dealId Deal ID
     * @param searchTerm Search term
     * @return List of matching note DTOs
     */
    public List<NoteDTO> searchNotesByDealId(String dealId, String searchTerm) {
        log.info("Searching notes for deal ID: {} with term: {}", dealId, searchTerm);

        // Validate that the deal exists
        validateDealExists(dealId);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new BadRequestException("Search term cannot be empty");
        }

        List<Note> notes = noteRepository.searchNotesByDealId(dealId, searchTerm);
        log.info("Found {} notes for deal ID: {} matching search term: {}", notes.size(), dealId, searchTerm);

        return noteMapper.toDTO(notes);
    }

    /**
     * Get total note count
     *
     * @return Total note count
     */
    public long getTotalNoteCount() {
        return noteRepository.countByDeletedFalse();
    }

    /**
     * Get note count for a specific deal
     *
     * @param dealId Deal ID
     * @return Note count for the deal
     */
    public long getNoteCountByDealId(String dealId) {
        // Validate that the deal exists
        validateDealExists(dealId);

        return noteRepository.countByDealIdAndDeletedFalse(dealId);
    }

    // ==================== PAGINATED METHODS ====================

    /**
     * Get all notes with pagination
     *
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Paginated response with note DTOs
     */
    public PaginatedResponse<NoteDTO> getAllNotesPaginated(Integer page, Integer size) {
        log.info("Fetching all notes with pagination - page: {}, size: {}", page, size);

        // Validate and set pagination parameters
        int pageNumber = (page != null && page >= 0) ? page : DEFAULT_PAGE;
        int pageSize = (size != null && size > 0) ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE;

        // Create pageable request
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        // Fetch paginated notes
        Page<Note> notePage = noteRepository.findByDeletedFalse(pageable);

        // Convert to DTOs
        List<NoteDTO> noteDTOs = noteMapper.toDTO(notePage.getContent());

        log.info("Found {} notes on page {} of {}", noteDTOs.size(), pageNumber, notePage.getTotalPages());

        // Build paginated response
        return PaginatedResponse.of(
                noteDTOs,
                notePage.getTotalElements(),
                notePage.getTotalPages(),
                pageNumber,
                pageSize,
                notePage.hasNext(),
                notePage.hasPrevious()
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
    public PaginatedResponse<NoteDTO> getNotesByDealIdPaginated(String dealId, Integer page, Integer size) {
        log.info("Fetching notes for deal ID: {} with pagination - page: {}, size: {}", dealId, page, size);

        // Validate that the deal exists
        validateDealExists(dealId);

        // Validate and set pagination parameters
        int pageNumber = (page != null && page >= 0) ? page : DEFAULT_PAGE;
        int pageSize = (size != null && size > 0) ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE;

        // Create pageable request
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        // Fetch paginated notes for the deal
        Page<Note> notePage = noteRepository.findByDealIdAndDeletedFalse(dealId, pageable);

        // Convert to DTOs
        List<NoteDTO> noteDTOs = noteMapper.toDTO(notePage.getContent());

        log.info("Found {} notes for deal ID: {} on page {} of {}",
                 noteDTOs.size(), dealId, pageNumber, notePage.getTotalPages());

        // Build paginated response
        return PaginatedResponse.of(
                noteDTOs,
                notePage.getTotalElements(),
                notePage.getTotalPages(),
                pageNumber,
                pageSize,
                notePage.hasNext(),
                notePage.hasPrevious()
        );
    }

    /**
     * Search notes by keyword with pagination
     *
     * @param searchTerm Search term
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Paginated response with matching note DTOs
     */
    public PaginatedResponse<NoteDTO> searchNotesPaginated(String searchTerm, Integer page, Integer size) {
        log.info("Searching notes with term: {} - page: {}, size: {}", searchTerm, page, size);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new BadRequestException("Search term cannot be empty");
        }

        // Validate and set pagination parameters
        int pageNumber = (page != null && page >= 0) ? page : DEFAULT_PAGE;
        int pageSize = (size != null && size > 0) ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE;

        // Create pageable request
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        // Search notes with pagination
        Page<Note> notePage = noteRepository.searchNotes(searchTerm, pageable);

        // Convert to DTOs
        List<NoteDTO> noteDTOs = noteMapper.toDTO(notePage.getContent());

        log.info("Found {} notes matching search term: {} on page {} of {}",
                 noteDTOs.size(), searchTerm, pageNumber, notePage.getTotalPages());

        // Build paginated response
        return PaginatedResponse.of(
                noteDTOs,
                notePage.getTotalElements(),
                notePage.getTotalPages(),
                pageNumber,
                pageSize,
                notePage.hasNext(),
                notePage.hasPrevious()
        );
    }

    /**
     * Search notes by keyword for a specific deal with pagination
     *
     * @param dealId Deal ID
     * @param searchTerm Search term
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Paginated response with matching note DTOs
     */
    public PaginatedResponse<NoteDTO> searchNotesByDealIdPaginated(String dealId, String searchTerm,
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

        // Create pageable request
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        // Search notes for the deal with pagination
        Page<Note> notePage = noteRepository.searchNotesByDealId(dealId, searchTerm, pageable);

        // Convert to DTOs
        List<NoteDTO> noteDTOs = noteMapper.toDTO(notePage.getContent());

        log.info("Found {} notes for deal ID: {} matching search term: {} on page {} of {}",
                 noteDTOs.size(), dealId, searchTerm, pageNumber, notePage.getTotalPages());

        // Build paginated response
        return PaginatedResponse.of(
                noteDTOs,
                notePage.getTotalElements(),
                notePage.getTotalPages(),
                pageNumber,
                pageSize,
                notePage.hasNext(),
                notePage.hasPrevious()
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
