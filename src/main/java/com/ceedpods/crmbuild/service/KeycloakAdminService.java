package com.ceedpods.crmbuild.service;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.service.KeycloakHealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Service for Keycloak admin operations like creating roles and assigning them to users
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final KeycloakHealthService keycloakHealthService;
    
    @Value("${keycloak.server-url}")
    private String keycloakServerUrl;
    
    @Value("${keycloak.realm-name}")
    private String keycloakRealmName;
    
    @Value("${keycloak.admin.username}")
    private String keycloakAdminUsername;
    
    @Value("${keycloak.admin.password}")
    private String keycloakAdminPassword;
    
    @Value("${keycloak.admin.client-id}")
    private String keycloakAdminClientId;

    /**
     * Create a realm role in Keycloak
     */
    public void createRealmRole(String roleName, String description) {

        
        try {
            String adminToken = getAdminToken();
            String rolesUrl = keycloakServerUrl + AppConstants.Keycloak.ADMIN_REALMS_PATH + keycloakRealmName + "/roles";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(adminToken);

            Map<String, Object> roleRepresentation = Map.of(
                "name", roleName,
                "description", description
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(roleRepresentation, headers);

            restTemplate.exchange(rolesUrl, HttpMethod.POST, entity, String.class);

            log.info("Realm role '{}' created successfully in Keycloak", roleName);

        } catch (Exception e) {
            // Role might already exist, which is fine
            if (e.getMessage() != null && e.getMessage().contains("409")) {
                log.info("Realm role '{}' already exists in Keycloak", roleName);
            } else {
                log.error("Error creating realm role '{}': {}", roleName, e.getMessage());
            }
        }
    }

    /**
     * Assign a realm role to a user in Keycloak
     */
    public void assignRealmRoleToUser(String userId, String roleName) {

        
        try {
            String adminToken = getAdminToken();

            // First, get the role details
            String getRoleUrl = keycloakServerUrl + AppConstants.Keycloak.ADMIN_REALMS_PATH + keycloakRealmName + "/roles/" + roleName;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(adminToken);

            HttpEntity<Void> getEntity = new HttpEntity<>(headers);
            ResponseEntity<Map> roleResponse = restTemplate.exchange(
                getRoleUrl,
                HttpMethod.GET,
                getEntity,
                Map.class
            );

            Map<String, Object> role = roleResponse.getBody();
            if (role == null) {
                log.error("Role '{}' not found in Keycloak", roleName);
                return;
            }

            // Now assign the role to the user
            String assignRoleUrl = keycloakServerUrl + AppConstants.Keycloak.ADMIN_REALMS_PATH + keycloakRealmName + "/users/" + userId + "/role-mappings/realm";

            List<Map<String, Object>> rolesToAssign = List.of(
                Map.of(
                    "id", role.get("id"),
                    "name", role.get("name")
                )
            );

            HttpEntity<List<Map<String, Object>>> assignEntity = new HttpEntity<>(rolesToAssign, headers);
            restTemplate.exchange(assignRoleUrl, HttpMethod.POST, assignEntity, String.class);

            log.info("Role '{}' assigned to user '{}' successfully", roleName, userId);

        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("409")) {
                log.info("User already has role '{}'", roleName);
            } else {
                log.error("Error assigning role '{}' to user '{}': {}", roleName, userId, e.getMessage());
            }
        }
    }

    /**
     * Get admin token for Keycloak admin operations
     */
    private String getAdminToken() {
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
            log.error("Error getting admin token: {}", e.getMessage(), e);
            throw new RuntimeException(AppConstants.Messages.KEYCLOAK_ADMIN_TOKEN_FAILED + ": " + e.getMessage());
        }
    }
}
