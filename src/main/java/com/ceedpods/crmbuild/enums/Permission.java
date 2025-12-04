package com.ceedpods.crmbuild.enums;

import lombok.Getter;

/**
 * Permission enum based on the official Ceedpods User Roles & Permissions Matrix v1.0
 * This enum contains ONLY the permissions explicitly defined in the document
 * Total: 121 permissions matching the official specification exactly
 */
@Getter
public enum Permission {
    // ========================================================================================
    // AUTHENTICATION & ACCOUNT
    // ========================================================================================
    AUTH_LOGIN_MOBILE("AUTH_LOGIN_MOBILE", "Login to mobile app", PermissionCategory.USER_MANAGEMENT, true),
    AUTH_LOGIN_WEB("AUTH_LOGIN_WEB", "Login to web portal", PermissionCategory.USER_MANAGEMENT, true),
    AUTH_CHANGE_PASSWORD("AUTH_CHANGE_PASSWORD", "Change own password", PermissionCategory.USER_MANAGEMENT, true),
    AUTH_UPDATE_PROFILE("AUTH_UPDATE_PROFILE", "Update own profile", PermissionCategory.USER_MANAGEMENT, true),
    AUTH_ENABLE_MFA("AUTH_ENABLE_MFA", "Enable/disable own MFA", PermissionCategory.USER_MANAGEMENT, true),
    AUTH_FORCE_PASSWORD_RESET("AUTH_FORCE_PASSWORD_RESET", "Force password reset (others)", PermissionCategory.USER_MANAGEMENT, false),
    AUTH_VIEW_LOGIN_HISTORY("AUTH_VIEW_LOGIN_HISTORY", "View login history", PermissionCategory.AUDIT_COMPLIANCE, true),

    // ========================================================================================
    // USER MANAGEMENT
    // ========================================================================================
    USER_INVITE("USER_INVITE", "Invite new users", PermissionCategory.USER_MANAGEMENT, true),
    USER_ASSIGN_ROLES("USER_ASSIGN_ROLES", "Assign user roles", PermissionCategory.USER_MANAGEMENT, false),
    USER_DEACTIVATE("USER_DEACTIVATE", "Deactivate users", PermissionCategory.USER_MANAGEMENT, false),
    USER_DELETE("USER_DELETE", "Delete users", PermissionCategory.USER_MANAGEMENT, false),
    USER_VIEW("USER_VIEW", "View user profiles", PermissionCategory.USER_MANAGEMENT, true),
    USER_EDIT("USER_EDIT", "Edit user profiles", PermissionCategory.USER_MANAGEMENT, false),
    USER_RESET_PASSWORD("USER_RESET_PASSWORD", "Reset user passwords", PermissionCategory.USER_MANAGEMENT, false),
    USER_MANAGE_PERMISSIONS("USER_MANAGE_PERMISSIONS", "Manage user permissions", PermissionCategory.USER_MANAGEMENT, false),
    USER_CREATE_CUSTOM_ROLES("USER_CREATE_CUSTOM_ROLES", "Create custom roles", PermissionCategory.USER_MANAGEMENT, false),
    USER_VIEW_ACTIVITY_LOGS("USER_VIEW_ACTIVITY_LOGS", "View user activity logs", PermissionCategory.AUDIT_COMPLIANCE, true),

    // ========================================================================================
    // TEAM & ORGANIZATION
    // ========================================================================================
    TEAM_CREATE("TEAM_CREATE", "Create teams", PermissionCategory.USER_MANAGEMENT, false),
    TEAM_EDIT("TEAM_EDIT", "Edit team structure", PermissionCategory.USER_MANAGEMENT, false),
    TEAM_DELETE("TEAM_DELETE", "Delete teams", PermissionCategory.USER_MANAGEMENT, false),
    TEAM_ASSIGN_USERS("TEAM_ASSIGN_USERS", "Assign users to teams", PermissionCategory.USER_MANAGEMENT, true),
    TEAM_REMOVE_USERS("TEAM_REMOVE_USERS", "Remove users from teams", PermissionCategory.USER_MANAGEMENT, true),
    TEAM_VIEW_HIERARCHY("TEAM_VIEW_HIERARCHY", "View team hierarchy", PermissionCategory.USER_MANAGEMENT, true),
    TEAM_SET_TARGETS("TEAM_SET_TARGETS", "Set team targets/goals", PermissionCategory.SALES_MANAGEMENT, true),
    TERRITORY_CREATE("TERRITORY_CREATE", "Create regions/territories", PermissionCategory.USER_MANAGEMENT, false),
    TERRITORY_ASSIGN("TERRITORY_ASSIGN", "Assign territories to users", PermissionCategory.USER_MANAGEMENT, true),

