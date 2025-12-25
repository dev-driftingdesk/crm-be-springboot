package com.ceedpods.crmbuild.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Factory class for creating mock Authentication objects for testing.
 */
public class MockAuthenticationFactory {

    // Test user constants
    public static final String ADMIN_KEYCLOAK_ID = "admin-keycloak-id-123";
    public static final String ADMIN_EMAIL = "admin@example.com";
    public static final String ADMIN_USERNAME = "admin";

    public static final String SALES_REP_KEYCLOAK_ID = "salesrep-keycloak-id-456";
    public static final String SALES_REP_EMAIL = "salesrep@example.com";
    public static final String SALES_REP_USERNAME = "salesrep";

    public static final String MANAGER_KEYCLOAK_ID = "manager-keycloak-id-789";
    public static final String MANAGER_EMAIL = "manager@example.com";
    public static final String MANAGER_USERNAME = "manager";

    /**
     * Creates a mock Authentication for an admin user.
     * Admin users have full access to all operations.
     */
    public static Authentication createAdminAuthentication() {
        return createMockAuthentication(
            ADMIN_KEYCLOAK_ID,
            ADMIN_EMAIL,
            ADMIN_USERNAME,
            "ROLE_ADMIN"
        );
    }

    /**
     * Creates a mock Authentication for a non-admin user (Sales Rep).
     * Sales reps have limited access to operations.
     */
    public static Authentication createNonAdminAuthentication() {
        return createMockAuthentication(
            SALES_REP_KEYCLOAK_ID,
            SALES_REP_EMAIL,
            SALES_REP_USERNAME,
            "ROLE_SALES_REP"
        );
    }

    /**
     * Creates a mock Authentication for a manager user.
     */
    public static Authentication createManagerAuthentication() {
        return createMockAuthentication(
            MANAGER_KEYCLOAK_ID,
            MANAGER_EMAIL,
            MANAGER_USERNAME,
            "ROLE_MANAGER"
        );
    }

    /**
     * Creates a mock unauthenticated Authentication.
     */
    public static Authentication createUnauthenticatedAuthentication() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        when(auth.getPrincipal()).thenReturn(null);
        when(auth.getName()).thenReturn(null);
        return auth;
    }

    /**
     * Creates a mock Authentication with custom parameters.
     *
     * @param keycloakId      The Keycloak subject ID
     * @param email           The user's email
     * @param preferredUsername The user's preferred username
     * @param role            The user's role
     * @return A mocked Authentication object
     */
    public static Authentication createMockAuthentication(
            String keycloakId,
            String email,
            String preferredUsername,
            String role) {

        Authentication auth = mock(Authentication.class);
        Jwt jwt = createMockJwt(keycloakId, email, preferredUsername);

        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(jwt);
        when(auth.getName()).thenReturn(keycloakId);

        // Set up authorities
        Collection<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority(role)
        );
        when(auth.getAuthorities()).thenAnswer(invocation -> authorities);

        return auth;
    }

    /**
     * Creates a mock JWT token with the specified claims.
     *
     * @param sub               The subject claim (Keycloak ID)
     * @param email             The email claim
     * @param preferredUsername The preferred_username claim
     * @return A mock JWT object
     */
    public static Jwt createMockJwt(String sub, String email, String preferredUsername) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");
        headers.put("typ", "JWT");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", sub);
        claims.put("email", email);
        claims.put("preferred_username", preferredUsername);
        claims.put("iat", Instant.now());
        claims.put("exp", Instant.now().plusSeconds(3600));

        return new Jwt(
            "mock-token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            headers,
            claims
        );
    }

    /**
     * Gets the admin user's Keycloak ID for test verification.
     */
    public static String getAdminKeycloakId() {
        return ADMIN_KEYCLOAK_ID;
    }

    /**
     * Gets the sales rep user's Keycloak ID for test verification.
     */
    public static String getSalesRepKeycloakId() {
        return SALES_REP_KEYCLOAK_ID;
    }

    /**
     * Gets the manager user's Keycloak ID for test verification.
     */
    public static String getManagerKeycloakId() {
        return MANAGER_KEYCLOAK_ID;
    }
}
