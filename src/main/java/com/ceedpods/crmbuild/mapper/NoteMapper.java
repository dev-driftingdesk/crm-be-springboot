package com.ceedpods.crmbuild.mapper;

import com.ceedpods.crmbuild.dto.note.NoteDTO;
import com.ceedpods.crmbuild.entity.note.Note;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Note entity and NoteDTO
 */
@Component
public class NoteMapper {

    /**
     * Convert Note entity to NoteDTO
     */
    public NoteDTO toDTO(Note entity) {
        if (entity == null) {
            return null;
        }

        NoteDTO dto = NoteDTO.builder()
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
     * Convert NoteDTO to Note entity
     */
    public Note toEntity(NoteDTO dto) {
        if (dto == null) {
            return null;
        }

        Note entity = Note.builder()
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
    public List<NoteDTO> toDTO(List<Note> entities) {
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
    public List<Note> toEntity(List<NoteDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