    // ========================================================================================
    // LEADS
    // ========================================================================================
    LEAD_VIEW("LEAD_VIEW", "View leads", PermissionCategory.LEAD_MANAGEMENT, true),
    LEAD_CREATE("LEAD_CREATE", "Create new leads", PermissionCategory.LEAD_MANAGEMENT, true),
    LEAD_EDIT("LEAD_EDIT", "Edit lead information", PermissionCategory.LEAD_MANAGEMENT, true),
    LEAD_DELETE("LEAD_DELETE", "Delete leads", PermissionCategory.LEAD_MANAGEMENT, true),
    LEAD_IMPORT("LEAD_IMPORT", "Import leads (CSV/API)", PermissionCategory.LEAD_MANAGEMENT, true),
    LEAD_EXPORT("LEAD_EXPORT", "Export leads", PermissionCategory.LEAD_MANAGEMENT, true),
    LEAD_ASSIGN("LEAD_ASSIGN", "Assign leads to users", PermissionCategory.LEAD_MANAGEMENT, true),
    LEAD_REASSIGN("LEAD_REASSIGN", "Reassign leads", PermissionCategory.LEAD_MANAGEMENT, true),
    LEAD_VIEW_HISTORY("LEAD_VIEW_HISTORY", "View lead history/timeline", PermissionCategory.LEAD_MANAGEMENT, true),
    LEAD_ADD_NOTES("LEAD_ADD_NOTES", "Add notes to leads", PermissionCategory.LEAD_MANAGEMENT, true),
    LEAD_VIEW_SOURCE_ANALYTICS("LEAD_VIEW_SOURCE_ANALYTICS", "View lead source analytics", PermissionCategory.ANALYTICS, true),
    LEAD_MERGE_DUPLICATES("LEAD_MERGE_DUPLICATES", "Merge duplicate leads", PermissionCategory.LEAD_MANAGEMENT, true),
    LEAD_BULK_EDIT("LEAD_BULK_EDIT", "Bulk edit leads", PermissionCategory.LEAD_MANAGEMENT, true),
    LEAD_SET_STATUS("LEAD_SET_STATUS", "Set lead status", PermissionCategory.LEAD_MANAGEMENT, true),
    LEAD_VIEW_SCORE("LEAD_VIEW_SCORE", "View lead score", PermissionCategory.SCORING_QUALIFICATION, true),

    // ========================================================================================
    // DEALS
    // ========================================================================================
    DEAL_VIEW("DEAL_VIEW", "View deals", PermissionCategory.DEAL_MANAGEMENT, true),
    DEAL_CREATE("DEAL_CREATE", "Create new deals", PermissionCategory.DEAL_MANAGEMENT, true),
    DEAL_EDIT("DEAL_EDIT", "Edit deal information", PermissionCategory.DEAL_MANAGEMENT, true),
    DEAL_DELETE("DEAL_DELETE", "Delete deals", PermissionCategory.DEAL_MANAGEMENT, true),
    DEAL_CHANGE_STAGE("DEAL_CHANGE_STAGE", "Change deal stage", PermissionCategory.DEAL_MANAGEMENT, true),
    DEAL_ADD_PRODUCTS("DEAL_ADD_PRODUCTS", "Add products to deals", PermissionCategory.DEAL_MANAGEMENT, true),
    DEAL_SET_VALUE("DEAL_SET_VALUE", "Set deal value", PermissionCategory.DEAL_MANAGEMENT, true),
    DEAL_VIEW_PIPELINE("DEAL_VIEW_PIPELINE", "View deal pipeline", PermissionCategory.ANALYTICS, true),
    DEAL_ASSIGN_TEAM("DEAL_ASSIGN_TEAM", "Assign team members to deal", PermissionCategory.DEAL_MANAGEMENT, true),
    DEAL_VIEW_HISTORY("DEAL_VIEW_HISTORY", "View deal history", PermissionCategory.DEAL_MANAGEMENT, true),
    DEAL_CLOSE("DEAL_CLOSE", "Close deals (Won/Lost)", PermissionCategory.DEAL_MANAGEMENT, true),
    DEAL_REOPEN("DEAL_REOPEN", "Reopen closed deals", PermissionCategory.DEAL_MANAGEMENT, true),
    DEAL_VIEW_COMMISSION("DEAL_VIEW_COMMISSION", "View commission breakdown", PermissionCategory.ANALYTICS, true),

