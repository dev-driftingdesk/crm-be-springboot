package com.ceedpods.crmbuild.service.filter;

import com.ceedpods.crmbuild.dto.filter.FilterCriteria;
import com.ceedpods.crmbuild.dto.filter.FilterFieldDefinition;
import com.ceedpods.crmbuild.dto.filter.GlobalFilterRequest;
import com.ceedpods.crmbuild.dto.filter.GlobalFilterResponse;
import com.ceedpods.crmbuild.enums.FilterableEntity;

import java.util.List;

/**
 * Strategy interface for entity-specific filtering logic.
 * Each filterable entity must implement this interface.
 */
public interface EntityFilterStrategy<T> {

    /**
     * Returns the entity type this strategy handles
     */
    FilterableEntity getEntityType();

    /**
     * Returns the list of filterable fields for this entity
     */
    List<FilterFieldDefinition> getFilterableFields();

    /**
     * Applies filters and returns paginated results
     */
    GlobalFilterResponse<T> filter(GlobalFilterRequest request);

    /**
     * Validates that the given filter criteria are valid for this entity
     */
    void validateFilters(List<FilterCriteria> filters);

    /**
     * Returns the default sort field for this entity
     */
    String getDefaultSortField();

    /**
     * Checks if a field is filterable
     */
    boolean isFieldFilterable(String fieldName);
}
