package com.ceedpods.crmbuild.enums;

public enum PermissionCategory {
    // Core business categories
    USER_MANAGEMENT("User Management"),
    LEAD_MANAGEMENT("Lead Management"),
    DEAL_MANAGEMENT("Deal Management"),
    CUSTOMER_MANAGEMENT("Customer Management"),
    PRODUCT_MANAGEMENT("Product Management"),
    SALES_MANAGEMENT("Sales Management"),
    
    // Communication & interaction
    COMMUNICATION("Communication & Calls"),
    CALENDAR_SCHEDULING("Calendar & Scheduling"),
    ACTION_ITEMS_TASKS("Action Items & Tasks"),
    
    // Lead processing
    LEAD_ROUTING_ASSIGNMENT("Lead Routing & Assignment"),
    SCORING_QUALIFICATION("Scoring & Qualification"),
    
    // Performance & quality
    ANALYTICS("Analytics & Reporting"),
    COACHING_QUALITY("Coaching & Quality"),
    GAMIFICATION_LEADERBOARDS("Gamification & Leaderboards"),
    
    // System features
    NOTIFICATIONS_ALERTS("Notifications & Alerts"),
    INTEGRATIONS("Integrations"),
    BILLING_SUBSCRIPTION("Billing & Subscription"),
    AUDIT_COMPLIANCE("Audit & Compliance"),
    SYSTEM_ADMINISTRATION("System Administration"),
    
    // Legacy categories (kept for backward compatibility)
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