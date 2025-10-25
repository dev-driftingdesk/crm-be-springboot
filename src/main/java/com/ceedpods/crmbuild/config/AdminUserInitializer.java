package com.ceedpods.crmbuild.config;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.dto.RegisterRequest;
import com.ceedpods.crmbuild.entity.User;
import com.ceedpods.crmbuild.enums.UserRole;
import com.ceedpods.crmbuild.repository.UserRepository;
import com.ceedpods.crmbuild.service.KeycloakHealthService;
import com.ceedpods.crmbuild.service.KeycloakService;
import com.ceedpods.crmbuild.service.KeycloakAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Initializes the default admin user on application startup
 * This will only create an admin user if none exists in the database
 */
@Component
@RequiredArgsConstructor
@Slf4j
@org.springframework.core.annotation.Order(1)
public class AdminUserInitializer {

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;
    private final KeycloakAdminService keycloakAdminService;
    @Value("${keycloak.server-url}")
    private String keycloakServerUrl;
    
    @Value("${keycloak.realm-name}")
    private String keycloakRealmName;
    
    private final KeycloakHealthService keycloakHealthService;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeAdmin() {
        try {
            log.info("=============================================================");
            log.info("CHECKING FOR ADMIN USER...");
            log.info("=============================================================");
            
            // Verify Keycloak is available - application requires Keycloak
            if (!keycloakHealthService.isKeycloakAvailable()) {
                log.error("Keycloak is not available. Application cannot start without Keycloak connectivity.");
                throw new RuntimeException("Keycloak connectivity required for application startup");
            }

            // Check if admin already exists
            long adminCount = userRepository.countByRole(UserRole.ADMIN);
            log.info("Admin users in database: {}", adminCount);

            if (adminCount > 0) {
                log.info("✓ Admin user already exists. Skipping initialization.");
                log.info("=============================================================");
                return;
            }

            log.info("No admin found. Creating admin user...");
            log.info("=============================================================");

            // Step 1: Create realm roles in Keycloak
            log.info("Step 1: Creating ADMIN and USER roles in Keycloak...");
            keycloakAdminService.createRealmRole(AppConstants.Keycloak.ROLE_ADMIN, AppConstants.Keycloak.ROLE_ADMIN_DESC);
            keycloakAdminService.createRealmRole(AppConstants.Keycloak.ROLE_USER, AppConstants.Keycloak.ROLE_USER_DESC);
            log.info("✓ Roles created");

            // Step 2: Create admin user in Keycloak
            log.info("Step 2: Creating admin user in Keycloak...");
            log.info("Email: {}", AppConstants.DefaultUsers.ADMIN_EMAIL);

            RegisterRequest adminRequest = RegisterRequest.builder()
                .email(AppConstants.DefaultUsers.ADMIN_EMAIL)
                .password(AppConstants.DefaultUsers.ADMIN_PASSWORD)
                .confirmPassword(AppConstants.DefaultUsers.ADMIN_PASSWORD)
                .firstName(AppConstants.DefaultUsers.ADMIN_FIRST_NAME)
                .lastName(AppConstants.DefaultUsers.ADMIN_LAST_NAME)
                .build();

            String keycloakId = keycloakService.registerUser(adminRequest);
            log.info("✓ User created in Keycloak. ID: {}", keycloakId);

            // Step 3: Assign ADMIN role
            log.info("Step 3: Assigning ADMIN role to user in Keycloak...");
            keycloakAdminService.assignRealmRoleToUser(keycloakId, AppConstants.Keycloak.ROLE_ADMIN);
            log.info("✓ ADMIN role assigned");

            // Step 4: Save to MongoDB
            log.info("Step 4: Saving admin user to MongoDB...");
            User adminUser = User.builder()
                .keycloakId(keycloakId)
                .email(AppConstants.DefaultUsers.ADMIN_EMAIL)
                .firstName(AppConstants.DefaultUsers.ADMIN_FIRST_NAME)
                .lastName(AppConstants.DefaultUsers.ADMIN_LAST_NAME)
                .role(UserRole.ADMIN)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

            userRepository.save(adminUser);
            log.info("✓ User saved to MongoDB");

            log.info("=============================================================");
            log.info("✓✓✓ ADMIN USER CREATED SUCCESSFULLY! ✓✓✓");
            log.info("=============================================================");
            log.info("USE THESE CREDENTIALS TO LOG IN:");
            log.info("Email: {}", AppConstants.DefaultUsers.ADMIN_EMAIL);
            log.info("Password: {}", AppConstants.DefaultUsers.ADMIN_PASSWORD);
            log.info("=============================================================");
            log.info("ADMIN ROLE ASSIGNED IN KEYCLOAK");
            log.info("After login, the JWT token should include 'ADMIN' in realm_access.roles");
            log.info("=============================================================");
            log.warn("⚠ CHANGE THE DEFAULT PASSWORD AFTER FIRST LOGIN!");
            log.info("=============================================================");

        } catch (Exception e) {
            log.error("=============================================================");
            log.error("✗✗✗ FAILED TO CREATE ADMIN USER ✗✗✗");
            log.error("=============================================================");
            log.error("Error: {}", e.getMessage());
            log.error("Full error:", e);
            log.error("=============================================================");
            log.error("CRITICAL: Application cannot start without Keycloak admin user!");
            log.error("Please check:");
            log.error("1. Keycloak is running at {}", keycloakServerUrl);
            log.error("2. The '{}' realm was created successfully", keycloakRealmName);
            log.error("3. MongoDB is running");
            log.error("4. Enable 'Login with email' in Keycloak realm settings if needed");
            log.error("5. Keycloak admin credentials are correct");
            log.error("=============================================================");
            throw new RuntimeException("Failed to create admin user. Application requires Keycloak connectivity.", e);
        }
    }
}
