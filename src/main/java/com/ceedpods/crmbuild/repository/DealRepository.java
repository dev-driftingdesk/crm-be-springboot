package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.deal.Deal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DealRepository extends MongoRepository<Deal, String> {

    @Query("{ 'deleted': false }")
    List<Deal> findByDeletedFalse();

    // Use Spring Data derived query instead of @Query for count methods
    long countByDeletedFalse();

    @Query(value = "{ 'dealName': ?0, 'deleted': false }", count = true)
    long countByDealNameAndDeletedFalse(String dealName);

    @Query(value = "{ 'dealName': ?0, '_id': { '$ne': ?1 }, 'deleted': false }", count = true)
    long countByDealNameAndIdNotAndDeletedFalse(String dealName, String id);

    @Query("{ 'leadId': ?0, 'deleted': false }")
    List<Deal> findByLeadIdAndDeletedFalse(String leadId);

    @Query(value = "{ 'leadId': ?0, 'deleted': false }", count = true)
    long countByLeadIdAndDeletedFalse(String leadId);

    // Query by product ID in the products array (products.productId)
    @Query("{ 'products.productId': ?0, 'deleted': false }")
    List<Deal> findByProductIdAndDeletedFalse(String productId);

    @Query(value = "{ 'products.productId': ?0, 'deleted': false }", count = true)
    long countByProductIdAndDeletedFalse(String productId);

    @Query(value = "{ 'products.productId': { '$in': ?0 }, 'deleted': false }")
    List<Deal> findByProductIdsInAndDeletedFalse(List<String> productIds);

    // Query by sales representative user ID (salesRepresentatives.userId)
    @Query("{ 'salesRepresentatives.userId': ?0, 'deleted': false }")
    List<Deal> findBySalesRepAndDeletedFalse(String salesRepUserId);

    @Query("{ '$or': [ " +
           "{ 'dealName': { '$regex': ?0, '$options': 'i' } }, " +
           "{ '_id': { '$regex': ?0, '$options': 'i' } } " +
           "], 'deleted': false }")
    List<Deal> searchDeals(String searchTerm);

    // Paginated search method for performance optimization
    @Query("{ '$or': [ " +
           "{ 'dealName': { '$regex': ?0, '$options': 'i' } }, " +
           "{ '_id': { '$regex': ?0, '$options': 'i' } } " +
           "], 'deleted': false }")
    Page<Deal> searchDeals(String searchTerm, Pageable pageable);

    // Count search results for pagination
    @Query(value = "{ '$or': [ " +
           "{ 'dealName': { '$regex': ?0, '$options': 'i' } }, " +
           "{ '_id': { '$regex': ?0, '$options': 'i' } } " +
           "], 'deleted': false }", count = true)
    long countSearchResults(String searchTerm);
}
