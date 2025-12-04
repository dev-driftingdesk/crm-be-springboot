package com.ceedpods.crmbuild.service.filter;

import com.ceedpods.crmbuild.dto.filter.FilterCriteria;
import com.ceedpods.crmbuild.dto.filter.FilterFieldDefinition;
import com.ceedpods.crmbuild.dto.filter.GlobalFilterRequest;
import com.ceedpods.crmbuild.enums.FilterFieldType;
import com.ceedpods.crmbuild.enums.FilterOperator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Abstract base class for filter strategies.
 * Provides common filtering logic that can be reused across entities.
 */
public abstract class BaseFilterStrategy<T> implements EntityFilterStrategy<T> {

    protected final MongoTemplate mongoTemplate;
    protected final Class<T> entityClass;

    protected BaseFilterStrategy(MongoTemplate mongoTemplate, Class<T> entityClass) {
        this.mongoTemplate = mongoTemplate;
        this.entityClass = entityClass;
    }

    /**
     * Get the map of field names to their definitions.
     * Subclasses must implement this.
     */
    protected abstract Map<String, FilterFieldDefinition> getFieldDefinitions();

    /**
     * Get the collection name for this entity.
     */
    protected String getCollectionName() {
        return getEntityType().getCollectionName();
    }

    @Override
    public List<FilterFieldDefinition> getFilterableFields() {
        return new ArrayList<>(getFieldDefinitions().values());
    }

    @Override
    public boolean isFieldFilterable(String fieldName) {
        return getFieldDefinitions().containsKey(fieldName);
    }

    @Override
    public void validateFilters(List<FilterCriteria> filters) {
        if (filters == null || filters.isEmpty()) {
            return;
        }

        Map<String, FilterFieldDefinition> definitions = getFieldDefinitions();

        for (FilterCriteria filter : filters) {
            String field = filter.getField();

            if (!definitions.containsKey(field)) {
                throw new IllegalArgumentException(
                    "Field '" + field + "' is not filterable for entity " + getEntityType().getDisplayName()
                );
            }

            FilterFieldDefinition definition = definitions.get(field);
            FilterOperator operator = filter.getOperator();

            if (operator != null && !definition.getSupportedOperators().contains(operator)) {
                throw new IllegalArgumentException(
                    "Operator '" + operator + "' is not supported for field '" + field + "'"
                );
            }
        }
    }

    /**
     * Creates a Pageable from the request
     */
    protected Pageable createPageable(GlobalFilterRequest request) {
        String sortField = request.getSortBy() != null ? request.getSortBy() : getDefaultSortField();
        Sort.Direction direction = "ASC".equalsIgnoreCase(request.getSortDirection())
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;

        return PageRequest.of(request.getPage(), request.getSize(), Sort.by(direction, sortField));
    }

    /**
     * Builds MongoDB Query from filter request
     */
    protected Query buildQuery(GlobalFilterRequest request) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        // Add soft delete filter unless explicitly including deleted
        if (!request.isIncludeDeleted()) {
            criteriaList.add(Criteria.where("deleted").is(false));
        }

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

    /**
     * Builds search criteria for global text search.
     * Override in subclasses to define searchable fields.
     */
    protected Criteria buildSearchCriteria(String searchTerm) {
        List<String> searchableFields = getSearchableFields();
        if (searchableFields.isEmpty()) {
            return null;
        }

        Pattern pattern = Pattern.compile(Pattern.quote(searchTerm), Pattern.CASE_INSENSITIVE);
        List<Criteria> orCriteria = searchableFields.stream()
            .map(field -> Criteria.where(field).regex(pattern))
            .collect(Collectors.toList());

        return new Criteria().orOperator(orCriteria.toArray(new Criteria[0]));
    }

