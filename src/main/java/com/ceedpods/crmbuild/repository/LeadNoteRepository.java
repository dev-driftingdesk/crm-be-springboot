package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.leadnote.LeadNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for LeadNote entity
 */
@Repository
public interface LeadNoteRepository extends MongoRepository<LeadNote, String> {

    /**
     * Find all non-deleted lead notes, ordered by creation date (newest first)
     */
    @Query(value = "{ 'deleted': false }", sort = "{ 'createdAt': -1 }")
    List<LeadNote> findByDeletedFalse();

    /**
     * Find all non-deleted lead notes for a specific lead, ordered by creation date (newest first)
     */
    @Query(value = "{ 'leadId': ?0, 'deleted': false }", sort = "{ 'createdAt': -1 }")
    List<LeadNote> findByLeadIdAndDeletedFalse(String leadId);

    /**
     * Count all non-deleted lead notes
     */
    long countByDeletedFalse();

    /**
     * Count all non-deleted lead notes for a specific lead
     */
    @Query(value = "{ 'leadId': ?0, 'deleted': false }", count = true)
    long countByLeadIdAndDeletedFalse(String leadId);

    /**
     * Search lead notes by keyword (searches in note title and content), ordered by creation date (newest first)
     */
    @Query(value = "{ '$or': [ " +
           "{ 'noteTitle': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'noteContent': { '$regex': ?0, '$options': 'i' } } " +
           "], 'deleted': false }", sort = "{ 'createdAt': -1 }")
    List<LeadNote> searchLeadNotes(String searchTerm);

    /**
     * Search lead notes by keyword for a specific lead, ordered by creation date (newest first)
     */
    @Query(value = "{ '$and': [ " +
           "{ 'leadId': ?0 }, " +
           "{ '$or': [ " +
           "{ 'noteTitle': { '$regex': ?1, '$options': 'i' } }, " +
           "{ 'noteContent': { '$regex': ?1, '$options': 'i' } } " +
           "] }, " +
           "{ 'deleted': false } " +
           "]}", sort = "{ 'createdAt': -1 }")
    List<LeadNote> searchLeadNotesByLeadId(String leadId, String searchTerm);

    // ==================== PAGINATED METHODS ====================

    /**
     * Find all non-deleted lead notes with pagination
     */
    @Query("{ 'deleted': false }")
    Page<LeadNote> findByDeletedFalse(Pageable pageable);

    /**
     * Find all non-deleted lead notes for a specific lead with pagination
     */
    @Query("{ 'leadId': ?0, 'deleted': false }")
    Page<LeadNote> findByLeadIdAndDeletedFalse(String leadId, Pageable pageable);

    /**
     * Search lead notes by keyword with pagination
     */
    @Query("{ '$or': [ " +
           "{ 'noteTitle': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'noteContent': { '$regex': ?0, '$options': 'i' } } " +
           "], 'deleted': false }")
    Page<LeadNote> searchLeadNotes(String searchTerm, Pageable pageable);

    /**
     * Search lead notes by keyword for a specific lead with pagination
     */
    @Query("{ '$and': [ " +
           "{ 'leadId': ?0 }, " +
           "{ '$or': [ " +
           "{ 'noteTitle': { '$regex': ?1, '$options': 'i' } }, " +
           "{ 'noteContent': { '$regex': ?1, '$options': 'i' } } " +
           "] }, " +
           "{ 'deleted': false } " +
           "]}")
    Page<LeadNote> searchLeadNotesByLeadId(String leadId, String searchTerm, Pageable pageable);

    /**
     * Count search results for pagination
     */
    @Query(value = "{ '$or': [ " +
           "{ 'noteTitle': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'noteContent': { '$regex': ?0, '$options': 'i' } } " +
           "], 'deleted': false }", count = true)
    long countSearchResults(String searchTerm);

    /**
     * Count search results for a specific lead
     */
    @Query(value = "{ '$and': [ " +
           "{ 'leadId': ?0 }, " +
           "{ '$or': [ " +
           "{ 'noteTitle': { '$regex': ?1, '$options': 'i' } }, " +
           "{ 'noteContent': { '$regex': ?1, '$options': 'i' } } " +
           "] }, " +
           "{ 'deleted': false } " +
           "]}", count = true)
    long countSearchResultsByLeadId(String leadId, String searchTerm);
}
