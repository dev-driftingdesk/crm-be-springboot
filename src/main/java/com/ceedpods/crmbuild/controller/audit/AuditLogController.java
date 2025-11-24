package com.ceedpods.crmbuild.controller.audit;

import com.ceedpods.crmbuild.dto.audit.AuditLogDTO;
import com.ceedpods.crmbuild.dto.response.ApiResponse;
import com.ceedpods.crmbuild.dto.response.PaginatedResponse;
import com.ceedpods.crmbuild.entity.audit.AuditLog;
import com.ceedpods.crmbuild.mapper.AuditLogMapper;
import com.ceedpods.crmbuild.security.RequirePermission;
import com.ceedpods.crmbuild.service.auditLogService.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Audit Log Management
 */
@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit Log Management", description = "Operations for viewing and querying system audit logs")
@SecurityRequirement(name = "Bearer Authentication")
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final AuditLogMapper auditLogMapper;

    /**
     * Get all audit logs (paginated)
     */
    @Operation(
            summary = "Get All Audit Logs",
            description = "Retrieves all audit logs with pagination. Requires AUDIT_VIEW_ALL permission."
    )
    @GetMapping
    @RequirePermission("AUDIT_VIEW_ALL")
    public ResponseEntity<ApiResponse<PaginatedResponse<AuditLogDTO>>> getAllAuditLogs(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (page < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Page number must be 0 or greater"));
            }

            if (size <= 0 || size > 100) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Page size must be between 1 and 100"));
            }

            log.info("Fetching all audit logs - page: {}, size: {}", page, size);
            Page<AuditLog> auditLogPage = auditLogService.getAllAuditLogs(page, size);

            List<AuditLogDTO> auditLogDTOs = auditLogPage.getContent().stream()
                    .map(auditLogMapper::toDTO)
                    .collect(Collectors.toList());

            PaginatedResponse<AuditLogDTO> paginatedResponse = PaginatedResponse.of(
                    auditLogDTOs,
                    auditLogPage.getTotalElements(),
                    auditLogPage.getTotalPages(),
                    auditLogPage.getNumber(),
                    auditLogPage.getSize(),
                    auditLogPage.hasNext(),
                    auditLogPage.hasPrevious()
            );

            return ResponseEntity.ok(ApiResponse.success(paginatedResponse));
        } catch (Exception e) {
            log.error("Error fetching audit logs: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to fetch audit logs: " + e.getMessage()));
        }
    }

    /**
     * Get audit logs by username
     */
    @Operation(
            summary = "Get Audit Logs by Username",
            description = "Retrieves all audit logs for a specific user. Requires AUDIT_VIEW_ALL permission."
    )
    @GetMapping("/user/{username}")
    @RequirePermission("AUDIT_VIEW_ALL")
    public ResponseEntity<ApiResponse<PaginatedResponse<AuditLogDTO>>> getAuditLogsByUsername(
            @Parameter(description = "Username", required = true)
            @PathVariable String username,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (page < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Page number must be 0 or greater"));
            }

            if (size <= 0 || size > 100) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Page size must be between 1 and 100"));
            }

            log.info("Fetching audit logs for username: {} - page: {}, size: {}", username, page, size);
            Page<AuditLog> auditLogPage = auditLogService.getAuditLogsByUsername(username, page, size);

            List<AuditLogDTO> auditLogDTOs = auditLogPage.getContent().stream()
                    .map(auditLogMapper::toDTO)
                    .collect(Collectors.toList());

            PaginatedResponse<AuditLogDTO> paginatedResponse = PaginatedResponse.of(
                    auditLogDTOs,
                    auditLogPage.getTotalElements(),
                    auditLogPage.getTotalPages(),
                    auditLogPage.getNumber(),
                    auditLogPage.getSize(),
                    auditLogPage.hasNext(),
                    auditLogPage.hasPrevious()
            );

            return ResponseEntity.ok(ApiResponse.success(paginatedResponse));
        } catch (Exception e) {
            log.error("Error fetching audit logs for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to fetch audit logs: " + e.getMessage()));
        }
    }

    /**
     * Get audit logs by entity type
     */
    @Operation(
            summary = "Get Audit Logs by Entity Type",
            description = "Retrieves all audit logs for a specific entity type (PRODUCT, LEAD, DEAL, etc.). Requires AUDIT_VIEW_ALL permission."
    )
    @GetMapping("/entity-type/{entityType}")
    @RequirePermission("AUDIT_VIEW_ALL")
    public ResponseEntity<ApiResponse<PaginatedResponse<AuditLogDTO>>> getAuditLogsByEntityType(
            @Parameter(description = "Entity Type (PRODUCT, LEAD, DEAL, DEAL_NOTE, LEAD_NOTE)", required = true)
            @PathVariable String entityType,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (page < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Page number must be 0 or greater"));
            }

            if (size <= 0 || size > 100) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Page size must be between 1 and 100"));
            }

            log.info("Fetching audit logs for entity type: {} - page: {}, size: {}", entityType, page, size);
            Page<AuditLog> auditLogPage = auditLogService.getAuditLogsByEntityType(entityType.toUpperCase(), page, size);

            List<AuditLogDTO> auditLogDTOs = auditLogPage.getContent().stream()
                    .map(auditLogMapper::toDTO)
                    .collect(Collectors.toList());

            PaginatedResponse<AuditLogDTO> paginatedResponse = PaginatedResponse.of(
                    auditLogDTOs,
                    auditLogPage.getTotalElements(),
                    auditLogPage.getTotalPages(),
                    auditLogPage.getNumber(),
                    auditLogPage.getSize(),
                    auditLogPage.hasNext(),
                    auditLogPage.hasPrevious()
            );

            return ResponseEntity.ok(ApiResponse.success(paginatedResponse));
        } catch (Exception e) {
            log.error("Error fetching audit logs for entity type {}: {}", entityType, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to fetch audit logs: " + e.getMessage()));
        }
    }

    /**
     * Get audit logs by entity ID
     */
    @Operation(
            summary = "Get Audit Logs by Entity ID",
            description = "Retrieves all audit logs for a specific entity ID. Requires AUDIT_VIEW_ALL permission."
    )
    @GetMapping("/entity/{entityId}")
    @RequirePermission("AUDIT_VIEW_ALL")
    public ResponseEntity<ApiResponse<PaginatedResponse<AuditLogDTO>>> getAuditLogsByEntityId(
            @Parameter(description = "Entity ID", required = true)
            @PathVariable String entityId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (page < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Page number must be 0 or greater"));
            }

            if (size <= 0 || size > 100) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Page size must be between 1 and 100"));
            }

            log.info("Fetching audit logs for entity ID: {} - page: {}, size: {}", entityId, page, size);
            Page<AuditLog> auditLogPage = auditLogService.getAuditLogsByEntityId(entityId, page, size);

            List<AuditLogDTO> auditLogDTOs = auditLogPage.getContent().stream()
                    .map(auditLogMapper::toDTO)
                    .collect(Collectors.toList());

            PaginatedResponse<AuditLogDTO> paginatedResponse = PaginatedResponse.of(
                    auditLogDTOs,
                    auditLogPage.getTotalElements(),
                    auditLogPage.getTotalPages(),
                    auditLogPage.getNumber(),
                    auditLogPage.getSize(),
                    auditLogPage.hasNext(),
                    auditLogPage.hasPrevious()
            );

            return ResponseEntity.ok(ApiResponse.success(paginatedResponse));
        } catch (Exception e) {
            log.error("Error fetching audit logs for entity ID {}: {}", entityId, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to fetch audit logs: " + e.getMessage()));
        }
    }

    /**
     * Get audit logs by action
     */
    @Operation(
            summary = "Get Audit Logs by Action",
            description = "Retrieves all audit logs for a specific action (LOGIN, CREATED, UPDATED, DELETED). Requires AUDIT_VIEW_ALL permission."
    )
    @GetMapping("/action/{action}")
    @RequirePermission("AUDIT_VIEW_ALL")
    public ResponseEntity<ApiResponse<PaginatedResponse<AuditLogDTO>>> getAuditLogsByAction(
            @Parameter(description = "Action (LOGIN, CREATED, UPDATED, DELETED)", required = true)
            @PathVariable String action,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (page < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Page number must be 0 or greater"));
            }

            if (size <= 0 || size > 100) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Page size must be between 1 and 100"));
            }

            log.info("Fetching audit logs for action: {} - page: {}, size: {}", action, page, size);
            Page<AuditLog> auditLogPage = auditLogService.getAuditLogsByAction(action.toUpperCase(), page, size);

            List<AuditLogDTO> auditLogDTOs = auditLogPage.getContent().stream()
                    .map(auditLogMapper::toDTO)
                    .collect(Collectors.toList());

            PaginatedResponse<AuditLogDTO> paginatedResponse = PaginatedResponse.of(
                    auditLogDTOs,
                    auditLogPage.getTotalElements(),
                    auditLogPage.getTotalPages(),
                    auditLogPage.getNumber(),
                    auditLogPage.getSize(),
                    auditLogPage.hasNext(),
                    auditLogPage.hasPrevious()
            );

            return ResponseEntity.ok(ApiResponse.success(paginatedResponse));
        } catch (Exception e) {
            log.error("Error fetching audit logs for action {}: {}", action, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to fetch audit logs: " + e.getMessage()));
        }
    }

    /**
     * Get audit logs by date range
     */
    @Operation(
            summary = "Get Audit Logs by Date Range",
            description = "Retrieves all audit logs within a specific date range. Requires AUDIT_VIEW_ALL permission."
    )
    @GetMapping("/date-range")
    @RequirePermission("AUDIT_VIEW_ALL")
    public ResponseEntity<ApiResponse<PaginatedResponse<AuditLogDTO>>> getAuditLogsByDateRange(
            @Parameter(description = "Start date (ISO format)", required = true, example = "2025-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)", required = true, example = "2025-12-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (page < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Page number must be 0 or greater"));
            }

            if (size <= 0 || size > 100) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Page size must be between 1 and 100"));
            }

            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Start date must be before end date"));
            }

            log.info("Fetching audit logs from {} to {} - page: {}, size: {}", startDate, endDate, page, size);
            Page<AuditLog> auditLogPage = auditLogService.getAuditLogsByDateRange(startDate, endDate, page, size);

            List<AuditLogDTO> auditLogDTOs = auditLogPage.getContent().stream()
                    .map(auditLogMapper::toDTO)
                    .collect(Collectors.toList());

            PaginatedResponse<AuditLogDTO> paginatedResponse = PaginatedResponse.of(
                    auditLogDTOs,
                    auditLogPage.getTotalElements(),
                    auditLogPage.getTotalPages(),
                    auditLogPage.getNumber(),
                    auditLogPage.getSize(),
                    auditLogPage.hasNext(),
                    auditLogPage.hasPrevious()
            );

            return ResponseEntity.ok(ApiResponse.success(paginatedResponse));
        } catch (Exception e) {
            log.error("Error fetching audit logs by date range: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to fetch audit logs: " + e.getMessage()));
        }
    }
}
