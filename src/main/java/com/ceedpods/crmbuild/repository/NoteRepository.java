package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.note.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Note entity
 */
@Repository
public interface NoteRepository extends MongoRepository<Note, String> {

    /**
     * Find all non-deleted notes
     */
    @Query("{ 'deleted': false }")
    List<Note> findByDeletedFalse();

    /**
     * Find all non-deleted notes for a specific deal
     */
    @Query("{ 'dealId': ?0, 'deleted': false }")
    List<Note> findByDealIdAndDeletedFalse(String dealId);

    /**
     * Count all non-deleted notes
     */
    long countByDeletedFalse();

    /**
     * Count all non-deleted notes for a specific deal
     */
    @Query(value = "{ 'dealId': ?0, 'deleted': false }", count = true)
    long countByDealIdAndDeletedFalse(String dealId);

    /**
     * Search notes by keyword (searches in note title and content)
     */
    @Query("{ '$or': [ " +
           "{ 'noteTitle': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'noteContent': { '$regex': ?0, '$options': 'i' } } " +
           "], 'deleted': false }")
    List<Note> searchNotes(String searchTerm);

    /**
     * Search notes by keyword for a specific deal
     */
    @Query("{ '$and': [ " +
           "{ 'dealId': ?0 }, " +
           "{ '$or': [ " +
           "{ 'noteTitle': { '$regex': ?1, '$options': 'i' } }, " +
           "{ 'noteContent': { '$regex': ?1, '$options': 'i' } } " +
           "] }, " +
           "{ 'deleted': false } " +
           "]}")
    List<Note> searchNotesByDealId(String dealId, String searchTerm);

    // ==================== PAGINATED METHODS ====================

    /**
     * Find all non-deleted notes with pagination
     */
    @Query("{ 'deleted': false }")
    Page<Note> findByDeletedFalse(Pageable pageable);

    /**
     * Find all non-deleted notes for a specific deal with pagination
     */
    @Query("{ 'dealId': ?0, 'deleted': false }")
    Page<Note> findByDealIdAndDeletedFalse(String dealId, Pageable pageable);

    /**
     * Search notes by keyword with pagination
     */
    @Query("{ '$or': [ " +
           "{ 'noteTitle': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'noteContent': { '$regex': ?0, '$options': 'i' } } " +
           "], 'deleted': false }")
    Page<Note> searchNotes(String searchTerm, Pageable pageable);

    /**
     * Search notes by keyword for a specific deal with pagination
     */
    @Query("{ '$and': [ " +
           "{ 'dealId': ?0 }, " +
           "{ '$or': [ " +
           "{ 'noteTitle': { '$regex': ?1, '$options': 'i' } }, " +
           "{ 'noteContent': { '$regex': ?1, '$options': 'i' } } " +
           "] }, " +
           "{ 'deleted': false } " +
           "]}")
    Page<Note> searchNotesByDealId(String dealId, String searchTerm, Pageable pageable);

    /**
     * Count search results for pagination
     */
    @Query(value = "{ '$or': [ " +
           "{ 'noteTitle': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'noteContent': { '$regex': ?0, '$options': 'i' } } " +
           "], 'deleted': false }", count = true)
    long countSearchResults(String searchTerm);

    /**
     * Count search results for a specific deal
     */
    @Query(value = "{ '$and': [ " +
           "{ 'dealId': ?0 }, " +
           "{ '$or': [ " +
           "{ 'noteTitle': { '$regex': ?1, '$options': 'i' } }, " +
           "{ 'noteContent': { '$regex': ?1, '$options': 'i' } } " +
           "] }, " +
           "{ 'deleted': false } " +
           "]}", count = true)
    long countSearchResultsByDealId(String dealId, String searchTerm);
}