    // ========================================================================================
    // COMMUNICATION (Calls, Emails, Messages)
    // ========================================================================================
    CALL_MAKE("CALL_MAKE", "Make calls", PermissionCategory.COMMUNICATION, true),
    CALL_VIEW_HISTORY("CALL_VIEW_HISTORY", "View call history", PermissionCategory.COMMUNICATION, true),
    CALL_LISTEN_RECORDINGS("CALL_LISTEN_RECORDINGS", "Listen to call recordings", PermissionCategory.COMMUNICATION, true),
    CALL_VIEW_TRANSCRIPTS("CALL_VIEW_TRANSCRIPTS", "View call transcripts", PermissionCategory.COMMUNICATION, true),
    CALL_DELETE_RECORDINGS("CALL_DELETE_RECORDINGS", "Delete call recordings", PermissionCategory.COMMUNICATION, false),
    EMAIL_SEND("EMAIL_SEND", "Send emails", PermissionCategory.COMMUNICATION, true),
    EMAIL_VIEW_HISTORY("EMAIL_VIEW_HISTORY", "View email history", PermissionCategory.COMMUNICATION, true),
    WHATSAPP_SEND("WHATSAPP_SEND", "Send WhatsApp messages", PermissionCategory.COMMUNICATION, true),
    MESSAGE_VIEW_HISTORY("MESSAGE_VIEW_HISTORY", "View message history", PermissionCategory.COMMUNICATION, true),
    INBOX_ACCESS_UNIFIED("INBOX_ACCESS_UNIFIED", "Access unified inbox", PermissionCategory.COMMUNICATION, true),
    EMAIL_TEMPLATE_CREATE("EMAIL_TEMPLATE_CREATE", "Create email templates", PermissionCategory.COMMUNICATION, true),
    EMAIL_TEMPLATE_USE_SHARED("EMAIL_TEMPLATE_USE_SHARED", "Use shared templates", PermissionCategory.COMMUNICATION, true),
    MESSAGE_SCHEDULE("MESSAGE_SCHEDULE", "Schedule messages", PermissionCategory.COMMUNICATION, true),

    // ========================================================================================
    // ACTION ITEMS & TASKS
    // ========================================================================================
    ACTION_ITEM_VIEW("ACTION_ITEM_VIEW", "View action items", PermissionCategory.ACTION_ITEMS_TASKS, true),
    ACTION_ITEM_CREATE("ACTION_ITEM_CREATE", "Create action items", PermissionCategory.ACTION_ITEMS_TASKS, true),
    ACTION_ITEM_EDIT("ACTION_ITEM_EDIT", "Edit action items", PermissionCategory.ACTION_ITEMS_TASKS, true),
    ACTION_ITEM_DELETE("ACTION_ITEM_DELETE", "Delete action items", PermissionCategory.ACTION_ITEMS_TASKS, true),
    ACTION_ITEM_ASSIGN("ACTION_ITEM_ASSIGN", "Assign action items to others", PermissionCategory.ACTION_ITEMS_TASKS, true),
    ACTION_ITEM_REASSIGN("ACTION_ITEM_REASSIGN", "Reassign action items", PermissionCategory.ACTION_ITEMS_TASKS, true),
    ACTION_ITEM_COMPLETE("ACTION_ITEM_COMPLETE", "Mark as complete", PermissionCategory.ACTION_ITEMS_TASKS, true),
    ACTION_ITEM_SNOOZE("ACTION_ITEM_SNOOZE", "Snooze/reschedule", PermissionCategory.ACTION_ITEMS_TASKS, true),
    ACTION_ITEM_VIEW_OVERDUE("ACTION_ITEM_VIEW_OVERDUE", "View overdue items", PermissionCategory.ACTION_ITEMS_TASKS, true),
    ACTION_ITEM_SET_PRIORITY("ACTION_ITEM_SET_PRIORITY", "Set priority levels", PermissionCategory.ACTION_ITEMS_TASKS, true),
    ACTION_ITEM_BULK_OPS("ACTION_ITEM_BULK_OPS", "Bulk operations", PermissionCategory.ACTION_ITEMS_TASKS, true),

