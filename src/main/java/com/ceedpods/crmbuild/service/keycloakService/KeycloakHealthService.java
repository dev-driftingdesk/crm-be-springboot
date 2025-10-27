package com.ceedpods.crmbuild.service.keycloakService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service for checking Keycloak server health and connectivity
 * Application will fail if Keycloak is not available
 */
@Service
@Slf4j
public class KeycloakHealthService {

    @Value("${keycloak.server-url}")
    private String keycloakServerUrl;
    
    @Value("${keycloak.realm-name}")
    private String keycloakRealmName;
    
    @Value("${keycloak.connection.timeout}")
    private int keycloakConnectionTimeout;
    
    private final RestTemplate restTemplate = new RestTemplate();

    private boolean lastHealthStatus = false;
    private LocalDateTime lastHealthCheck = LocalDateTime.now().minusMinutes(5);

    /**
     * Check if Keycloak server is available and healthy
     * Uses the realm endpoint which is the standard way to verify Keycloak connectivity
     * Caches result for 1 minute to avoid excessive calls
     */
    public boolean isKeycloakAvailable() {
        // Check cache first (1 minute cache)
        if (Duration.between(lastHealthCheck, LocalDateTime.now()).toMinutes() < 1) {
            return lastHealthStatus;
        }

        try {
            // Attempt async health check with timeout using realm endpoint
            CompletableFuture<Boolean> healthCheck = CompletableFuture.supplyAsync(() -> {
                try {
                    // Use realm endpoint instead of /health which doesn't exist in Keycloak
                    String realmUrl = keycloakServerUrl + "/realms/" + keycloakRealmName;
                    
                    log.debug("Checking Keycloak connectivity at: {}", realmUrl);
                    
                    ResponseEntity<String> response = restTemplate.exchange(
                        realmUrl,
                        HttpMethod.GET,
                        null,
                        String.class
                    );

                    boolean isHealthy = response.getStatusCode().is2xxSuccessful();
                    log.debug("Keycloak connectivity check result: {}", isHealthy ? "CONNECTED" : "FAILED");
                    
                    return isHealthy;
                } catch (Exception e) {
                    log.debug("Keycloak connectivity check failed: {}", e.getMessage());
                    return false;
                }
            });

            // Wait for result with timeout
            lastHealthStatus = healthCheck.get(keycloakConnectionTimeout, TimeUnit.MILLISECONDS);
            lastHealthCheck = LocalDateTime.now();

            if (lastHealthStatus) {
                log.debug("✓ Keycloak server is available at: {}", keycloakServerUrl);
            } else {
                log.error("✗ Keycloak server is not available at: {} - Application requires Keycloak", keycloakServerUrl);
                throw new RuntimeException("Keycloak server is not available. Application cannot start without Keycloak connectivity.");
            }

            return lastHealthStatus;

        } catch (Exception e) {
            log.error("Keycloak connectivity check failed: {} - Application requires Keycloak", e.getMessage());
            lastHealthStatus = false;
            lastHealthCheck = LocalDateTime.now();
            throw new RuntimeException("Failed to connect to Keycloak server. Application cannot start without Keycloak connectivity.", e);
        }
    }

    /**
     * Get a detailed health status with connection information
     */
    public KeecloakHealthStatus getDetailedHealthStatus() {
        boolean isAvailable = isKeycloakAvailable();
        
        return KeecloakHealthStatus.builder()
            .available(isAvailable)
            .serverUrl(keycloakServerUrl)
            .realmName(keycloakRealmName)
            .lastChecked(lastHealthCheck)
            .build();
    }

    /**
     * Health status data transfer object
     */
    public static class KeecloakHealthStatus {
        private boolean available;
        private String serverUrl;
        private String realmName;
        private LocalDateTime lastChecked;

        // Builder pattern for easy construction
        public static KeecloakHealthStatusBuilder builder() {
            return new KeecloakHealthStatusBuilder();
        }

        // Getters
        public boolean isAvailable() { return available; }
        public String getServerUrl() { return serverUrl; }
        public String getRealmName() { return realmName; }
        public LocalDateTime getLastChecked() { return lastChecked; }

        // Builder class
        public static class KeecloakHealthStatusBuilder {
            private boolean available;
            private String serverUrl;
            private String realmName;
            private LocalDateTime lastChecked;

            public KeecloakHealthStatusBuilder available(boolean available) {
                this.available = available;
                return this;
            }

            public KeecloakHealthStatusBuilder serverUrl(String serverUrl) {
                this.serverUrl = serverUrl;
                return this;
            }

            public KeecloakHealthStatusBuilder realmName(String realmName) {
                this.realmName = realmName;
                return this;
            }

            public KeecloakHealthStatusBuilder lastChecked(LocalDateTime lastChecked) {
                this.lastChecked = lastChecked;
                return this;
            }

            public KeecloakHealthStatus build() {
                KeecloakHealthStatus status = new KeecloakHealthStatus();
                status.available = this.available;
                status.serverUrl = this.serverUrl;
                status.realmName = this.realmName;
                status.lastChecked = this.lastChecked;
                return status;
            }
        }

        @Override
        public String toString() {
            return String.format("KeecloakHealthStatus{available=%s, serverUrl='%s', realmName='%s', lastChecked=%s}",
                available, serverUrl, realmName, lastChecked);
        }
    }
}