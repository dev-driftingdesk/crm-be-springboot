package com.ceedpods.crmbuild.controller.admin;

import com.ceedpods.crmbuild.dto.request.FirebaseConfigRequest;
import com.ceedpods.crmbuild.dto.response.ApiResponse;
import com.ceedpods.crmbuild.entity.FirebaseConfiguration;
import com.ceedpods.crmbuild.service.FirebaseConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/firebase-config")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Firebase Configuration", description = "Admin endpoints for Firebase configuration management")
public class FirebaseConfigController {

    private final FirebaseConfigService firebaseConfigService;

    @PostMapping
    @Operation(summary = "Create or update Firebase configuration",
            description = "Saves Firebase configuration with automatic encryption of sensitive fields")
    public ResponseEntity<ApiResponse<Map<String, String>>> saveConfiguration(
            @Valid @RequestBody FirebaseConfigRequest request) {
        try {
            log.info("Received request to save Firebase configuration for project: {}", request.getProjectId());

            FirebaseConfiguration saved = firebaseConfigService.saveConfiguration(request);

            Map<String, String> response = new HashMap<>();
            response.put("id", saved.getId());
            response.put("projectId", saved.getProjectId());
            response.put("message", "Firebase configuration saved successfully. Restart the application to apply changes.");

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Firebase configuration saved successfully", response));
        } catch (Exception e) {
            log.error("Error saving Firebase configuration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to save Firebase configuration: " + e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Get current Firebase configuration",
            description = "Returns the active Firebase configuration with masked sensitive fields")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getConfiguration() {
        try {
            return firebaseConfigService.getActiveConfiguration()
                    .map(config -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("id", config.getId());
                        response.put("type", config.getType());
                        response.put("projectId", config.getProjectId());
                        response.put("privateKeyId", firebaseConfigService.maskSensitiveField(config.getPrivateKeyId()));
                        response.put("privateKey", "****ENCRYPTED****");
                        response.put("clientEmail", firebaseConfigService.maskSensitiveField(config.getClientEmail()));
                        response.put("clientId", firebaseConfigService.maskSensitiveField(config.getClientId()));
                        response.put("authUri", config.getAuthUri());
                        response.put("tokenUri", config.getTokenUri());
                        response.put("universeDomain", config.getUniverseDomain());
                        response.put("active", config.isActive());
                        response.put("createdAt", config.getCreatedAt());
                        response.put("updatedAt", config.getUpdatedAt());

                        return ResponseEntity.ok(ApiResponse.success(response));
                    })
                    .orElse(ResponseEntity.ok(ApiResponse.success("No active Firebase configuration found", null)));
        } catch (Exception e) {
            log.error("Error retrieving Firebase configuration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve Firebase configuration"));
        }
    }
}
