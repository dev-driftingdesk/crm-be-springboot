package com.ceedpods.crmbuild.service;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.dto.AuthResponse;
import com.ceedpods.crmbuild.dto.LoginRequest;
import com.ceedpods.crmbuild.dto.RegisterRequest;
import com.ceedpods.crmbuild.dto.UserResponse;
import com.ceedpods.crmbuild.entity.User;
import com.ceedpods.crmbuild.enums.UserRole;
import com.ceedpods.crmbuild.exception.AuthenticationException;
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
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    /**
     * Register a new user in both Keycloak and MongoDB
     *
     * @param request Registration request with user details
     * @return UserResponse with created user details
     * @throws ResourceAlreadyExistsException if username or email already exists (409 Conflict)
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: Username already exists - {}", request.getUsername());
            throw new ResourceAlreadyExistsException(AppConstants.Messages.USERNAME_ALREADY_EXISTS);
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email already exists - {}", request.getEmail());
            throw new ResourceAlreadyExistsException(AppConstants.Messages.EMAIL_ALREADY_EXISTS);
        }

        try {
            // Register user in Keycloak
            String keycloakId = keycloakService.registerUser(request);

            // Save user in MongoDB
            User user = User.builder()
                .keycloakId(keycloakId)
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(UserRole.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

            user = userRepository.save(user);

            log.info("User registered successfully: {}", user.getUsername());

            return modelMapper.map(user, UserResponse.class);

        } catch (Exception e) {
            log.error("Error during registration for user {}: {}", request.getUsername(), e.getMessage(), e);
            throw new RuntimeException(AppConstants.Messages.REGISTRATION_FAILED + ": " + e.getMessage());
        }
    }

    /**
     * Login user and get tokens
     *
     * @param request Login request with username and password
     * @return AuthResponse with access token and user details
     * @throws AuthenticationException if authentication fails (401 Unauthorized)
     * @throws ResourceNotFoundException if user not found in MongoDB (404 Not Found)
     */
    public AuthResponse login(LoginRequest request) {
        try {
            // Authenticate with Keycloak
            AuthResponse authResponse = keycloakService.login(request);

            // Get user from MongoDB
            User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("User not found in MongoDB: {}", request.getUsername());
                    return new ResourceNotFoundException(AppConstants.Messages.USER_NOT_FOUND);
                });

            // Update response with user details
            authResponse.setUsername(user.getUsername());
            authResponse.setEmail(user.getEmail());

            log.info("User logged in successfully: {}", user.getUsername());

            return authResponse;

        } catch (ResourceNotFoundException e) {
            // Re-throw ResourceNotFoundException as-is
            throw e;
        } catch (Exception e) {
            log.error("Authentication failed for user {}: {}", request.getUsername(), e.getMessage());
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
     * @param username Username from JWT token
     * @return UserResponse with user details
     * @throws ResourceNotFoundException if user not found (404 Not Found)
     */
    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> {
                log.warn("User not found: {}", username);
                return new ResourceNotFoundException(AppConstants.Messages.USER_NOT_FOUND);
            });

        return modelMapper.map(user, UserResponse.class);
    }
}
