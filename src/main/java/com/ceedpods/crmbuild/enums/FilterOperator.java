package com.ceedpods.crmbuild.enums;

/**
 * Enum representing filter operators for querying.
 * Supports various comparison and matching operations.
 */
public enum FilterOperator {
    // Equality operators
    EQUALS("eq", "Equals"),
    NOT_EQUALS("ne", "Not Equals"),

    // String operators
    CONTAINS("contains", "Contains"),
    STARTS_WITH("startsWith", "Starts With"),
    ENDS_WITH("endsWith", "Ends With"),

    // Comparison operators
    GREATER_THAN("gt", "Greater Than"),
    GREATER_THAN_OR_EQUALS("gte", "Greater Than or Equals"),
    LESS_THAN("lt", "Less Than"),
    LESS_THAN_OR_EQUALS("lte", "Less Than or Equals"),

    // Range operators
    BETWEEN("between", "Between"),

    // Collection operators
    IN("in", "In"),
    NOT_IN("notIn", "Not In"),

    // Null operators
    IS_NULL("isNull", "Is Null"),
    IS_NOT_NULL("isNotNull", "Is Not Null"),

    // Boolean operators
    IS_TRUE("isTrue", "Is True"),
    IS_FALSE("isFalse", "Is False");

    private final String code;
    private final String displayName;

    FilterOperator(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static FilterOperator fromCode(String code) {
        for (FilterOperator op : values()) {
            if (op.code.equalsIgnoreCase(code)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Unknown filter operator: " + code);
    }
}
