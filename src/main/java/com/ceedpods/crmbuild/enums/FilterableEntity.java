package com.ceedpods.crmbuild.enums;

/**
 * Enum representing all entities that support global filtering.
 * Add new entities here as the system grows.
 */
public enum FilterableEntity {
    LEAD("leads", "Lead"),
    DEAL("deals", "Deal"),
    PRODUCT("products", "Product"),
    USER("users", "User"),
    LEAD_NOTE("lead_notes", "Lead Note"),
    DEAL_NOTE("deal_notes", "Deal Note"),
    MESSAGE("messages", "Message"),
    AUDIT_LOG("audit_logs", "Audit Log");

    private final String collectionName;
    private final String displayName;

    FilterableEntity(String collectionName, String displayName) {
        this.collectionName = collectionName;
        this.displayName = displayName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
