package com.ceedpods.crmbuild.enums;

public enum CommunicationType {
    EMAIL("Email"),
    SMS("SMS"),
    CALL("Call"),
    MEETING("Meeting"),
    NOTE("Note");

    private final String displayName;

    CommunicationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}