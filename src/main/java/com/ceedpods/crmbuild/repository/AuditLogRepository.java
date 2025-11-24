package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.audit.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AuditLog entity
 */
@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    /**
     * Find audit logs by username
     */
    Page<AuditLog> findByUsername(String username, Pageable pageable);

    /**
     * Find audit logs by user ID
     */
    Page<AuditLog> findByUserId(String userId, Pageable pageable);

    /**
     * Find audit logs by action
     */
    Page<AuditLog> findByAction(String action, Pageable pageable);

    /**
     * Find audit logs by entity type
     */
    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);

    /**
     * Find audit logs by entity ID
     */
    Page<AuditLog> findByEntityId(String entityId, Pageable pageable);

    /**
     * Find audit logs by entity type and entity ID
     */
    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId, Pageable pageable);

    /**
     * Find audit logs within a date range
     */
    Page<AuditLog> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find audit logs by user and action
     */
    Page<AuditLog> findByUsernameAndAction(String username, String action, Pageable pageable);

    /**
     * Find audit logs by entity type and action
     */
    Page<AuditLog> findByEntityTypeAndAction(String entityType, String action, Pageable pageable);

    /**
     * Find all audit logs ordered by timestamp descending
     */
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    /**
     * Find audit logs by multiple criteria
     */
    @Query("{ $and: [ " +
            "{ $or: [ { 'username': ?0 }, { 'username': { $exists: false } } ] }, " +
            "{ $or: [ { 'action': ?1 }, { 'action': { $exists: false } } ] }, " +
            "{ $or: [ { 'entityType': ?2 }, { 'entityType': { $exists: false } } ] } " +
            "] }")
    Page<AuditLog> findByMultipleCriteria(String username, String action, String entityType, Pageable pageable);

    /**
     * Count audit logs by user
     */
    long countByUsername(String username);

    /**
     * Count audit logs by action
     */
    long countByAction(String action);

    /**
     * Count audit logs by entity type
     */
    long countByEntityType(String entityType);
}
