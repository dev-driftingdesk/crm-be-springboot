package com.ceedpods.crmbuild.enums;

/**
 * Enumeration of auditable entity types
 */
public enum AuditEntityType {
    PRODUCT("Product"),
    LEAD("Lead"),
    DEAL("Deal"),
    DEAL_NOTE("Deal Note"),
    LEAD_NOTE("Lead Note"),
    USER("User"),
    PERMISSION("Permission"),
    AUTHENTICATION("Authentication"),

    // Communication entity types
    EMAIL("Email"),
    SMS("SMS"),
    WHATSAPP("WhatsApp"),
    VOICE_CALL("Voice Call"),

    // Configuration entity types
    EMAIL_CREDENTIALS("Email Credentials"),
    SMS_CREDENTIALS("SMS Credentials"),
    WHATSAPP_CREDENTIALS("WhatsApp Credentials"),
    VOICE_CREDENTIALS("Voice Call Credentials"),
    SYSTEM_CONFIG("System Configuration");

    private final String description;

    AuditEntityType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
