package com.ceedpods.crmbuild.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DiscountAddOnType {
    DISCOUNT("discount"),
    ADD_ON("add-on");

    private final String value;

    DiscountAddOnType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static DiscountAddOnType fromValue(String value) {
        if (value == null) {
            return null;
        }

        for (DiscountAddOnType type : DiscountAddOnType.values()) {
            if (type.value.equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException(
            "Invalid DiscountAddOnType value: '" + value + "'. " +
            "Accepted values are: discount, add-on"
        );
    }
}
