package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.dealnote.DealNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for DealNote entity
 */
@Repository
public interface DealNoteRepository extends MongoRepository<DealNote, String> {

    /**
     * Find all non-deleted deal notes
     */
    @Query("{ 'deleted': false }")
    List<DealNote> findByDeletedFalse();

    /**
     * Find all non-deleted deal notes for a specific deal
     */
    @Query("{ 'dealId': ?0, 'deleted': false }")
    List<DealNote> findByDealIdAndDeletedFalse(String dealId);

    /**
     * Count all non-deleted deal notes
     */
    long countByDeletedFalse();

    /**
     * Count all non-deleted deal notes for a specific deal
     */
    @Query(value = "{ 'dealId': ?0, 'deleted': false }", count = true)
    long countByDealIdAndDeletedFalse(String dealId);

    /**
     * Search deal notes by keyword (searches in note title and content)
     */
    @Query("{ '$or': [ " +
           "{ 'noteTitle': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'noteContent': { '$regex': ?0, '$options': 'i' } } " +
           "], 'deleted': false }")
    List<DealNote> searchDealNotes(String searchTerm);

    /**
     * Search deal notes by keyword for a specific deal
     */
    @Query("{ '$and': [ " +
           "{ 'dealId': ?0 }, " +
           "{ '$or': [ " +
           "{ 'noteTitle': { '$regex': ?1, '$options': 'i' } }, " +
           "{ 'noteContent': { '$regex': ?1, '$options': 'i' } } " +
           "] }, " +
           "{ 'deleted': false } " +
           "]}")
    List<DealNote> searchDealNotesByDealId(String dealId, String searchTerm);

    // ==================== PAGINATED METHODS ====================

    /**
     * Find all non-deleted deal notes with pagination
     */
    @Query("{ 'deleted': false }")
    Page<DealNote> findByDeletedFalse(Pageable pageable);

    /**
     * Find all non-deleted deal notes for a specific deal with pagination
     */
    @Query("{ 'dealId': ?0, 'deleted': false }")
    Page<DealNote> findByDealIdAndDeletedFalse(String dealId, Pageable pageable);

    /**
     * Search deal notes by keyword with pagination
     */
    @Query("{ '$or': [ " +
           "{ 'noteTitle': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'noteContent': { '$regex': ?0, '$options': 'i' } } " +
           "], 'deleted': false }")
    Page<DealNote> searchDealNotes(String searchTerm, Pageable pageable);

    /**
     * Search deal notes by keyword for a specific deal with pagination
     */
    @Query("{ '$and': [ " +
           "{ 'dealId': ?0 }, " +
           "{ '$or': [ " +
           "{ 'noteTitle': { '$regex': ?1, '$options': 'i' } }, " +
           "{ 'noteContent': { '$regex': ?1, '$options': 'i' } } " +
           "] }, " +
           "{ 'deleted': false } " +
           "]}")
    Page<DealNote> searchDealNotesByDealId(String dealId, String searchTerm, Pageable pageable);

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
