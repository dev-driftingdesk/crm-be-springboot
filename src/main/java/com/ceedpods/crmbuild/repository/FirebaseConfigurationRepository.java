package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.FirebaseConfiguration;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FirebaseConfigurationRepository extends MongoRepository<FirebaseConfiguration, String> {

    Optional<FirebaseConfiguration> findByActiveTrue();
}
