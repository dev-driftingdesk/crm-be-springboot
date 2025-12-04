package com.ceedpods.crmbuild.dto.filter;

import com.ceedpods.crmbuild.enums.FilterableEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request DTO for global filtering operations.
 * Contains all parameters needed to perform a filter query.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalFilterRequest {

    /**
     * The entity type to filter
     */
    @NotNull(message = "Entity type is required")
    private FilterableEntity entity;

    /**
     * List of filter criteria to apply (AND logic by default)
     */
    @Valid
    private List<FilterCriteria> filters;

    /**
     * Page number (0-indexed)
     */
    @Builder.Default
    private int page = 0;

    /**
     * Page size
     */
    @Builder.Default
    private int size = 20;

    /**
     * Sort field name
     */
    private String sortBy;

    /**
     * Sort direction (ASC or DESC)
     */
    @Builder.Default
    private String sortDirection = "DESC";

    /**
     * Whether to include soft-deleted records
     */
    @Builder.Default
    private boolean includeDeleted = false;

    /**
     * Search term for global text search across searchable fields
     */
    private String searchTerm;

    /**
     * Logical operator for combining filters (AND/OR)
     */
    @Builder.Default
    private LogicalOperator logicalOperator = LogicalOperator.AND;

    public enum LogicalOperator {
        AND, OR
    }
}
