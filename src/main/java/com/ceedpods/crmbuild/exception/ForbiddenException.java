package com.ceedpods.crmbuild.exception;

/**
 * Exception thrown when user doesn't have permission to perform an action (403 Forbidden)
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
