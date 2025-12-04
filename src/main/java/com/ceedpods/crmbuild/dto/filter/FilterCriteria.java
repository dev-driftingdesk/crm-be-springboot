package com.ceedpods.crmbuild.dto.filter;

import com.ceedpods.crmbuild.enums.FilterOperator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a single filter criterion.
 * Example: { "field": "leadName", "operator": "CONTAINS", "value": "John" }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterCriteria {

    /**
     * The field name to filter on (must match entity field name)
     */
    private String field;

    /**
     * The filter operator to apply
     */
    private FilterOperator operator;

    /**
     * The value to filter by (can be single value, list, or range)
     */
    private Object value;

    /**
     * Secondary value for range operators (e.g., BETWEEN)
     */
    private Object valueTo;

    /**
     * Case sensitivity flag for string operations (default: false = case-insensitive)
     */
    @Builder.Default
    private boolean caseSensitive = false;

    /**
     * Helper method to get value as String
     */
    public String getValueAsString() {
        return value != null ? value.toString() : null;
    }

    /**
     * Helper method to get value as List
     */
    @SuppressWarnings("unchecked")
    public List<Object> getValueAsList() {
        if (value instanceof List) {
            return (List<Object>) value;
        }
        return List.of(value);
    }
}
