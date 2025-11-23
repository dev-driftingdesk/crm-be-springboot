package com.ceedpods.crmbuild.controller.authentication;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.dto.AuthResponse;
import com.ceedpods.crmbuild.dto.LoginRequest;
import com.ceedpods.crmbuild.dto.RegisterRequest;
import com.ceedpods.crmbuild.dto.UserDTO;
import com.ceedpods.crmbuild.dto.request.ForgotPasswordRequest;
import com.ceedpods.crmbuild.dto.request.ResetPasswordRequest;
import com.ceedpods.crmbuild.dto.request.VerifyResetCodeRequest;
import com.ceedpods.crmbuild.dto.response.ApiResponse;
import com.ceedpods.crmbuild.exception.BadRequestException;
import com.ceedpods.crmbuild.exception.ResourceNotFoundException;
import com.ceedpods.crmbuild.service.authService.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and user session management endpoints")
public class

AuthController {
    
    private final AuthService authService;

    /**
     * Super admin endpoint to register new admin users
     * This should only be used during system setup or by existing super admin
     *
     * @param request Registration request with admin user details
     * @return UserDTO with created admin user details
     */
    @Operation(
        summary = "Create Admin User",
        description = "Creates a new admin user in the system. This endpoint should only be used during initial system setup or by existing super admins.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Admin user created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(value = """
                    {
                      "message": "Admin user created successfully",
                      "admin": {
                        "id": "123e4567-e89b-12d3-a456-426614174000",
                        "email": "admin@example.com",
                        "firstName": "John",
                        "lastName": "Doe",
                        "role": "ADMIN",
                        "enabled": true
                      }
                    }
                    """)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request data or email already exists",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "error": "Failed to create admin user: Email already exists"
                    }
                    """)
            )
        )
    })
    @PostMapping("/admin/create-admin")
    public ResponseEntity<Map<String, Object>> createAdminUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Admin user registration details",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = RegisterRequest.class),
                    examples = @ExampleObject(value = """
                        {
                          "email": "admin@example.com",
                          "firstName": "John",
                          "lastName": "Doe",
                          "password": "SecurePassword123!"
                        }
                        """)
                )
            )
            @Valid @RequestBody RegisterRequest request) {
        try {
            UserDTO adminUser = authService.registerAdminUser(request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Admin user created successfully");
            response.put("admin", adminUser);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create admin user: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @Operation(
        summary = "User Login",
        description = "Authenticates a user with email and password. Returns access token and refresh token for subsequent API calls."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                      "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                      "expiresIn": 3600,
                      "tokenType": "Bearer",
                      "user": {
                        "id": "123e4567-e89b-12d3-a456-426614174000",
                        "email": "user@example.com",
                        "firstName": "John",
                        "lastName": "Doe",
                        "role": "SALES_REP"
                      }
                    }
                    """)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "success": false,
                      "message": "Invalid email or password",
                      "data": null
                    }
                    """)
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Login credentials",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = LoginRequest.class),
                    examples = @ExampleObject(value = """
                        {
                          "email": "user@example.com",
                          "password": "SecurePassword123!"
                        }
                        """)
                )
            )
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Refresh Access Token",
        description = "Generates a new access token using a valid refresh token. Use this when the access token expires."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Token refreshed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                      "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                      "expiresIn": 3600,
                      "tokenType": "Bearer"
                    }
                    """)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Refresh token is missing, invalid or expired",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "success": false,
                      "message": "Refresh token is required",
                      "data": null
                    }
                    """)
            )
        )
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Refresh token request",
                required = true,
                content = @Content(
                    examples = @ExampleObject(value = """
                        {
                          "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                        }
                        """)
                )
            )
            @RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BadRequestException("Refresh token is required");
        }
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "User Logout",
        description = "Logs out the user by invalidating the refresh token. The access token will remain valid until it expires."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Logout successful",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "message": "User logged out successfully"
                    }
                    """)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Refresh token is required",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "success": false,
                      "message": "Refresh token is required",
                      "data": null
                    }
                    """)
            )
        )
    })
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Logout request with refresh token",
                required = true,
                content = @Content(
                    examples = @ExampleObject(value = """
                        {
                          "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                        }
                        """)
                )
            )
            @RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BadRequestException("Refresh token is required");
        }
        authService.logout(refreshToken);
        Map<String, String> response = new HashMap<>();
        response.put("message", AppConstants.Messages.USER_LOGGED_OUT_SUCCESS);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get Current User",
        description = "Retrieves the profile information of the currently authenticated user based on the JWT token.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "User profile retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDTO.class),
                examples = @ExampleObject(value = """
                    {
                      "id": "123e4567-e89b-12d3-a456-426614174000",
                      "email": "user@example.com",
                      "firstName": "John",
                      "lastName": "Doe",
                      "role": "SALES_REP",
                      "phoneNumber": "+1234567890",
                      "department": "Sales",
                      "territory": "North",
                      "enabled": true
                    }
                    """)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing token"
        )
    })
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        UserDTO response = authService.getCurrentUser(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Request password reset by sending verification code to email
     *
     * @param request ForgotPasswordRequest containing user email
     * @return Success message (doesn't reveal if email exists for security)
     */
    @Operation(
        summary = "Request Password Reset",
        description = "Sends a verification code to the user's email for password reset. Returns a generic success message regardless of whether the email exists (for security)."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Request processed",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "success": true,
                      "message": "If the email exists, a verification code has been sent to it.",
                      "data": null
                    }
                    """)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request data"
        )
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Email address for password reset",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = ForgotPasswordRequest.class),
                    examples = @ExampleObject(value = """
                        {
                          "email": "user@example.com"
                        }
                        """)
                )
            )
            @Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authService.requestPasswordReset(request);
            return ResponseEntity.ok(
                ApiResponse.success("If the email exists, a verification code has been sent to it.")
            );
        } catch (ResourceNotFoundException e) {
            // Don't reveal if email exists for security reasons
            return ResponseEntity.ok(
                ApiResponse.success("If the email exists, a verification code has been sent to it.")
            );
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }

    /**
     * Verify the password reset code
     *
     * @param request VerifyResetCodeRequest containing email and verification code
     * @return Success message if code is valid
     */
    @Operation(
        summary = "Verify Reset Code",
        description = "Verifies that the password reset code sent to the user's email is valid and not expired."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Code is valid",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "success": true,
                      "message": "Verification code is valid. You may now reset your password.",
                      "data": null
                    }
                    """)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid or expired code",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "success": false,
                      "message": "Invalid or expired verification code",
                      "data": null
                    }
                    """)
            )
        )
    })
    @PostMapping("/verify-reset-code")
    public ResponseEntity<ApiResponse<Void>> verifyResetCode(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Email and verification code",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = VerifyResetCodeRequest.class),
                    examples = @ExampleObject(value = """
                        {
                          "email": "user@example.com",
                          "code": "123456"
                        }
                        """)
                )
            )
            @Valid @RequestBody VerifyResetCodeRequest request) {
        try {
            authService.verifyResetCode(request);
            return ResponseEntity.ok(
                ApiResponse.success("Verification code is valid. You may now reset your password.")
            );
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }

    /**
     * Reset password using verification code
     *
     * @param request ResetPasswordRequest containing email, code, and new password
     * @return Success message if password was reset
     */
    @Operation(
        summary = "Reset Password",
        description = "Resets the user's password using a verified reset code. The code must be verified first using /verify-reset-code."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Password reset successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "success": true,
                      "message": "Password has been reset successfully. You can now login with your new password.",
                      "data": null
                    }
                    """)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request or verification code",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "success": false,
                      "message": "Invalid or expired verification code",
                      "data": null
                    }
                    """)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Password reset details",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = ResetPasswordRequest.class),
                    examples = @ExampleObject(value = """
                        {
                          "email": "user@example.com",
                          "code": "123456",
                          "newPassword": "NewSecurePassword123!"
                        }
                        """)
                )
            )
            @Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok(
                ApiResponse.success("Password has been reset successfully. You can now login with your new password.")
            );
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.error(e.getMessage())
            );
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("An error occurred while resetting password. Please try again.")
            );
        }
    }
}
