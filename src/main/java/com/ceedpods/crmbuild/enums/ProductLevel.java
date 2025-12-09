package com.ceedpods.crmbuild.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProductLevel {
    BEGINNER("beginner"),
    INTERMEDIATE("intermediate"),
    ADVANCED("advanced");

    private final String value;

    ProductLevel(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ProductLevel fromValue(String value) {
        if (value == null) {
            return null;
        }

        for (ProductLevel level : ProductLevel.values()) {
            if (level.value.equalsIgnoreCase(value) || level.name().equalsIgnoreCase(value)) {
                return level;
            }
        }

        throw new IllegalArgumentException(
            "Invalid ProductLevel value: '" + value + "'. " +
            "Accepted values are: beginner, intermediate, advanced"
        );
    }
}
