package com.ceedpods.crmbuild.mapper;

import com.ceedpods.crmbuild.dto.product.ProductDTO;
import com.ceedpods.crmbuild.entity.product.Product;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    public ProductDTO toDTO(Product entity) {
        if (entity == null) {
            return null;
        }

        ProductDTO dto = ProductDTO.builder()
            .id(entity.getId())
            .productId(entity.getProductId())
            .productName(entity.getProductName())
            .productDescription(entity.getProductDescription())
            .productSubDescription(entity.getProductSubDescription())
            .productValue(entity.getProductValue())
            .productStatus(entity.getProductStatus())
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

    public Product toEntity(ProductDTO dto) {
        if (dto == null) {
            return null;
        }

        Product entity = Product.builder()
            .productId(dto.getProductId())
            .productName(dto.getProductName())
            .productDescription(dto.getProductDescription())
            .productSubDescription(dto.getProductSubDescription())
            .productValue(dto.getProductValue())
            .productStatus(dto.getProductStatus())
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

    public List<ProductDTO> toDTO(List<Product> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<Product> toEntity(List<ProductDTO> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
            .map(this::toEntity)
            .collect(Collectors.toList());
    }
}
