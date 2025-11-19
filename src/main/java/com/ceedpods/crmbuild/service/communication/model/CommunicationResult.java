package com.ceedpods.crmbuild.service.communication.model;

import com.ceedpods.crmbuild.service.communication.enums.CommunicationChannel;
import com.ceedpods.crmbuild.service.communication.enums.CommunicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Standardized result object returned by all communication service operations.
 * <p>
 * This class provides a vendor-agnostic representation of communication operation results,
 * allowing consumers to handle responses uniformly regardless of the underlying provider
 * (Twilio, Meta, SendGrid, etc.).
 * </p>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li><b>Vendor-Agnostic:</b> No vendor-specific fields in the core structure</li>
 *   <li><b>Consistent:</b> Same response format across all channels</li>
 *   <li><b>Informative:</b> Provides both success/failure status and detailed information</li>
 *   <li><b>Traceable:</b> Includes both internal and external message IDs</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * CommunicationResult result = communicationService.sendMessage(request);
 *
 * if (result.isSuccess()) {
 *     logger.info("Message sent successfully. ID: {}", result.getMessageId());
 *     logger.info("Vendor tracking ID: {}", result.getVendorMessageId());
 * } else {
 *     logger.error("Message failed: {}", result.getErrorDetails().getMessage());
 *     logger.error("Error code: {}", result.getErrorDetails().getCode());
 * }
 * }</pre>
 *
 * @since 1.0
 * @see CommunicationStatus
 * @see CommunicationChannel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunicationResult {

    /**
     * Indicates whether the communication operation was successful.
     * <p>
     * True if the message was accepted by the provider (SENT or DELIVERED status).
     * False if the message failed to send or was rejected (FAILED status).
     * </p>
     */
    private boolean success;

    /**
     * Internal message identifier assigned by the CRM system.
     * <p>
     * This ID can be used to:
     * <ul>
     *   <li>Track the message in the CRM database</li>
     *   <li>Query message status via {@code getMessageStatus(messageId)}</li>
     *   <li>Correlate messages across different system components</li>
     *   <li>Support audit trails and reporting</li>
     * </ul>
     * </p>
     *
     * <p><b>Format:</b> UUID string (e.g., "123e4567-e89b-12d3-a456-426614174000")</p>
     */
    private String messageId;

    /**
     * External message identifier assigned by the communication vendor.
     * <p>
     * This ID is provided by the external service (Twilio, Meta, etc.) and can be used to:
     * <ul>
     *   <li>Track the message in the vendor's system</li>
     *   <li>Query delivery status via vendor APIs</li>
     *   <li>Reference the message in support requests to the vendor</li>
     *   <li>Correlate with vendor webhooks and callbacks</li>
     * </ul>
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <ul>
     *   <li>Twilio SMS: "SM1234567890abcdef1234567890abcdef"</li>
     *   <li>Meta WhatsApp: "wamid.HBgNMTIzNDU2Nzg5MAUCABEYEjBBNzE5M..."</li>
     *   <li>Email: Provider-specific message ID or UUID</li>
     * </ul>
     */
    private String vendorMessageId;

    /**
     * Current status of the message.
     * <p>
     * Possible values:
     * <ul>
     *   <li>{@link CommunicationStatus#PENDING} - Created but not sent</li>
     *   <li>{@link CommunicationStatus#SENT} - Accepted by provider</li>
     *   <li>{@link CommunicationStatus#DELIVERED} - Confirmed delivered to recipient</li>
     *   <li>{@link CommunicationStatus#FAILED} - Failed to send or deliver</li>
     * </ul>
     * </p>
     *
     * @see CommunicationStatus
     */
    private CommunicationStatus status;

    /**
     * The communication channel used for this message.
     *
     * @see CommunicationChannel
     */
    private CommunicationChannel channel;

    /**
     * Timestamp when the message was processed by the CRM system.
     * <p>
     * This is the time when the communication request was received and processed,
     * not necessarily when it was delivered to the recipient.
     * </p>
     */
    private LocalDateTime timestamp;

    /**
     * Timestamp when the message was sent to the provider.
     * <p>
     * This may be null if the message is still in PENDING status.
     * </p>
     */
    private LocalDateTime sentAt;

    /**
     * Timestamp when the message was delivered (if available).
     * <p>
     * Only populated when the provider supports delivery confirmation and
     * the message status is DELIVERED. May be null for many providers.
     * </p>
     */
    private LocalDateTime deliveredAt;

    /**
     * Error details if the operation failed.
     * <p>
     * Only populated when {@code success} is false or status is FAILED.
     * Contains information about what went wrong.
     * </p>
     */
    private ErrorDetails errorDetails;

    /**
     * Additional metadata about the communication result.
     * <p>
     * This map can contain:
     * <ul>
     *   <li>Vendor-specific response data</li>
     *   <li>Retry information</li>
     *   <li>Cost/credits used (if available)</li>
     *   <li>Delivery receipt URLs</li>
     *   <li>Custom tracking parameters</li>
     * </ul>
     * </p>
     */
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    /**
     * Nested class for error details.
     * <p>
     * Provides structured information about communication failures.
     * </p>
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetails {

        /**
         * Error code for categorizing the failure.
         * <p>
         * Common codes:
         * <ul>
         *   <li><b>CREDENTIALS_NOT_FOUND:</b> Agent credentials missing</li>
         *   <li><b>INVALID_RECIPIENT:</b> Malformed email/phone number</li>
         *   <li><b>VENDOR_ERROR:</b> Provider API returned an error</li>
         *   <li><b>NETWORK_ERROR:</b> Network connectivity issue</li>
         *   <li><b>RATE_LIMIT:</b> Provider rate limit exceeded</li>
         *   <li><b>INVALID_CREDENTIALS:</b> Authentication failed with provider</li>
         *   <li><b>VALIDATION_ERROR:</b> Request validation failed</li>
         * </ul>
         * </p>
         */
        private String code;

        /**
         * Human-readable error message.
         * <p>
         * This message should be informative but safe to display to users
         * (avoid exposing sensitive technical details).
         * </p>
         */
        private String message;

        /**
         * Detailed technical error message for debugging.
         * <p>
         * May contain stack traces, vendor-specific error responses,
         * or other technical details. Should only be logged, not displayed to users.
         * </p>
         */
        private String technicalDetails;

        /**
         * Vendor-specific error code (if available).
         * <p>
         * The original error code returned by the communication provider.
         * Useful for troubleshooting with vendor support.
         * </p>
         */
        private String vendorErrorCode;

        /**
         * Indicates if this error is retryable.
         * <p>
         * True for transient errors (network issues, rate limits, temporary outages).
         * False for permanent errors (invalid credentials, malformed recipient).
         * </p>
         */
        private boolean retryable;
    }

    /**
     * Static factory method for creating a success result.
     *
     * @param messageId internal message ID
     * @param vendorMessageId vendor-provided message ID
     * @param channel communication channel used
     * @return a successful CommunicationResult with SENT status
     */
    public static CommunicationResult success(String messageId, String vendorMessageId, CommunicationChannel channel) {
        return CommunicationResult.builder()
            .success(true)
            .messageId(messageId)
            .vendorMessageId(vendorMessageId)
            .status(CommunicationStatus.SENT)
            .channel(channel)
            .timestamp(LocalDateTime.now())
            .sentAt(LocalDateTime.now())
            .build();
    }

    /**
     * Static factory method for creating a failure result.
     *
     * @param messageId internal message ID (may be null if message wasn't created)
     * @param channel communication channel attempted
     * @param errorCode error code
     * @param errorMessage error message
     * @return a failed CommunicationResult with FAILED status
     */
    public static CommunicationResult failure(String messageId, CommunicationChannel channel,
                                             String errorCode, String errorMessage) {
        return CommunicationResult.builder()
            .success(false)
            .messageId(messageId)
            .status(CommunicationStatus.FAILED)
            .channel(channel)
            .timestamp(LocalDateTime.now())
            .errorDetails(ErrorDetails.builder()
                .code(errorCode)
                .message(errorMessage)
                .retryable(isRetryableErrorCode(errorCode))
                .build())
            .build();
    }

    /**
     * Static factory method for creating a failure result with full error details.
     *
     * @param messageId internal message ID
     * @param channel communication channel attempted
     * @param errorDetails complete error details
     * @return a failed CommunicationResult with FAILED status
     */
    public static CommunicationResult failure(String messageId, CommunicationChannel channel,
                                             ErrorDetails errorDetails) {
        return CommunicationResult.builder()
            .success(false)
            .messageId(messageId)
            .status(CommunicationStatus.FAILED)
            .channel(channel)
            .timestamp(LocalDateTime.now())
            .errorDetails(errorDetails)
            .build();
    }

    /**
     * Determines if an error code represents a retryable error.
     *
     * @param errorCode the error code to check
     * @return true if the error is typically retryable
     */
    private static boolean isRetryableErrorCode(String errorCode) {
        if (errorCode == null) {
            return false;
        }

        return switch (errorCode.toUpperCase()) {
            case "NETWORK_ERROR", "RATE_LIMIT", "VENDOR_TIMEOUT", "SERVICE_UNAVAILABLE" -> true;
            default -> false;
        };
    }

    /**
     * Adds a metadata entry to this result.
     *
     * @param key metadata key
     * @param value metadata value
     * @return this result instance for method chaining
     */
    public CommunicationResult addMetadata(String key, String value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }

    /**
     * Gets a metadata value by key.
     *
     * @param key metadata key
     * @return metadata value or null
     */
    public String getMetadata(String key) {
        return this.metadata != null ? this.metadata.get(key) : null;
    }

    /**
     * Checks if this result represents a terminal state.
     *
     * @return true if status is terminal (DELIVERED or FAILED)
     */
    public boolean isTerminal() {
        return status != null && status.isTerminal();
    }

    /**
     * Checks if this result represents a pending state.
     *
     * @return true if status is PENDING
     */
    public boolean isPending() {
        return status != null && status.isPending();
    }

    /**
     * Checks if the error (if present) is retryable.
     *
     * @return true if there's an error and it's marked as retryable
     */
    public boolean isRetryable() {
        return !success && errorDetails != null && errorDetails.isRetryable();
    }
}
