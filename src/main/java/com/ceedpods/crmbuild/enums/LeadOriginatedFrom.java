package com.ceedpods.crmbuild.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LeadOriginatedFrom {
    FACEBOOK("facebook"),
    WHATSAPP("whatsapp"),
    INSTAGRAM("instagram"),
    EMAIL("email");

    private final String value;

    LeadOriginatedFrom(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static LeadOriginatedFrom fromValue(String value) {
        if (value == null) {
            return null;
        }

        for (LeadOriginatedFrom origin : LeadOriginatedFrom.values()) {
            if (origin.name().equalsIgnoreCase(value) || origin.value.equalsIgnoreCase(value)) {
                return origin;
            }
        }

        throw new IllegalArgumentException("Invalid LeadOriginatedFrom value: '" + value +
            "'. Accepted values are: facebook, whatsapp, instagram, email");
    }
}
