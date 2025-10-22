package com.ceedpods.crmbuild.exception;

/**
 * Exception thrown when authentication fails (401 Unauthorized)
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
