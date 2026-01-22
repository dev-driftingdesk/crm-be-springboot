package com.ceedpods.crmbuild.service.communication.exception;

/**
 * Base exception for all communication-related errors.
 * <p>
 * This is the parent exception for all exceptions thrown by the communication service.
 * It provides a common type for catching any communication-related error and supports
 * error codes for more granular error handling.
 * </p>
 *
 * <p><b>Exception Hierarchy:</b></p>
 * <pre>
 * CommunicationException (base)
 *   ├── ChannelNotSupportedException
 *   ├── CredentialsNotFoundException
 *   └── MessageSendException
 * </pre>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * try {
 *     communicationService.sendMessage(request);
 * } catch (CredentialsNotFoundException e) {
 *     logger.error("Credentials missing: {}", e.getMessage());
 *     // Handle credentials issue
 * } catch (CommunicationException e) {
 *     logger.error("Communication error [{}]: {}", e.getErrorCode(), e.getMessage());
 *     // Handle other communication errors
 * }
 * }</pre>
 *
 * @since 1.0
 */
public class CommunicationException extends RuntimeException {

    /**
     * Error code for categorizing the exception.
     * <p>
     * Common codes:
     * <ul>
     *   <li><b>CHANNEL_NOT_SUPPORTED:</b> Requested channel is not available</li>
     *   <li><b>CREDENTIALS_NOT_FOUND:</b> Agent credentials are missing</li>
     *   <li><b>MESSAGE_SEND_FAILED:</b> Message sending operation failed</li>
     *   <li><b>VALIDATION_ERROR:</b> Request validation failed</li>
     *   <li><b>VENDOR_ERROR:</b> External provider error</li>
     * </ul>
     * </p>
     */
    private final String errorCode;

    /**
     * Constructs a new communication exception with the specified detail message.
     *
     * @param message the detail message
     */
    public CommunicationException(String message) {
        super(message);
        this.errorCode = "COMMUNICATION_ERROR";
    }

    /**
     * Constructs a new communication exception with the specified detail message and error code.
     *
     * @param message the detail message
     * @param errorCode the error code for categorization
     */
    public CommunicationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new communication exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public CommunicationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "COMMUNICATION_ERROR";
    }

    /**
     * Constructs a new communication exception with the specified detail message, error code, and cause.
     *
     * @param message the detail message
     * @param errorCode the error code for categorization
     * @param cause the cause of this exception
     */
    public CommunicationException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Gets the error code associated with this exception.
     *
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Returns a string representation including the error code.
     *
     * @return string representation of this exception
     */
    @Override
    public String toString() {
        String message = getMessage();
        return String.format("%s [%s]: %s",
            getClass().getSimpleName(),
            errorCode,
            message != null ? message : "");
    }
}
