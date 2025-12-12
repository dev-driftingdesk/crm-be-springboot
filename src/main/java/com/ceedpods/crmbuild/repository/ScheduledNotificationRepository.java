package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.notification.ScheduledNotification;
import com.ceedpods.crmbuild.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledNotificationRepository extends MongoRepository<ScheduledNotification, String> {

    @Query("{ 'status': 'SCHEDULED', 'scheduledAt': { $lte: ?0 }, 'deleted': false }")
    List<ScheduledNotification> findDueNotifications(LocalDateTime now);

    @Query("{ 'creatorId': ?0, 'deleted': false }")
    Page<ScheduledNotification> findByCreatorId(String creatorId, Pageable pageable);

    @Query("{ 'creatorId': ?0, 'status': 'SCHEDULED', 'deleted': false }")
    List<ScheduledNotification> findPendingByCreatorId(String creatorId);

    @Query("{ 'status': ?0, 'deleted': false }")
    List<ScheduledNotification> findByStatus(NotificationStatus status);
}