    /**
     * Returns list of fields that support text search.
     * Override in subclasses.
     */
    protected List<String> getSearchableFields() {
        return getFieldDefinitions().entrySet().stream()
            .filter(e -> e.getValue().getType() == FilterFieldType.STRING)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * Builds a single filter criterion
     */
    protected Criteria buildFilterCriteria(FilterCriteria filter) {
        String field = filter.getField();
        FilterOperator operator = filter.getOperator();
        Object value = filter.getValue();

        FilterFieldDefinition definition = getFieldDefinitions().get(field);
        if (definition == null) {
            return null;
        }

        // Convert value based on field type
        Object convertedValue = convertValue(value, definition.getType());
        Object convertedValueTo = filter.getValueTo() != null
            ? convertValue(filter.getValueTo(), definition.getType())
            : null;

        return buildOperatorCriteria(field, operator, convertedValue, convertedValueTo, filter.isCaseSensitive());
    }

    /**
     * Converts a value to the appropriate type for the field
     */
    protected Object convertValue(Object value, FilterFieldType type) {
        if (value == null) {
            return null;
        }

        try {
            switch (type) {
                case NUMBER:
                    if (value instanceof Number) {
                        return ((Number) value).longValue();
                    }
                    return Long.parseLong(value.toString());

                case DECIMAL:
                    if (value instanceof Number) {
                        return ((Number) value).doubleValue();
                    }
                    return Double.parseDouble(value.toString());

                case BOOLEAN:
                    if (value instanceof Boolean) {
                        return value;
                    }
                    return Boolean.parseBoolean(value.toString());

                case DATE:
                case DATETIME:
                    return parseDateTime(value);

                case STRING:
                case ENUM:
                case OBJECT_ID:
                default:
                    return value.toString();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to convert value '" + value + "' to type " + type, e);
        }
    }

    /**
     * Parses date/datetime values
     */
    protected Object parseDateTime(Object value) {
        if (value instanceof Instant) {
            return value;
        }
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant();
        }
        if (value instanceof LocalDate) {
            return ((LocalDate) value).atStartOfDay(ZoneId.systemDefault()).toInstant();
        }

        String strValue = value.toString();

        // Try parsing as Instant first
        try {
            return Instant.parse(strValue);
        } catch (DateTimeParseException e) {
            // Continue to other formats
        }

        // Try ISO LocalDateTime format
        try {
            return LocalDateTime.parse(strValue).atZone(ZoneId.systemDefault()).toInstant();
        } catch (DateTimeParseException e) {
            // Continue to other formats
        }

        // Try ISO LocalDate format
        try {
            return LocalDate.parse(strValue).atStartOfDay(ZoneId.systemDefault()).toInstant();
        } catch (DateTimeParseException e) {
            // Continue to other formats
        }

        // Try common date formats
        String[] patterns = {"yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy", "yyyy-MM-dd'T'HH:mm:ss"};
        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                if (pattern.contains("HH")) {
                    return LocalDateTime.parse(strValue, formatter).atZone(ZoneId.systemDefault()).toInstant();
                } else {
                    return LocalDate.parse(strValue, formatter).atStartOfDay(ZoneId.systemDefault()).toInstant();
                }
            } catch (DateTimeParseException e) {
                // Continue to next format
            }
        }