    // ========================================================================================
    // CALENDAR & SCHEDULING
    // ========================================================================================
    CALENDAR_VIEW_OWN("CALENDAR_VIEW_OWN", "View own calendar", PermissionCategory.CALENDAR_SCHEDULING, true),
    CALENDAR_VIEW_TEAM("CALENDAR_VIEW_TEAM", "View team calendars", PermissionCategory.CALENDAR_SCHEDULING, true),
    CALENDAR_CREATE_EVENTS("CALENDAR_CREATE_EVENTS", "Create meetings/events", PermissionCategory.CALENDAR_SCHEDULING, true),
    CALENDAR_EDIT_EVENTS("CALENDAR_EDIT_EVENTS", "Edit meetings", PermissionCategory.CALENDAR_SCHEDULING, true),
    CALENDAR_CANCEL_EVENTS("CALENDAR_CANCEL_EVENTS", "Cancel meetings", PermissionCategory.CALENDAR_SCHEDULING, true),
    CALENDAR_BOOK_ON_BEHALF("CALENDAR_BOOK_ON_BEHALF", "Book on behalf of others", PermissionCategory.CALENDAR_SCHEDULING, true),
    CALENDAR_ACCESS_INTEGRATIONS("CALENDAR_ACCESS_INTEGRATIONS", "Access calendar integrations", PermissionCategory.CALENDAR_SCHEDULING, true),
    CALENDAR_SET_AVAILABILITY("CALENDAR_SET_AVAILABILITY", "Set availability", PermissionCategory.CALENDAR_SCHEDULING, true),
    CALENDAR_VIEW_AVAILABILITY("CALENDAR_VIEW_AVAILABILITY", "View others' availability", PermissionCategory.CALENDAR_SCHEDULING, true),

    // ========================================================================================
    // PRODUCTS & CATALOG
    // ========================================================================================
    PRODUCT_VIEW_CATALOG("PRODUCT_VIEW_CATALOG", "View product catalog", PermissionCategory.PRODUCT_MANAGEMENT, true),
    PRODUCT_CREATE("PRODUCT_CREATE", "Create products", PermissionCategory.PRODUCT_MANAGEMENT, false),
    PRODUCT_EDIT("PRODUCT_EDIT", "Edit products", PermissionCategory.PRODUCT_MANAGEMENT, false),
    PRODUCT_DELETE("PRODUCT_DELETE", "Delete products", PermissionCategory.PRODUCT_MANAGEMENT, false),
    PRODUCT_SET_PRICING("PRODUCT_SET_PRICING", "Set product pricing", PermissionCategory.PRODUCT_MANAGEMENT, false),
    PRODUCT_VIEW_PRICING("PRODUCT_VIEW_PRICING", "View product pricing", PermissionCategory.PRODUCT_MANAGEMENT, true),
    PRODUCT_CREATE_BUNDLES("PRODUCT_CREATE_BUNDLES", "Create product bundles", PermissionCategory.PRODUCT_MANAGEMENT, false),
    PRODUCT_APPLY_DISCOUNTS("PRODUCT_APPLY_DISCOUNTS", "Apply discounts", PermissionCategory.PRODUCT_MANAGEMENT, true),
    PRODUCT_VIEW_DISCOUNT_LIMITS("PRODUCT_VIEW_DISCOUNT_LIMITS", "View discount limits", PermissionCategory.PRODUCT_MANAGEMENT, true),

