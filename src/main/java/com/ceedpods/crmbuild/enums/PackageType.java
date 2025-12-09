package com.ceedpods.crmbuild.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PackageType {
    STANDARD("standard"),
    PRO("pro"),
    ENTERPRISE("enterprise");

    private final String value;

    PackageType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PackageType fromValue(String value) {
        if (value == null) {
            return null;
        }

        for (PackageType type : PackageType.values()) {
            if (type.value.equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException(
            "Invalid PackageType value: '" + value + "'. " +
            "Accepted values are: standard, pro, enterprise"
        );
    }
}
