package com.ceedpods.crmbuild.service.communication.enums;

/**
 * Enumeration representing the lifecycle states of a communication message.
 * <p>
 * This enum tracks the status of messages sent through the communication service,
 * from initial creation through final delivery or failure. It provides a vendor-agnostic
 * way to represent message states across different communication providers.
 * </p>
 *
 * <p><b>Status Flow:</b></p>
 * <pre>
 * PENDING → SENT → DELIVERED (success path)
 *    ↓
 * FAILED (error path)
 * </pre>
 *
 * @since 1.0
 * @see com.ceedpods.crmbuild.service.communication.model.CommunicationResult
 */
public enum CommunicationStatus {

    /**
     * Message has been created but not yet sent to the provider.
     * <p>
     * This is the initial state when a message is created in the system
     * but hasn't been dispatched to the communication vendor yet.
     * </p>
     */
    PENDING,

    /**
     * Message has been successfully sent to the communication provider.
     * <p>
     * The message has been accepted by the vendor's API and is being processed
     * for delivery. This does not guarantee final delivery to the recipient.
     * </p>
     */
    SENT,

    /**
     * Message has been confirmed as delivered to the recipient.
     * <p>
     * The communication provider has confirmed successful delivery.
     * Note: Not all providers support delivery confirmations.
     * For providers without delivery callbacks, SENT may be the final status.
     * </p>
     */
    DELIVERED,

    /**
     * Message sending or delivery has failed.
     * <p>
     * The message could not be sent to the provider, or the provider
     * reported a delivery failure. Check {@link com.ceedpods.crmbuild.service.communication.model.CommunicationResult#getErrorDetails()}
     * for specific failure reasons.
     * </p>
     */
    FAILED;

    /**
     * Checks if this status represents a terminal state (no further processing).
     * <p>
     * Terminal states indicate that no further automatic processing should occur.
     * Messages in terminal states may be eligible for manual retry.
     * </p>
     *
     * @return true if this is a terminal status (DELIVERED or FAILED)
     */
    public boolean isTerminal() {
        return this == DELIVERED || this == FAILED;
    }

    /**
     * Checks if this status represents a successful state.
     *
     * @return true if this status indicates success (SENT or DELIVERED)
     */
    public boolean isSuccessful() {
        return this == SENT || this == DELIVERED;
    }

    /**
     * Checks if this status indicates the message is still being processed.
     *
     * @return true if this status is PENDING
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * Checks if this status indicates a failure.
     *
     * @return true if this status is FAILED
     */
    public boolean isFailed() {
        return this == FAILED;
    }

    /**
     * Returns a human-readable description of this status.
     *
     * @return a descriptive string for this status
     */
    public String getDescription() {
        return switch (this) {
            case PENDING -> "Message created, awaiting dispatch to provider";
            case SENT -> "Message sent to provider, awaiting delivery confirmation";
            case DELIVERED -> "Message successfully delivered to recipient";
            case FAILED -> "Message sending or delivery failed";
        };
    }
}