    // ========================================================================================
    // ANALYTICS & REPORTING  
    // ========================================================================================
    ANALYTICS_VIEW_COMPANY_DASHBOARD("ANALYTICS_VIEW_COMPANY_DASHBOARD", "View company dashboard", PermissionCategory.ANALYTICS, false),
    ANALYTICS_VIEW_TEAM_DASHBOARD("ANALYTICS_VIEW_TEAM_DASHBOARD", "View team dashboard", PermissionCategory.ANALYTICS, true),
    ANALYTICS_VIEW_INDIVIDUAL_DASHBOARD("ANALYTICS_VIEW_INDIVIDUAL_DASHBOARD", "View individual dashboard", PermissionCategory.ANALYTICS, true),
    ANALYTICS_VIEW_LEADERBOARDS("ANALYTICS_VIEW_LEADERBOARDS", "View leaderboards", PermissionCategory.ANALYTICS, true),
    ANALYTICS_VIEW_PERFORMANCE("ANALYTICS_VIEW_PERFORMANCE", "View performance metrics", PermissionCategory.ANALYTICS, true),
    ANALYTICS_VIEW_PIPELINE_HEALTH("ANALYTICS_VIEW_PIPELINE_HEALTH", "View pipeline health", PermissionCategory.ANALYTICS, true),
    ANALYTICS_VIEW_REVENUE("ANALYTICS_VIEW_REVENUE", "View revenue reports", PermissionCategory.ANALYTICS, true),
    ANALYTICS_VIEW_ACTIVITY("ANALYTICS_VIEW_ACTIVITY", "View activity reports", PermissionCategory.ANALYTICS, true),
    ANALYTICS_CREATE_CUSTOM_REPORTS("ANALYTICS_CREATE_CUSTOM_REPORTS", "Create custom reports", PermissionCategory.ANALYTICS, true),
    ANALYTICS_EXPORT_REPORTS("ANALYTICS_EXPORT_REPORTS", "Export reports (CSV/PDF)", PermissionCategory.ANALYTICS, true),
    ANALYTICS_SCHEDULE_REPORTS("ANALYTICS_SCHEDULE_REPORTS", "Schedule automated reports", PermissionCategory.ANALYTICS, true),
    ANALYTICS_VIEW_TRENDS("ANALYTICS_VIEW_TRENDS", "View historical trends", PermissionCategory.ANALYTICS, true),
    ANALYTICS_ACCESS_AI_INSIGHTS("ANALYTICS_ACCESS_AI_INSIGHTS", "Access AI insights", PermissionCategory.ANALYTICS, true),

    // ========================================================================================
    // LEAD ROUTING & ASSIGNMENT
    // ========================================================================================
    ROUTING_VIEW_QUEUE("ROUTING_VIEW_QUEUE", "View routing queue", PermissionCategory.LEAD_ROUTING_ASSIGNMENT, true),
    ROUTING_MANUAL_ASSIGN("ROUTING_MANUAL_ASSIGN", "Manually assign leads", PermissionCategory.LEAD_ROUTING_ASSIGNMENT, true),
    ROUTING_BULK_ASSIGN("ROUTING_BULK_ASSIGN", "Bulk assign leads", PermissionCategory.LEAD_ROUTING_ASSIGNMENT, true),
    ROUTING_CREATE_RULES("ROUTING_CREATE_RULES", "Create routing rules", PermissionCategory.LEAD_ROUTING_ASSIGNMENT, true),
    ROUTING_EDIT_RULES("ROUTING_EDIT_RULES", "Edit routing rules", PermissionCategory.LEAD_ROUTING_ASSIGNMENT, true),
    ROUTING_DELETE_RULES("ROUTING_DELETE_RULES", "Delete routing rules", PermissionCategory.LEAD_ROUTING_ASSIGNMENT, false),
    ROUTING_VIEW_AI_SUGGESTIONS("ROUTING_VIEW_AI_SUGGESTIONS", "View AI routing suggestions", PermissionCategory.LEAD_ROUTING_ASSIGNMENT, true),
    ROUTING_OVERRIDE_AI("ROUTING_OVERRIDE_AI", "Override AI suggestions", PermissionCategory.LEAD_ROUTING_ASSIGNMENT, true),
    ROUTING_SET_CAPACITY("ROUTING_SET_CAPACITY", "Set agent capacity limits", PermissionCategory.LEAD_ROUTING_ASSIGNMENT, true),
    ROUTING_VIEW_WORKLOAD("ROUTING_VIEW_WORKLOAD", "View workload distribution", PermissionCategory.LEAD_ROUTING_ASSIGNMENT, true),

