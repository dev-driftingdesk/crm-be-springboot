package com.ceedpods.crmbuild.mapper;

import com.ceedpods.crmbuild.dto.UserPermissionDTO;
import com.ceedpods.crmbuild.entity.UserPermission;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserPermissionMapper {
    
    public UserPermissionDTO toDTO(UserPermission entity) {
        if (entity == null) {
            return null;
        }
        
        UserPermissionDTO dto = UserPermissionDTO.builder()
            .id(entity.getId())
            .userId(entity.getUserId())
            .permissionCode(entity.getPermissionCode())
            .assignedBy(entity.getAssignedBy())
            .assignedAt(entity.getAssignedAt())
            .expiresAt(entity.getExpiresAt())
            .active(entity.isActive())
            .notes(entity.getNotes())
            .revokedBy(entity.getRevokedBy())
            .revokedAt(entity.getRevokedAt())
            .revocationReason(entity.getRevocationReason())
            .build();
        
        // Set computed fields
        dto.setExpired(entity.isExpired());
        dto.setEffective(entity.isEffective());
        
        if (entity.getExpiresAt() != null) {
            dto.setDaysUntilExpiry(ChronoUnit.DAYS.between(LocalDateTime.now(), entity.getExpiresAt()));
        }
        
        if (entity.getAssignedAt() != null) {
            dto.setDaysSinceAssigned(ChronoUnit.DAYS.between(entity.getAssignedAt(), LocalDateTime.now()));
        }
        
        return dto;
    }
    
    public UserPermission toEntity(UserPermissionDTO dto) {
        if (dto == null) {
            return null;
        }
        
        UserPermission entity = UserPermission.builder()
            .userId(dto.getUserId())
            .permissionCode(dto.getPermissionCode())
            .assignedBy(dto.getAssignedBy())
            .assignedAt(dto.getAssignedAt())
            .expiresAt(dto.getExpiresAt())
            .active(dto.isActive())
            .notes(dto.getNotes())
            .revokedBy(dto.getRevokedBy())
            .revokedAt(dto.getRevokedAt())
            .revocationReason(dto.getRevocationReason())
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
    
    public List<UserPermissionDTO> toDTO(List<UserPermission> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    public List<UserPermission> toEntity(List<UserPermissionDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        
        return dtos.stream()
            .map(this::toEntity)
            .collect(Collectors.toList());
    }
}