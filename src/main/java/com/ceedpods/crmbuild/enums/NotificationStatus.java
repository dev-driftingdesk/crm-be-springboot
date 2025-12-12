package com.ceedpods.crmbuild.enums;

public enum NotificationStatus {
    PENDING("Pending"),
    SENT("Sent"),
    DELIVERED("Delivered"),
    FAILED("Failed"),
    SCHEDULED("Scheduled"),
    CANCELLED("Cancelled");

    private final String description;

    NotificationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
