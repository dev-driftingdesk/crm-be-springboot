package com.ceedpods.crmbuild.mapper;

import com.ceedpods.crmbuild.dto.product.ProductDTO;
import com.ceedpods.crmbuild.dto.request.CreateProductRequest;
import com.ceedpods.crmbuild.entity.product.DiscountAddOn;
import com.ceedpods.crmbuild.entity.product.PricingPackage;
import com.ceedpods.crmbuild.entity.product.PricingPackages;
import com.ceedpods.crmbuild.entity.product.Product;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    public ProductDTO toDTO(Product entity) {
        if (entity == null) {
            return null;
        }

        ProductDTO.ProductDTOBuilder builder = ProductDTO.builder()
            .id(entity.getId())
            .productName(entity.getProductName())
            .productStatus(entity.getProductStatus())
            .basePrice(entity.getBasePrice())
            .keyLearningOutcomes(entity.getKeyLearningOutcomes())
            .format(entity.getFormat())
            .duration(entity.getDuration())
            .level(entity.getLevel())
            .instructors(entity.getInstructors());

        // Map pricing packages
        if (entity.getPricingPackages() != null) {
            builder.pricingPackages(mapPricingPackagesToDTO(entity.getPricingPackages()));
        }

        // Map discounts and add-ons
        if (entity.getDiscountsAddOns() != null) {
            builder.discountsAddOns(mapDiscountsAddOnsToDTO(entity.getDiscountsAddOns()));
        }

        ProductDTO dto = builder.build();

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

    private ProductDTO.PricingPackagesDTO mapPricingPackagesToDTO(PricingPackages pricingPackages) {
        if (pricingPackages == null) {
            return null;
        }

        List<ProductDTO.PackageDTO> packageDTOs = null;
        if (pricingPackages.getPackages() != null) {
            packageDTOs = pricingPackages.getPackages().stream()
                .map(pkg -> ProductDTO.PackageDTO.builder()
                    .packageType(pkg.getPackageType())
                    .description(pkg.getDescription())
                    .price(pkg.getPrice())
                    .commissionRate(pkg.getCommissionRate())
                    .notes(pkg.getNotes())
                    .build())
                .collect(Collectors.toList());
        }

        return ProductDTO.PricingPackagesDTO.builder()
            .enabled(pricingPackages.getEnabled())
            .packages(packageDTOs)
            .build();
    }

    private List<ProductDTO.DiscountAddOnDTO> mapDiscountsAddOnsToDTO(List<DiscountAddOn> discountsAddOns) {
        if (discountsAddOns == null) {
            return null;
        }

        return discountsAddOns.stream()
            .map(item -> ProductDTO.DiscountAddOnDTO.builder()
                .type(item.getType())
                .description(item.getDescription())
                .build())
            .collect(Collectors.toList());
    }

    public Product toEntity(ProductDTO dto) {
        if (dto == null) {
            return null;
        }

        Product.ProductBuilder builder = Product.builder()
            .id(dto.getId())
            .productName(dto.getProductName())
            .productStatus(dto.getProductStatus())
            .basePrice(dto.getBasePrice())
            .keyLearningOutcomes(dto.getKeyLearningOutcomes())
            .format(dto.getFormat())
            .duration(dto.getDuration())
            .level(dto.getLevel())
            .instructors(dto.getInstructors());

        // Map pricing packages
        if (dto.getPricingPackages() != null) {
            builder.pricingPackages(mapPricingPackagesToEntity(dto.getPricingPackages()));
        }

        // Map discounts and add-ons
        if (dto.getDiscountsAddOns() != null) {
            builder.discountsAddOns(mapDiscountsAddOnsToEntity(dto.getDiscountsAddOns()));
        }

        Product entity = builder.build();

        // Map audit fields from BaseDTO to BaseEntity
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setDeleted(dto.isDeleted());
        entity.setDeletedAt(dto.getDeletedAt());
        entity.setDeletedBy(dto.getDeletedBy());

        return entity;
    }

    private PricingPackages mapPricingPackagesToEntity(ProductDTO.PricingPackagesDTO pricingPackagesDTO) {
        if (pricingPackagesDTO == null) {
            return null;
        }

        List<PricingPackage> packages = null;
        if (pricingPackagesDTO.getPackages() != null) {
            packages = pricingPackagesDTO.getPackages().stream()
                .map(pkg -> PricingPackage.builder()
                    .packageType(pkg.getPackageType())
                    .description(pkg.getDescription())
                    .price(pkg.getPrice())
                    .commissionRate(pkg.getCommissionRate())
                    .notes(pkg.getNotes())
                    .build())
                .collect(Collectors.toList());
        }

        return PricingPackages.builder()
            .enabled(pricingPackagesDTO.getEnabled())
            .packages(packages)
            .build();
    }

    private List<DiscountAddOn> mapDiscountsAddOnsToEntity(List<ProductDTO.DiscountAddOnDTO> discountsAddOnsDTOs) {
        if (discountsAddOnsDTOs == null) {
            return null;
        }

        return discountsAddOnsDTOs.stream()
            .map(item -> DiscountAddOn.builder()
                .type(item.getType())
                .description(item.getDescription())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * Converts CreateProductRequest to Entity for creation
     * Optimized: Direct field assignment, minimal object creation
     */
    public Product toEntityForCreation(CreateProductRequest request) {
        if (request == null) {
            return null;
        }

        CreateProductRequest.BasicInformation basicInfo = request.getBasicInformation();
        CreateProductRequest.PricingPackagesRequest pricingReq = request.getPricingPackages();
        List<CreateProductRequest.DiscountAddOnRequest> discountsReq = request.getDiscountsAddOns();

        return Product.builder()
            .id(java.util.UUID.randomUUID().toString())
            .productName(basicInfo.getProductName())
            .basePrice(basicInfo.getBasePrice())
            .keyLearningOutcomes(basicInfo.getKeyLearningOutcomes())
            .format(basicInfo.getFormat())
            .duration(basicInfo.getDuration())
            .level(basicInfo.getLevel())
            .instructors(basicInfo.getInstructors())
            .productStatus(request.getProductStatus())
            .pricingPackages(pricingReq != null ? mapPricingPackagesRequestToEntity(pricingReq) : null)
            .discountsAddOns(discountsReq != null && !discountsReq.isEmpty() ? mapDiscountsAddOnsRequestToEntity(discountsReq) : null)
            .build();
    }

    private PricingPackages mapPricingPackagesRequestToEntity(CreateProductRequest.PricingPackagesRequest pricingPackagesRequest) {
        List<CreateProductRequest.PackageRequest> packageRequests = pricingPackagesRequest.getPackages();

        List<PricingPackage> packages = (packageRequests != null && !packageRequests.isEmpty())
            ? packageRequests.stream()
                .map(this::mapPackageRequestToEntity)
                .collect(Collectors.toList())
            : null;

        return PricingPackages.builder()
            .enabled(pricingPackagesRequest.getEnabled())
            .packages(packages)
            .build();
    }

    private PricingPackage mapPackageRequestToEntity(CreateProductRequest.PackageRequest pkg) {
        return PricingPackage.builder()
            .packageType(pkg.getPackageType())
            .description(pkg.getDescription())
            .price(pkg.getPrice())
            .commissionRate(pkg.getCommissionRate())
            .notes(pkg.getNotes())
            .build();
    }

    private List<DiscountAddOn> mapDiscountsAddOnsRequestToEntity(List<CreateProductRequest.DiscountAddOnRequest> discountsAddOnsRequests) {
        return discountsAddOnsRequests.stream()
            .map(this::mapDiscountAddOnRequestToEntity)
            .collect(Collectors.toList());
    }

    private DiscountAddOn mapDiscountAddOnRequestToEntity(CreateProductRequest.DiscountAddOnRequest item) {
        return DiscountAddOn.builder()
            .type(item.getType())
            .description(item.getDescription())
            .build();
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
