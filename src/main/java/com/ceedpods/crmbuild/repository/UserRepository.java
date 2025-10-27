package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.user.User;
import com.ceedpods.crmbuild.enums.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByKeycloakId(String keycloakId);

    boolean existsByEmail(String email);

    boolean existsByKeycloakId(String keycloakId);

    boolean existsByRole(UserRole role);

    long countByRole(UserRole role);

    List<User> findByRole(UserRole role);
    
    Optional<User> findFirstByRole(UserRole role);
    
    @Query("{ 'deleted': false }")
    List<User> findByDeletedFalse();
    
    @Query("{ 'enabled': true, 'deleted': false }")
    List<User> findByEnabledTrueAndDeletedFalse();
    
    @Query("{ 'deleted': false }")
    long countByDeletedFalse();
    
    @Query("{ 'enabled': true, 'deleted': false }")
    long countByEnabledTrueAndDeletedFalse();
    
    @Query("{ 'role': ?0, 'deleted': false }")
    long countByRoleAndDeletedFalse(UserRole role);
    
    @Query("{ '$or': [ " +
           "{ 'firstName': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'lastName': { '$regex': ?1, '$options': 'i' } }, " +
           "{ 'email': { '$regex': ?2, '$options': 'i' } } " +
           "], 'deleted': false }")
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String firstName, String lastName, String email);
}
