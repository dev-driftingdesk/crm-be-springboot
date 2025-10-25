package com.ceedpods.crmbuild.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Conditional Keycloak configuration based on server availability
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ConditionalKeycloakConfiguration {

    private final KeycloakConfigurationProperties keycloakConfig;

    /**
     * Create a minimal client registration for fallback mode
     * This prevents OAuth2 auto-configuration from failing during startup
     */
    @Bean
    @Primary
    @ConditionalOnProperty(
        name = "keycloak.fallback.enabled", 
        havingValue = "true",
        matchIfMissing = false
    )
    public ClientRegistrationRepository fallbackClientRegistrationRepository() {
        log.info("Creating fallback OAuth2 client registration repository");
        
        ClientRegistration registration = ClientRegistration
            .withRegistrationId("keycloak")
            .clientId(keycloakConfig.getClientId())
            .clientSecret(keycloakConfig.getClientSecret())
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("openid", "profile", "email")
            // Use placeholder URLs that won't be validated during startup
            .authorizationUri("http://placeholder/auth")
            .tokenUri("http://placeholder/token") 
            .userInfoUri("http://placeholder/userinfo")
            .jwkSetUri("http://placeholder/jwks")
            .userNameAttributeName("preferred_username")
            .clientName("Keycloak Fallback")
            .build();

        return new InMemoryClientRegistrationRepository(registration);
    }

    /**
     * Create a fallback JwtDecoder for when Keycloak is unavailable
     * This prevents SecurityConfig from failing to create the security filter chain
     */
    @Bean
    @Primary
    @ConditionalOnProperty(
        name = "keycloak.fallback.enabled", 
        havingValue = "true",
        matchIfMissing = false
    )
    public JwtDecoder fallbackJwtDecoder() {
        log.info("Creating fallback JWT decoder for development mode");
        
        return new JwtDecoder() {
            @Override
            public Jwt decode(String token) throws JwtException {
                log.debug("Fallback JWT decoder called - creating mock JWT for development");
                
                // Create a mock JWT for fallback mode
                Map<String, Object> headers = new HashMap<>();
                headers.put("alg", "none");
                headers.put("typ", "JWT");
                
                Map<String, Object> claims = new HashMap<>();
                claims.put("sub", "fallback-user");
                claims.put("preferred_username", "fallback-user");
                claims.put("email", "fallback@localhost");
                claims.put("iss", "fallback-issuer");
                claims.put("aud", keycloakConfig.getClientId());
                claims.put("exp", Instant.now().plusSeconds(3600).getEpochSecond());
                claims.put("iat", Instant.now().getEpochSecond());
                claims.put("auth_time", Instant.now().getEpochSecond());
                
                // Add basic roles for fallback user
                Map<String, Object> realmAccess = new HashMap<>();
                realmAccess.put("roles", java.util.List.of("USER", "FALLBACK_USER"));
                claims.put("realm_access", realmAccess);
                
                return new Jwt(
                    token,
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                    headers,
                    claims
                );
            }
        };
    }
}