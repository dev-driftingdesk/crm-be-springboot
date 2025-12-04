package com.ceedpods.crmbuild.enums;

public enum UserRole {
    ADMIN("Admin"),
    MANAGER("Manager"),
    SALES_REP("Sales Rep"),
    VIEWER("Viewer");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if this role can manage users of the target role
     */
    public boolean canManage(UserRole targetRole) {
        switch (this) {
            case ADMIN:
                return true; // Admin can manage all roles
            case MANAGER:
                return targetRole == SALES_REP || targetRole == VIEWER; // Manager can manage Sales Rep and Viewer only
            default:
                return false; // Sales Rep and Viewer cannot manage others
        }
    }

    /**
     * Get the hierarchical level for role comparison
     */
    public int getHierarchyLevel() {
        switch (this) {
            case ADMIN: return 4;
            case MANAGER: return 3;
            case SALES_REP: return 2;
            case VIEWER: return 1;
            default: return 0;
        }
    }

    /**
     * Check if this role inherits permissions from the other role
     */
    public boolean inheritsFrom(UserRole otherRole) {
        return this.getHierarchyLevel() > otherRole.getHierarchyLevel();
    }
}