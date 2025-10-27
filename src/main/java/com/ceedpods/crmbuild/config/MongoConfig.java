package com.ceedpods.crmbuild.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
public class MongoConfig {

    /**
     * Provides current auditor for audit fields
     * Uses email from JWT token if authenticated, otherwise "system"
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() && 
                authentication.getPrincipal() instanceof Jwt) {
                
                Jwt jwt = (Jwt) authentication.getPrincipal();
                String email = jwt.getClaimAsString("email");
                return Optional.ofNullable(email);
            }
            
            // For system operations (like admin user initialization)
            return Optional.of("system");
        };
    }
}