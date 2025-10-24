package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.User;
import com.ceedpods.crmbuild.enums.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByKeycloakId(String keycloakId);

    boolean existsByEmail(String email);

    boolean existsByRole(UserRole role);

    long countByRole(UserRole role);

    Optional<User> findByRole(UserRole role);
}
