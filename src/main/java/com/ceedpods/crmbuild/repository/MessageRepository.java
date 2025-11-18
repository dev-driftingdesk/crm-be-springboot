package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.messaging.Message;
import com.ceedpods.crmbuild.enums.MessageStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    @Query("{ 'agentId': ?0, 'deleted': false }")
    List<Message> findByAgentIdAndDeletedFalse(String agentId);

    /**
     * Find messages that are eligible for retry
     * - Status is FAILED
     * - retryCount < maxRetries
     * - nextRetryAt is null (first retry) or before current time
     * - Not deleted
     */
    @Query("{ 'status': 'FAILED', 'deleted': false, " +
           "$expr: { $lt: ['$retryCount', '$maxRetries'] }, " +
           "$or: [ " +
           "  { 'nextRetryAt': null }, " +
           "  { 'nextRetryAt': { $lte: ?0 } } " +
           "] }")
    List<Message> findMessagesForRetry(LocalDateTime now);

    /**
     * Find all failed messages by agent
     */
    @Query("{ 'agentId': ?0, 'status': 'FAILED', 'deleted': false }")
    List<Message> findFailedMessagesByAgentId(String agentId);
}
