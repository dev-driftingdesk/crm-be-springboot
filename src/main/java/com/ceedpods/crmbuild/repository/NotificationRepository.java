package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.notification.Notification;
import com.ceedpods.crmbuild.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    @Query("{ 'recipientUserIds': ?0, 'deleted': false }")
    Page<Notification> findByRecipientUserId(String userId, Pageable pageable);

    @Query("{ 'senderId': ?0, 'deleted': false }")
    Page<Notification> findBySenderId(String senderId, Pageable pageable);

    @Query("{ 'status': 'FAILED', 'deleted': false, " +
           "$expr: { $lt: ['$retryCount', '$maxRetries'] }, " +
           "$or: [ { 'nextRetryAt': null }, { 'nextRetryAt': { $lte: ?0 } } ] }")
    List<Notification> findNotificationsForRetry(LocalDateTime now);

    @Query("{ 'relatedEntityType': ?0, 'relatedEntityId': ?1, 'deleted': false }")
    List<Notification> findByRelatedEntity(String entityType, String entityId);

    @Query("{ 'status': ?0, 'deleted': false }")
    List<Notification> findByStatus(NotificationStatus status);
}
