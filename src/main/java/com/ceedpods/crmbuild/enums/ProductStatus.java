package com.ceedpods.crmbuild.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProductStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    DISCONTINUED("Discontinued"),
    OUT_OF_STOCK("Out of Stock"),
    COMING_SOON("Coming Soon");

    private final String displayName;

    ProductStatus(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static ProductStatus fromValue(String value) {
        if (value == null) {
            return null;
        }

        // Try to match by enum name (case-insensitive)
        for (ProductStatus status : ProductStatus.values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }

        // Try to match by display name (case-insensitive)
        for (ProductStatus status : ProductStatus.values()) {
            if (status.displayName.equalsIgnoreCase(value)) {
                return status;
            }
        }

        // If no match found, throw exception with helpful message
        throw new IllegalArgumentException(
            "Invalid ProductStatus value: '" + value + "'. " +
            "Accepted values are: ACTIVE, INACTIVE, DISCONTINUED, OUT_OF_STOCK, COMING_SOON"
        );
    }
}
