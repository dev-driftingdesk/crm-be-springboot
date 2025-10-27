package com.ceedpods.crmbuild.service.keycloakService;

import com.ceedpods.crmbuild.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing Keycloak realms
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakRealmService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final KeycloakHealthService keycloakHealthService;
    
    @Value("${keycloak.server-url}")
    private String keycloakServerUrl;
    
    @Value("${keycloak.admin.username}")
    private String keycloakAdminUsername;
    
    @Value("${keycloak.admin.password}")
    private String keycloakAdminPassword;
    
    @Value("${keycloak.admin.client-id}")
    private String keycloakAdminClientId;

    /**
     * Get admin token from master realm for administrative operations
     */
    private String getMasterAdminToken() {
        if (!keycloakHealthService.isKeycloakAvailable()) {
            throw new RuntimeException("Keycloak server is not available");
        }

        try {
            String tokenUrl = keycloakServerUrl +
                AppConstants.Keycloak.getMasterRealmPath() +
                AppConstants.Keycloak.TOKEN_ENDPOINT;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", AppConstants.Keycloak.GRANT_TYPE_PASSWORD);
            body.add("client_id", keycloakAdminClientId);
            body.add("username", keycloakAdminUsername);
            body.add("password", keycloakAdminPassword);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                entity,
                Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            return responseBody != null ? (String) responseBody.get("access_token") : null;

        } catch (Exception e) {
            log.error("Error getting master admin token: {}", e.getMessage());
            throw new RuntimeException("Failed to get admin token from master realm: " + e.getMessage());
        }
    }

    /**
     * Check if a realm exists
     */
    public boolean realmExists(String realmName) {


        try {
            String adminToken = getMasterAdminToken();
            String realmUrl = keycloakServerUrl + "/admin/realms/" + realmName;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            restTemplate.exchange(realmUrl, HttpMethod.GET, entity, Map.class);
            return true;

        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (Exception e) {
            log.error("Error checking if realm exists: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Create a new realm
     */
    public void createRealm(String realmName, String displayName) {


        try {
            String adminToken = getMasterAdminToken();
            String realmsUrl = keycloakServerUrl + "/admin/realms";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(adminToken);

            Map<String, Object> realmConfig = new HashMap<>();
            realmConfig.put("realm", realmName);
            realmConfig.put("displayName", displayName);
            realmConfig.put("enabled", true);
            realmConfig.put("loginWithEmailAllowed", true);
            realmConfig.put("duplicateEmailsAllowed", false);
            realmConfig.put("registrationAllowed", false);
            realmConfig.put("resetPasswordAllowed", true);
            realmConfig.put("editUsernameAllowed", false);

            // Token settings
            realmConfig.put("accessTokenLifespan", 300); // 5 minutes
            realmConfig.put("ssoSessionIdleTimeout", 1800); // 30 minutes
            realmConfig.put("ssoSessionMaxLifespan", 36000); // 10 hours

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(realmConfig, headers);

            restTemplate.exchange(realmsUrl, HttpMethod.POST, entity, String.class);

            log.info("✓ Realm '{}' created successfully", realmName);

        } catch (HttpClientErrorException.Conflict e) {
            log.info("Realm '{}' already exists", realmName);
        } catch (Exception e) {
            log.error("Error creating realm '{}': {}", realmName, e.getMessage(), e);
            throw new RuntimeException("Failed to create realm: " + realmName + ". Error: " + e.getMessage());
        }
    }

    /**
     * Create a client in a realm
     */
    public void createClient(String realmName, String clientId, String clientSecret) {


        try {
            String adminToken = getMasterAdminToken();
            String clientsUrl = keycloakServerUrl + "/admin/realms/" + realmName + "/clients";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(adminToken);

            Map<String, Object> clientConfig = new HashMap<>();
            clientConfig.put("clientId", clientId);
            clientConfig.put("enabled", true);
            clientConfig.put("publicClient", false);
            clientConfig.put("secret", clientSecret);
            clientConfig.put("directAccessGrantsEnabled", true);
            clientConfig.put("serviceAccountsEnabled", false);
            clientConfig.put("standardFlowEnabled", true);
            clientConfig.put("implicitFlowEnabled", false);

            // OAuth2 settings
            clientConfig.put("protocol", "openid-connect");
            clientConfig.put("fullScopeAllowed", true);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(clientConfig, headers);

            restTemplate.exchange(clientsUrl, HttpMethod.POST, entity, String.class);

            log.info("✓ Client '{}' created successfully in realm '{}'", clientId, realmName);

        } catch (HttpClientErrorException.Conflict e) {
            log.info("Client '{}' already exists in realm '{}'", clientId, realmName);
        } catch (Exception e) {
            log.error("Error creating client '{}' in realm '{}': {}", clientId, realmName, e.getMessage());
            throw new RuntimeException("Failed to create client: " + clientId + ". Error: " + e.getMessage());
        }
    }

    /**
     * Create a realm role
     */
    public void createRealmRole(String realmName, String roleName, String description) {


        try {
            String adminToken = getMasterAdminToken();
            String rolesUrl = keycloakServerUrl + "/admin/realms/" + realmName + "/roles";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(adminToken);

            Map<String, Object> roleConfig = new HashMap<>();
            roleConfig.put("name", roleName);
            roleConfig.put("description", description);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(roleConfig, headers);

            restTemplate.exchange(rolesUrl, HttpMethod.POST, entity, String.class);

            log.info("✓ Role '{}' created successfully in realm '{}'", roleName, realmName);

        } catch (HttpClientErrorException.Conflict e) {
            log.info("Role '{}' already exists in realm '{}'", roleName, realmName);
        } catch (Exception e) {
            log.error("Error creating role '{}' in realm '{}': {}", roleName, realmName, e.getMessage());
        }
    }
}
