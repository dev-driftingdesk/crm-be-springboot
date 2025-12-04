package com.ceedpods.crmbuild.service.filter.strategy;

import com.ceedpods.crmbuild.dto.filter.FilterFieldDefinition;
import com.ceedpods.crmbuild.dto.filter.GlobalFilterRequest;
import com.ceedpods.crmbuild.dto.filter.GlobalFilterResponse;
import com.ceedpods.crmbuild.entity.product.Product;
import com.ceedpods.crmbuild.enums.FilterFieldType;
import com.ceedpods.crmbuild.enums.FilterableEntity;
import com.ceedpods.crmbuild.enums.ProductStatus;
import com.ceedpods.crmbuild.service.filter.BaseFilterStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Filter strategy for Product entity.
 */
@Component
public class ProductFilterStrategy extends BaseFilterStrategy<Product> {

    private static final Map<String, FilterFieldDefinition> FIELD_DEFINITIONS = new HashMap<>();

    static {
        FIELD_DEFINITIONS.put("id", FilterFieldDefinition.builder()
                .field("id")
                .label("Product ID")
                .type(FilterFieldType.OBJECT_ID)
                .supportedOperators(getObjectIdOperators())
                .build());

        FIELD_DEFINITIONS.put("productName", FilterFieldDefinition.builder()
                .field("productName")
                .label("Product Name")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("productDescription", FilterFieldDefinition.builder()
                .field("productDescription")
                .label("Description")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("productSubDescription", FilterFieldDefinition.builder()
                .field("productSubDescription")
                .label("Sub Description")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("productValue", FilterFieldDefinition.builder()
                .field("productValue")
                .label("Product Value")
                .type(FilterFieldType.DECIMAL)
                .supportedOperators(getNumericOperators())
                .build());

        FIELD_DEFINITIONS.put("productStatus", FilterFieldDefinition.builder()
                .field("productStatus")
                .label("Status")
                .type(FilterFieldType.ENUM)
                .supportedOperators(getEnumOperators())
                .enumValues(Arrays.stream(ProductStatus.values())
                        .map(Enum::name)
                        .collect(Collectors.toList()))
                .build());

        // Base entity fields
        FIELD_DEFINITIONS.put("createdAt", FilterFieldDefinition.builder()
                .field("createdAt")
                .label("Created At")
                .type(FilterFieldType.DATETIME)
                .supportedOperators(getDateOperators())
                .build());

        FIELD_DEFINITIONS.put("updatedAt", FilterFieldDefinition.builder()
                .field("updatedAt")
                .label("Updated At")
                .type(FilterFieldType.DATETIME)
                .supportedOperators(getDateOperators())
                .build());

        FIELD_DEFINITIONS.put("createdBy", FilterFieldDefinition.builder()
                .field("createdBy")
                .label("Created By")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("deleted", FilterFieldDefinition.builder()
                .field("deleted")
                .label("Deleted")
                .type(FilterFieldType.BOOLEAN)
                .supportedOperators(getBooleanOperators())
                .build());
    }

    public ProductFilterStrategy(MongoTemplate mongoTemplate) {
        super(mongoTemplate, Product.class);
    }

    @Override
    public FilterableEntity getEntityType() {
        return FilterableEntity.PRODUCT;
    }

    @Override
    protected Map<String, FilterFieldDefinition> getFieldDefinitions() {
        return FIELD_DEFINITIONS;
    }

    @Override
    public String getDefaultSortField() {
        return "createdAt";
    }

    @Override
    protected List<String> getSearchableFields() {
        return List.of("productName", "productDescription", "productSubDescription");
    }

    @Override
    public GlobalFilterResponse<Product> filter(GlobalFilterRequest request) {
        validateFilters(request.getFilters());

        Query query = buildQuery(request);
        Pageable pageable = createPageable(request);

        long total = mongoTemplate.count(query, entityClass);
        query.with(pageable);

        List<Product> products = mongoTemplate.find(query, entityClass);
        Page<Product> page = new PageImpl<>(products, pageable, total);

        return GlobalFilterResponse.fromPage(page, getEntityType());
    }
}
