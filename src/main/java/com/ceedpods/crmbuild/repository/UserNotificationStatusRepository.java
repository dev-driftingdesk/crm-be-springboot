package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.notification.UserNotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationStatusRepository extends MongoRepository<UserNotificationStatus, String> {

    /**
     * Find status by user and notification
     */
    Optional<UserNotificationStatus> findByUserIdAndNotificationId(String userId, String notificationId);

    /**
     * Count unread notifications for a user
     */
    long countByUserIdAndReadFalse(String userId);

    /**
     * Find all unread notification statuses for a user (paginated)
     */
    Page<UserNotificationStatus> findByUserIdAndReadFalseOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * Find all notification statuses for a user (paginated)
     */
    Page<UserNotificationStatus> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * Find all statuses for a notification
     */
    List<UserNotificationStatus> findByNotificationId(String notificationId);

    /**
     * Find multiple statuses by user and notification IDs
     */
    @Query("{ 'userId': ?0, 'notificationId': { $in: ?1 } }")
    List<UserNotificationStatus> findByUserIdAndNotificationIds(String userId, List<String> notificationIds);

    /**
     * Mark all notifications as read for a user
     */
    @Query("{ 'userId': ?0, 'read': false }")
    @Update("{ '$set': { 'read': true, 'readAt': ?1 } }")
    long markAllAsReadForUser(String userId, java.time.LocalDateTime readAt);
}
