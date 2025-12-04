package com.ceedpods.crmbuild.service.filter.strategy;

import com.ceedpods.crmbuild.dto.filter.FilterFieldDefinition;
import com.ceedpods.crmbuild.dto.filter.GlobalFilterRequest;
import com.ceedpods.crmbuild.dto.filter.GlobalFilterResponse;
import com.ceedpods.crmbuild.entity.deal.Deal;
import com.ceedpods.crmbuild.enums.FilterFieldType;
import com.ceedpods.crmbuild.enums.FilterableEntity;
import com.ceedpods.crmbuild.service.filter.BaseFilterStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Filter strategy for Deal entity.
 */
@Component
public class DealFilterStrategy extends BaseFilterStrategy<Deal> {

    private static final Map<String, FilterFieldDefinition> FIELD_DEFINITIONS = new HashMap<>();

    static {
        FIELD_DEFINITIONS.put("id", FilterFieldDefinition.builder()
                .field("id")
                .label("Deal ID")
                .type(FilterFieldType.OBJECT_ID)
                .supportedOperators(getObjectIdOperators())
                .build());

        FIELD_DEFINITIONS.put("dealName", FilterFieldDefinition.builder()
                .field("dealName")
                .label("Deal Name")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("leadId", FilterFieldDefinition.builder()
                .field("leadId")
                .label("Lead ID")
                .type(FilterFieldType.OBJECT_ID)
                .supportedOperators(getObjectIdOperators())
                .relatedEntity("Lead")
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

    public DealFilterStrategy(MongoTemplate mongoTemplate) {
        super(mongoTemplate, Deal.class);
    }

    @Override
    public FilterableEntity getEntityType() {
        return FilterableEntity.DEAL;
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
        return List.of("dealName");
    }

    @Override
    public GlobalFilterResponse<Deal> filter(GlobalFilterRequest request) {
        validateFilters(request.getFilters());

        Query query = buildQuery(request);
        Pageable pageable = createPageable(request);

        long total = mongoTemplate.count(query, entityClass);
        query.with(pageable);

        List<Deal> deals = mongoTemplate.find(query, entityClass);
        Page<Deal> page = new PageImpl<>(deals, pageable, total);

        return GlobalFilterResponse.fromPage(page, getEntityType());
    }
}
