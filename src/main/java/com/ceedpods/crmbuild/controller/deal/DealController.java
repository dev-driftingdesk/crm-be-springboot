package com.ceedpods.crmbuild.controller.deal;

import com.ceedpods.crmbuild.dto.deal.DealDTO;
import com.ceedpods.crmbuild.dto.request.CreateDealRequest;
import com.ceedpods.crmbuild.dto.request.UpdateDealRequest;
import com.ceedpods.crmbuild.dto.response.ApiResponse;
import com.ceedpods.crmbuild.security.CustomPermissionEvaluator;
import com.ceedpods.crmbuild.security.RequirePermission;
import com.ceedpods.crmbuild.service.deal.DealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
@Slf4j
public class DealController {

    private final DealService dealService;
    private final CustomPermissionEvaluator permissionEvaluator;

    /**
     * Create a new deal (Admin only)
     */
    @PostMapping
    @RequirePermission("DEAL_CREATE")
    public ResponseEntity<ApiResponse<DealDTO>> createDeal(
            @Valid @RequestBody CreateDealRequest request,
            Authentication authentication) {
        try {
            log.info("Creating new deal");
            DealDTO deal = dealService.createDeal(request, authentication);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Deal created successfully", deal));
        } catch (Exception e) {
            log.error("Error creating deal: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to create deal: " + e.getMessage()));
        }
    }

    /**
     * Get all deals
     */
    @GetMapping
    @RequirePermission("DEAL_VIEW_ALL")
    public ResponseEntity<ApiResponse<List<DealDTO>>> getAllDeals() {
        try {
            log.info("Fetching all deals");
            List<DealDTO> deals = dealService.getAllDeals();
            return ResponseEntity.ok(ApiResponse.success(deals));
        } catch (Exception e) {
            log.error("Error fetching deals: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to fetch deals: " + e.getMessage()));
        }
    }

    /**
     * Get deal by UUID
     */
    @GetMapping("/{id}")
    @RequirePermission("DEAL_VIEW_ALL")
    public ResponseEntity<ApiResponse<DealDTO>> getDealById(@PathVariable String id) {
        try {
            log.info("Fetching deal with UUID: {}", id);
            DealDTO deal = dealService.getDealById(id);
            return ResponseEntity.ok(ApiResponse.success(deal));
        } catch (Exception e) {
            log.error("Error fetching deal: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to fetch deal: " + e.getMessage()));
        }
    }

    /**
     * Update deal (Admin only)
     */
    @PutMapping("/{id}")
    @RequirePermission("DEAL_EDIT")
    public ResponseEntity<ApiResponse<DealDTO>> updateDeal(
            @PathVariable String id,
            @Valid @RequestBody UpdateDealRequest request,
            Authentication authentication) {
        try {
            log.info("Updating deal with ID: {}", id);
            DealDTO deal = dealService.updateDeal(id, request, authentication);
            return ResponseEntity.ok(ApiResponse.success("Deal updated successfully", deal));
        } catch (Exception e) {
            log.error("Error updating deal: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to update deal: " + e.getMessage()));
        }
    }

    /**
     * Delete deal (hard delete) (Admin only)
     */
    @DeleteMapping("/{id}")
    @RequirePermission("DEAL_DELETE")
    public ResponseEntity<ApiResponse<Void>> deleteDeal(
            @PathVariable String id,
            Authentication authentication) {
        try {
            log.info("Deleting deal with ID: {}", id);
            dealService.deleteDeal(id, authentication);
            return ResponseEntity.ok(ApiResponse.success("Deal deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting deal: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to delete deal: " + e.getMessage()));
        }
    }

    /**
     * Search deals
     */
    @GetMapping("/search")
    @RequirePermission("DEAL_VIEW_ALL")
    public ResponseEntity<ApiResponse<List<DealDTO>>> searchDeals(
            @RequestParam String query) {
        try {
            log.info("Searching deals with query: {}", query);
            List<DealDTO> deals = dealService.searchDeals(query);
            return ResponseEntity.ok(ApiResponse.success(deals));
        } catch (Exception e) {
            log.error("Error searching deals: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to search deals: " + e.getMessage()));
        }
    }

    /**
     * Get total deal count
     */
    @GetMapping("/count")
    @RequirePermission("DEAL_VIEW_ALL")
    public ResponseEntity<ApiResponse<Long>> getDealCount() {
        try {
            log.info("Fetching deal count");
            long count = dealService.getTotalDealCount();
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            log.error("Error fetching deal count: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to fetch deal count: " + e.getMessage()));
        }
    }

    /**
     * Get deals by Lead ID
     */
    @GetMapping("/lead/{leadId}")
    @RequirePermission("DEAL_VIEW_ALL")
    public ResponseEntity<ApiResponse<List<DealDTO>>> getDealsByLeadId(@PathVariable String leadId) {
        try {
            log.info("Fetching deals with Lead ID: {}", leadId);
            List<DealDTO> deals = dealService.getDealsByLeadId(leadId);
            return ResponseEntity.ok(ApiResponse.success(deals));
        } catch (Exception e) {
            log.error("Error fetching deals by lead ID: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to fetch deals by lead ID: " + e.getMessage()));
        }
    }

    /**
     * Get deals by Product ID
     */
    @GetMapping("/product/{productId}")
    @RequirePermission("DEAL_VIEW_ALL")
    public ResponseEntity<ApiResponse<List<DealDTO>>> getDealsByProductId(@PathVariable String productId) {
        try {
            log.info("Fetching deals with Product ID: {}", productId);
            List<DealDTO> deals = dealService.getDealsByProductId(productId);
            return ResponseEntity.ok(ApiResponse.success(deals));
        } catch (Exception e) {
            log.error("Error fetching deals by product ID: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to fetch deals by product ID: " + e.getMessage()));
        }
    }

    /**
     * Get deals by Sales Rep ID
     */
    @GetMapping("/salesrep/{salesRepId}")
    @RequirePermission("DEAL_VIEW_ALL")
    public ResponseEntity<ApiResponse<List<DealDTO>>> getDealsBySalesRepId(@PathVariable String salesRepId) {
        try {
            log.info("Fetching deals with Sales Rep ID: {}", salesRepId);
            List<DealDTO> deals = dealService.getDealsBySalesRepId(salesRepId);
            return ResponseEntity.ok(ApiResponse.success(deals));
        } catch (Exception e) {
            log.error("Error fetching deals by sales rep ID: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to fetch deals by sales rep ID: " + e.getMessage()));
        }
    }
}