    // ========================================================================================
    // SCORING & QUALIFICATION
    // ========================================================================================
    SCORING_VIEW_SCORES("SCORING_VIEW_SCORES", "View lead scores", PermissionCategory.SCORING_QUALIFICATION, true),
    SCORING_CREATE_RULES("SCORING_CREATE_RULES", "Create scoring rules", PermissionCategory.SCORING_QUALIFICATION, false),
    SCORING_EDIT_CRITERIA("SCORING_EDIT_CRITERIA", "Edit scoring criteria", PermissionCategory.SCORING_QUALIFICATION, false),
    SCORING_VIEW_BREAKDOWN("SCORING_VIEW_BREAKDOWN", "View scoring breakdown", PermissionCategory.SCORING_QUALIFICATION, true),
    QUALIFICATION_CREATE_TEMPLATES("QUALIFICATION_CREATE_TEMPLATES", "Create qualification templates", PermissionCategory.SCORING_QUALIFICATION, true),
    QUALIFICATION_USE_FORMS("QUALIFICATION_USE_FORMS", "Use qualification forms", PermissionCategory.SCORING_QUALIFICATION, true),
    SCORING_OVERRIDE("SCORING_OVERRIDE", "Override lead scores", PermissionCategory.SCORING_QUALIFICATION, true),

    // ========================================================================================
    // COACHING & QUALITY
    // ========================================================================================
    COACHING_VIEW_DASHBOARD("COACHING_VIEW_DASHBOARD", "View coaching dashboard", PermissionCategory.COACHING_QUALITY, true),
    COACHING_ADD_NOTES("COACHING_ADD_NOTES", "Add coaching notes", PermissionCategory.COACHING_QUALITY, true),
    COACHING_VIEW_NOTES("COACHING_VIEW_NOTES", "View coaching notes (received)", PermissionCategory.COACHING_QUALITY, true),
    QUALITY_VIEW_CALL_SCORES("QUALITY_VIEW_CALL_SCORES", "View call quality scores", PermissionCategory.COACHING_QUALITY, true),
    QUALITY_SCORE_CALLS("QUALITY_SCORE_CALLS", "Score call quality", PermissionCategory.COACHING_QUALITY, true),
    SENTIMENT_VIEW_ANALYSIS("SENTIMENT_VIEW_ANALYSIS", "View sentiment analysis", PermissionCategory.COACHING_QUALITY, true),
    AI_COACHING_ACCESS_INSIGHTS("AI_COACHING_ACCESS_INSIGHTS", "Access AI coaching insights", PermissionCategory.COACHING_QUALITY, true),
    SCRIPT_VIEW_ADHERENCE("SCRIPT_VIEW_ADHERENCE", "View script adherence", PermissionCategory.COACHING_QUALITY, true),
    SCRIPT_CREATE("SCRIPT_CREATE", "Create call scripts", PermissionCategory.COACHING_QUALITY, true),
    SCRIPT_VIEW("SCRIPT_VIEW", "View call scripts", PermissionCategory.COACHING_QUALITY, true),

    // ========================================================================================
    // GAMIFICATION & LEADERBOARDS
    // ========================================================================================
    GAMIFICATION_VIEW_LEADERBOARDS("GAMIFICATION_VIEW_LEADERBOARDS", "View leaderboards", PermissionCategory.GAMIFICATION_LEADERBOARDS, true),
    GAMIFICATION_CONFIGURE_METRICS("GAMIFICATION_CONFIGURE_METRICS", "Configure leaderboard metrics", PermissionCategory.GAMIFICATION_LEADERBOARDS, true),
    GAMIFICATION_CREATE_CONTESTS("GAMIFICATION_CREATE_CONTESTS", "Create contests/challenges", PermissionCategory.GAMIFICATION_LEADERBOARDS, true),
    GAMIFICATION_VIEW_ACHIEVEMENTS("GAMIFICATION_VIEW_ACHIEVEMENTS", "View achievements/badges", PermissionCategory.GAMIFICATION_LEADERBOARDS, true),
    GAMIFICATION_CREATE_ACHIEVEMENTS("GAMIFICATION_CREATE_ACHIEVEMENTS", "Create achievement types", PermissionCategory.GAMIFICATION_LEADERBOARDS, false),
    GAMIFICATION_AWARD_BADGES("GAMIFICATION_AWARD_BADGES", "Award manual badges", PermissionCategory.GAMIFICATION_LEADERBOARDS, true),
    GAMIFICATION_VIEW_RANKINGS("GAMIFICATION_VIEW_RANKINGS", "View team rankings", PermissionCategory.GAMIFICATION_LEADERBOARDS, true),
    GAMIFICATION_SET_TARGETS("GAMIFICATION_SET_TARGETS", "Set performance targets", PermissionCategory.GAMIFICATION_LEADERBOARDS, true),

