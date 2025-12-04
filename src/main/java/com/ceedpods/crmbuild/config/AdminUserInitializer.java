package com.ceedpods.crmbuild.config;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.dto.RegisterRequest;
import com.ceedpods.crmbuild.entity.user.User;
import com.ceedpods.crmbuild.enums.UserRole;
import com.ceedpods.crmbuild.repository.UserRepository;
import com.ceedpods.crmbuild.service.keycloakService.KeycloakHealthService;
import com.ceedpods.crmbuild.service.keycloakService.KeycloakAdminService;
import com.ceedpods.crmbuild.service.keycloakService.KeycloakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

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
            log.info("Step 1: Creating all user roles in Keycloak...");
            keycloakAdminService.createRealmRole(AppConstants.Keycloak.ROLE_ADMIN, AppConstants.Keycloak.ROLE_ADMIN_DESC);
            keycloakAdminService.createRealmRole(AppConstants.Keycloak.ROLE_MANAGER, AppConstants.Keycloak.ROLE_MANAGER_DESC);
            keycloakAdminService.createRealmRole(AppConstants.Keycloak.ROLE_SALES_REP, AppConstants.Keycloak.ROLE_USER_DESC);
            keycloakAdminService.createRealmRole(AppConstants.Keycloak.ROLE_SALES_REP, AppConstants.Keycloak.ROLE_USER_DESC);
            log.info("✓ All roles created: ADMIN, MANAGER, SALES_REP, USER");

            // Step 2: Create admin user in Keycloak or get existing user
            log.info("Step 2: Creating admin user in Keycloak...");
            log.info("Email: {}", AppConstants.DefaultUsers.ADMIN_EMAIL);

            String keycloakId = null;
            try {
                RegisterRequest adminRequest = RegisterRequest.builder()
                    .email(AppConstants.DefaultUsers.ADMIN_EMAIL)
                    .password(AppConstants.DefaultUsers.ADMIN_PASSWORD)
                    .confirmPassword(AppConstants.DefaultUsers.ADMIN_PASSWORD)
                    .firstName(AppConstants.DefaultUsers.ADMIN_FIRST_NAME)
                    .lastName(AppConstants.DefaultUsers.ADMIN_LAST_NAME)
                    .build();

                keycloakId = keycloakService.registerUser(adminRequest);
                log.info("✓ User created in Keycloak. ID: {}", keycloakId);
            } catch (org.springframework.web.client.HttpClientErrorException.Conflict e) {
                // User already exists (409 Conflict) - retrieve existing user
                log.info("User already exists in Keycloak, retrieving existing user...");
                keycloakId = keycloakAdminService.getUserByEmail(AppConstants.DefaultUsers.ADMIN_EMAIL);
                if (keycloakId != null) {
                    log.info("✓ Using existing user from Keycloak. ID: {}", keycloakId);
                } else {
                    throw new RuntimeException("User exists in Keycloak but could not retrieve user ID");
                }
            } catch (Exception e) {
                // Check if error message indicates user exists (fallback check)
                if (e.getMessage() != null && (e.getMessage().contains("409") || e.getMessage().contains("User exists"))) {
                    log.info("User already exists in Keycloak (detected via message), retrieving existing user...");
                    keycloakId = keycloakAdminService.getUserByEmail(AppConstants.DefaultUsers.ADMIN_EMAIL);
                    if (keycloakId != null) {
                        log.info("✓ Using existing user from Keycloak. ID: {}", keycloakId);
                    } else {
                        throw new RuntimeException("User exists in Keycloak but could not retrieve user ID");
                    }
                } else {
                    throw e;
                }
            }

            // Step 3: Assign ADMIN role
            log.info("Step 3: Assigning ADMIN role to user in Keycloak...");
            keycloakAdminService.assignRealmRoleToUser(keycloakId, AppConstants.Keycloak.ROLE_ADMIN);
            log.info("✓ ADMIN role assigned");

            // Step 4: Save to MongoDB or update existing user
            log.info("Step 4: Saving admin user to MongoDB...");

            // Check if user already exists in MongoDB
            User existingUser = userRepository.findByKeycloakId(keycloakId).orElse(null);

            if (existingUser != null) {
                log.info("User already exists in MongoDB, updating if necessary...");
                // Update existing user to ensure it has admin role
                existingUser.setRole(UserRole.ADMIN);
                existingUser.setEnabled(true);
                userRepository.save(existingUser);
                log.info("✓ Existing user updated in MongoDB");
            } else {
                // Create new user
                User adminUser = User.builder()
                    .keycloakId(keycloakId)
                    .email(AppConstants.DefaultUsers.ADMIN_EMAIL)
                    .firstName(AppConstants.DefaultUsers.ADMIN_FIRST_NAME)
                    .lastName(AppConstants.DefaultUsers.ADMIN_LAST_NAME)
                    .role(UserRole.ADMIN)
                    .enabled(true)
                    .build();

                userRepository.save(adminUser);
                log.info("✓ User saved to MongoDB");
            }

            log.info("=============================================================");
            log.info("✓✓✓ ADMIN USER INITIALIZED SUCCESSFULLY! ✓✓✓");
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
