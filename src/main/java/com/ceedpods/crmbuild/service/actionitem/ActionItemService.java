package com.ceedpods.crmbuild.service.actionitem;

import com.ceedpods.crmbuild.dto.actionitem.CreateActionItemResponse;
import com.ceedpods.crmbuild.dto.request.CreateActionItemRequest;
import com.ceedpods.crmbuild.entity.actionitem.ActionItem;
import com.ceedpods.crmbuild.exception.ResourceNotFoundException;
import com.ceedpods.crmbuild.repository.ActionItemRepository;
import com.ceedpods.crmbuild.repository.DealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActionItemService {

    private final ActionItemRepository actionItemRepository;
    private final DealRepository dealRepository;

    @Transactional
    public CreateActionItemResponse addActionItem(String dealId, CreateActionItemRequest request) {
        log.info("Adding action item to deal: {}", dealId);

        // Validate deal exists
        dealRepository.findById(dealId)
            .filter(d -> !d.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException("Deal not found with ID: " + dealId));

        // Create action item
        ActionItem actionItem = ActionItem.builder()
            .id(UUID.randomUUID().toString())
            .dealId(dealId)
            .title(request.getTitle())
            .dueDate(request.getDueDate())
            .assignedTo(request.getAssignedTo())
            .priority(request.getPriority())
            .status("pending")
            .build();

        ActionItem saved = actionItemRepository.save(actionItem);
        log.info("Successfully added action item {} to deal {}", saved.getId(), dealId);

        return CreateActionItemResponse.builder()
            .actionItemId(saved.getId())
            .title(saved.getTitle())
            .dueDate(saved.getDueDate())
            .assignedTo(saved.getAssignedTo())
            .priority(saved.getPriority().getValue())
            .status(saved.getStatus())
            .createdAt(saved.getCreatedAt())
            .build();
    }

    public List<ActionItem> getActionItemsByDealId(String dealId) {
        return actionItemRepository.findByDealIdAndDeletedFalse(dealId);
    }
}
