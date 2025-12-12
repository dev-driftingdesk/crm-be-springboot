package com.ceedpods.crmbuild.enums;

public enum NotificationType {
    MANUAL("Manual Notification"),
    NEW_LEAD("New Lead Created"),
    LEAD_UPDATED("Lead Updated"),
    DEAL_CREATED("Deal Created"),
    DEAL_UPDATED("Deal Updated"),
    DEAL_WON("Deal Won"),
    DEAL_LOST("Deal Lost"),
    INBOUND_MESSAGE("Inbound Message Received"),
    SCHEDULED("Scheduled Notification"),
    SYSTEM("System Notification");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
