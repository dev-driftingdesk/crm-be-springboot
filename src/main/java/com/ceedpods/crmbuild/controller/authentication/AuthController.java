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
    @PostMapping("/admin/create-admin")
    public ResponseEntity<Map<String, Object>> createAdminUser(
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

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BadRequestException("Refresh token is required");
        }
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BadRequestException("Refresh token is required");
        }
        authService.logout(refreshToken);
        Map<String, String> response = new HashMap<>();
        response.put("message", AppConstants.Messages.USER_LOGGED_OUT_SUCCESS);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
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
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
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
    @PostMapping("/verify-reset-code")
    public ResponseEntity<ApiResponse<Void>> verifyResetCode(
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
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
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
