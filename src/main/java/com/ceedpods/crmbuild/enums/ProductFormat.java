package com.ceedpods.crmbuild.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProductFormat {
    IN_PERSON("in-person"),
    ONLINE("online"),
    HYBRID("hybrid");

    private final String value;

    ProductFormat(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ProductFormat fromValue(String value) {
        if (value == null) {
            return null;
        }

        for (ProductFormat format : ProductFormat.values()) {
            if (format.value.equalsIgnoreCase(value) || format.name().equalsIgnoreCase(value)) {
                return format;
            }
        }

        throw new IllegalArgumentException(
            "Invalid ProductFormat value: '" + value + "'. " +
            "Accepted values are: in-person, online, hybrid"
        );
    }
}
