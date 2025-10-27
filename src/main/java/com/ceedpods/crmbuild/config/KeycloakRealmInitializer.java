package com.ceedpods.crmbuild.config;


import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.service.keycloakService.KeycloakHealthService;
import com.ceedpods.crmbuild.service.keycloakService.KeycloakRealmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Initializes the Keycloak realm on application startup
 * This runs BEFORE AdminUserInitializer to ensure the realm exists
 * Requires Keycloak to be available for application startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(0) // Run first, before AdminUserInitializer
public class KeycloakRealmInitializer {

    private final KeycloakRealmService keycloakRealmService;
    private final KeycloakHealthService keycloakHealthService;
    
    @Value("${keycloak.server-url}")
    private String keycloakServerUrl;
    
    @Value("${keycloak.realm-name}")
    private String keycloakRealmName;
    
    @Value("${keycloak.client-id}")
    private String keycloakClientId;
    
    @Value("${keycloak.client-secret}")
    private String keycloakClientSecret;
    
    @Value("${keycloak.admin.username}")
    private String keycloakAdminUsername;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeRealm() {
        try {
            log.info("╔════════════════════════════════════════════════════════════╗");
            log.info("║          KEYCLOAK REALM INITIALIZATION                     ║");
            log.info("╚════════════════════════════════════════════════════════════╝");
            
            // Verify Keycloak is available - application requires Keycloak
            if (!keycloakHealthService.isKeycloakAvailable()) {
                log.error("Keycloak is not available. Application cannot start without Keycloak connectivity.");
                throw new RuntimeException("Keycloak connectivity required for realm initialization");
            }

            // Step 1: Check if realm exists
            log.info("Step 1: Checking if realm '{}' exists...", keycloakRealmName);
            boolean realmExists = keycloakRealmService.realmExists(keycloakRealmName);

            if (!realmExists) {
                log.info("Realm '{}' does not exist. Creating...", keycloakRealmName);

                // Step 2: Create realm
                log.info("Step 2: Creating realm '{}'...", keycloakRealmName);
                keycloakRealmService.createRealm(keycloakRealmName, AppConstants.Keycloak.REALM_DISPLAY_NAME);

                // Step 3: Create client
                log.info("Step 3: Creating client '{}' in realm '{}'...", keycloakClientId, keycloakRealmName);
                keycloakRealmService.createClient(keycloakRealmName, keycloakClientId, keycloakClientSecret);

                // Step 4: Create roles
                log.info("Step 4: Creating roles in realm '{}'...", keycloakRealmName);
                keycloakRealmService.createRealmRole(keycloakRealmName, AppConstants.Keycloak.ROLE_ADMIN, AppConstants.Keycloak.ROLE_ADMIN_DESC);
                keycloakRealmService.createRealmRole(keycloakRealmName, AppConstants.Keycloak.ROLE_USER, AppConstants.Keycloak.ROLE_USER_DESC);

                log.info("╔════════════════════════════════════════════════════════════╗");
                log.info("║     ✓✓✓ REALM CREATED SUCCESSFULLY! ✓✓✓                   ║");
                log.info("╠════════════════════════════════════════════════════════════╣");
                log.info("║ Realm Name: {}", String.format("%-45s", keycloakRealmName) + "║");
                log.info("║ Client ID:  {}", String.format("%-45s", keycloakClientId) + "║");
                log.info("║ Roles:      ADMIN, USER" + String.format("%-32s", "") + "║");
                log.info("╚════════════════════════════════════════════════════════════╝");

            } else {
                log.info("✓ Realm '{}' already exists. Skipping creation.", keycloakRealmName);

                // Ensure roles exist even if realm exists
                log.info("Ensuring roles exist in realm '{}'...", keycloakRealmName);
                keycloakRealmService.createRealmRole(keycloakRealmName, AppConstants.Keycloak.ROLE_ADMIN, AppConstants.Keycloak.ROLE_ADMIN_DESC);
                keycloakRealmService.createRealmRole(keycloakRealmName, AppConstants.Keycloak.ROLE_USER, AppConstants.Keycloak.ROLE_USER_DESC);

                log.info("╔════════════════════════════════════════════════════════════╗");
                log.info("║     ✓ REALM VERIFIED                                       ║");
                log.info("╠════════════════════════════════════════════════════════════╣");
                log.info("║ Realm Name: {}", String.format("%-45s", keycloakRealmName) + "║");
                log.info("║ Status:     Ready" + String.format("%-40s", "") + "║");
                log.info("╚════════════════════════════════════════════════════════════╝");
            }

        } catch (Exception e) {
            log.error("╔════════════════════════════════════════════════════════════╗");
            log.error("║     ✗✗✗ REALM INITIALIZATION FAILED ✗✗✗                    ║");
            log.error("╚════════════════════════════════════════════════════════════╝");
            log.error("Error: {}", e.getMessage(), e);
            log.error("════════════════════════════════════════════════════════════");
            log.error("CRITICAL: Application cannot start without Keycloak realm!");
            log.error("Please check:");
            log.error("1. Keycloak is running at: {}", keycloakServerUrl);
            log.error("2. Master realm admin credentials are correct ({}/{})",
                keycloakAdminUsername, "***");
            log.error("3. Network connectivity to Keycloak server");
            log.error("4. Keycloak admin API is accessible");
            log.error("════════════════════════════════════════════════════════════");
            throw new RuntimeException("Failed to initialize Keycloak realm. Application requires Keycloak connectivity.", e);
        }
    }
}
