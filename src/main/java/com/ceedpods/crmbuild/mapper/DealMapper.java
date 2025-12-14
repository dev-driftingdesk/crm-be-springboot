package com.ceedpods.crmbuild.mapper;

import com.ceedpods.crmbuild.dto.deal.DealDTO;
import com.ceedpods.crmbuild.dto.deal.DealProduct;
import com.ceedpods.crmbuild.dto.deal.SalesRepAssignment;
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

        // Create a copy of salesRepresentatives list to avoid shared references
        List<SalesRepAssignment> salesRepsCopy = null;
        if (entity.getSalesRepresentatives() != null) {
            salesRepsCopy = entity.getSalesRepresentatives().stream()
                .map(sr -> SalesRepAssignment.builder()
                    .userId(sr.getUserId())
                    .role(sr.getRole())
                    .build())
                .collect(Collectors.toList());
        }

        // Create a copy of products list to avoid shared references
        List<DealProduct> productsCopy = null;
        if (entity.getProducts() != null) {
            productsCopy = entity.getProducts().stream()
                .map(p -> DealProduct.builder()
                    .productId(p.getProductId())
                    .packageType(p.getPackageType())
                    .quantity(p.getQuantity())
                    .build())
                .collect(Collectors.toList());
        }

        DealDTO dto = DealDTO.builder()
            .id(entity.getId())
            .dealName(entity.getDealName())
            .status(entity.getStatus())
            .dealValue(entity.getDealValue())
            .commission(entity.getCommission())
            .products(productsCopy)
            .salesRepresentatives(salesRepsCopy)
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

        // Create a copy of salesRepresentatives list to avoid shared references
        List<SalesRepAssignment> salesRepsCopy = null;
        if (dto.getSalesRepresentatives() != null) {
            salesRepsCopy = dto.getSalesRepresentatives().stream()
                .map(sr -> SalesRepAssignment.builder()
                    .userId(sr.getUserId())
                    .role(sr.getRole())
                    .build())
                .collect(Collectors.toList());
        }

        // Create a copy of products list to avoid shared references
        List<DealProduct> productsCopy = null;
        if (dto.getProducts() != null) {
            productsCopy = dto.getProducts().stream()
                .map(p -> DealProduct.builder()
                    .productId(p.getProductId())
                    .packageType(p.getPackageType())
                    .quantity(p.getQuantity())
                    .build())
                .collect(Collectors.toList());
        }

        Deal entity = Deal.builder()
            .dealName(dto.getDealName())
            .status(dto.getStatus())
            .dealValue(dto.getDealValue())
            .commission(dto.getCommission())
            .products(productsCopy)
            .salesRepresentatives(salesRepsCopy)
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
