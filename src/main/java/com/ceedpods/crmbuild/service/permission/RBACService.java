package com.ceedpods.crmbuild.service.permission;

import com.ceedpods.crmbuild.entity.UserRolePermission;
import com.ceedpods.crmbuild.entity.user.User;
import com.ceedpods.crmbuild.enums.*;
import com.ceedpods.crmbuild.repository.UserRolePermissionRepository;
import com.ceedpods.crmbuild.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Role-Based Access Control Service implementing the new RBAC requirements
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RBACService {
    
    private final UserRolePermissionRepository rolePermissionRepository;
    private final UserService userService;
    
    @PostConstruct
    @Transactional
    public void initializeRolePermissions() {
        log.info("Initializing RBAC role permissions matrix...");
        
        // Clear existing role permissions to rebuild from scratch
        rolePermissionRepository.deleteAll();
        
        // Initialize permissions for each role
        initializeAdminPermissions();
        initializeManagerPermissions();
        initializeSalesRepPermissions();
        initializeViewerPermissions();
        
        log.info("RBAC role permissions matrix initialization completed");
    }
    
    /**
     * Initialize Admin role permissions (full access)
     */
    private void initializeAdminPermissions() {
        List<UserRolePermission> adminPermissions = new ArrayList<>();
        
        // Admin gets ALL permissions with ALL scope
        for (Permission permission : Permission.values()) {
            adminPermissions.add(UserRolePermission.create(UserRole.ADMIN, permission, PermissionScope.ALL));
        }
        
        rolePermissionRepository.saveAll(adminPermissions);
        log.debug("Initialized {} permissions for ADMIN role", adminPermissions.size());
    }
    
    /**
     * Initialize Manager role permissions according to RBAC matrix
     */
    private void initializeManagerPermissions() {
        List<UserRolePermission> managerPermissions = new ArrayList<>();
        
        // Authentication & Account - Basic access
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.AUTH_LOGIN_MOBILE, PermissionScope.ALL));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.AUTH_LOGIN_WEB, PermissionScope.ALL));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.AUTH_CHANGE_PASSWORD, PermissionScope.ALL));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.AUTH_UPDATE_PROFILE, PermissionScope.ALL));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.AUTH_ENABLE_MFA, PermissionScope.ALL));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.AUTH_VIEW_LOGIN_HISTORY, PermissionScope.TEAM));
        
        // User Management - Team level
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.USER_INVITE, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.USER_VIEW, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.TEAM_ASSIGN_USERS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.TEAM_REMOVE_USERS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.TEAM_VIEW_HIERARCHY, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.TEAM_SET_TARGETS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.TERRITORY_ASSIGN, PermissionScope.TEAM));
        
        // Leads - Team scope
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.LEAD_VIEW, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.LEAD_CREATE, PermissionScope.ALL));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.LEAD_EDIT, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.LEAD_DELETE, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.LEAD_IMPORT, PermissionScope.ALL));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.LEAD_EXPORT, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.LEAD_ASSIGN, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.LEAD_REASSIGN, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.LEAD_VIEW_HISTORY, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.LEAD_ADD_NOTES, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.LEAD_VIEW_SOURCE_ANALYTICS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.LEAD_MERGE_DUPLICATES, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.LEAD_BULK_EDIT, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.LEAD_SET_STATUS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.LEAD_VIEW_SCORE, PermissionScope.TEAM));
        
        // Deals - Team scope
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.DEAL_VIEW, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.DEAL_CREATE, PermissionScope.ALL));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.DEAL_EDIT, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.DEAL_DELETE, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.DEAL_CHANGE_STAGE, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.DEAL_ADD_PRODUCTS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.DEAL_SET_VALUE, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.DEAL_VIEW_PIPELINE, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.DEAL_ASSIGN_TEAM, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.DEAL_VIEW_HISTORY, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.DEAL_CLOSE, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.DEAL_REOPEN, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.DEAL_VIEW_COMMISSION, PermissionScope.TEAM));
        
        // Communication - Team scope
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.CALL_MAKE, PermissionScope.ALL));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.CALL_VIEW_HISTORY, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.CALL_LISTEN_RECORDINGS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.CALL_VIEW_TRANSCRIPTS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.EMAIL_SEND, PermissionScope.ALL));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.EMAIL_VIEW_HISTORY, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.WHATSAPP_SEND, PermissionScope.ALL));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.MESSAGE_VIEW_HISTORY, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.INBOX_ACCESS_UNIFIED, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.EMAIL_TEMPLATE_CREATE, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.EMAIL_TEMPLATE_USE_SHARED, PermissionScope.ALL));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.MESSAGE_SCHEDULE, PermissionScope.ALL));
        
        // Action Items - Team scope
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ACTION_ITEM_VIEW, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ACTION_ITEM_CREATE, PermissionScope.ALL));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ACTION_ITEM_EDIT, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ACTION_ITEM_DELETE, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ACTION_ITEM_ASSIGN, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ACTION_ITEM_REASSIGN, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ACTION_ITEM_COMPLETE, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ACTION_ITEM_SNOOZE, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ACTION_ITEM_VIEW_OVERDUE, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ACTION_ITEM_SET_PRIORITY, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ACTION_ITEM_BULK_OPS, PermissionScope.TEAM));
        
        // Calendar - Team scope
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.CALENDAR_VIEW_OWN, PermissionScope.OWN));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.CALENDAR_VIEW_TEAM, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.CALENDAR_CREATE_EVENTS, PermissionScope.ALL));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.CALENDAR_EDIT_EVENTS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.CALENDAR_CANCEL_EVENTS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.CALENDAR_BOOK_ON_BEHALF, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.CALENDAR_ACCESS_INTEGRATIONS, PermissionScope.OWN));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.CALENDAR_SET_AVAILABILITY, PermissionScope.OWN));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.CALENDAR_VIEW_AVAILABILITY, PermissionScope.TEAM));
        
        // Products - View only
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.PRODUCT_VIEW_CATALOG, PermissionScope.ALL));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.PRODUCT_VIEW_PRICING, PermissionScope.ALL));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.PRODUCT_APPLY_DISCOUNTS, PermissionScope.ALL));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.PRODUCT_VIEW_DISCOUNT_LIMITS, PermissionScope.ALL));
        
        // Analytics - Team scope
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ANALYTICS_VIEW_TEAM_DASHBOARD, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ANALYTICS_VIEW_INDIVIDUAL_DASHBOARD, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ANALYTICS_VIEW_LEADERBOARDS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ANALYTICS_VIEW_PERFORMANCE, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ANALYTICS_VIEW_PIPELINE_HEALTH, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ANALYTICS_VIEW_REVENUE, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ANALYTICS_VIEW_ACTIVITY, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ANALYTICS_CREATE_CUSTOM_REPORTS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ANALYTICS_EXPORT_REPORTS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ANALYTICS_SCHEDULE_REPORTS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ANALYTICS_VIEW_TRENDS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ANALYTICS_ACCESS_AI_INSIGHTS, PermissionScope.TEAM));
        
        // Lead Routing - Team scope
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ROUTING_VIEW_QUEUE, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ROUTING_MANUAL_ASSIGN, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ROUTING_BULK_ASSIGN, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ROUTING_CREATE_RULES, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ROUTING_EDIT_RULES, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ROUTING_VIEW_AI_SUGGESTIONS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ROUTING_OVERRIDE_AI, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ROUTING_SET_CAPACITY, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.ROUTING_VIEW_WORKLOAD, PermissionScope.TEAM));
        
        // Scoring - Team scope (limited configuration)
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.SCORING_VIEW_SCORES, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.SCORING_VIEW_BREAKDOWN, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.QUALIFICATION_CREATE_TEMPLATES, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.QUALIFICATION_USE_FORMS, PermissionScope.ALL));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.SCORING_OVERRIDE, PermissionScope.TEAM));
        
        // Coaching & Quality - Team scope
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.COACHING_VIEW_DASHBOARD, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.COACHING_ADD_NOTES, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.COACHING_VIEW_NOTES, PermissionScope.OWN));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.QUALITY_VIEW_CALL_SCORES, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.QUALITY_SCORE_CALLS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.SENTIMENT_VIEW_ANALYSIS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.AI_COACHING_ACCESS_INSIGHTS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.SCRIPT_VIEW_ADHERENCE, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.SCRIPT_CREATE, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.SCRIPT_VIEW, PermissionScope.ALL));
        
        // Gamification - Team scope
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.GAMIFICATION_VIEW_LEADERBOARDS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.GAMIFICATION_CONFIGURE_METRICS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.GAMIFICATION_CREATE_CONTESTS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.GAMIFICATION_VIEW_ACHIEVEMENTS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.GAMIFICATION_AWARD_BADGES, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.GAMIFICATION_VIEW_RANKINGS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.GAMIFICATION_SET_TARGETS, PermissionScope.TEAM));
        
        // Notifications - Team scope
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.NOTIFICATION_RECEIVE_PUSH, PermissionScope.OWN));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.NOTIFICATION_CONFIGURE_PREFERENCES, PermissionScope.OWN));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.NOTIFICATION_CREATE_TEAM_ALERTS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.NOTIFICATION_VIEW_HISTORY, PermissionScope.OWN));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.NOTIFICATION_SET_ESCALATION, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.NOTIFICATION_RECEIVE_ESCALATION, PermissionScope.TEAM));
        
        // Integrations - Limited
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.INTEGRATION_VIEW_MARKETPLACE, PermissionScope.ALL));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.INTEGRATION_VIEW_STATUS, PermissionScope.ALL));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.INTEGRATION_CONNECT_PERSONAL, PermissionScope.OWN));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.INTEGRATION_VIEW_API_USAGE, PermissionScope.TEAM));
        
        // System Configuration - Limited team settings only
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.SYSTEM_CONFIGURE_EMAIL_TEMPLATES, PermissionScope.TEAM));
        
        // Billing - Limited view
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.BILLING_VIEW_USAGE, PermissionScope.TEAM));
        
        // Audit - Team scope
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.AUDIT_VIEW_TEAM_ACTIVITY, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.AUDIT_VIEW_DATA_ACCESS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.COMPLIANCE_VIEW_REPORTS, PermissionScope.TEAM));
        managerPermissions.add(UserRolePermission.create(UserRole.MANAGER, Permission.DATA_APPROVE_EXPORTS, PermissionScope.TEAM));
        
        rolePermissionRepository.saveAll(managerPermissions);
        log.debug("Initialized {} permissions for MANAGER role", managerPermissions.size());
    }
    
    /**
     * Initialize Sales Rep role permissions according to RBAC matrix
     */
    private void initializeSalesRepPermissions() {
        List<UserRolePermission> salesRepPermissions = new ArrayList<>();
        
        // Authentication & Account - Basic access
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.AUTH_LOGIN_MOBILE, PermissionScope.ALL));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.AUTH_LOGIN_WEB, PermissionScope.ALL)); // Limited per document
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.AUTH_CHANGE_PASSWORD, PermissionScope.ALL));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.AUTH_UPDATE_PROFILE, PermissionScope.ALL));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.AUTH_ENABLE_MFA, PermissionScope.ALL));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.AUTH_VIEW_LOGIN_HISTORY, PermissionScope.OWN));
        
        // User Management - Own scope only
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.USER_VIEW, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.TEAM_VIEW_HIERARCHY, PermissionScope.OWN));
        
        // Leads - Assigned only
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.LEAD_VIEW, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.LEAD_CREATE, PermissionScope.ALL));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.LEAD_EDIT, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.LEAD_VIEW_HISTORY, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.LEAD_ADD_NOTES, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.LEAD_SET_STATUS, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.LEAD_VIEW_SCORE, PermissionScope.OWN));
        
        // Deals - Own only
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.DEAL_VIEW, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.DEAL_CREATE, PermissionScope.ALL));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.DEAL_EDIT, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.DEAL_CHANGE_STAGE, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.DEAL_ADD_PRODUCTS, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.DEAL_SET_VALUE, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.DEAL_VIEW_PIPELINE, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.DEAL_ASSIGN_TEAM, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.DEAL_VIEW_HISTORY, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.DEAL_CLOSE, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.DEAL_VIEW_COMMISSION, PermissionScope.OWN));
        
        // Communication - Own scope
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.CALL_MAKE, PermissionScope.ALL));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.CALL_VIEW_HISTORY, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.CALL_LISTEN_RECORDINGS, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.CALL_VIEW_TRANSCRIPTS, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.EMAIL_SEND, PermissionScope.ALL));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.EMAIL_VIEW_HISTORY, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.WHATSAPP_SEND, PermissionScope.ALL));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.MESSAGE_VIEW_HISTORY, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.INBOX_ACCESS_UNIFIED, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.EMAIL_TEMPLATE_CREATE, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.EMAIL_TEMPLATE_USE_SHARED, PermissionScope.ALL));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.MESSAGE_SCHEDULE, PermissionScope.ALL));
        
        // Action Items - Own scope
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.ACTION_ITEM_VIEW, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.ACTION_ITEM_CREATE, PermissionScope.ALL));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.ACTION_ITEM_EDIT, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.ACTION_ITEM_DELETE, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.ACTION_ITEM_COMPLETE, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.ACTION_ITEM_SNOOZE, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.ACTION_ITEM_VIEW_OVERDUE, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.ACTION_ITEM_SET_PRIORITY, PermissionScope.OWN));
        
        // Calendar - Own scope
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.CALENDAR_VIEW_OWN, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.CALENDAR_CREATE_EVENTS, PermissionScope.ALL));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.CALENDAR_EDIT_EVENTS, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.CALENDAR_CANCEL_EVENTS, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.CALENDAR_ACCESS_INTEGRATIONS, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.CALENDAR_SET_AVAILABILITY, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.CALENDAR_VIEW_AVAILABILITY, PermissionScope.TEAM));
        
        // Products - View only
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.PRODUCT_VIEW_CATALOG, PermissionScope.ALL));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.PRODUCT_VIEW_PRICING, PermissionScope.ALL));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.PRODUCT_APPLY_DISCOUNTS, PermissionScope.ALL)); // Limited %
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.PRODUCT_VIEW_DISCOUNT_LIMITS, PermissionScope.ALL));
        
        // Analytics - Own scope
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.ANALYTICS_VIEW_INDIVIDUAL_DASHBOARD, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.ANALYTICS_VIEW_LEADERBOARDS, PermissionScope.ALL));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.ANALYTICS_VIEW_PERFORMANCE, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.ANALYTICS_VIEW_PIPELINE_HEALTH, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.ANALYTICS_VIEW_ACTIVITY, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.ANALYTICS_VIEW_TRENDS, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.ANALYTICS_ACCESS_AI_INSIGHTS, PermissionScope.OWN));
        
        // Scoring - Own scope
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.SCORING_VIEW_SCORES, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.SCORING_VIEW_BREAKDOWN, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.QUALIFICATION_USE_FORMS, PermissionScope.ALL));
        
        // Coaching & Quality - Own scope
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.COACHING_VIEW_NOTES, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.QUALITY_VIEW_CALL_SCORES, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.SENTIMENT_VIEW_ANALYSIS, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.AI_COACHING_ACCESS_INSIGHTS, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.SCRIPT_VIEW_ADHERENCE, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.SCRIPT_VIEW, PermissionScope.ALL));
        
        // Gamification - View access
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.GAMIFICATION_VIEW_LEADERBOARDS, PermissionScope.ALL));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.GAMIFICATION_VIEW_ACHIEVEMENTS, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.GAMIFICATION_VIEW_RANKINGS, PermissionScope.ALL));
        
        // Notifications - Own scope
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.NOTIFICATION_RECEIVE_PUSH, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.NOTIFICATION_CONFIGURE_PREFERENCES, PermissionScope.OWN));
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.NOTIFICATION_VIEW_HISTORY, PermissionScope.OWN));
        
        // Integrations - Personal only
        salesRepPermissions.add(UserRolePermission.create(UserRole.SALES_REP, Permission.INTEGRATION_CONNECT_PERSONAL, PermissionScope.OWN));
        
        rolePermissionRepository.saveAll(salesRepPermissions);
        log.debug("Initialized {} permissions for SALES_REP role", salesRepPermissions.size());
    }
    
    /**
     * Initialize Viewer role permissions (read-only)
     */
    private void initializeViewerPermissions() {
        List<UserRolePermission> viewerPermissions = new ArrayList<>();
        
        // Authentication & Account - Basic access
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.AUTH_LOGIN_MOBILE, PermissionScope.ALL)); // Limited per document
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.AUTH_LOGIN_WEB, PermissionScope.ALL)); // Read-only mode
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.AUTH_CHANGE_PASSWORD, PermissionScope.ALL));
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.AUTH_UPDATE_PROFILE, PermissionScope.ALL));
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.AUTH_ENABLE_MFA, PermissionScope.ALL));
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.AUTH_VIEW_LOGIN_HISTORY, PermissionScope.OWN));
        
        // Data Access - As permitted by Admin
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.LEAD_VIEW, PermissionScope.AS_PERMITTED));
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.DEAL_VIEW, PermissionScope.AS_PERMITTED));
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.LEAD_VIEW_HISTORY, PermissionScope.AS_PERMITTED));
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.DEAL_VIEW_HISTORY, PermissionScope.AS_PERMITTED));
        
        // Analytics - As permitted
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.ANALYTICS_VIEW_INDIVIDUAL_DASHBOARD, PermissionScope.AS_PERMITTED));
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.ANALYTICS_VIEW_LEADERBOARDS, PermissionScope.AS_PERMITTED));
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.ANALYTICS_VIEW_PERFORMANCE, PermissionScope.AS_PERMITTED));
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.ANALYTICS_VIEW_PIPELINE_HEALTH, PermissionScope.AS_PERMITTED));
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.ANALYTICS_VIEW_ACTIVITY, PermissionScope.AS_PERMITTED));
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.ANALYTICS_VIEW_TRENDS, PermissionScope.AS_PERMITTED));
        
        // Team hierarchy - Own only
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.TEAM_VIEW_HIERARCHY, PermissionScope.OWN));
        
        // Products - View only
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.PRODUCT_VIEW_CATALOG, PermissionScope.ALL));
        
        // Calendar - Own only
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.CALENDAR_VIEW_OWN, PermissionScope.OWN));
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.CALENDAR_SET_AVAILABILITY, PermissionScope.OWN));
        
        // Gamification - View only
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.GAMIFICATION_VIEW_LEADERBOARDS, PermissionScope.AS_PERMITTED));
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.GAMIFICATION_VIEW_ACHIEVEMENTS, PermissionScope.AS_PERMITTED));
        
        // Notifications - Own scope
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.NOTIFICATION_RECEIVE_PUSH, PermissionScope.OWN));
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.NOTIFICATION_CONFIGURE_PREFERENCES, PermissionScope.OWN));
        viewerPermissions.add(UserRolePermission.create(UserRole.VIEWER, Permission.NOTIFICATION_VIEW_HISTORY, PermissionScope.OWN));
        
        rolePermissionRepository.saveAll(viewerPermissions);
        log.debug("Initialized {} permissions for VIEWER role", viewerPermissions.size());
    }
    
    /**
     * Check if a user role has a specific permission with the given scope
     */
    public boolean hasPermission(UserRole role, Permission permission, PermissionScope requiredScope) {
        Optional<UserRolePermission> rolePermission = rolePermissionRepository
            .findEnabledByRoleAndPermission(role, permission);
            
        if (rolePermission.isEmpty()) {
            return false;
        }
        
        PermissionScope grantedScope = rolePermission.get().getEffectiveScope();
        return grantedScope.includes(requiredScope);
    }
    
    /**
     * Get the effective scope for a role-permission combination
     */
    public PermissionScope getPermissionScope(UserRole role, Permission permission) {
        return rolePermissionRepository
            .findEnabledByRoleAndPermission(role, permission)
            .map(UserRolePermission::getEffectiveScope)
            .orElse(PermissionScope.NONE);
    }
    
    /**
     * Get all permissions for a specific role
     */
    public List<UserRolePermission> getRolePermissions(UserRole role) {
        return rolePermissionRepository.findEnabledPermissionsByRole(role);
    }
    
    /**
     * Get role permission for specific permission
     */
    public Optional<UserRolePermission> getRolePermission(UserRole role, Permission permission) {
        return rolePermissionRepository.findByRoleAndPermission(role, permission);
    }
    
    /**
     * Update a role permission scope
     */
    @Transactional
    public UserRolePermission updateRolePermissionScope(UserRole role, Permission permission, 
                                                       PermissionScope newScope, boolean enabled) {
        Optional<UserRolePermission> existing = rolePermissionRepository.findByRoleAndPermission(role, permission);
        
        UserRolePermission rolePermission;
        if (existing.isPresent()) {
            rolePermission = existing.get();
            rolePermission.setScope(newScope);
            rolePermission.setEnabled(enabled);
        } else {
            rolePermission = UserRolePermission.create(role, permission, newScope);
            rolePermission.setEnabled(enabled);
        }
        
        return rolePermissionRepository.save(rolePermission);
    }
}