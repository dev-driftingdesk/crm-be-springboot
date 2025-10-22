package com.ceedpods.crmbuild.exception;

/**
 * Exception thrown when attempting to create a resource that already exists (409 Conflict)
 */
public class ResourceAlreadyExistsException extends RuntimeException {

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }

    public ResourceAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
