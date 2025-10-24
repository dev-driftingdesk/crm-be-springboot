package com.ceedpods.crmbuild.service;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.dto.AuthResponse;
import com.ceedpods.crmbuild.dto.LoginRequest;
import com.ceedpods.crmbuild.dto.RegisterRequest;
import com.ceedpods.crmbuild.dto.UserResponse;
import com.ceedpods.crmbuild.entity.User;
import com.ceedpods.crmbuild.enums.UserRole;
import com.ceedpods.crmbuild.exception.AuthenticationException;
import com.ceedpods.crmbuild.exception.BadRequestException;
import com.ceedpods.crmbuild.exception.ResourceAlreadyExistsException;
import com.ceedpods.crmbuild.exception.ResourceNotFoundException;
import com.ceedpods.crmbuild.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final KeycloakService keycloakService;
    private final KeycloakAdminService keycloakAdminService;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    /**
     * Register a new user in both Keycloak and MongoDB (Admin only)
     * This method is restricted to admin users only
     *
     * @param request Registration request with user details
     * @return UserResponse with created user details
     * @throws ResourceAlreadyExistsException if username or email already exists (409 Conflict)
     * @throws BadRequestException if passwords don't match (400 Bad Request)
     */
    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        // Validate password confirmation
        validatePasswordConfirmation(request.getPassword(), request.getConfirmPassword());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email already exists - {}", request.getEmail());
            throw new ResourceAlreadyExistsException(AppConstants.Messages.EMAIL_ALREADY_EXISTS);
        }

        try {
            // Register user in Keycloak (using email as username)
            String keycloakId = keycloakService.registerUser(request);

            // Assign USER role to the user in Keycloak
            keycloakAdminService.assignRealmRoleToUser(keycloakId, AppConstants.Keycloak.ROLE_USER);

            // Save user in MongoDB with USER role (admin can only create regular users)
            User user = User.builder()
                .keycloakId(keycloakId)
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(UserRole.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

            user = userRepository.save(user);

            log.info("User registered successfully by admin: {}", user.getEmail());

            return modelMapper.map(user, UserResponse.class);

        } catch (ResourceAlreadyExistsException | BadRequestException e) {
            // Re-throw custom exceptions as-is
            throw e;
        } catch (Exception e) {
            log.error("Error during registration for user {}: {}", request.getEmail(), e.getMessage(), e);
            throw new RuntimeException(AppConstants.Messages.REGISTRATION_FAILED + ": " + e.getMessage());
        }
    }

    /**
     * Validate that password and confirm password match
     *
     * @param password Password
     * @param confirmPassword Confirm password
     * @throws BadRequestException if passwords don't match
     */
    private void validatePasswordConfirmation(String password, String confirmPassword) {
        if (password == null || confirmPassword == null || !password.equals(confirmPassword)) {
            log.warn("Password validation failed: Passwords do not match");
            throw new BadRequestException(AppConstants.Messages.PASSWORD_MISMATCH);
        }
    }

    /**
     * Check if an admin user already exists in the system
     *
     * @return true if admin exists, false otherwise
     */
    public boolean adminExists() {
        return userRepository.existsByRole(UserRole.ADMIN);
    }

    /**
     * Get count of admin users in the system
     *
     * @return count of admin users
     */
    public long getAdminCount() {
        return userRepository.countByRole(UserRole.ADMIN);
    }

    /**
     * Login user and get tokens
     *
     * @param request Login request with email and password
     * @return AuthResponse with access token and user details
     * @throws AuthenticationException if authentication fails (401 Unauthorized)
     * @throws ResourceNotFoundException if user not found in MongoDB (404 Not Found)
     */
    public AuthResponse login(LoginRequest request) {
        try {
            // Authenticate with Keycloak
            AuthResponse authResponse = keycloakService.login(request);

            // Get user from MongoDB by email
            User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("User not found in MongoDB: {}", request.getEmail());
                    return new ResourceNotFoundException(AppConstants.Messages.USER_NOT_FOUND);
                });

            // Update response with user details
            authResponse.setEmail(user.getEmail());

            log.info("User logged in successfully: {}", user.getEmail());

            return authResponse;

        } catch (ResourceNotFoundException e) {
            // Re-throw ResourceNotFoundException as-is
            throw e;
        } catch (Exception e) {
            log.error("Authentication failed for user {}: {}", request.getEmail(), e.getMessage());
            throw new AuthenticationException(AppConstants.Messages.LOGIN_FAILED);
        }
    }

    /**
     * Refresh access token
     *
     * @param refreshToken Refresh token
     * @return AuthResponse with new access token
     * @throws AuthenticationException if refresh token is invalid (401 Unauthorized)
     */
    public AuthResponse refreshToken(String refreshToken) {
        try {
            return keycloakService.refreshToken(refreshToken);
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new AuthenticationException("Invalid or expired refresh token");
        }
    }

    /**
     * Logout user
     *
     * @param refreshToken Refresh token
     * @throws AuthenticationException if logout fails (401 Unauthorized)
     */
    public void logout(String refreshToken) {
        try {
            keycloakService.logout(refreshToken);
            log.info("User logged out successfully");
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            throw new AuthenticationException("Logout failed");
        }
    }

    /**
     * Get current user profile
     *
     * @param email Email from JWT token
     * @return UserResponse with user details
     * @throws ResourceNotFoundException if user not found (404 Not Found)
     */
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.warn("User not found: {}", email);
                return new ResourceNotFoundException(AppConstants.Messages.USER_NOT_FOUND);
            });

        return modelMapper.map(user, UserResponse.class);
    }
}
