package com.ceedpods.crmbuild.mapper;

import com.ceedpods.crmbuild.dto.UserInvitationDTO;
import com.ceedpods.crmbuild.entity.UserInvitation;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserInvitationMapper {
    
    public UserInvitationDTO toDTO(UserInvitation entity) {
        if (entity == null) {
            return null;
        }
        
        UserInvitationDTO dto = UserInvitationDTO.builder()
            .id(entity.getId())
            .invitationToken(entity.getInvitationToken())
            .email(entity.getEmail())
            .firstName(entity.getFirstName())
            .lastName(entity.getLastName())
            .fullName(entity.getFirstName() + " " + entity.getLastName())
            .role(entity.getRole())
            .assignedManagerId(entity.getAssignedManagerId())
            .permissionCodes(entity.getPermissionCodes())
            .invitedBy(entity.getInvitedBy())
            .invitedAt(entity.getInvitedAt())
            .expiresAt(entity.getExpiresAt())
            .used(entity.isUsed())
            .usedAt(entity.getUsedAt())
            .registeredUserId(entity.getRegisteredUserId())
            .emailSent(entity.isEmailSent())
            .emailSentAt(entity.getEmailSentAt())
            .emailAttempts(entity.getEmailAttempts())
            .lastEmailAttempt(entity.getLastEmailAttempt())
            .reminderCount(entity.getReminderCount())
            .lastReminderSent(entity.getLastReminderSent())
            .build();
        
        // Set computed fields
        dto.setExpired(entity.isExpired());
        dto.setValid(entity.isValid());
        
        if (entity.getExpiresAt() != null) {
            long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDateTime.now(), entity.getExpiresAt());
            dto.setDaysUntilExpiry(daysUntilExpiry);
            
            long hoursUntilExpiry = ChronoUnit.HOURS.between(LocalDateTime.now(), entity.getExpiresAt());
            dto.setHoursUntilExpiry(hoursUntilExpiry);
        }
        
        if (entity.getInvitedAt() != null) {
            dto.setDaysSinceInvited(ChronoUnit.DAYS.between(entity.getInvitedAt(), LocalDateTime.now()));
        }
        
        // Set invitation status
        if (entity.isUsed()) {
            dto.setInvitationStatus("USED");
        } else if (entity.isExpired()) {
            dto.setInvitationStatus("EXPIRED");
        } else if (!entity.isValid()) {
            dto.setInvitationStatus("INVALID");
        } else {
            dto.setInvitationStatus("PENDING");
        }
        
        return dto;
    }
    
    public UserInvitation toEntity(UserInvitationDTO dto) {
        if (dto == null) {
            return null;
        }
        
        UserInvitation entity = UserInvitation.builder()
            .invitationToken(dto.getInvitationToken())
            .email(dto.getEmail())
            .firstName(dto.getFirstName())
            .lastName(dto.getLastName())
            .role(dto.getRole())
            .assignedManagerId(dto.getAssignedManagerId())
            .permissionCodes(dto.getPermissionCodes())
            .invitedBy(dto.getInvitedBy())
            .invitedAt(dto.getInvitedAt())
            .expiresAt(dto.getExpiresAt())
            .used(dto.isUsed())
            .usedAt(dto.getUsedAt())
            .registeredUserId(dto.getRegisteredUserId())
            .emailSent(dto.isEmailSent())
            .emailSentAt(dto.getEmailSentAt())
            .emailAttempts(dto.getEmailAttempts())
            .lastEmailAttempt(dto.getLastEmailAttempt())
            .reminderCount(dto.getReminderCount())
            .lastReminderSent(dto.getLastReminderSent())
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
    
    public List<UserInvitationDTO> toDTO(List<UserInvitation> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    public List<UserInvitation> toEntity(List<UserInvitationDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        
        return dtos.stream()
            .map(this::toEntity)
            .collect(Collectors.toList());
    }
    
    public UserInvitationDTO toDTOWithoutToken(UserInvitation entity) {
        UserInvitationDTO dto = toDTO(entity);
        if (dto != null) {
            // Remove sensitive token information
            dto.setInvitationToken(null);
        }
        return dto;
    }
}