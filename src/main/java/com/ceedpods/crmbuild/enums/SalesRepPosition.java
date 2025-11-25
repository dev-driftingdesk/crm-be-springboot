package com.ceedpods.crmbuild.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SalesRepPosition {
    PRIMARY("Primary"),
    CO_PRIMARY("Co-primary"),
    CONSULTANT("Consultant");

    private final String displayName;

    SalesRepPosition(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static SalesRepPosition fromValue(String value) {
        if (value == null) {
            return null;
        }

        // Try to match by enum name (case-insensitive)
        for (SalesRepPosition position : SalesRepPosition.values()) {
            if (position.name().equalsIgnoreCase(value)) {
                return position;
            }
        }

        // Try to match by display name (case-insensitive)
        for (SalesRepPosition position : SalesRepPosition.values()) {
            if (position.displayName.equalsIgnoreCase(value)) {
                return position;
            }
        }

        // If no match found, throw exception with helpful message
        throw new IllegalArgumentException(
            "Invalid SalesRepPosition value: '" + value + "'. " +
            "Accepted values are: Primary, Co-primary, Consultant"
        );
    }
}
