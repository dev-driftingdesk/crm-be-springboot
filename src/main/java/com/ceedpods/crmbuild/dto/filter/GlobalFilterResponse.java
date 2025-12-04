package com.ceedpods.crmbuild.dto.filter;

import com.ceedpods.crmbuild.enums.FilterableEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for global filtering operations.
 * Contains filtered data with pagination metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalFilterResponse<T> {

    /**
     * The entity type that was filtered
     */
    private FilterableEntity entity;

    /**
     * The filtered data
     */
    private List<T> data;

    /**
     * Total number of records matching the filter
     */
    private long totalElements;

    /**
     * Total number of pages
     */
    private int totalPages;

    /**
     * Current page number
     */
    private int currentPage;

    /**
     * Page size
     */
    private int pageSize;

    /**
     * Whether this is the first page
     */
    private boolean first;

    /**
     * Whether this is the last page
     */
    private boolean last;

    /**
     * Whether there are more pages
     */
    private boolean hasNext;

    /**
     * Whether there are previous pages
     */
    private boolean hasPrevious;

    /**
     * Number of elements in current page
     */
    private int numberOfElements;

    /**
     * Factory method to create response from Spring Data Page
     */
    public static <T> GlobalFilterResponse<T> fromPage(
            org.springframework.data.domain.Page<T> page,
            FilterableEntity entity) {
        return GlobalFilterResponse.<T>builder()
                .entity(entity)
                .data(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .numberOfElements(page.getNumberOfElements())
                .build();
    }
}
