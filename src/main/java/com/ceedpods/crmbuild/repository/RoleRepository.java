package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.RoleEntity;
import com.ceedpods.crmbuild.enums.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends MongoRepository<RoleEntity, String> {
    
    Optional<RoleEntity> findByRoleName(UserRole roleName);
    
    boolean existsByRoleName(UserRole roleName);
    
    @Query("{ 'active': true }")
    List<RoleEntity> findAllActive();
    
    @Query("{ 'active': true, 'isSystemRole': true }")
    List<RoleEntity> findAllSystemRoles();
    
    @Query("{ 'active': true, 'isSystemRole': false }")
    List<RoleEntity> findAllCustomRoles();
}