package com.ceedpods.crmbuild.service;

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

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String keycloakIssuerUri;
    
    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;
    
    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret:}")
    private String clientSecret;
    
    /**
     * Register a new user in Keycloak
     */
    public String registerUser(RegisterRequest request) {
        try {
            String adminToken = getAdminToken();
            // Extract base URL (remove /realms/lahiru from issuer URI)
            String baseUrl = keycloakIssuerUri.replace(AppConstants.Keycloak.REALM_LAHIRU, "");
            String usersUrl = baseUrl + AppConstants.Keycloak.ADMIN_USERS_PATH;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(adminToken);
            
            Map<String, Object> userRepresentation = new HashMap<>();
            userRepresentation.put("username", request.getUsername());
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
            
            log.info("User registered successfully in Keycloak: {}", request.getUsername());
            return null;

        } catch (Exception e) {
            log.error("Error registering user in Keycloak: {}", e.getMessage(), e);
            throw new RuntimeException(AppConstants.Messages.KEYCLOAK_REGISTRATION_FAILED + ": " + e.getMessage());
        }
    }
    
    /**
     * Login user and get tokens
     */
    public AuthResponse login(LoginRequest request) {
        try {
            String tokenUrl = keycloakIssuerUri + AppConstants.Keycloak.TOKEN_ENDPOINT;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", AppConstants.Keycloak.GRANT_TYPE_PASSWORD);
            body.add("client_id", clientId);
            if (clientSecret != null && !clientSecret.isEmpty()) {
                body.add("client_secret", clientSecret);
            }
            body.add("username", request.getUsername());
            body.add("password", request.getPassword());
            
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            Map<String, Object> responseBody = response.getBody();

            if (responseBody == null) {
                throw new RuntimeException(AppConstants.Messages.KEYCLOAK_TOKEN_FAILED);
            }

            return AuthResponse.builder()
                .accessToken((String) responseBody.get("access_token"))
                .refreshToken((String) responseBody.get("refresh_token"))
                .tokenType(AppConstants.Keycloak.TOKEN_TYPE_BEARER)
                .expiresIn(((Number) responseBody.get("expires_in")).longValue())
                .username(request.getUsername())
                .build();

        } catch (Exception e) {
            log.error("Error during login: {}", e.getMessage(), e);
            throw new RuntimeException(AppConstants.Messages.LOGIN_FAILED + ": " + e.getMessage());
        }
    }
    
    /**
     * Refresh access token
     */
    public AuthResponse refreshToken(String refreshToken) {
        try {
            String tokenUrl = keycloakIssuerUri + AppConstants.Keycloak.TOKEN_ENDPOINT;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", AppConstants.Keycloak.GRANT_TYPE_REFRESH_TOKEN);
            body.add("client_id", clientId);
            if (clientSecret != null && !clientSecret.isEmpty()) {
                body.add("client_secret", clientSecret);
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

            if (responseBody == null) {
                throw new RuntimeException(AppConstants.Messages.TOKEN_REFRESH_FAILED);
            }

            return AuthResponse.builder()
                .accessToken((String) responseBody.get("access_token"))
                .refreshToken((String) responseBody.get("refresh_token"))
                .tokenType(AppConstants.Keycloak.TOKEN_TYPE_BEARER)
                .expiresIn(((Number) responseBody.get("expires_in")).longValue())
                .build();

        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage(), e);
            throw new RuntimeException(AppConstants.Messages.TOKEN_REFRESH_FAILED + ": " + e.getMessage());
        }
    }
    
    /**
     * Get admin token for Keycloak admin operations
     */
    private String getAdminToken() {
        try {
            String tokenUrl = keycloakIssuerUri.replace(AppConstants.Keycloak.REALM_LAHIRU, "")
                + AppConstants.Keycloak.REALM_MASTER + AppConstants.Keycloak.TOKEN_ENDPOINT;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", AppConstants.Keycloak.GRANT_TYPE_PASSWORD);
            body.add("client_id", AppConstants.Keycloak.ADMIN_CLIENT_ID);
            body.add("username", AppConstants.Keycloak.ADMIN_USERNAME);
            body.add("password", AppConstants.Keycloak.ADMIN_PASSWORD);
            
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
            throw new RuntimeException(AppConstants.Messages.KEYCLOAK_ADMIN_TOKEN_FAILED);
        }
    }
    
    /**
     * Logout user
     */
    public void logout(String refreshToken) {
        try {
            String logoutUrl = keycloakIssuerUri + AppConstants.Keycloak.LOGOUT_ENDPOINT;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            if (clientSecret != null && !clientSecret.isEmpty()) {
                body.add("client_secret", clientSecret);
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
}
