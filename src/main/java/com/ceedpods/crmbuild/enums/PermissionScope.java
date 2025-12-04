package com.ceedpods.crmbuild.enums;

import lombok.Getter;

/**
 * Defines the scope of data visibility for permissions
 */
@Getter
public enum PermissionScope {
    ALL("all", "Full access to all data across company"),
    TEAM("team", "Access to team data only"),
    OWN("own", "Access to own assigned data only"), 
    NONE("none", "No access"),
    AS_PERMITTED("as_permitted", "Access as configured by Admin");

    private final String code;
    private final String description;

    PermissionScope(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static PermissionScope fromCode(String code) {
        for (PermissionScope scope : values()) {
            if (scope.getCode().equals(code)) {
                return scope;
            }
        }
        throw new IllegalArgumentException("Unknown permission scope: " + code);
    }
    
    /**
     * Check if this scope includes the other scope
     * ALL > TEAM > OWN > NONE
     */
    public boolean includes(PermissionScope other) {
        switch (this) {
            case ALL:
                return true;
            case TEAM:
                return other == TEAM || other == OWN;
            case OWN:
                return other == OWN;
            case AS_PERMITTED:
                return false; // AS_PERMITTED needs explicit configuration
            case NONE:
            default:
                return false;
        }
    }
}