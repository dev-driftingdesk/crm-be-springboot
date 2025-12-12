package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.notification.DeviceToken;
import com.ceedpods.crmbuild.enums.NotificationPlatform;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends MongoRepository<DeviceToken, String> {

    @Query("{ 'userId': ?0, 'active': true, 'deleted': false }")
    List<DeviceToken> findActiveByUserId(String userId);

    @Query("{ 'userId': { $in: ?0 }, 'active': true, 'deleted': false }")
    List<DeviceToken> findActiveByUserIds(List<String> userIds);

    @Query("{ 'token': ?0, 'deleted': false }")
    Optional<DeviceToken> findByToken(String token);

    @Query("{ 'userId': ?0, 'platform': ?1, 'active': true, 'deleted': false }")
    List<DeviceToken> findActiveByUserIdAndPlatform(String userId, NotificationPlatform platform);

    @Query("{ 'subscribedTopics': ?0, 'active': true, 'deleted': false }")
    List<DeviceToken> findActiveByTopic(String topic);

    @Query("{ 'token': { $in: ?0 }, 'deleted': false }")
    List<DeviceToken> findByTokens(List<String> tokens);
}
