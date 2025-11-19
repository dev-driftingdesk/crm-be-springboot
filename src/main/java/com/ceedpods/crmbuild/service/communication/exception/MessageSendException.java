package com.ceedpods.crmbuild.service.communication.exception;

import com.ceedpods.crmbuild.service.communication.enums.CommunicationChannel;

/**
 * Exception thrown when a message fails to send through the communication provider.
 * <p>
 * This exception wraps various failure scenarios that occur during the actual message
 * sending process, including:
 * <ul>
 *   <li>Network connectivity issues</li>
 *   <li>Provider API errors (authentication, rate limits, service outages)</li>
 *   <li>Invalid recipient (rejected by provider)</li>
 *   <li>Message content validation failures</li>
 *   <li>Provider-specific errors (quota exceeded, blocked sender, etc.)</li>
 * </ul>
 * </p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * try {
 *     communicationService.sendMessage(request);
 * } catch (MessageSendException e) {
 *     logger.error("Failed to send message on {}: {}",
 *         e.getChannel(), e.getMessage());
 *
 *     if (e.isRetryable()) {
 *         logger.info("Error is retryable. Scheduling retry...");
 *         retryService.scheduleRetry(messageId);
 *     } else {
 *         logger.error("Permanent failure. Vendor error code: {}",
 *             e.getVendorErrorCode());
 *     }
 * }
 * }</pre>
 *
 * <p><b>HTTP Mapping:</b> When this exception is thrown in a REST controller,
 * it should typically be mapped to HTTP 502 Bad Gateway or 500 Internal Server Error,
 * depending on whether the error is from the external provider or internal processing.</p>
 *
 * @since 1.0
 * @see CommunicationException
 * @see CommunicationChannel
 */
public class MessageSendException extends CommunicationException {

    /**
     * The communication channel where the send failure occurred.
     */
    private final CommunicationChannel channel;

    /**
     * The vendor-specific error code (if available).
     */
    private final String vendorErrorCode;

    /**
     * Indicates whether this error is retryable.
     * <p>
     * True for transient errors (network issues, rate limits, temporary outages).
     * False for permanent errors (invalid credentials, malformed recipient).
     * </p>
     */
    private final boolean retryable;

    /**
     * The recipient identifier where the message was being sent.
     */
    private final String recipient;

    /**
     * Constructs a new exception for a message send failure.
     *
     * @param channel the communication channel
     * @param message the error message
     */
    public MessageSendException(CommunicationChannel channel, String message) {
        super(message, "MESSAGE_SEND_FAILED");
        this.channel = channel;
        this.vendorErrorCode = null;
        this.retryable = false;
        this.recipient = null;
    }

    /**
     * Constructs a new exception with cause.
     *
     * @param channel the communication channel
     * @param message the error message
     * @param cause the underlying cause
     */
    public MessageSendException(CommunicationChannel channel, String message, Throwable cause) {
        super(message, "MESSAGE_SEND_FAILED", cause);
        this.channel = channel;
        this.vendorErrorCode = null;
        this.retryable = determineRetryability(cause);
        this.recipient = null;
    }

    /**
     * Constructs a new exception with full details.
     *
     * @param channel the communication channel
     * @param recipient the recipient identifier
     * @param message the error message
     * @param vendorErrorCode the vendor-specific error code
     * @param retryable whether this error is retryable
     */
    public MessageSendException(CommunicationChannel channel, String recipient, String message,
                               String vendorErrorCode, boolean retryable) {
        super(message, "MESSAGE_SEND_FAILED");
        this.channel = channel;
        this.recipient = recipient;
        this.vendorErrorCode = vendorErrorCode;
        this.retryable = retryable;
    }

    /**
     * Constructs a new exception with full details and cause.
     *
     * @param channel the communication channel
     * @param recipient the recipient identifier
     * @param message the error message
     * @param vendorErrorCode the vendor-specific error code
     * @param retryable whether this error is retryable
     * @param cause the underlying cause
     */
    public MessageSendException(CommunicationChannel channel, String recipient, String message,
                               String vendorErrorCode, boolean retryable, Throwable cause) {
        super(message, "MESSAGE_SEND_FAILED", cause);
        this.channel = channel;
        this.recipient = recipient;
        this.vendorErrorCode = vendorErrorCode;
        this.retryable = retryable;
    }

    /**
     * Gets the communication channel where the failure occurred.
     *
     * @return the communication channel
     */
    public CommunicationChannel getChannel() {
        return channel;
    }

    /**
     * Gets the vendor-specific error code if available.
     *
     * @return the vendor error code, or null if not available
     */
    public String getVendorErrorCode() {
        return vendorErrorCode;
    }

    /**
     * Checks if this error is retryable.
     *
     * @return true if the operation can be retried
     */
    public boolean isRetryable() {
        return retryable;
    }

    /**
     * Gets the recipient identifier where the message was being sent.
     *
     * @return the recipient (email or phone number), or null if not available
     */
    public String getRecipient() {
        return recipient;
    }

    /**
     * Determines if an exception represents a retryable error.
     * <p>
     * This is a heuristic based on common exception types.
     * Specific implementations may override this logic.
     * </p>
     *
     * @param cause the exception to analyze
     * @return true if the error appears to be retryable
     */
    private static boolean determineRetryability(Throwable cause) {
        if (cause == null) {
            return false;
        }

        String className = cause.getClass().getSimpleName().toLowerCase();
        String message = cause.getMessage() != null ? cause.getMessage().toLowerCase() : "";

        // Network and timeout errors are typically retryable
        if (className.contains("timeout") || className.contains("socket") ||
            className.contains("connect") || className.contains("network")) {
            return true;
        }

        // Common retryable error patterns in messages
        if (message.contains("timeout") || message.contains("rate limit") ||
            message.contains("temporarily unavailable") || message.contains("try again")) {
            return true;
        }

        // HTTP 5xx errors are typically retryable
        if (message.contains("500") || message.contains("502") ||
            message.contains("503") || message.contains("504")) {
            return true;
        }

        return false;
    }

    /**
     * Returns a detailed string representation including all error details.
     *
     * @return string representation of this exception
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
          .append(" [").append(getErrorCode()).append("]")
          .append(": Channel=").append(channel);

        if (recipient != null) {
            sb.append(", Recipient=").append(recipient);
        }

        if (vendorErrorCode != null) {
            sb.append(", VendorErrorCode=").append(vendorErrorCode);
        }

        sb.append(", Retryable=").append(retryable)
          .append(", Message=").append(getMessage());

        return sb.toString();
    }

    /**
     * Creates a retryable MessageSendException from a network or temporary error.
     *
     * @param channel the communication channel
     * @param message the error message
     * @param cause the underlying network/timeout exception
     * @return a retryable MessageSendException
     */
    public static MessageSendException retryable(CommunicationChannel channel, String message, Throwable cause) {
        return new MessageSendException(channel, null, message, null, true, cause);
    }

    /**
     * Creates a non-retryable MessageSendException for permanent failures.
     *
     * @param channel the communication channel
     * @param recipient the recipient identifier
     * @param message the error message
     * @param vendorErrorCode the vendor error code
     * @return a non-retryable MessageSendException
     */
    public static MessageSendException permanent(CommunicationChannel channel, String recipient,
                                                String message, String vendorErrorCode) {
        return new MessageSendException(channel, recipient, message, vendorErrorCode, false);
    }
}
