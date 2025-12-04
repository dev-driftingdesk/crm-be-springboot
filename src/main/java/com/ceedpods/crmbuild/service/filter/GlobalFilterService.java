package com.ceedpods.crmbuild.service.filter;

import com.ceedpods.crmbuild.dto.filter.FilterFieldDefinition;
import com.ceedpods.crmbuild.dto.filter.GlobalFilterRequest;
import com.ceedpods.crmbuild.dto.filter.GlobalFilterResponse;
import com.ceedpods.crmbuild.enums.FilterableEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global filter service that delegates filtering to appropriate strategies.
 * Acts as a facade for the filter strategy pattern implementation.
 */
@Service
@Slf4j
public class GlobalFilterService {

    private final Map<FilterableEntity, EntityFilterStrategy<?>> strategyMap;

    /**
     * Constructor injection collects all EntityFilterStrategy beans
     * and maps them by their entity type.
     */
    public GlobalFilterService(List<EntityFilterStrategy<?>> strategies) {
        this.strategyMap = new EnumMap<>(FilterableEntity.class);

        for (EntityFilterStrategy<?> strategy : strategies) {
            strategyMap.put(strategy.getEntityType(), strategy);
            log.info("Registered filter strategy for entity: {}", strategy.getEntityType().getDisplayName());
        }

        log.info("Global filter service initialized with {} strategies", strategyMap.size());
    }

    /**
     * Applies global filter based on the request.
     *
     * @param request The filter request containing entity type and filter criteria
     * @return Paginated and filtered results
     */
    @SuppressWarnings("unchecked")
    public <T> GlobalFilterResponse<T> filter(GlobalFilterRequest request) {
        validateRequest(request);

        FilterableEntity entityType = request.getEntity();
        EntityFilterStrategy<?> strategy = getStrategy(entityType);

        log.debug("Filtering {} with {} filters", entityType.getDisplayName(),
                request.getFilters() != null ? request.getFilters().size() : 0);

        return (GlobalFilterResponse<T>) strategy.filter(request);
    }

    /**
     * Gets the list of filterable fields for a given entity.
     *
     * @param entity The entity type
     * @return List of field definitions
     */
    public List<FilterFieldDefinition> getFilterableFields(FilterableEntity entity) {
        EntityFilterStrategy<?> strategy = getStrategy(entity);
        return strategy.getFilterableFields();
    }

    /**
     * Gets all supported filterable entities.
     *
     * @return Map of entity types to their display names
     */
    public Map<String, String> getSupportedEntities() {
        return strategyMap.keySet().stream()
                .collect(Collectors.toMap(
                        Enum::name,
                        FilterableEntity::getDisplayName
                ));
    }

    /**
     * Gets metadata about all filterable entities and their fields.
     *
     * @return Map of entity names to their field definitions
     */
    public Map<String, List<FilterFieldDefinition>> getAllFilterMetadata() {
        return strategyMap.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name(),
                        e -> e.getValue().getFilterableFields()
                ));
    }

    /**
     * Validates if a specific entity supports filtering.
     *
     * @param entity The entity to check
     * @return true if the entity supports filtering
     */
    public boolean isEntityFilterable(FilterableEntity entity) {
        return strategyMap.containsKey(entity);
    }

    /**
     * Gets the strategy for a given entity type.
     *
     * @param entity The entity type
     * @return The corresponding filter strategy
     * @throws IllegalArgumentException if no strategy is found
     */
    private EntityFilterStrategy<?> getStrategy(FilterableEntity entity) {
        EntityFilterStrategy<?> strategy = strategyMap.get(entity);
        if (strategy == null) {
            throw new IllegalArgumentException(
                    "No filter strategy found for entity: " + entity.getDisplayName()
            );
        }
        return strategy;
    }

    /**
     * Validates the filter request.
     *
     * @param request The request to validate
     */
    private void validateRequest(GlobalFilterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Filter request cannot be null");
        }
        if (request.getEntity() == null) {
            throw new IllegalArgumentException("Entity type is required");
        }
        if (request.getPage() < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (request.getSize() <= 0 || request.getSize() > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
    }
}
