package com.ceedpods.crmbuild.enums;

public enum UserRole {
    ADMIN("Admin"),
    MANAGER("Manager"),
    SALES_REP("Sales Representative");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}