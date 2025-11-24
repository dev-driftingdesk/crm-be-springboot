package com.ceedpods.crmbuild.enums;

import lombok.Getter;

@Getter
public enum Permission {
    // User Management Permissions
    USER_CREATE("USER_CREATE", "Create new users", PermissionCategory.USER_MANAGEMENT, true),
    USER_EDIT("USER_EDIT", "Edit user details", PermissionCategory.USER_MANAGEMENT, true),
    USER_DELETE("USER_DELETE", "Delete users", PermissionCategory.USER_MANAGEMENT, true),
    USER_ASSIGN("USER_ASSIGN", "Assign users to managers", PermissionCategory.USER_MANAGEMENT, true),
    USER_VIEW_ALL("USER_VIEW_ALL", "View all users", PermissionCategory.USER_MANAGEMENT, true),
    USER_VIEW_TEAM("USER_VIEW_TEAM", "View team users only", PermissionCategory.USER_MANAGEMENT, true),
    USER_MANAGE_PERMISSIONS("USER_MANAGE_PERMISSIONS", "Manage user permissions", PermissionCategory.USER_MANAGEMENT, false),
    
    // Product Management Permissions
    PRODUCT_CREATE("PRODUCT_CREATE", "Create products", PermissionCategory.PRODUCT_MANAGEMENT, false),
    PRODUCT_EDIT("PRODUCT_EDIT", "Edit products", PermissionCategory.PRODUCT_MANAGEMENT, false),
    PRODUCT_DELETE("PRODUCT_DELETE", "Delete products", PermissionCategory.PRODUCT_MANAGEMENT, false),
    PRODUCT_VIEW_ALL("PRODUCT_VIEW_ALL", "View all products", PermissionCategory.PRODUCT_MANAGEMENT, true),
    PRODUCT_PRICING("PRODUCT_PRICING", "Manage product pricing", PermissionCategory.PRODUCT_MANAGEMENT, true),
    
    // Analytics Permissions
    ANALYTICS_FULL("ANALYTICS_FULL", "Full analytics access", PermissionCategory.ANALYTICS, true),
    ANALYTICS_TEAM("ANALYTICS_TEAM", "Team analytics only", PermissionCategory.ANALYTICS, true),
    ANALYTICS_PERSONAL("ANALYTICS_PERSONAL", "Personal analytics only", PermissionCategory.ANALYTICS, true),
    ANALYTICS_REVENUE("ANALYTICS_REVENUE", "Revenue analytics", PermissionCategory.ANALYTICS, true),
    ANALYTICS_EXPORT("ANALYTICS_EXPORT", "Export analytics data", PermissionCategory.ANALYTICS, true),
    
    // Communication Permissions
    CALL_RECORDINGS_VIEW("CALL_RECORDINGS_VIEW", "View call recordings", PermissionCategory.COMMUNICATION, true),
    CALL_RECORDINGS_DOWNLOAD("CALL_RECORDINGS_DOWNLOAD", "Download call recordings", PermissionCategory.COMMUNICATION, true),
    CALL_RECORDINGS_DELETE("CALL_RECORDINGS_DELETE", "Delete call recordings", PermissionCategory.COMMUNICATION, true),
    COMMUNICATION_SEND_EMAIL("COMMUNICATION_SEND_EMAIL", "Send emails to customers", PermissionCategory.COMMUNICATION, true),
    COMMUNICATION_SEND_SMS("COMMUNICATION_SEND_SMS", "Send SMS to customers", PermissionCategory.COMMUNICATION, true),
    
    // Lead Management Permissions
    LEAD_CREATE("LEAD_CREATE", "Create leads", PermissionCategory.LEAD_MANAGEMENT, true),
    LEAD_EDIT("LEAD_EDIT", "Edit leads", PermissionCategory.LEAD_MANAGEMENT, true),
    LEAD_DELETE("LEAD_DELETE", "Delete leads", PermissionCategory.LEAD_MANAGEMENT, true),
    LEAD_VIEW_ALL("LEAD_VIEW_ALL", "View all leads", PermissionCategory.LEAD_MANAGEMENT, true),
    LEAD_VIEW_TEAM("LEAD_VIEW_TEAM", "View team leads only", PermissionCategory.LEAD_MANAGEMENT, true),
    LEAD_ASSIGN("LEAD_ASSIGN", "Assign leads to sales reps", PermissionCategory.LEAD_MANAGEMENT, true),
    LEAD_IMPORT("LEAD_IMPORT", "Import leads from external sources", PermissionCategory.LEAD_MANAGEMENT, true),
    LEAD_EXPORT("LEAD_EXPORT", "Export lead data", PermissionCategory.LEAD_MANAGEMENT, true),
    LEAD_VIEW_PERSONAL("LEAD_VIEW_PERSONAL", "View personal leads only", PermissionCategory.LEAD_MANAGEMENT, true),

