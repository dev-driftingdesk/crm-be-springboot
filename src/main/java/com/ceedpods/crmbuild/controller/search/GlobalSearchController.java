package com.ceedpods.crmbuild.controller.search;

import com.ceedpods.crmbuild.dto.response.ApiResponse;
import com.ceedpods.crmbuild.dto.response.GlobalSearchResponse;
import com.ceedpods.crmbuild.service.search.GlobalSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Slf4j
public class GlobalSearchController {

    private final GlobalSearchService globalSearchService;

    /**
     * Global search endpoint that searches across Products, Leads, and Deals
     * Accessible to any authenticated user (no specific permission required)
     * Supports pagination for production-scale performance
     *
     * @param query The search keyword
     * @param page The page number (0-indexed, default: 0)
     * @param size The number of results per entity per page (default: 20, max: 100)
     * @return GlobalSearchResponse containing paginated results from all entities
     */
    @GetMapping("/global")
    public ResponseEntity<ApiResponse<GlobalSearchResponse>> globalSearch(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        try {
            log.info("Global search requested with query: {}, page: {}, size: {}", query, page, size);

            // Validate query parameter
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Search query cannot be empty"));
            }

            // Validate pagination parameters
            if (page != null && page < 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Page number must be 0 or greater"));
            }

            if (size != null && size <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Page size must be greater than 0"));
            }

            // Perform global search with pagination
            GlobalSearchResponse searchResults = globalSearchService.globalSearch(query.trim(), page, size);

            return ResponseEntity.ok(
                ApiResponse.success("Search completed successfully", searchResults));
        } catch (Exception e) {
            log.error("Error performing global search: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to perform search: " + e.getMessage()));
        }
    }
}
