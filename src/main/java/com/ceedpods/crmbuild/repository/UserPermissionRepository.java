package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.UserPermission;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserPermissionRepository extends MongoRepository<UserPermission, String> {
    
    List<UserPermission> findByUserId(String userId);
    
    @Query("{ 'userId': ?0, 'active': true, 'deleted': false }")
    List<UserPermission> findActivePermissionsByUserId(String userId);
    
    @Query("{ 'userId': ?0, 'permissionCode': ?1, 'active': true, 'deleted': false }")
    Optional<UserPermission> findActivePermissionByUserIdAndCode(String userId, String permissionCode);
    
    Optional<UserPermission> findByUserIdAndPermissionCode(String userId, String permissionCode);
    
    List<UserPermission> findByPermissionCode(String permissionCode);
    
    List<UserPermission> findByAssignedBy(String assignedBy);
    
    @Query("{ 'userId': ?0, 'active': true, 'deleted': false, '$or': [ { 'expiresAt': null }, { 'expiresAt': { '$gte': ?1 } } ] }")
    List<UserPermission> findEffectivePermissionsByUserId(String userId, LocalDateTime currentTime);
    
    @Query("{ 'expiresAt': { '$lt': ?0 }, 'active': true, 'deleted': false }")
    List<UserPermission> findExpiredPermissions(LocalDateTime currentTime);
    
    @Query("{ 'userId': ?0, 'active': true, 'deleted': false }")
    long countActivePermissionsByUserId(String userId);
    
    @Query("{ 'assignedBy': ?0, 'active': true, 'deleted': false }")
    long countPermissionsAssignedBy(String assignedBy);
    
    void deleteByUserId(String userId);
}