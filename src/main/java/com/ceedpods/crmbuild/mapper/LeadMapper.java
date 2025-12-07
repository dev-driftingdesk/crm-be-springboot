package com.ceedpods.crmbuild.mapper;

import com.ceedpods.crmbuild.dto.lead.LeadDTO;
import com.ceedpods.crmbuild.entity.lead.Lead;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LeadMapper {

    public LeadDTO toDTO(Lead entity) {
        if (entity == null) {
            return null;
        }

        List<String> dealIds = entity.getDealIds() != null ? entity.getDealIds() : new ArrayList<>();

        LeadDTO dto = LeadDTO.builder()
            .id(entity.getId())
            .originatedFrom(entity.getOriginatedFrom())
            .status(entity.getStatus())
            .leadName(entity.getLeadName())
            .company(entity.getCompany())
            .companyAddress(entity.getCompanyAddress())
            .companyWebsite(entity.getCompanyWebsite())
            .communication(entity.getCommunication())
            .platform(entity.getPlatform())
            .contactNumber(entity.getContactNumber())
            .dealIds(dealIds)
            .totalDeals(dealIds.size())
            .totalValue(BigDecimal.ZERO)
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

    public Lead toEntity(LeadDTO dto) {
        if (dto == null) {
            return null;
        }

        Lead entity = Lead.builder()
            .originatedFrom(dto.getOriginatedFrom())
            .status(dto.getStatus())
            .leadName(dto.getLeadName())
            .company(dto.getCompany())
            .companyAddress(dto.getCompanyAddress())
            .companyWebsite(dto.getCompanyWebsite())
            .communication(dto.getCommunication())
            .platform(dto.getPlatform())
            .contactNumber(dto.getContactNumber())
            .dealIds(dto.getDealIds())
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

    public List<LeadDTO> toDTO(List<Lead> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<Lead> toEntity(List<LeadDTO> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
            .map(this::toEntity)
            .collect(Collectors.toList());
    }
}
