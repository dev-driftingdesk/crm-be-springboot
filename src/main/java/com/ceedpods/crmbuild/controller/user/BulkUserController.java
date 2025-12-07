package com.ceedpods.crmbuild.controller.user;

import com.ceedpods.crmbuild.dto.request.BulkUserRegistrationRequest;
import com.ceedpods.crmbuild.dto.response.ApiResponse;
import com.ceedpods.crmbuild.dto.response.BulkUserRegistrationResponse;
import com.ceedpods.crmbuild.security.CustomPermissionEvaluator;
import com.ceedpods.crmbuild.security.RequireAnyPermission;
import com.ceedpods.crmbuild.service.user.BulkUserRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for bulk user registration operations
 * Allows admins to register multiple sales users at once from a list of email addresses
 */
@RestController
@RequestMapping("/users/bulk")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bulk User Operations", description = "Endpoints for bulk user registration and management")
public class BulkUserController {

    private final BulkUserRegistrationService bulkUserRegistrationService;
    private final CustomPermissionEvaluator permissionEvaluator;

    /**
     * Bulk register sales users from a list of email addresses
     *
     * Creates users with SALES_REP role, generates one-time passwords,
     * flags accounts for password change on first login, and sends welcome emails.
     *
     * @param request BulkUserRegistrationRequest containing list of email addresses
     * @param authentication Current admin authentication
     * @return BulkUserRegistrationResponse with results for each email
     */
    @Operation(
        summary = "Bulk Register Sales Users",
        description = """
            Registers multiple sales users at once from a list of email addresses.

            **Behavior:**
            - Creates each email as a user with SALES_REP role
            - Generates one-time password from email (format: localPart + "123@")
            - Flags account to require password change on first login
            - Sends welcome email with login credentials
            - Skips emails that already exist (returns 'already_exists' status)
            - Reports per-email status in the response

            **Password Generation Rule:**
            - lahiru@gmail.com → lahiru123@
            - kasun@company.com → kasun123@

            **Requirements:**
            - Caller must be an ADMIN user
            - Maximum 100 emails per request
            """,
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Bulk registration completed (check individual results for status)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BulkUserRegistrationResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "success": true,
                      "message": "Bulk registration completed",
                      "data": {
                        "summary": {
                          "totalProcessed": 3,
                          "created": 2,
                          "alreadyExists": 1,
                          "failed": 0,
                          "emailsSent": 2,
                          "emailsFailed": 0
                        },
                        "results": [
                          {
                            "email": "lahiru@gmail.com",
                            "status": "created",
                            "temporaryPassword": "lahiru123@",
                            "emailStatus": "sent",
                            "userId": "abc123"
                          },
                          {
                            "email": "kasun@gmail.com",
                            "status": "created",
                            "temporaryPassword": "kasun123@",
                            "emailStatus": "sent",
                            "userId": "def456"
                          },
                          {
                            "email": "existing@gmail.com",
                            "status": "already_exists",
                            "emailStatus": "not_applicable"
                          }
                        ],
                        "timestamp": "2025-01-15 14:30:00"
                      }
                    }
                    """)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request - empty email list or validation failed",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "success": false,
                      "message": "Email list cannot be empty",
                      "data": null
                    }
                    """)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - User is not an ADMIN"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "503",
            description = "Service unavailable - Keycloak or email service is not available",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "success": false,
                      "message": "Keycloak authentication service is not available",
                      "data": null
                    }
                    """)
            )
        )
    })
    @PostMapping("/register")
    @RequireAnyPermission({"ADMIN"})
    public ResponseEntity<ApiResponse<BulkUserRegistrationResponse>> bulkRegisterUsers(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "List of email addresses to register as sales users",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = BulkUserRegistrationRequest.class),
                    examples = @ExampleObject(value = """
                        {
                          "emails": [
                            "lahiru@gmail.com",
                            "kasun@gmail.com",
                            "nimal@company.com"
                          ]
                        }
                        """)
                )
            )
            @Valid @RequestBody BulkUserRegistrationRequest request,
            Authentication authentication) {

        try {
            // Extract admin info from authentication
            String adminId = permissionEvaluator.getCurrentKeycloakId(authentication);
            String adminEmail = getEmailFromAuthentication(authentication);

            log.info("Bulk user registration initiated by admin: {} for {} emails",
                    adminEmail, request.getEmails().size());

            // Perform bulk registration
            BulkUserRegistrationResponse response = bulkUserRegistrationService.bulkRegisterUsers(
                    request, adminEmail, adminId);

            // Determine appropriate message based on results
            String message;
            if (response.getSummary().getFailed() == 0 && response.getSummary().getCreated() > 0) {
                message = String.format("Bulk registration completed successfully. Created %d users.",
                        response.getSummary().getCreated());
            } else if (response.getSummary().getCreated() == 0 && response.getSummary().getAlreadyExists() > 0) {
                message = "All provided emails already exist in the system.";
            } else {
                message = String.format("Bulk registration completed. Created: %d, Already exists: %d, Failed: %d",
                        response.getSummary().getCreated(),
                        response.getSummary().getAlreadyExists(),
                        response.getSummary().getFailed());
            }

            return ResponseEntity.ok(ApiResponse.success(message, response));

        } catch (RuntimeException e) {
            log.error("Error during bulk user registration: {}", e.getMessage(), e);

            // Check for service unavailability
            String errorMessage = e.getMessage();
            if (errorMessage != null &&
                (errorMessage.contains("Keycloak") ||
                 errorMessage.contains("not available"))) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error("Keycloak authentication service is not available. Error: " + errorMessage));
            }

            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Bulk registration failed: " + errorMessage));

        } catch (Exception e) {
            log.error("Unexpected error during bulk user registration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred during bulk registration. Please check server logs."));
        }
    }

    /**
     * Extract email from JWT authentication
     */
    private String getEmailFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString("email");
        }
        return "unknown";
    }
}
