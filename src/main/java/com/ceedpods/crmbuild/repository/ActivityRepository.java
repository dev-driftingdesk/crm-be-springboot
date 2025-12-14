package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.activity.Activity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Activity entity
 */
@Repository
public interface ActivityRepository extends MongoRepository<Activity, String> {

    /**
     * Find all non-deleted activities, ordered by performedAt (newest first)
     */
    @Query(value = "{ 'deleted': false }", sort = "{ 'performedAt': -1 }")
    List<Activity> findByDeletedFalse();

    /**
     * Find all non-deleted activities for a specific deal, ordered by performedAt (newest first)
     */
    @Query(value = "{ 'dealId': ?0, 'deleted': false }", sort = "{ 'performedAt': -1 }")
    List<Activity> findByDealIdAndDeletedFalse(String dealId);

    /**
     * Count all non-deleted activities
     */
    long countByDeletedFalse();

    /**
     * Count all non-deleted activities for a specific deal
     */
    @Query(value = "{ 'dealId': ?0, 'deleted': false }", count = true)
    long countByDealIdAndDeletedFalse(String dealId);
}
