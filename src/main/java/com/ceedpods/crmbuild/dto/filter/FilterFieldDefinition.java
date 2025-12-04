package com.ceedpods.crmbuild.dto.filter;

import com.ceedpods.crmbuild.enums.FilterFieldType;
import com.ceedpods.crmbuild.enums.FilterOperator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Defines metadata about a filterable field.
 * Used to communicate available filters to the frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterFieldDefinition {

    /**
     * The field name (must match entity field)
     */
    private String field;

    /**
     * Display label for the UI
     */
    private String label;

    /**
     * The data type of the field
     */
    private FilterFieldType type;

    /**
     * List of supported operators for this field
     */
    private List<FilterOperator> supportedOperators;

    /**
     * For ENUM fields, the list of possible values
     */
    private List<String> enumValues;

    /**
     * For OBJECT_ID fields, the related entity name
     */
    private String relatedEntity;

    /**
     * Whether this field is required for filtering
     */
    @Builder.Default
    private boolean required = false;

    /**
     * Whether this is a nested field (e.g., "address.city")
     */
    @Builder.Default
    private boolean nested = false;

    /**
     * Additional metadata for custom field handling
     */
    private Map<String, Object> metadata;
}