        throw new IllegalArgumentException("Unable to parse date value: " + strValue);
    }

    /**
     * Builds criteria for a specific operator
     */
    protected Criteria buildOperatorCriteria(String field, FilterOperator operator,
                                              Object value, Object valueTo, boolean caseSensitive) {
        if (operator == null) {
            operator = FilterOperator.EQUALS;
        }

        switch (operator) {
            case EQUALS:
                if (value instanceof String && !caseSensitive) {
                    return Criteria.where(field).regex("^" + Pattern.quote((String) value) + "$", "i");
                }
                return Criteria.where(field).is(value);

            case NOT_EQUALS:
                return Criteria.where(field).ne(value);

            case CONTAINS:
                Pattern containsPattern = caseSensitive
                    ? Pattern.compile(Pattern.quote(value.toString()))
                    : Pattern.compile(Pattern.quote(value.toString()), Pattern.CASE_INSENSITIVE);
                return Criteria.where(field).regex(containsPattern);

            case STARTS_WITH:
                Pattern startsWithPattern = caseSensitive
                    ? Pattern.compile("^" + Pattern.quote(value.toString()))
                    : Pattern.compile("^" + Pattern.quote(value.toString()), Pattern.CASE_INSENSITIVE);
                return Criteria.where(field).regex(startsWithPattern);

            case ENDS_WITH:
                Pattern endsWithPattern = caseSensitive
                    ? Pattern.compile(Pattern.quote(value.toString()) + "$")
                    : Pattern.compile(Pattern.quote(value.toString()) + "$", Pattern.CASE_INSENSITIVE);
                return Criteria.where(field).regex(endsWithPattern);

            case GREATER_THAN:
                return Criteria.where(field).gt(value);

            case GREATER_THAN_OR_EQUALS:
                return Criteria.where(field).gte(value);

            case LESS_THAN:
                return Criteria.where(field).lt(value);

            case LESS_THAN_OR_EQUALS:
                return Criteria.where(field).lte(value);

            case BETWEEN:
                if (valueTo == null) {
                    throw new IllegalArgumentException("BETWEEN operator requires 'valueTo' parameter");
                }
                return Criteria.where(field).gte(value).lte(valueTo);

            case IN:
                if (value instanceof List) {
                    return Criteria.where(field).in((List<?>) value);
                }
                return Criteria.where(field).in(value);

            case NOT_IN:
                if (value instanceof List) {
                    return Criteria.where(field).nin((List<?>) value);
                }
                return Criteria.where(field).nin(value);

            case IS_NULL:
                return Criteria.where(field).isNull();

            case IS_NOT_NULL:
                return Criteria.where(field).ne(null);

            case IS_TRUE:
                return Criteria.where(field).is(true);

            case IS_FALSE:
                return Criteria.where(field).is(false);

            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }

    /**
     * Common string operators for text fields
     */
    protected static List<FilterOperator> getStringOperators() {
        return List.of(
            FilterOperator.EQUALS,
            FilterOperator.NOT_EQUALS,
            FilterOperator.CONTAINS,
            FilterOperator.STARTS_WITH,
            FilterOperator.ENDS_WITH,
            FilterOperator.IN,
            FilterOperator.NOT_IN,
            FilterOperator.IS_NULL,
            FilterOperator.IS_NOT_NULL
        );
    }

    /**
     * Common operators for numeric fields
     */
    protected static List<FilterOperator> getNumericOperators() {
        return List.of(
            FilterOperator.EQUALS,
            FilterOperator.NOT_EQUALS,
            FilterOperator.GREATER_THAN,
            FilterOperator.GREATER_THAN_OR_EQUALS,
            FilterOperator.LESS_THAN,
            FilterOperator.LESS_THAN_OR_EQUALS,
            FilterOperator.BETWEEN,
            FilterOperator.IN,
            FilterOperator.NOT_IN,
            FilterOperator.IS_NULL,
            FilterOperator.IS_NOT_NULL
        );
    }

    /**
     * Common operators for date/datetime fields
     */
    protected static List<FilterOperator> getDateOperators() {
        return List.of(
            FilterOperator.EQUALS,
            FilterOperator.NOT_EQUALS,
            FilterOperator.GREATER_THAN,
            FilterOperator.GREATER_THAN_OR_EQUALS,
            FilterOperator.LESS_THAN,
            FilterOperator.LESS_THAN_OR_EQUALS,
            FilterOperator.BETWEEN,
            FilterOperator.IS_NULL,
            FilterOperator.IS_NOT_NULL
        );
    }

    /**
     * Common operators for boolean fields
     */
    protected static List<FilterOperator> getBooleanOperators() {
        return List.of(
            FilterOperator.EQUALS,
            FilterOperator.IS_TRUE,
            FilterOperator.IS_FALSE,
            FilterOperator.IS_NULL,
            FilterOperator.IS_NOT_NULL
        );
    }

    /**
     * Common operators for enum fields
     */
    protected static List<FilterOperator> getEnumOperators() {
        return List.of(
            FilterOperator.EQUALS,
            FilterOperator.NOT_EQUALS,
            FilterOperator.IN,
            FilterOperator.NOT_IN,
            FilterOperator.IS_NULL,
            FilterOperator.IS_NOT_NULL
        );
    }

    /**
     * Common operators for object ID fields
     */
    protected static List<FilterOperator> getObjectIdOperators() {
        return List.of(
            FilterOperator.EQUALS,
            FilterOperator.NOT_EQUALS,
            FilterOperator.IN,
            FilterOperator.NOT_IN,
            FilterOperator.IS_NULL,
            FilterOperator.IS_NOT_NULL
        );
    }
}
