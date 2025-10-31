package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.deal.Deal;
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

    @Query("{ 'leadId': ?0, 'deleted': false }")
    List<Deal> findByLeadIdAndDeletedFalse(String leadId);

    @Query("{ 'productIds': { '$in': [?0] }, 'deleted': false }")
    List<Deal> findByProductIdAndDeletedFalse(String productId);

    @Query("{ 'salesReps': { '$in': [?0] }, 'deleted': false }")
    List<Deal> findBySalesRepAndDeletedFalse(String salesRepId);

    @Query("{ '$or': [ " +
           "{ 'dealName': { '$regex': ?0, '$options': 'i' } }, " +
           "{ '_id': { '$regex': ?0, '$options': 'i' } } " +
           "], 'deleted': false }")
    List<Deal> searchDeals(String searchTerm);
}
