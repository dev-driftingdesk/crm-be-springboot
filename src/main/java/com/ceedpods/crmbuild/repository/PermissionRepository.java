package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.PermissionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends MongoRepository<PermissionEntity, String> {
    
    Optional<PermissionEntity> findByPermissionCode(String permissionCode);
    
    List<PermissionEntity> findByCategory(String category);
    
    List<PermissionEntity> findByAssignableTrue();
    
    @Query("{ 'active': true, 'deleted': false }")
    List<PermissionEntity> findAllActive();
    
    @Query("{ 'assignable': true, 'active': true, 'deleted': false }")
    List<PermissionEntity> findAllAssignablePermissions();
    
    boolean existsByPermissionCode(String permissionCode);
}