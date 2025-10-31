package com.ceedpods.crmbuild.enums;

public enum PermissionCategory {
    USER_MANAGEMENT("User Management"),
    PRODUCT_MANAGEMENT("Product Management"),
    ANALYTICS("Analytics & Reporting"),
    COMMUNICATION("Communication & Calls"),
    LEAD_MANAGEMENT("Lead Management"),
    DEAL_MANAGEMENT("Deal Management"),
    SYSTEM_ADMINISTRATION("System Administration"),
    CUSTOMER_MANAGEMENT("Customer Management"),
    SALES_MANAGEMENT("Sales Management"),
    MARKETING("Marketing"),
    SUPPORT("Support & Service");

    private final String displayName;

    PermissionCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}