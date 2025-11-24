package com.ceedpods.crmbuild.mapper;

import com.ceedpods.crmbuild.dto.PermissionDTO;
import com.ceedpods.crmbuild.entity.PermissionEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PermissionMapper {
    
    public PermissionDTO toDTO(PermissionEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return PermissionDTO.builder()
            .id(entity.getId())
            .permissionCode(entity.getPermissionCode())
            .displayName(entity.getDisplayName())
            .description(entity.getDescription())
            .category(entity.getCategory())
            .assignable(entity.isAssignable())
            .active(entity.isActive())
            .isSystemPermission(true) // All permissions are system permissions
            .build();
    }
    
    public PermissionEntity toEntity(PermissionDTO dto) {
        if (dto == null) {
            return null;
        }
        
        PermissionEntity entity = PermissionEntity.builder()
            .permissionCode(dto.getPermissionCode())
            .displayName(dto.getDisplayName())
            .description(dto.getDescription())
            .category(dto.getCategory())
            .assignable(dto.isAssignable())
            .active(dto.isActive())
            .build();
        
        entity.setId(dto.getId());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setDeleted(dto.isDeleted());
        entity.setDeletedAt(dto.getDeletedAt());
        entity.setDeletedBy(dto.getDeletedBy());
        
        return entity;
    }
    
    public List<PermissionDTO> toDTO(List<PermissionEntity> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    public List<PermissionEntity> toEntity(List<PermissionDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        
        return dtos.stream()
            .map(this::toEntity)
            .collect(Collectors.toList());
    }
}