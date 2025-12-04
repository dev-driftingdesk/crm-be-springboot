package com.ceedpods.crmbuild.service.filter.strategy;

import com.ceedpods.crmbuild.dto.filter.FilterFieldDefinition;
import com.ceedpods.crmbuild.dto.filter.GlobalFilterRequest;
import com.ceedpods.crmbuild.dto.filter.GlobalFilterResponse;
import com.ceedpods.crmbuild.entity.lead.Lead;
import com.ceedpods.crmbuild.enums.FilterFieldType;
import com.ceedpods.crmbuild.enums.FilterableEntity;
import com.ceedpods.crmbuild.enums.LeadOriginatedFrom;
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
 * Filter strategy for Lead entity.
 */
@Component
public class LeadFilterStrategy extends BaseFilterStrategy<Lead> {

    private static final Map<String, FilterFieldDefinition> FIELD_DEFINITIONS = new HashMap<>();

    static {
        FIELD_DEFINITIONS.put("id", FilterFieldDefinition.builder()
                .field("id")
                .label("Lead ID")
                .type(FilterFieldType.OBJECT_ID)
                .supportedOperators(getObjectIdOperators())
                .build());

        FIELD_DEFINITIONS.put("leadName", FilterFieldDefinition.builder()
                .field("leadName")
                .label("Lead Name")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("company", FilterFieldDefinition.builder()
                .field("company")
                .label("Company")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("companyAddress", FilterFieldDefinition.builder()
                .field("companyAddress")
                .label("Company Address")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("companyWebsite", FilterFieldDefinition.builder()
                .field("companyWebsite")
                .label("Company Website")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("platform", FilterFieldDefinition.builder()
                .field("platform")
                .label("Platform")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("contactNumber", FilterFieldDefinition.builder()
                .field("contactNumber")
                .label("Contact Number")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("originatedFrom", FilterFieldDefinition.builder()
                .field("originatedFrom")
                .label("Originated From")
                .type(FilterFieldType.ENUM)
                .supportedOperators(getEnumOperators())
                .enumValues(Arrays.stream(LeadOriginatedFrom.values())
                        .map(Enum::name)
                        .collect(Collectors.toList()))
                .build());

        FIELD_DEFINITIONS.put("dealId", FilterFieldDefinition.builder()
                .field("dealId")
                .label("Deal ID")
                .type(FilterFieldType.OBJECT_ID)
                .supportedOperators(getObjectIdOperators())
                .relatedEntity("Deal")
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

    public LeadFilterStrategy(MongoTemplate mongoTemplate) {
        super(mongoTemplate, Lead.class);
    }

    @Override
    public FilterableEntity getEntityType() {
        return FilterableEntity.LEAD;
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
        return List.of("leadName", "company", "companyAddress", "contactNumber", "platform");
    }

    @Override
    public GlobalFilterResponse<Lead> filter(GlobalFilterRequest request) {
        validateFilters(request.getFilters());

        Query query = buildQuery(request);
        Pageable pageable = createPageable(request);

        long total = mongoTemplate.count(query, entityClass);
        query.with(pageable);

        List<Lead> leads = mongoTemplate.find(query, entityClass);
        Page<Lead> page = new PageImpl<>(leads, pageable, total);

        return GlobalFilterResponse.fromPage(page, getEntityType());
    }
}