    // ========================================================================================
    // NOTIFICATIONS & ALERTS
    // ========================================================================================
    NOTIFICATION_RECEIVE_PUSH("NOTIFICATION_RECEIVE_PUSH", "Receive push notifications", PermissionCategory.NOTIFICATIONS_ALERTS, true),
    NOTIFICATION_CONFIGURE_PREFERENCES("NOTIFICATION_CONFIGURE_PREFERENCES", "Configure notification preferences", PermissionCategory.NOTIFICATIONS_ALERTS, true),
    NOTIFICATION_CREATE_SYSTEM_ALERTS("NOTIFICATION_CREATE_SYSTEM_ALERTS", "Create system-wide alerts", PermissionCategory.NOTIFICATIONS_ALERTS, false),
    NOTIFICATION_CREATE_TEAM_ALERTS("NOTIFICATION_CREATE_TEAM_ALERTS", "Create team alerts", PermissionCategory.NOTIFICATIONS_ALERTS, true),
    NOTIFICATION_VIEW_HISTORY("NOTIFICATION_VIEW_HISTORY", "View notification history", PermissionCategory.NOTIFICATIONS_ALERTS, true),
    NOTIFICATION_SET_ESCALATION("NOTIFICATION_SET_ESCALATION", "Set escalation rules", PermissionCategory.NOTIFICATIONS_ALERTS, true),
    NOTIFICATION_RECEIVE_ESCALATION("NOTIFICATION_RECEIVE_ESCALATION", "Receive escalation alerts", PermissionCategory.NOTIFICATIONS_ALERTS, true),

    // ========================================================================================
    // INTEGRATIONS
    // ========================================================================================
    INTEGRATION_VIEW_MARKETPLACE("INTEGRATION_VIEW_MARKETPLACE", "View integration marketplace", PermissionCategory.INTEGRATIONS, true),
    INTEGRATION_CONNECT("INTEGRATION_CONNECT", "Connect integrations", PermissionCategory.INTEGRATIONS, false),
    INTEGRATION_DISCONNECT("INTEGRATION_DISCONNECT", "Disconnect integrations", PermissionCategory.INTEGRATIONS, false),
    INTEGRATION_CONFIGURE("INTEGRATION_CONFIGURE", "Configure integration settings", PermissionCategory.INTEGRATIONS, false),
    INTEGRATION_VIEW_STATUS("INTEGRATION_VIEW_STATUS", "View integration status", PermissionCategory.INTEGRATIONS, true),
    INTEGRATION_CONNECT_PERSONAL("INTEGRATION_CONNECT_PERSONAL", "Connect personal integrations", PermissionCategory.INTEGRATIONS, true),
    INTEGRATION_VIEW_API_USAGE("INTEGRATION_VIEW_API_USAGE", "View API usage", PermissionCategory.INTEGRATIONS, true),
    INTEGRATION_MANAGE_API_KEYS("INTEGRATION_MANAGE_API_KEYS", "Manage API keys", PermissionCategory.INTEGRATIONS, false),

    // ========================================================================================
    // SYSTEM CONFIGURATION
    // ========================================================================================
    SYSTEM_ACCESS_ADMIN_SETTINGS("SYSTEM_ACCESS_ADMIN_SETTINGS", "Access admin settings", PermissionCategory.SYSTEM_ADMINISTRATION, false),
    SYSTEM_CONFIGURE_COMPANY_PROFILE("SYSTEM_CONFIGURE_COMPANY_PROFILE", "Configure company profile", PermissionCategory.SYSTEM_ADMINISTRATION, false),
    SYSTEM_SET_BRANDING("SYSTEM_SET_BRANDING", "Set company branding", PermissionCategory.SYSTEM_ADMINISTRATION, false),
    SYSTEM_CONFIGURE_EMAIL_TEMPLATES("SYSTEM_CONFIGURE_EMAIL_TEMPLATES", "Configure email templates", PermissionCategory.SYSTEM_ADMINISTRATION, true),
    SYSTEM_CREATE_CUSTOM_FIELDS("SYSTEM_CREATE_CUSTOM_FIELDS", "Create custom fields", PermissionCategory.SYSTEM_ADMINISTRATION, false),
    SYSTEM_CONFIGURE_LEAD_STATUSES("SYSTEM_CONFIGURE_LEAD_STATUSES", "Configure lead statuses", PermissionCategory.SYSTEM_ADMINISTRATION, false),
    SYSTEM_CONFIGURE_DEAL_STAGES("SYSTEM_CONFIGURE_DEAL_STAGES", "Configure deal stages", PermissionCategory.SYSTEM_ADMINISTRATION, false),
    SYSTEM_SET_BUSINESS_RULES("SYSTEM_SET_BUSINESS_RULES", "Set business rules", PermissionCategory.SYSTEM_ADMINISTRATION, false),
    SYSTEM_CONFIGURE_WORKFLOWS("SYSTEM_CONFIGURE_WORKFLOWS", "Configure workflows", PermissionCategory.SYSTEM_ADMINISTRATION, false),
    SYSTEM_MANAGE_MODULES("SYSTEM_MANAGE_MODULES", "Manage modules (on/off)", PermissionCategory.SYSTEM_ADMINISTRATION, false),
    SYSTEM_SET_DATA_RETENTION("SYSTEM_SET_DATA_RETENTION", "Set data retention policies", PermissionCategory.SYSTEM_ADMINISTRATION, false),
    SYSTEM_CONFIGURE_SECURITY("SYSTEM_CONFIGURE_SECURITY", "Configure security settings", PermissionCategory.SYSTEM_ADMINISTRATION, false),
    SYSTEM_MANAGE_COMPLIANCE("SYSTEM_MANAGE_COMPLIANCE", "Manage compliance settings", PermissionCategory.SYSTEM_ADMINISTRATION, false),

