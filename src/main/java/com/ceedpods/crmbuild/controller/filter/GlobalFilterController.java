package com.ceedpods.crmbuild.controller.filter;

import com.ceedpods.crmbuild.dto.filter.FilterFieldDefinition;
import com.ceedpods.crmbuild.dto.filter.GlobalFilterRequest;
import com.ceedpods.crmbuild.dto.filter.GlobalFilterResponse;
import com.ceedpods.crmbuild.dto.response.ApiResponse;
import com.ceedpods.crmbuild.enums.FilterableEntity;
import com.ceedpods.crmbuild.service.filter.GlobalFilterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for global filtering operations.
 * Provides endpoints to filter any supported entity with various criteria.
 */
@RestController
@RequestMapping("/filter")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Global Filter", description = "API for filtering any entity type with flexible criteria")
public class GlobalFilterController {

    private final GlobalFilterService globalFilterService;

    /**
     * Apply filter to any supported entity.
     *
     * @param request The filter request containing entity type and criteria
     * @return Paginated and filtered results
     */
    @PostMapping
    @Operation(
            summary = "Filter entities",
            description = "Apply filters to any supported entity type. Supports pagination, sorting, and various filter operators."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully filtered entities",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid filter request",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<ApiResponse<GlobalFilterResponse<?>>> filter(
            @Valid @RequestBody GlobalFilterRequest request) {
        try {
            log.info("Filter request received for entity: {}", request.getEntity());
            GlobalFilterResponse<?> response = globalFilterService.filter(request);
            return ResponseEntity.ok(ApiResponse.success("Filtered successfully", response));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid filter request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), "INVALID_FILTER"));
        } catch (Exception e) {
            log.error("Error processing filter request", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("An error occurred while filtering", "FILTER_ERROR"));
        }
    }

    /**
     * Get all supported filterable entities.
     *
     * @return Map of entity names to display names
     */
    @GetMapping("/entities")
    @Operation(
            summary = "Get supported entities",
            description = "Returns a list of all entity types that support filtering"
    )
    public ResponseEntity<ApiResponse<Map<String, String>>> getSupportedEntities() {
        try {
            Map<String, String> entities = globalFilterService.getSupportedEntities();
            return ResponseEntity.ok(ApiResponse.success("Supported entities retrieved", entities));
        } catch (Exception e) {
            log.error("Error retrieving supported entities", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve supported entities", "ENTITY_ERROR"));
        }
    }

    /**
     * Get filterable fields for a specific entity.
     *
     * @param entity The entity type
     * @return List of field definitions with supported operators
     */
    @GetMapping("/fields/{entity}")
    @Operation(
            summary = "Get filterable fields",
            description = "Returns all filterable fields for a specific entity with their supported operators"
    )
    public ResponseEntity<ApiResponse<List<FilterFieldDefinition>>> getFilterableFields(
            @Parameter(description = "The entity type to get fields for")
            @PathVariable FilterableEntity entity) {
        try {
            List<FilterFieldDefinition> fields = globalFilterService.getFilterableFields(entity);
            return ResponseEntity.ok(ApiResponse.success("Fields retrieved for " + entity.getDisplayName(), fields));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid entity type requested: {}", entity);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), "INVALID_ENTITY"));
        } catch (Exception e) {
            log.error("Error retrieving filterable fields", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve filterable fields", "FIELDS_ERROR"));
        }
    }

    /**
     * Get filter metadata for all entities.
     *
     * @return Map of entity names to their field definitions
     */
    @GetMapping("/metadata")
    @Operation(
            summary = "Get all filter metadata",
            description = "Returns filter metadata for all supported entities including field definitions and operators"
    )
    public ResponseEntity<ApiResponse<Map<String, List<FilterFieldDefinition>>>> getAllFilterMetadata() {
        try {
            Map<String, List<FilterFieldDefinition>> metadata = globalFilterService.getAllFilterMetadata();
            return ResponseEntity.ok(ApiResponse.success("Filter metadata retrieved", metadata));
        } catch (Exception e) {
            log.error("Error retrieving filter metadata", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve filter metadata", "METADATA_ERROR"));
        }
    }
}
