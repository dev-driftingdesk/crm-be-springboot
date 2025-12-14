package com.ceedpods.crmbuild.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ActivityType {
    CALL("call"),
    EMAIL("email"),
    MEETING("meeting"),
    TASK("task"),
    NOTE("note"),
    OTHER("other");

    private final String value;

    ActivityType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ActivityType fromValue(String value) {
        if (value == null) {
            return null;
        }

        // Try to match by enum name (case-insensitive)
        for (ActivityType type : ActivityType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }

        // Try to match by value (case-insensitive)
        for (ActivityType type : ActivityType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }

        // If no match found, throw exception with helpful message
        throw new IllegalArgumentException(
            "Invalid ActivityType value: '" + value + "'. " +
            "Accepted values are: call, email, meeting, task, note, other"
        );
    }
}
