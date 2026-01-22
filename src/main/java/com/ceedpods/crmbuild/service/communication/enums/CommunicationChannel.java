package com.ceedpods.crmbuild.service.communication.enums;

/**
 * Enumeration of supported communication channels in the CRM system.
 * <p>
 * This enum defines the available channels through which messages can be sent.
 * It serves as a vendor-agnostic abstraction, allowing the system to support
 * multiple vendors per channel without changing the interface contract.
 * </p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * CommunicationRequest request = CommunicationRequest.builder()
 *     .channel(CommunicationChannel.EMAIL)
 *     .recipient("user@example.com")
 *     .body("Hello World")
 *     .build();
 * }</pre>
 *
 * @since 1.0
 * @see com.ceedpods.crmbuild.service.communication.model.CommunicationRequest
 */
public enum CommunicationChannel {

    /**
     * Email communication channel.
     * <p>
     * Supports sending emails via various SMTP providers or email service APIs.
     * Examples: Gmail SMTP, SendGrid, Amazon SES, Mailgun, etc.
     * </p>
     */
    EMAIL,

    /**
     * SMS (Short Message Service) communication channel.
     * <p>
     * Supports sending text messages via various SMS gateway providers.
     * Examples: Twilio, AWS SNS, Vonage, MessageBird, etc.
     * </p>
     */
    SMS,

    /**
     * WhatsApp messaging channel.
     * <p>
     * Supports sending messages via WhatsApp Business API providers.
     * Examples: Meta WhatsApp Business API, Twilio WhatsApp, etc.
     * </p>
     */
    WHATSAPP;

    /**
     * Checks if this channel supports the given recipient identifier format.
     * <p>
     * This method provides basic validation for recipient format based on channel type:
     * <ul>
     *   <li>EMAIL: Should contain '@' symbol</li>
     *   <li>SMS/WHATSAPP: Should start with '+' (E.164 format recommended)</li>
     * </ul>
     * </p>
     *
     * @param recipient the recipient identifier to validate
     * @return true if the recipient format is appropriate for this channel
     * @throws IllegalArgumentException if recipient is null or empty
     */
    public boolean isValidRecipientFormat(String recipient) {
        if (recipient == null || recipient.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient cannot be null or empty");
        }

        return switch (this) {
            case EMAIL -> recipient.contains("@");
            case SMS, WHATSAPP -> recipient.trim().startsWith("+") || recipient.matches("\\d+");
        };
    }

    /**
     * Returns a human-readable description of this communication channel.
     *
     * @return a descriptive string for this channel
     */
    public String getDescription() {
        return switch (this) {
            case EMAIL -> "Email messaging via SMTP or API providers";
            case SMS -> "SMS text messaging via gateway providers";
            case WHATSAPP -> "WhatsApp messaging via Business API";
        };
    }
}
