package com.ceedpods.crmbuild.service.activity;

import com.ceedpods.crmbuild.dto.activity.CreateActivityResponse;
import com.ceedpods.crmbuild.dto.request.CreateActivityRequest;
import com.ceedpods.crmbuild.entity.activity.Activity;
import com.ceedpods.crmbuild.exception.ResourceNotFoundException;
import com.ceedpods.crmbuild.repository.ActivityRepository;
import com.ceedpods.crmbuild.repository.DealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service class for Activity operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final DealRepository dealRepository;

    /**
     * Add a new activity to a deal
     */
    @Transactional
    public CreateActivityResponse addActivity(String dealId, CreateActivityRequest request, Authentication authentication) {
        log.info("Adding activity to deal: {}", dealId);

        // Validate deal exists
        dealRepository.findById(dealId)
            .filter(d -> !d.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException("Deal not found with ID: " + dealId));

        // Get the current user's keycloak ID from authentication
        String performedBy = authentication != null ? authentication.getName() : null;

        // Generate UUID for the activity
        String activityId = UUID.randomUUID().toString();

        // Create activity entity
        Activity activity = Activity.builder()
            .id(activityId)
            .dealId(dealId)
            .type(request.getType())
            .description(request.getDescription())
            .performedAt(request.getPerformedAt())
            .performedBy(performedBy)
            .build();

        // Save activity
        Activity savedActivity = activityRepository.save(activity);
        log.info("Successfully added activity {} to deal {}", savedActivity.getId(), dealId);

        // Return response
        return CreateActivityResponse.builder()
            .activityId(savedActivity.getId())
            .type(savedActivity.getType().getValue())
            .description(savedActivity.getDescription())
            .performedAt(savedActivity.getPerformedAt())
            .createdAt(savedActivity.getCreatedAt())
            .build();
    }

    /**
     * Get all activities for a deal
     */
    public List<Activity> getActivitiesByDealId(String dealId) {
        log.info("Fetching activities for deal: {}", dealId);
        return activityRepository.findByDealIdAndDeletedFalse(dealId);
    }
}
