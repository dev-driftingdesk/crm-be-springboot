package com.ceedpods.crmbuild.config;

import com.ceedpods.crmbuild.entity.user.User;
import com.ceedpods.crmbuild.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof Jwt) {

                Jwt jwt = (Jwt) authentication.getPrincipal();
                // Get Keycloak ID from JWT
                String keycloakId = jwt.getSubject();

                if (keycloakId != null) {
                    // Find user by Keycloak ID and return MongoDB ID
                    Optional<User> user = userRepository.findByKeycloakId(keycloakId);
                    if (user.isPresent()) {
                        return Optional.of(user.get().getId());
                    }
                }
            }

            // For system operations (like admin user initialization)
            return Optional.of("system");
        };
    }
}