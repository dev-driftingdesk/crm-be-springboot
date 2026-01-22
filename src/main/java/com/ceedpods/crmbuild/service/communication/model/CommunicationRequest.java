package com.ceedpods.crmbuild.service.communication.model;

import com.ceedpods.crmbuild.service.communication.enums.CommunicationChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all communication requests in the CRM system.
 * <p>
 * This class defines the common properties required for sending any type of message
 * (Email, SMS, WhatsApp, etc.). It serves as the foundation of the vendor-agnostic
 * communication interface, ensuring consistent structure across all communication channels.
 * </p>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li><b>Vendor-Agnostic:</b> Contains no vendor-specific fields or logic</li>
 *   <li><b>Extensible:</b> Supports custom metadata for channel-specific requirements</li>
 *   <li><b>Type-Safe:</b> Uses builder pattern for fluent and safe object construction</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * CommunicationRequest request = CommunicationRequest.builder()
 *     .channel(CommunicationChannel.SMS)
 *     .agentId("agent-123")
 *     .recipient("+1234567890")
 *     .body("Hello from CRM!")
 *     .metadata(Map.of("priority", "high"))
 *     .build();
 * }</pre>
 *
 * <p><b>Subclasses:</b></p>
 * For channel-specific requirements, use specialized subclasses:
 * <ul>
 *   <li>{@link EmailRequest} - For email with subject and HTML support</li>
 *   <li>{@link SmsRequest} - For SMS with typed phone number</li>
 *   <li>{@link WhatsAppRequest} - For WhatsApp with typed phone number</li>
 * </ul>
 *
 * @since 1.0
 * @see CommunicationChannel
 * @see EmailRequest
 * @see SmsRequest
 * @see WhatsAppRequest
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CommunicationRequest {

    /**
     * The communication channel to use for sending this message.
     * <p>
     * Determines which type of communication service will process this request.
     * The channel must be supported by the system and the agent must have
     * valid credentials configured for the selected channel.
     * </p>
     *
     * @see CommunicationChannel
     */
    private CommunicationChannel channel;

    /**
     * The unique identifier of the agent initiating this communication.
     * <p>
     * Used to:
     * <ul>
     *   <li>Retrieve agent-specific communication credentials</li>
     *   <li>Track message ownership for audit purposes</li>
     *   <li>Apply agent-specific routing or quota rules</li>
     * </ul>
     * </p>
     */
    private String agentId;

    /**
     * The recipient identifier (email address or phone number).
     * <p>
     * Format depends on the channel:
     * <ul>
     *   <li><b>EMAIL:</b> Valid email address (e.g., "user@example.com")</li>
     *   <li><b>SMS/WHATSAPP:</b> Phone number in E.164 format (e.g., "+1234567890")</li>
     * </ul>
     * </p>
     *
     * <p>
     * Note: For type-safe recipient handling, consider using channel-specific
     * request classes like {@link EmailRequest} or {@link SmsRequest}.
     * </p>
     */
    private String recipient;

    /**
     * The message content to be sent.
     * <p>
     * This is the primary text content of the message. For channels that support
     * rich content (like email), additional formatting can be provided through
     * channel-specific properties (e.g., {@link EmailRequest#getHtmlBody()}).
     * </p>
     */
    private String body;

    /**
     * Optional metadata for extensibility and channel-specific features.
     * <p>
     * This map allows passing additional parameters without modifying the core interface.
     * Common use cases:
     * <ul>
     *   <li><b>Priority:</b> "high", "normal", "low"</li>
     *   <li><b>Tags:</b> For categorization and filtering</li>
     *   <li><b>Tracking:</b> Campaign IDs, reference numbers</li>
     *   <li><b>Vendor-specific:</b> Custom parameters for specific providers</li>
     * </ul>
     * </p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * Map<String, String> metadata = Map.of(
     *     "priority", "high",
     *     "campaign", "spring-sale-2024",
     *     "category", "promotional"
     * );
     * }</pre>
     */
    private Map<String, String> metadata;

    /**
     * Validates that all required fields are present and properly formatted.
     * <p>
     * This method should be called before processing the request to ensure
     * it contains all necessary information for message sending.
     * </p>
     *
     * @throws IllegalArgumentException if any required field is missing or invalid
     */
    public void validate() {
        if (channel == null) {
            throw new IllegalArgumentException("Communication channel is required");
        }
        if (agentId == null || agentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Agent ID is required");
        }
        if (recipient == null || recipient.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient is required");
        }
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("Message body is required");
        }
        if (!channel.isValidRecipientFormat(recipient)) {
            throw new IllegalArgumentException(
                String.format("Invalid recipient format '%s' for channel %s", recipient, channel)
            );
        }
    }

    /**
     * Adds a metadata entry to this request.
     * <p>
     * Convenience method for adding metadata without direct map manipulation.
     * Initializes the metadata map if it doesn't exist.
     * </p>
     *
     * @param key the metadata key
     * @param value the metadata value
     * @return this request instance for method chaining
     */
    public CommunicationRequest addMetadata(String key, String value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }

    /**
     * Gets a metadata value by key.
     *
     * @param key the metadata key
     * @return the metadata value, or null if not present
     */
    public String getMetadata(String key) {
        return this.metadata != null ? this.metadata.get(key) : null;
    }

    /**
     * Checks if metadata contains a specific key.
     *
     * @param key the metadata key to check
     * @return true if the key exists in metadata
     */
    public boolean hasMetadata(String key) {
        return this.metadata != null && this.metadata.containsKey(key);
    }
}
