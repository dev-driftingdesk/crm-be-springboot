package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.UserRolePermission;
import com.ceedpods.crmbuild.enums.Permission;
import com.ceedpods.crmbuild.enums.PermissionScope;
import com.ceedpods.crmbuild.enums.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRolePermissionRepository extends MongoRepository<UserRolePermission, String> {
    
    /**
     * Find all permissions for a specific role
     */
    List<UserRolePermission> findByRole(UserRole role);
    
    /**
     * Find all enabled permissions for a specific role
     */
    @Query("{'role': ?0, 'enabled': true}")
    List<UserRolePermission> findEnabledPermissionsByRole(UserRole role);
    
    /**
     * Find a specific role-permission combination
     */
    Optional<UserRolePermission> findByRoleAndPermission(UserRole role, Permission permission);
    
    /**
     * Find all permissions with a specific scope
     */
    List<UserRolePermission> findByScope(PermissionScope scope);
    
    /**
     * Find all permissions for a role with specific scope
     */
    List<UserRolePermission> findByRoleAndScope(UserRole role, PermissionScope scope);
    
    /**
     * Check if a role has a specific permission with any scope
     */
    @Query("{'role': ?0, 'permission': ?1, 'enabled': true}")
    Optional<UserRolePermission> findEnabledByRoleAndPermission(UserRole role, Permission permission);
    
    /**
     * Get all role-permission combinations where the role can access data at the given scope or higher
     */
    @Query("{'role': ?0, 'enabled': true, 'scope': {'$in': ?1}}")
    List<UserRolePermission> findByRoleAndScopeIn(UserRole role, List<PermissionScope> scopes);
    
    /**
     * Delete all permissions for a specific role (used for role cleanup)
     */
    void deleteByRole(UserRole role);
    
    /**
     * Count enabled permissions for a role
     */
    @Query(value = "{'role': ?0, 'enabled': true}", count = true)
    long countEnabledPermissionsByRole(UserRole role);
}