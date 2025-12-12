package com.ceedpods.crmbuild.enums;

public enum NotificationPlatform {
    WEB("Web Browser"),
    ANDROID("Android Device"),
    IOS("iOS Device");

    private final String description;

    NotificationPlatform(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