    // Deal Management Permissions
    DEAL_CREATE("DEAL_CREATE", "Create deals", PermissionCategory.DEAL_MANAGEMENT, true),
    DEAL_EDIT("DEAL_EDIT", "Edit deals", PermissionCategory.DEAL_MANAGEMENT, true),
    DEAL_DELETE("DEAL_DELETE", "Delete deals", PermissionCategory.DEAL_MANAGEMENT, true),
    DEAL_VIEW_ALL("DEAL_VIEW_ALL", "View all deals", PermissionCategory.DEAL_MANAGEMENT, true),
    DEAL_VIEW_TEAM("DEAL_VIEW_TEAM", "View team deals only", PermissionCategory.DEAL_MANAGEMENT, true),
    DEAL_ASSIGN("DEAL_ASSIGN", "Assign deals to sales reps", PermissionCategory.DEAL_MANAGEMENT, true),
    DEAL_VIEW_PERSONAL("DEAL_VIEW_PERSONAL", "View personal deals only", PermissionCategory.DEAL_MANAGEMENT, true),

    // Customer Management Permissions
    CUSTOMER_CREATE("CUSTOMER_CREATE", "Create customers", PermissionCategory.CUSTOMER_MANAGEMENT, true),
    CUSTOMER_EDIT("CUSTOMER_EDIT", "Edit customer details", PermissionCategory.CUSTOMER_MANAGEMENT, true),
    CUSTOMER_DELETE("CUSTOMER_DELETE", "Delete customers", PermissionCategory.CUSTOMER_MANAGEMENT, true),
    CUSTOMER_VIEW_ALL("CUSTOMER_VIEW_ALL", "View all customers", PermissionCategory.CUSTOMER_MANAGEMENT, true),
    CUSTOMER_VIEW_TEAM("CUSTOMER_VIEW_TEAM", "View team customers only", PermissionCategory.CUSTOMER_MANAGEMENT, true),
    CUSTOMER_VIEW_PERSONAL("CUSTOMER_VIEW_PERSONAL", "View personal customers only", PermissionCategory.CUSTOMER_MANAGEMENT, true),
    
    // Sales Management Permissions
    SALES_CREATE_OPPORTUNITY("SALES_CREATE_OPPORTUNITY", "Create sales opportunities", PermissionCategory.SALES_MANAGEMENT, true),
    SALES_EDIT_OPPORTUNITY("SALES_EDIT_OPPORTUNITY", "Edit sales opportunities", PermissionCategory.SALES_MANAGEMENT, true),
    SALES_DELETE_OPPORTUNITY("SALES_DELETE_OPPORTUNITY", "Delete sales opportunities", PermissionCategory.SALES_MANAGEMENT, true),
    SALES_VIEW_ALL_OPPORTUNITIES("SALES_VIEW_ALL_OPPORTUNITIES", "View all sales opportunities", PermissionCategory.SALES_MANAGEMENT, true),
    SALES_MANAGE_PIPELINE("SALES_MANAGE_PIPELINE", "Manage sales pipeline", PermissionCategory.SALES_MANAGEMENT, true),
    
    // System Administration Permissions
    SYSTEM_CONFIGURATION("SYSTEM_CONFIGURATION", "System configuration", PermissionCategory.SYSTEM_ADMINISTRATION, false),
    SYSTEM_BACKUP("SYSTEM_BACKUP", "System backup operations", PermissionCategory.SYSTEM_ADMINISTRATION, false),
    SYSTEM_AUDIT_LOGS("SYSTEM_AUDIT_LOGS", "View system audit logs", PermissionCategory.SYSTEM_ADMINISTRATION, false),
    SYSTEM_INTEGRATION_MANAGEMENT("SYSTEM_INTEGRATION_MANAGEMENT", "Manage system integrations", PermissionCategory.SYSTEM_ADMINISTRATION, false);

    private final String code;
    private final String description;
    private final PermissionCategory category;
    private final boolean assignable;

    Permission(String code, String description, PermissionCategory category, boolean assignable) {
        this.code = code;
        this.description = description;
        this.category = category;
        this.assignable = assignable;
    }

    public static Permission fromCode(String code) {
        for (Permission permission : values()) {
            if (permission.getCode().equals(code)) {
                return permission;
            }
        }
        throw new IllegalArgumentException("Unknown permission code: " + code);
    }
}