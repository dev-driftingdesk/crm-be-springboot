package com.ceedpods.crmbuild.service.communication.exception;

import com.ceedpods.crmbuild.service.communication.enums.CommunicationChannel;

/**
 * Exception thrown when a requested communication channel is not supported.
 * <p>
 * This exception is thrown in scenarios such as:
 * <ul>
 *   <li>Requesting a channel that is not implemented in the system</li>
 *   <li>Using a channel that is temporarily disabled</li>
 *   <li>Attempting to use a channel not available for the specific agent/organization</li>
 * </ul>
 * </p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * try {
 *     communicationService.sendMessage(request);
 * } catch (ChannelNotSupportedException e) {
 *     logger.error("Channel {} is not supported", e.getChannel());
 *     // Offer user alternative channels
 * }
 * }</pre>
 *
 * <p><b>HTTP Mapping:</b> When this exception is thrown in a REST controller,
 * it should typically be mapped to HTTP 400 Bad Request or 501 Not Implemented.</p>
 *
 * @since 1.0
 * @see CommunicationException
 * @see CommunicationChannel
 */
public class ChannelNotSupportedException extends CommunicationException {

    /**
     * The unsupported channel that was requested.
     */
    private final CommunicationChannel channel;

    /**
     * Constructs a new exception for an unsupported channel.
     *
     * @param channel the channel that is not supported
     */
    public ChannelNotSupportedException(CommunicationChannel channel) {
        super(
            String.format("Communication channel '%s' is not supported or not available", channel),
            "CHANNEL_NOT_SUPPORTED"
        );
        this.channel = channel;
    }

    /**
     * Constructs a new exception with a custom message.
     *
     * @param channel the channel that is not supported
     * @param message custom detail message
     */
    public ChannelNotSupportedException(CommunicationChannel channel, String message) {
        super(message, "CHANNEL_NOT_SUPPORTED");
        this.channel = channel;
    }

    /**
     * Constructs a new exception with a custom message and cause.
     *
     * @param channel the channel that is not supported
     * @param message custom detail message
     * @param cause the cause of this exception
     */
    public ChannelNotSupportedException(CommunicationChannel channel, String message, Throwable cause) {
        super(message, "CHANNEL_NOT_SUPPORTED", cause);
        this.channel = channel;
    }

    /**
     * Gets the channel that was not supported.
     *
     * @return the unsupported communication channel
     */
    public CommunicationChannel getChannel() {
        return channel;
    }

    /**
     * Returns a detailed string representation including the channel.
     *
     * @return string representation of this exception
     */
    @Override
    public String toString() {
        return String.format("%s [%s]: Channel=%s, Message=%s",
            getClass().getSimpleName(),
            getErrorCode(),
            channel,
            getMessage());
    }
}
