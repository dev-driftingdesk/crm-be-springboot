package com.ceedpods.crmbuild.mapper;

import com.ceedpods.crmbuild.dto.dealnote.DealNoteDTO;
import com.ceedpods.crmbuild.entity.dealnote.DealNote;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between DealNote entity and DealNoteDTO
 */
@Component
public class DealNoteMapper {

    /**
     * Convert DealNote entity to DealNoteDTO
     */
    public DealNoteDTO toDTO(DealNote entity) {
        if (entity == null) {
            return null;
        }

        DealNoteDTO dto = DealNoteDTO.builder()
                .id(entity.getId())
                .dealId(entity.getDealId())
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
     * Convert DealNoteDTO to DealNote entity
     */
    public DealNote toEntity(DealNoteDTO dto) {
        if (dto == null) {
            return null;
        }

        DealNote entity = DealNote.builder()
                .dealId(dto.getDealId())
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
    public List<DealNoteDTO> toDTO(List<DealNote> entities) {
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
    public List<DealNote> toEntity(List<DealNoteDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
