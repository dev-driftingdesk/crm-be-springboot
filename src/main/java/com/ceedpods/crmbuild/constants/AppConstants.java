package com.ceedpods.crmbuild.constants;

public final class AppConstants {

    private AppConstants() {
        throw new IllegalStateException("Constants class");
    }

    // ==================== Keycloak Constants ====================
    public static final class Keycloak {
        private Keycloak() {
            throw new IllegalStateException("Constants class");
        }

        // Admin credentials
        public static final String ADMIN_CLIENT_ID = "admin-cli";
        public static final String ADMIN_USERNAME = "admin";
        public static final String ADMIN_PASSWORD = "admin";

        // Realm paths
        public static final String REALM_LAHIRU = "/realms/lahiru";
        public static final String REALM_MASTER = "/realms/master";

        // API endpoints
        public static final String ADMIN_USERS_PATH = "/admin/realms/lahiru/users";
        public static final String TOKEN_ENDPOINT = "/protocol/openid-connect/token";
        public static final String LOGOUT_ENDPOINT = "/protocol/openid-connect/logout";

        // Grant types
        public static final String GRANT_TYPE_PASSWORD = "password";
        public static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";

        // Token types
        public static final String TOKEN_TYPE_BEARER = "Bearer";

        // Credential types
        public static final String CREDENTIAL_TYPE_PASSWORD = "password";

        // Claims
        public static final String CLAIM_PREFERRED_USERNAME = "preferred_username";
        public static final String CLAIM_REALM_ACCESS_ROLES = "realm_access.roles";
    }

    // ==================== Security Constants ====================
    public static final class Security {
        private Security() {
            throw new IllegalStateException("Constants class");
        }

        // Public endpoints
        public static final String[] PUBLIC_POST_ENDPOINTS = {
            "/auth/register",
            "/auth/login",
            "/auth/refresh"
        };

        public static final String[] PUBLIC_GET_ENDPOINTS = {
            "/actuator/**"
        };

        // Authority settings
        public static final String AUTHORITY_PREFIX = "ROLE_";

        // CORS settings
        public static final String[] ALLOWED_ORIGINS = {
            "http://localhost:3000",
            "http://localhost:4200"
        };

        public static final String[] ALLOWED_METHODS = {
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        };

        public static final String[] ALLOWED_HEADERS = {"*"};
    }

    // ==================== MongoDB Constants ====================
    public static final class MongoDB {
        private MongoDB() {
            throw new IllegalStateException("Constants class");
        }

        // Collection names
        public static final String COLLECTION_USERS = "users";
        public static final String COLLECTION_LEADS = "leads";
        public static final String COLLECTION_ACTIVITIES = "activities";
    }

    // ==================== HTTP Constants ====================
    public static final class Http {
        private Http() {
            throw new IllegalStateException("Constants class");
        }

        // Header names
        public static final String HEADER_AUTHORIZATION = "Authorization";
        public static final String HEADER_CONTENT_TYPE = "Content-Type";

        // Content types
        public static final String CONTENT_TYPE_JSON = "application/json";
        public static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";
    }

    // ==================== Response Messages ====================
    public static final class Messages {
        private Messages() {
            throw new IllegalStateException("Constants class");
        }

        // Success messages
        public static final String USER_REGISTERED_SUCCESS = "User registered successfully";
        public static final String USER_LOGGED_IN_SUCCESS = "User logged in successfully";
        public static final String USER_LOGGED_OUT_SUCCESS = "Logged out successfully";

        // Error messages
        public static final String USERNAME_ALREADY_EXISTS = "Username already exists";
        public static final String EMAIL_ALREADY_EXISTS = "Email already exists";
        public static final String USER_NOT_FOUND = "User not found";
        public static final String REGISTRATION_FAILED = "Registration failed";
        public static final String LOGIN_FAILED = "Login failed";
        public static final String LOGOUT_FAILED = "Logout failed";
        public static final String TOKEN_REFRESH_FAILED = "Token refresh failed";
        public static final String KEYCLOAK_REGISTRATION_FAILED = "Failed to register user in Keycloak";
        public static final String KEYCLOAK_TOKEN_FAILED = "Failed to get token from Keycloak";
        public static final String KEYCLOAK_ADMIN_TOKEN_FAILED = "Failed to get admin token";
    }

    // ==================== API Constants ====================
    public static final class Api {
        private Api() {
            throw new IllegalStateException("Constants class");
        }

        // Base paths
        public static final String BASE_PATH = "/api/v1";
        public static final String AUTH_PATH = "/auth";

        // Endpoints
        public static final String REGISTER_ENDPOINT = "/register";
        public static final String LOGIN_ENDPOINT = "/login";
        public static final String REFRESH_ENDPOINT = "/refresh";
        public static final String LOGOUT_ENDPOINT = "/logout";
        public static final String ME_ENDPOINT = "/me";
    }
}
