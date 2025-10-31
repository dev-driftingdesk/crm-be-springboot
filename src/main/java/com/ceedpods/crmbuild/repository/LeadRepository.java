package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.lead.Lead;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadRepository extends MongoRepository<Lead, String> {

    @Query("{ 'deleted': false }")
    List<Lead> findByDeletedFalse();

    // Use Spring Data derived query instead of @Query for count methods
    long countByDeletedFalse();

    @Query("{ '$or': [ " +
           "{ 'leadName': { '$regex': ?0, '$options': 'i' } }, " +
           "{ '_id': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'company': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'contactNumber': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'platform': { '$regex': ?0, '$options': 'i' } } " +
           "], 'deleted': false }")
    List<Lead> searchLeads(String searchTerm);

    @Query("{ 'dealId': ?0, 'deleted': false }")
    List<Lead> findByDealIdAndDeletedFalse(String dealId);
}
