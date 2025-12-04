package com.ceedpods.crmbuild.service.filter.strategy;

import com.ceedpods.crmbuild.dto.filter.FilterCriteria;
import com.ceedpods.crmbuild.dto.filter.FilterFieldDefinition;
import com.ceedpods.crmbuild.dto.filter.GlobalFilterRequest;
import com.ceedpods.crmbuild.dto.filter.GlobalFilterResponse;
import com.ceedpods.crmbuild.entity.audit.AuditLog;
import com.ceedpods.crmbuild.enums.FilterFieldType;
import com.ceedpods.crmbuild.enums.FilterOperator;
import com.ceedpods.crmbuild.enums.FilterableEntity;
import com.ceedpods.crmbuild.service.filter.BaseFilterStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Filter strategy for AuditLog entity.
 * Note: AuditLog does not extend BaseEntity, so it has different fields.
 */
@Component
public class AuditLogFilterStrategy extends BaseFilterStrategy<AuditLog> {

    private static final Map<String, FilterFieldDefinition> FIELD_DEFINITIONS = new HashMap<>();

    static {
        FIELD_DEFINITIONS.put("id", FilterFieldDefinition.builder()
                .field("id")
                .label("Audit Log ID")
                .type(FilterFieldType.OBJECT_ID)
                .supportedOperators(getObjectIdOperators())
                .build());

        FIELD_DEFINITIONS.put("username", FilterFieldDefinition.builder()
                .field("username")
                .label("Username")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("userId", FilterFieldDefinition.builder()
                .field("userId")
                .label("User ID")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("userEmail", FilterFieldDefinition.builder()
                .field("userEmail")
                .label("User Email")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("action", FilterFieldDefinition.builder()
                .field("action")
                .label("Action")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .enumValues(List.of("LOGIN", "CREATED", "UPDATED", "DELETED"))
                .build());

        FIELD_DEFINITIONS.put("entityType", FilterFieldDefinition.builder()
                .field("entityType")
                .label("Entity Type")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .enumValues(List.of("PRODUCT", "LEAD", "DEAL", "DEAL_NOTE", "LEAD_NOTE", "USER", "MESSAGE"))
                .build());

        FIELD_DEFINITIONS.put("entityId", FilterFieldDefinition.builder()
                .field("entityId")
                .label("Entity ID")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("entityName", FilterFieldDefinition.builder()
                .field("entityName")
                .label("Entity Name")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("details", FilterFieldDefinition.builder()
                .field("details")
                .label("Details")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("ipAddress", FilterFieldDefinition.builder()
                .field("ipAddress")
                .label("IP Address")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("timestamp", FilterFieldDefinition.builder()
                .field("timestamp")
                .label("Timestamp")
                .type(FilterFieldType.DATETIME)
                .supportedOperators(getDateOperators())
                .build());

        FIELD_DEFINITIONS.put("status", FilterFieldDefinition.builder()
                .field("status")
                .label("Status")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .enumValues(List.of("SUCCESS", "FAILED"))
                .build());

        FIELD_DEFINITIONS.put("errorMessage", FilterFieldDefinition.builder()
                .field("errorMessage")
                .label("Error Message")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());
    }

    public AuditLogFilterStrategy(MongoTemplate mongoTemplate) {
        super(mongoTemplate, AuditLog.class);
    }

    @Override
    public FilterableEntity getEntityType() {
        return FilterableEntity.AUDIT_LOG;
    }

    @Override
    protected Map<String, FilterFieldDefinition> getFieldDefinitions() {
        return FIELD_DEFINITIONS;
    }

    @Override
    public String getDefaultSortField() {
        return "timestamp";
    }

    @Override
    protected List<String> getSearchableFields() {
        return List.of("username", "userEmail", "action", "entityType", "entityName", "details");
    }

    /**
     * Override buildQuery since AuditLog doesn't have a 'deleted' field
     */
    @Override
    protected Query buildQuery(GlobalFilterRequest request) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        // Add search term filter if provided
        if (request.getSearchTerm() != null && !request.getSearchTerm().trim().isEmpty()) {
            Criteria searchCriteria = buildSearchCriteria(request.getSearchTerm());
            if (searchCriteria != null) {
                criteriaList.add(searchCriteria);
            }
        }

        // Add individual filters
        if (request.getFilters() != null && !request.getFilters().isEmpty()) {
            for (FilterCriteria filter : request.getFilters()) {
                Criteria criteria = buildFilterCriteria(filter);
                if (criteria != null) {
                    criteriaList.add(criteria);
                }
            }
        }

        // Combine criteria based on logical operator
        if (!criteriaList.isEmpty()) {
            if (request.getLogicalOperator() == GlobalFilterRequest.LogicalOperator.OR) {
                query.addCriteria(new Criteria().orOperator(criteriaList.toArray(new Criteria[0])));
            } else {
                query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
            }
        }

        return query;
    }

    @Override
    public GlobalFilterResponse<AuditLog> filter(GlobalFilterRequest request) {
        validateFilters(request.getFilters());

        Query query = buildQuery(request);
        Pageable pageable = createPageable(request);

        long total = mongoTemplate.count(query, entityClass);
        query.with(pageable);

        List<AuditLog> logs = mongoTemplate.find(query, entityClass);
        Page<AuditLog> page = new PageImpl<>(logs, pageable, total);

        return GlobalFilterResponse.fromPage(page, getEntityType());
    }
}
