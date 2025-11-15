package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.messaging.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    @Query("{ 'agentId': ?0, 'deleted': false }")
    List<Message> findByAgentIdAndDeletedFalse(String agentId);
}
