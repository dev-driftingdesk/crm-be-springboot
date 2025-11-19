package com.ceedpods.crmbuild.service.communication.exception;

import com.ceedpods.crmbuild.service.communication.enums.CommunicationChannel;

/**
 * Exception thrown when communication credentials are not found for an agent.
 * <p>
 * This exception is thrown in scenarios such as:
 * <ul>
 *   <li>Agent has not configured credentials for the requested channel</li>
 *   <li>Credentials have been deleted or deactivated</li>
 *   <li>Agent ID is invalid or not found in the system</li>
 * </ul>
 * </p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * try {
 *     communicationService.sendMessage(request);
 * } catch (CredentialsNotFoundException e) {
 *     logger.error("Credentials not found for agent {} on channel {}",
 *         e.getAgentId(), e.getChannel());
 *     // Redirect user to credentials setup page
 * }
 * }</pre>
 *
 * <p><b>HTTP Mapping:</b> When this exception is thrown in a REST controller,
 * it should typically be mapped to HTTP 424 Failed Dependency or 400 Bad Request
 * with a clear error message directing the user to configure their credentials.</p>
 *
 * <p><b>Security Note:</b> When exposing this exception to end users, be careful
 * not to reveal sensitive information about internal system structure or other agents.</p>
 *
 * @since 1.0
 * @see CommunicationException
 * @see CommunicationChannel
 */
public class CredentialsNotFoundException extends CommunicationException {

    /**
     * The agent ID for which credentials were not found.
     */
    private final String agentId;

    /**
     * The channel for which credentials were requested.
     */
    private final CommunicationChannel channel;

    /**
     * Constructs a new exception for missing credentials.
     *
     * @param agentId the agent ID
     * @param channel the communication channel
     */
    public CredentialsNotFoundException(String agentId, CommunicationChannel channel) {
        super(
            String.format("Communication credentials not found for agent '%s' on channel '%s'. " +
                "Please configure your credentials before sending messages.", agentId, channel),
            "CREDENTIALS_NOT_FOUND"
        );
        this.agentId = agentId;
        this.channel = channel;
    }

    /**
     * Constructs a new exception with a custom message.
     *
     * @param agentId the agent ID
     * @param channel the communication channel
     * @param message custom detail message
     */
    public CredentialsNotFoundException(String agentId, CommunicationChannel channel, String message) {
        super(message, "CREDENTIALS_NOT_FOUND");
        this.agentId = agentId;
        this.channel = channel;
    }

    /**
     * Constructs a new exception with a custom message and cause.
     *
     * @param agentId the agent ID
     * @param channel the communication channel
     * @param message custom detail message
     * @param cause the cause of this exception
     */
    public CredentialsNotFoundException(String agentId, CommunicationChannel channel,
                                       String message, Throwable cause) {
        super(message, "CREDENTIALS_NOT_FOUND", cause);
        this.agentId = agentId;
        this.channel = channel;
    }

    /**
     * Gets the agent ID for which credentials were not found.
     *
     * @return the agent ID
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * Gets the channel for which credentials were requested.
     *
     * @return the communication channel
     */
    public CommunicationChannel getChannel() {
        return channel;
    }

    /**
     * Returns a detailed string representation including agent and channel information.
     *
     * @return string representation of this exception
     */
    @Override
    public String toString() {
        return String.format("%s [%s]: AgentId=%s, Channel=%s, Message=%s",
            getClass().getSimpleName(),
            getErrorCode(),
            agentId,
            channel,
            getMessage());
    }
}
