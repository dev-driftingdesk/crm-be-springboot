package com.ceedpods.crmbuild.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Priority {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high");

    private final String value;

    Priority(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Priority fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (Priority priority : Priority.values()) {
            if (priority.name().equalsIgnoreCase(value) || priority.value.equalsIgnoreCase(value)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Invalid Priority value: '" + value + "'. Accepted values are: low, medium, high");
    }
}
