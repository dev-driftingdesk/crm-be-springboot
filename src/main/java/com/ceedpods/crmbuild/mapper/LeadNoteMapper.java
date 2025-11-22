package com.ceedpods.crmbuild.mapper;

import com.ceedpods.crmbuild.dto.leadnote.LeadNoteDTO;
import com.ceedpods.crmbuild.entity.leadnote.LeadNote;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between LeadNote entity and LeadNoteDTO
 */
@Component
public class LeadNoteMapper {

    /**
     * Convert LeadNote entity to LeadNoteDTO
     */
    public LeadNoteDTO toDTO(LeadNote entity) {
        if (entity == null) {
            return null;
        }

        LeadNoteDTO dto = LeadNoteDTO.builder()
                .id(entity.getId())
                .leadId(entity.getLeadId())
                .noteTitle(entity.getNoteTitle())
                .noteContent(entity.getNoteContent())
                .build();

        // Map audit fields from BaseEntity to BaseDTO
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setDeleted(entity.isDeleted());
        dto.setDeletedAt(entity.getDeletedAt());
        dto.setDeletedBy(entity.getDeletedBy());

        return dto;
    }

    /**
     * Convert LeadNoteDTO to LeadNote entity
     */
    public LeadNote toEntity(LeadNoteDTO dto) {
        if (dto == null) {
            return null;
        }

        LeadNote entity = LeadNote.builder()
                .leadId(dto.getLeadId())
                .noteTitle(dto.getNoteTitle())
                .noteContent(dto.getNoteContent())
                .build();

        // Map ID and audit fields
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

    /**
     * Batch conversion from entities to DTOs
     */
    public List<LeadNoteDTO> toDTO(List<LeadNote> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Batch conversion from DTOs to entities
     */
    public List<LeadNote> toEntity(List<LeadNoteDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
