package com.ceedpods.crmbuild.mapper;

import com.ceedpods.crmbuild.dto.UserDTO;
import com.ceedpods.crmbuild.entity.user.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    
    public UserDTO toDTO(User entity) {
        if (entity == null) {
            return null;
        }
        
        UserDTO dto = UserDTO.builder()
            .id(entity.getId())
            .keycloakId(entity.getKeycloakId())
            .email(entity.getEmail())
            .firstName(entity.getFirstName())
            .lastName(entity.getLastName())
            .fullName(entity.getFullName())
            .role(entity.getRole())
            .enabled(entity.isEnabled())
            .emailVerified(entity.isEmailVerified())
            .phoneNumber(entity.getPhoneNumber())
            .department(entity.getDepartment())
            .territory(entity.getTerritory())
            .jobTitle(entity.getJobTitle())
            .managerId(entity.getManagerId())
            .lastLogin(entity.getLastLogin())
            .passwordChangedAt(entity.getPasswordChangedAt())
            .mustChangePassword(entity.isMustChangePassword())
            .failedLoginAttempts(entity.getFailedLoginAttempts())
            .lastFailedLogin(entity.getLastFailedLogin())
            .accountLocked(entity.isAccountLocked())
            .accountLockedAt(entity.getAccountLockedAt())
            .invitationUsed(entity.isInvitationUsed())
            .invitationUsedAt(entity.getInvitationUsedAt())
            .isManager(entity.isManager())
            .isAdmin(entity.isAdmin())
            .build();
        
        if (entity.getReportIds() != null) {
            dto.setReportsCount(entity.getReportIds().size());
        } else {
            dto.setReportsCount(0);
        }
        
        return dto;
    }
    
    public User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        
        User entity = User.builder()
            .keycloakId(dto.getKeycloakId())
            .email(dto.getEmail())
            .firstName(dto.getFirstName())
            .lastName(dto.getLastName())
            .role(dto.getRole())
            .enabled(dto.isEnabled())
            .emailVerified(dto.isEmailVerified())
            .phoneNumber(dto.getPhoneNumber())
            .department(dto.getDepartment())
            .territory(dto.getTerritory())
            .jobTitle(dto.getJobTitle())
            .managerId(dto.getManagerId())
            .lastLogin(dto.getLastLogin())
            .passwordChangedAt(dto.getPasswordChangedAt())
            .mustChangePassword(dto.isMustChangePassword())
            .failedLoginAttempts(dto.getFailedLoginAttempts())
            .lastFailedLogin(dto.getLastFailedLogin())
            .accountLocked(dto.isAccountLocked())
            .accountLockedAt(dto.getAccountLockedAt())
            .invitationUsed(dto.isInvitationUsed())
            .invitationUsedAt(dto.getInvitationUsedAt())
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
    
    public List<UserDTO> toDTO(List<User> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    public List<User> toEntity(List<UserDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        
        return dtos.stream()
            .map(this::toEntity)
            .collect(Collectors.toList());
    }
    
    public UserDTO toDTOWithoutSensitiveData(User entity) {
        UserDTO dto = toDTO(entity);
        if (dto != null) {
            // Remove sensitive information
            dto.setFailedLoginAttempts(0);
            dto.setLastFailedLogin(null);
            dto.setAccountLocked(false);
            dto.setAccountLockedAt(null);
            dto.setMustChangePassword(false);
            dto.setPasswordChangedAt(null);
        }
        return dto;
    }
}