package com.ceedpods.crmbuild.config;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.service.KeycloakRealmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Initializes the crmAdmin realm in Keycloak on application startup
 * This runs BEFORE AdminUserInitializer to ensure the realm exists
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(0) // Run first, before AdminUserInitializer
public class KeycloakRealmInitializer {

    private final KeycloakRealmService keycloakRealmService;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeRealm() {
        try {
            log.info("╔════════════════════════════════════════════════════════════╗");
            log.info("║          KEYCLOAK REALM INITIALIZATION                     ║");
            log.info("╚════════════════════════════════════════════════════════════╝");

            // Step 1: Check if realm exists
            log.info("Step 1: Checking if realm '{}' exists...", AppConstants.Keycloak.REALM_NAME);
            boolean realmExists = keycloakRealmService.realmExists(AppConstants.Keycloak.REALM_NAME);

            if (!realmExists) {
                log.info("Realm '{}' does not exist. Creating...", AppConstants.Keycloak.REALM_NAME);

                // Step 2: Create realm
                log.info("Step 2: Creating realm '{}'...", AppConstants.Keycloak.REALM_NAME);
                keycloakRealmService.createRealm(AppConstants.Keycloak.REALM_NAME, AppConstants.Keycloak.REALM_DISPLAY_NAME);

                // Step 3: Create client
                log.info("Step 3: Creating client '{}' in realm '{}'...", AppConstants.Keycloak.CLIENT_ID, AppConstants.Keycloak.REALM_NAME);
                keycloakRealmService.createClient(AppConstants.Keycloak.REALM_NAME, AppConstants.Keycloak.CLIENT_ID, AppConstants.Keycloak.CLIENT_SECRET);

                // Step 4: Create roles
                log.info("Step 4: Creating roles in realm '{}'...", AppConstants.Keycloak.REALM_NAME);
                keycloakRealmService.createRealmRole(AppConstants.Keycloak.REALM_NAME, AppConstants.Keycloak.ROLE_ADMIN, AppConstants.Keycloak.ROLE_ADMIN_DESC);
                keycloakRealmService.createRealmRole(AppConstants.Keycloak.REALM_NAME, AppConstants.Keycloak.ROLE_USER, AppConstants.Keycloak.ROLE_USER_DESC);

                log.info("╔════════════════════════════════════════════════════════════╗");
                log.info("║     ✓✓✓ REALM CREATED SUCCESSFULLY! ✓✓✓                   ║");
                log.info("╠════════════════════════════════════════════════════════════╣");
                log.info("║ Realm Name: {}", String.format("%-45s", AppConstants.Keycloak.REALM_NAME) + "║");
                log.info("║ Client ID:  {}", String.format("%-45s", AppConstants.Keycloak.CLIENT_ID) + "║");
                log.info("║ Roles:      ADMIN, USER" + String.format("%-32s", "") + "║");
                log.info("╚════════════════════════════════════════════════════════════╝");

            } else {
                log.info("✓ Realm '{}' already exists. Skipping creation.", AppConstants.Keycloak.REALM_NAME);

                // Ensure roles exist even if realm exists
                log.info("Ensuring roles exist in realm '{}'...", AppConstants.Keycloak.REALM_NAME);
                keycloakRealmService.createRealmRole(AppConstants.Keycloak.REALM_NAME, AppConstants.Keycloak.ROLE_ADMIN, AppConstants.Keycloak.ROLE_ADMIN_DESC);
                keycloakRealmService.createRealmRole(AppConstants.Keycloak.REALM_NAME, AppConstants.Keycloak.ROLE_USER, AppConstants.Keycloak.ROLE_USER_DESC);

                log.info("╔════════════════════════════════════════════════════════════╗");
                log.info("║     ✓ REALM VERIFIED                                       ║");
                log.info("╠════════════════════════════════════════════════════════════╣");
                log.info("║ Realm Name: {}", String.format("%-45s", AppConstants.Keycloak.REALM_NAME) + "║");
                log.info("║ Status:     Ready" + String.format("%-40s", "") + "║");
                log.info("╚════════════════════════════════════════════════════════════╝");
            }

        } catch (Exception e) {
            log.error("╔════════════════════════════════════════════════════════════╗");
            log.error("║     ✗✗✗ REALM INITIALIZATION FAILED ✗✗✗                    ║");
            log.error("╚════════════════════════════════════════════════════════════╝");
            log.error("Error: {}", e.getMessage(), e);
            log.error("════════════════════════════════════════════════════════════");
            log.error("CRITICAL: The application may not function correctly!");
            log.error("Please check:");
            log.error("1. Keycloak is running at: {}", AppConstants.Keycloak.SERVER_URL);
            log.error("2. Master realm admin credentials are correct ({}/{})",
                AppConstants.Keycloak.MASTER_ADMIN_USERNAME, AppConstants.Keycloak.MASTER_ADMIN_PASSWORD);
            log.error("3. Network connectivity to Keycloak server");
            log.error("════════════════════════════════════════════════════════════");
        }
    }
}