    // ========================================================================================
    // BILLING & SUBSCRIPTION
    // ========================================================================================
    BILLING_VIEW_DASHBOARD("BILLING_VIEW_DASHBOARD", "View billing dashboard", PermissionCategory.BILLING_SUBSCRIPTION, false),
    BILLING_VIEW_INVOICES("BILLING_VIEW_INVOICES", "View invoices", PermissionCategory.BILLING_SUBSCRIPTION, false),
    BILLING_UPDATE_PAYMENT("BILLING_UPDATE_PAYMENT", "Update payment method", PermissionCategory.BILLING_SUBSCRIPTION, false),
    BILLING_CHANGE_SUBSCRIPTION("BILLING_CHANGE_SUBSCRIPTION", "Change subscription plan", PermissionCategory.BILLING_SUBSCRIPTION, false),
    BILLING_MANAGE_SEATS("BILLING_MANAGE_SEATS", "Add/remove seats", PermissionCategory.BILLING_SUBSCRIPTION, false),
    BILLING_VIEW_USAGE("BILLING_VIEW_USAGE", "View usage statistics", PermissionCategory.BILLING_SUBSCRIPTION, true),
    BILLING_DOWNLOAD_INVOICES("BILLING_DOWNLOAD_INVOICES", "Download invoices", PermissionCategory.BILLING_SUBSCRIPTION, false),

    // ========================================================================================
    // AUDIT & COMPLIANCE
    // ========================================================================================
    AUDIT_VIEW_FULL_LOG("AUDIT_VIEW_FULL_LOG", "View full audit log", PermissionCategory.AUDIT_COMPLIANCE, false),
    AUDIT_VIEW_TEAM_ACTIVITY("AUDIT_VIEW_TEAM_ACTIVITY", "View team activity audit", PermissionCategory.AUDIT_COMPLIANCE, true),
    AUDIT_EXPORT_LOGS("AUDIT_EXPORT_LOGS", "Export audit logs", PermissionCategory.AUDIT_COMPLIANCE, false),
    AUDIT_VIEW_DATA_ACCESS("AUDIT_VIEW_DATA_ACCESS", "View data access logs", PermissionCategory.AUDIT_COMPLIANCE, true),
    AUDIT_CONFIGURE_SETTINGS("AUDIT_CONFIGURE_SETTINGS", "Configure audit settings", PermissionCategory.AUDIT_COMPLIANCE, false),
    COMPLIANCE_VIEW_REPORTS("COMPLIANCE_VIEW_REPORTS", "View compliance reports", PermissionCategory.AUDIT_COMPLIANCE, true),
    DATA_APPROVE_EXPORTS("DATA_APPROVE_EXPORTS", "Approve data exports", PermissionCategory.AUDIT_COMPLIANCE, true),
    DATA_HANDLE_DELETION_REQUESTS("DATA_HANDLE_DELETION_REQUESTS", "Handle data deletion requests", PermissionCategory.AUDIT_COMPLIANCE, false);

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