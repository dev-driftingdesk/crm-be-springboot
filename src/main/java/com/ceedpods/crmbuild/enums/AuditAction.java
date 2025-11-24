package com.ceedpods.crmbuild.enums;

/**
 * Enumeration of audit actions
 */
public enum AuditAction {
    LOGIN("User Login"),
    LOGOUT("User Logout"),
    CREATED("Entity Created"),
    UPDATED("Entity Updated"),
    DELETED("Entity Deleted"),
    FAILED_LOGIN("Failed Login Attempt"),
    PASSWORD_RESET("Password Reset"),
    PERMISSION_GRANTED("Permission Granted"),
    PERMISSION_REVOKED("Permission Revoked"),

    // Communication actions
    SEND_EMAIL("Email Sent"),
    SEND_SMS("SMS Sent"),
    SEND_WHATSAPP("WhatsApp Message Sent"),
    MAKE_CALL("Voice Call Initiated"),

    // Configuration actions
    SAVE_CREDENTIALS("Credentials Saved"),
    UPDATE_CREDENTIALS("Credentials Updated"),
    DELETE_CREDENTIALS("Credentials Deleted"),
    SAVE_CONFIG("Configuration Saved"),
    UPDATE_CONFIG("Configuration Updated");

    private final String description;

    AuditAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
