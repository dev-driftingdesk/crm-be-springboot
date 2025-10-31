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

        // Realm configuration (configurable via properties)
        public static final String REALM_DISPLAY_NAME = "CRM Admin Realm";

        // API endpoints (relative paths)
        public static final String ADMIN_REALMS_PATH = "/admin/realms/";
        public static final String TOKEN_ENDPOINT = "/protocol/openid-connect/token";
        public static final String LOGOUT_ENDPOINT = "/protocol/openid-connect/logout";

        // Grant types
        public static final String GRANT_TYPE_PASSWORD = "password";
        public static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";

        // Token types
        public static final String TOKEN_TYPE_BEARER = "Bearer";

        // Credential types
        public static final String CREDENTIAL_TYPE_PASSWORD = "password";

        // Role names
        public static final String ROLE_ADMIN = "ADMIN";
        public static final String ROLE_MANAGER = "MANAGER";
        public static final String ROLE_SALES_REP = "SALES_REP";
        public static final String ROLE_USER = "USER";

        // Role descriptions
        public static final String ROLE_ADMIN_DESC = "Administrator role with full access";
        public static final String ROLE_MANAGER_DESC = "Manager role with team management access";
        public static final String ROLE_SALES_REP_DESC = "Sales Representative role with sales access";
        public static final String ROLE_USER_DESC = "Regular user role with limited access";

        // Helper methods to build dynamic paths
        public static String getRealmPath(String realmName) {
            return "/realms/" + realmName;
        }

        public static String getMasterRealmPath() {
            return "/realms/master";
        }

        public static String getAdminUsersPath(String realmName) {
            return "/admin/realms/" + realmName + "/users";
        }
    }

    // ==================== Application User Constants ====================
    public static final class DefaultUsers {
        private DefaultUsers() {
            throw new IllegalStateException("Constants class");
        }

        // Default admin user credentials
        public static final String ADMIN_EMAIL = "admin@example.com";
        public static final String ADMIN_PASSWORD = "AdminPass123";
        public static final String ADMIN_FIRST_NAME = "Admin";
        public static final String ADMIN_LAST_NAME = "User";
    }

    // ==================== Security Constants ====================
    public static final class Security {
        private Security() {
            throw new IllegalStateException("Constants class");
        }

        // Public endpoints
        public static final String[] PUBLIC_POST_ENDPOINTS = {
            "/auth/login",
            "/auth/refresh",
            "/auth/logout",
            "/auth/admin/create-admin",
            "/invitations/complete"
        };

        public static final String[] PUBLIC_GET_ENDPOINTS = {
            "/actuator/**",
            "/invitations/validate/*"
        };

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
        public static final String COLLECTION_PERMISSIONS = "permissions";
        public static final String COLLECTION_USER_PERMISSIONS = "user_permissions";
        public static final String COLLECTION_USER_RELATIONSHIPS = "user_relationships";
        public static final String COLLECTION_USER_INVITATIONS = "user_invitations";
        public static final String COLLECTION_AUDIT_LOGS = "audit_logs";
        public static final String COLLECTION_PRODUCTS = "products";
        public static final String COLLECTION_LEADS = "leads";
        public static final String COLLECTION_DEALS = "deals";
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
        public static final String PASSWORD_MISMATCH = "Password and confirm password do not match";
        public static final String ADMIN_ALREADY_EXISTS = "An admin user already exists in the system";
        public static final String UNAUTHORIZED_REGISTRATION = "Only admin users can register new users";
        public static final String USER_CREATED_SUCCESS = "User created successfully";
    }
}
