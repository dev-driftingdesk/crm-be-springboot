package com.ceedpods.crmbuild.service.keycloakService;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.dto.AuthResponse;
import com.ceedpods.crmbuild.dto.LoginRequest;
import com.ceedpods.crmbuild.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for Keycloak user authentication operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakService {

    private final RestTemplate restTemplate = new RestTemplate();
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

    @Value("${keycloak.admin.password}")
    private String keycloakAdminPassword;

    @Value("${keycloak.admin.client-id}")
    private String keycloakAdminClientId;

    /**
     * Register a new user in Keycloak (using email as username)
     *
     * @param request Registration request with user details
     * @return Keycloak user ID
     */
    public String registerUser(RegisterRequest request) {
        // Verify Keycloak is available
        if (!keycloakHealthService.isKeycloakAvailable()) {
            throw new RuntimeException("Keycloak is required for user registration but is not available");
        }

        try {
            String adminToken = getAdminToken();
            String usersUrl = keycloakServerUrl + AppConstants.Keycloak.getAdminUsersPath(keycloakRealmName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(adminToken);

            Map<String, Object> userRepresentation = new HashMap<>();
            // Use email as username in Keycloak
            userRepresentation.put("username", request.getEmail());
            userRepresentation.put("email", request.getEmail());
            userRepresentation.put("firstName", request.getFirstName());
            userRepresentation.put("lastName", request.getLastName());
            userRepresentation.put("enabled", true);
            userRepresentation.put("emailVerified", false);

            // Set credentials
            Map<String, Object> credential = new HashMap<>();
            credential.put("type", AppConstants.Keycloak.CREDENTIAL_TYPE_PASSWORD);
            credential.put("value", request.getPassword());
            credential.put("temporary", false);
            userRepresentation.put("credentials", List.of(credential));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(userRepresentation, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                usersUrl,
                HttpMethod.POST,
                entity,
                String.class
            );

            // Extract user ID from Location header
            String location = response.getHeaders().getFirst("Location");
            if (location != null) {
                return location.substring(location.lastIndexOf('/') + 1);
            }

            log.info("User registered successfully in Keycloak: {}", request.getEmail());
            return null;

        } catch (Exception e) {
            log.error("Error registering user in Keycloak: {}", e.getMessage(), e);
            throw new RuntimeException(AppConstants.Messages.KEYCLOAK_REGISTRATION_FAILED + ": " + e.getMessage());
        }
    }

    /**
     * Login user and get tokens using email
     * Keycloak supports email login directly if configured
     */
    public AuthResponse login(LoginRequest request) {
        // Verify Keycloak is available
        if (!keycloakHealthService.isKeycloakAvailable()) {
            throw new RuntimeException("Keycloak is required for user login but is not available");
        }

        try {
            String tokenUrl = keycloakServerUrl +
                AppConstants.Keycloak.getRealmPath(keycloakRealmName) +
                AppConstants.Keycloak.TOKEN_ENDPOINT;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", AppConstants.Keycloak.GRANT_TYPE_PASSWORD);
            body.add("client_id", keycloakClientId);
            
            if (keycloakClientSecret != null && !keycloakClientSecret.isEmpty()) {
                body.add("client_secret", keycloakClientSecret);
            }
            // Keycloak allows login with email when "login with email" is enabled
            body.add("username", request.getEmail());
            body.add("password", request.getPassword());

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                entity,
                Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                return AuthResponse.builder()
                    .accessToken((String) responseBody.get("access_token"))
                    .refreshToken((String) responseBody.get("refresh_token"))
                    .expiresIn((Integer) responseBody.get("expires_in"))
                    .tokenType((String) responseBody.get("token_type"))
                    .build();
            }

            throw new RuntimeException("Invalid response from Keycloak");

        } catch (Exception e) {
            log.error("Error during login with email {}: {}", request.getEmail(), e.getMessage(), e);
            throw new RuntimeException(AppConstants.Messages.LOGIN_FAILED + ": " + e.getMessage());
        }
    }

    /**
     * Refresh access token
     */
    public AuthResponse refreshToken(String refreshToken) {
        try {
            String tokenUrl = keycloakServerUrl +
                AppConstants.Keycloak.getRealmPath(keycloakRealmName) +
                AppConstants.Keycloak.TOKEN_ENDPOINT;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", AppConstants.Keycloak.GRANT_TYPE_REFRESH_TOKEN);
            body.add("client_id", keycloakClientId);
            
            if (keycloakClientSecret != null && !keycloakClientSecret.isEmpty()) {
                body.add("client_secret", keycloakClientSecret);
            }
            body.add("refresh_token", refreshToken);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                entity,
                Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                return AuthResponse.builder()
                    .accessToken((String) responseBody.get("access_token"))
                    .refreshToken((String) responseBody.get("refresh_token"))
                    .expiresIn((Integer) responseBody.get("expires_in"))
                    .tokenType((String) responseBody.get("token_type"))
                    .build();
            }

            throw new RuntimeException("Invalid response from Keycloak");

        } catch (Exception e) {
            log.error("Error during token refresh: {}", e.getMessage(), e);
            throw new RuntimeException("Token refresh failed: " + e.getMessage());
        }
    }

    /**
     * Logout user
     */
    public void logout(String refreshToken) {
        try {
            String logoutUrl = keycloakServerUrl +
                AppConstants.Keycloak.getRealmPath(keycloakRealmName) +
                AppConstants.Keycloak.LOGOUT_ENDPOINT;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", keycloakClientId);
            
            if (keycloakClientSecret != null && !keycloakClientSecret.isEmpty()) {
                body.add("client_secret", keycloakClientSecret);
            }
            body.add("refresh_token", refreshToken);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

            restTemplate.exchange(
                logoutUrl,
                HttpMethod.POST,
                entity,
                String.class
            );

            log.info(AppConstants.Messages.USER_LOGGED_OUT_SUCCESS);

        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage(), e);
            throw new RuntimeException(AppConstants.Messages.LOGOUT_FAILED + ": " + e.getMessage());
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