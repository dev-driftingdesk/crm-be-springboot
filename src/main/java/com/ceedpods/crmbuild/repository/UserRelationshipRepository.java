package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.UserRelationship;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRelationshipRepository extends MongoRepository<UserRelationship, String> {
    
    @Query("{ 'managerId': ?0, 'active': true, 'deleted': false }")
    List<UserRelationship> findActiveReportsByManagerId(String managerId);
    
    @Query("{ 'salesRepId': ?0, 'active': true, 'deleted': false }")
    Optional<UserRelationship> findActiveManagerBySalesRepId(String salesRepId);
    
    @Query("{ 'salesRepId': ?0, 'managerId': ?1, 'active': true, 'deleted': false }")
    Optional<UserRelationship> findActiveRelationship(String salesRepId, String managerId);
    
    List<UserRelationship> findByManagerIdAndActiveTrue(String managerId);
    
    List<UserRelationship> findBySalesRepIdAndActiveTrue(String salesRepId);
    
    List<UserRelationship> findByAssignedBy(String assignedBy);
    
    @Query("{ 'territory': ?0, 'active': true, 'deleted': false }")
    List<UserRelationship> findActiveRelationshipsByTerritory(String territory);
    
    @Query("{ 'managerId': ?0, 'active': true, 'deleted': false }")
    long countActiveReportsByManagerId(String managerId);
    
    @Query("{ 'salesRepId': ?0 }")
    List<UserRelationship> findAllRelationshipsBySalesRepId(String salesRepId);
    
    @Query("{ 'managerId': ?0 }")
    List<UserRelationship> findAllRelationshipsByManagerId(String managerId);
    
    boolean existsBySalesRepIdAndManagerIdAndActiveTrue(String salesRepId, String managerId);
}