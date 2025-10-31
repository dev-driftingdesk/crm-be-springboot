package com.ceedpods.crmbuild.mapper;

import com.ceedpods.crmbuild.dto.deal.DealDTO;
import com.ceedpods.crmbuild.entity.deal.Deal;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DealMapper {

    public DealDTO toDTO(Deal entity) {
        if (entity == null) {
            return null;
        }

        DealDTO dto = DealDTO.builder()
            .id(entity.getId())
            .dealName(entity.getDealName())
            .productIds(entity.getProductIds())
            .salesReps(entity.getSalesReps())
            .leadId(entity.getLeadId())
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

    public Deal toEntity(DealDTO dto) {
        if (dto == null) {
            return null;
        }

        Deal entity = Deal.builder()
            .dealName(dto.getDealName())
            .productIds(dto.getProductIds())
            .salesReps(dto.getSalesReps())
            .leadId(dto.getLeadId())
            .build();

        // Map ID and audit fields from BaseDTO to BaseEntity
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

    public List<DealDTO> toDTO(List<Deal> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<Deal> toEntity(List<DealDTO> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
            .map(this::toEntity)
            .collect(Collectors.toList());
    }
}
