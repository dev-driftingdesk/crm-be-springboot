package com.ceedpods.crmbuild.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic paginated response wrapper
 *
 * @param <T> Type of data in the response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {

    @JsonProperty("data")
    private List<T> data;           // List of items for current page

    @JsonProperty("totalRecords")
    private long totalRecords;      // Total number of records across all pages

    @JsonProperty("totalPages")
    private int totalPages;         // Total number of pages

    @JsonProperty("currentPage")
    private int currentPage;        // Current page number (0-indexed)

    @JsonProperty("pageSize")
    private int pageSize;           // Number of items per page

    @JsonProperty("hasNext")
    private boolean hasNext;        // Whether there is a next page

    @JsonProperty("hasPrevious")
    private boolean hasPrevious;    // Whether there is a previous page

    /**
     * Create a paginated response from Spring Data Page object
     */
    public static <T> PaginatedResponse<T> of(List<T> content, long totalElements, int totalPages,
                                               int currentPage, int pageSize, boolean hasNext, boolean hasPrevious) {
        return PaginatedResponse.<T>builder()
                .data(content)
                .totalRecords(totalElements)
                .totalPages(totalPages)
                .currentPage(currentPage)
                .pageSize(pageSize)
                .hasNext(hasNext)
                .hasPrevious(hasPrevious)
                .build();
    }
}
