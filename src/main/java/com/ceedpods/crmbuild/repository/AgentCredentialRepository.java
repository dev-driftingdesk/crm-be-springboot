package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.messaging.AgentCredential;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentCredentialRepository extends MongoRepository<AgentCredential, String> {

    @Query("{ 'agentId': ?0, 'active': true, 'deleted': false }")
    Optional<AgentCredential> findActiveByAgentId(String agentId);

    @Query("{ 'agentId': ?0, 'channel': ?1, 'active': true, 'deleted': false }")
    Optional<AgentCredential> findActiveByAgentIdAndChannel(String agentId, String channel);
}
