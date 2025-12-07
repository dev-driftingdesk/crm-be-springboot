package com.ceedpods.crmbuild.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DealStatus {
    OPEN("Open"),
    WON("Won"),
    LOST("Lost"),
    PENDING("Pending"),
    NEGOTIATION("Negotiation");

    private final String displayName;

    DealStatus(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static DealStatus fromValue(String value) {
        if (value == null) {
            return null;
        }

        // Try to match by enum name (case-insensitive)
        for (DealStatus status : DealStatus.values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }

        // Try to match by display name (case-insensitive)
        for (DealStatus status : DealStatus.values()) {
            if (status.displayName.equalsIgnoreCase(value)) {
                return status;
            }
        }

        // If no match found, throw exception with helpful message
        throw new IllegalArgumentException(
            "Invalid DealStatus value: '" + value + "'. " +
            "Accepted values are: OPEN, WON, LOST, PENDING, NEGOTIATION"
        );
    }
}
