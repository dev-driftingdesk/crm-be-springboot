package com.ceedpods.crmbuild.exception;

/**
 * Exception thrown for invalid client requests (400 Bad Request)
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
