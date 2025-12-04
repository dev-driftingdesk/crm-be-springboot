package com.ceedpods.crmbuild.enums;

/**
 * Enum representing the data types of filterable fields.
 * Used for validation and proper query construction.
 */
public enum FilterFieldType {
    STRING,
    NUMBER,
    DECIMAL,
    DATE,
    DATETIME,
    BOOLEAN,
    ENUM,
    OBJECT_ID
}
