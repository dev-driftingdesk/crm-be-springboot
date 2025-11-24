package com.ceedpods.crmbuild.enums;

public enum UserRole {
    USER("User"),
    ADMIN("Admin"),
    MANAGER("Manager"),
    SALES_EXECUTIVE("Sales Executive");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}