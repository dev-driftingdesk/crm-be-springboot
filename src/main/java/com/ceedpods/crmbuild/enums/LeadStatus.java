package com.ceedpods.crmbuild.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LeadStatus {
    NEW("New"),
    CONTACTED("Contacted"),
    QUALIFIED("Qualified"),
    CONVERTED("Converted"),
    LOST("Lost");

    private final String displayName;

    LeadStatus(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static LeadStatus fromValue(String value) {
        if (value == null) {
            return null;
        }

        // Try to match by enum name (case-insensitive)
        for (LeadStatus status : LeadStatus.values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }

        // Try to match by display name (case-insensitive)
        for (LeadStatus status : LeadStatus.values()) {
            if (status.displayName.equalsIgnoreCase(value)) {
                return status;
            }
        }

        // If no match found, throw exception with helpful message
        throw new IllegalArgumentException(
            "Invalid LeadStatus value: '" + value + "'. " +
            "Accepted values are: NEW, CONTACTED, QUALIFIED, CONVERTED, LOST"
        );
    }
}
