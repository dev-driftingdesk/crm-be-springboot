package com.ceedpods.crmbuild.config;

import com.ceedpods.crmbuild.entity.user.User;
import com.ceedpods.crmbuild.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

/**
 * MongoDB configuration for auditing support
 */
@Slf4j
@Configuration
@EnableMongoAuditing
@RequiredArgsConstructor
public class MongoConfig {

    @Lazy
    private final UserRepository userRepository;

    /**
     * Provides current auditor for audit fields
     * Uses MongoDB User ID by looking up the user via Keycloak ID from JWT token
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                if (authentication != null && authentication.isAuthenticated() &&
                    authentication.getPrincipal() instanceof Jwt) {

                    Jwt jwt = (Jwt) authentication.getPrincipal();
                    // Get Keycloak ID from JWT
                    String keycloakId = jwt.getSubject();

                    if (keycloakId != null) {
                        try {
                            // Find user by Keycloak ID and return MongoDB ID
                            Optional<User> user = userRepository.findByKeycloakId(keycloakId);
                            if (user.isPresent() && !user.get().isDeleted()) {
                                log.debug("Auditor: Found user with ID {} for Keycloak ID {}", user.get().getId(), keycloakId);
                                return Optional.of(user.get().getId());
                            } else {
                                log.warn("Auditor: User not found or deleted for Keycloak ID: {}", keycloakId);
                            }
                        } catch (Exception e) {
                            log.error("Auditor: Error looking up user by Keycloak ID {}: {}", keycloakId, e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Auditor: Error in auditorProvider: {}", e.getMessage());
            }

            // For system operations or when user lookup fails
            log.debug("Auditor: Using 'system' as auditor");
            return Optional.of("system");
        };
    }
}