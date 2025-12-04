package com.ceedpods.crmbuild.service.filter.strategy;

import com.ceedpods.crmbuild.dto.filter.FilterFieldDefinition;
import com.ceedpods.crmbuild.dto.filter.GlobalFilterRequest;
import com.ceedpods.crmbuild.dto.filter.GlobalFilterResponse;
import com.ceedpods.crmbuild.entity.user.User;
import com.ceedpods.crmbuild.enums.FilterFieldType;
import com.ceedpods.crmbuild.enums.FilterableEntity;
import com.ceedpods.crmbuild.enums.UserRole;
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
 * Filter strategy for User entity.
 */
@Component
public class UserFilterStrategy extends BaseFilterStrategy<User> {

    private static final Map<String, FilterFieldDefinition> FIELD_DEFINITIONS = new HashMap<>();

    static {
        FIELD_DEFINITIONS.put("id", FilterFieldDefinition.builder()
                .field("id")
                .label("User ID")
                .type(FilterFieldType.OBJECT_ID)
                .supportedOperators(getObjectIdOperators())
                .build());

        FIELD_DEFINITIONS.put("keycloakId", FilterFieldDefinition.builder()
                .field("keycloakId")
                .label("Keycloak ID")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("email", FilterFieldDefinition.builder()
                .field("email")
                .label("Email")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("firstName", FilterFieldDefinition.builder()
                .field("firstName")
                .label("First Name")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("lastName", FilterFieldDefinition.builder()
                .field("lastName")
                .label("Last Name")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("role", FilterFieldDefinition.builder()
                .field("role")
                .label("Role")
                .type(FilterFieldType.ENUM)
                .supportedOperators(getEnumOperators())
                .enumValues(Arrays.stream(UserRole.values())
                        .map(Enum::name)
                        .collect(Collectors.toList()))
                .build());

        FIELD_DEFINITIONS.put("enabled", FilterFieldDefinition.builder()
                .field("enabled")
                .label("Enabled")
                .type(FilterFieldType.BOOLEAN)
                .supportedOperators(getBooleanOperators())
                .build());

        FIELD_DEFINITIONS.put("emailVerified", FilterFieldDefinition.builder()
                .field("emailVerified")
                .label("Email Verified")
                .type(FilterFieldType.BOOLEAN)
                .supportedOperators(getBooleanOperators())
                .build());

        FIELD_DEFINITIONS.put("phoneNumber", FilterFieldDefinition.builder()
                .field("phoneNumber")
                .label("Phone Number")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("department", FilterFieldDefinition.builder()
                .field("department")
                .label("Department")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("territory", FilterFieldDefinition.builder()
                .field("territory")
                .label("Territory")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("jobTitle", FilterFieldDefinition.builder()
                .field("jobTitle")
                .label("Job Title")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("managerId", FilterFieldDefinition.builder()
                .field("managerId")
                .label("Manager ID")
                .type(FilterFieldType.OBJECT_ID)
                .supportedOperators(getObjectIdOperators())
                .relatedEntity("User")
                .build());

        FIELD_DEFINITIONS.put("lastLogin", FilterFieldDefinition.builder()
                .field("lastLogin")
                .label("Last Login")
                .type(FilterFieldType.DATETIME)
                .supportedOperators(getDateOperators())
                .build());

        FIELD_DEFINITIONS.put("accountLocked", FilterFieldDefinition.builder()
                .field("accountLocked")
                .label("Account Locked")
                .type(FilterFieldType.BOOLEAN)
                .supportedOperators(getBooleanOperators())
                .build());

        FIELD_DEFINITIONS.put("mustChangePassword", FilterFieldDefinition.builder()
                .field("mustChangePassword")
                .label("Must Change Password")
                .type(FilterFieldType.BOOLEAN)
                .supportedOperators(getBooleanOperators())
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

        FIELD_DEFINITIONS.put("deleted", FilterFieldDefinition.builder()
                .field("deleted")
                .label("Deleted")
                .type(FilterFieldType.BOOLEAN)
                .supportedOperators(getBooleanOperators())
                .build());
    }

    public UserFilterStrategy(MongoTemplate mongoTemplate) {
        super(mongoTemplate, User.class);
    }

    @Override
    public FilterableEntity getEntityType() {
        return FilterableEntity.USER;
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
        return List.of("email", "firstName", "lastName", "phoneNumber", "department", "territory", "jobTitle");
    }

    @Override
    public GlobalFilterResponse<User> filter(GlobalFilterRequest request) {
        validateFilters(request.getFilters());

        Query query = buildQuery(request);
        Pageable pageable = createPageable(request);

        long total = mongoTemplate.count(query, entityClass);
        query.with(pageable);

        List<User> users = mongoTemplate.find(query, entityClass);
        Page<User> page = new PageImpl<>(users, pageable, total);

        return GlobalFilterResponse.fromPage(page, getEntityType());
    }
}
