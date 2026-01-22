package com.ceedpods.crmbuild.service.communication.model;

import com.ceedpods.crmbuild.service.communication.enums.CommunicationChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Specialized communication request for sending WhatsApp messages.
 * <p>
 * This class extends {@link CommunicationRequest} to provide WhatsApp-specific features
 * and validation. It automatically sets the channel to {@link CommunicationChannel#WHATSAPP}.
 * </p>
 *
 * <p><b>Features:</b></p>
 * <ul>
 *   <li><b>Type-Safe Phone Number:</b> Explicitly typed recipient field</li>
 *   <li><b>E.164 Format Support:</b> WhatsApp requires international phone format</li>
 *   <li><b>Business API Support:</b> Designed for WhatsApp Business API integration</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * WhatsAppRequest request = WhatsAppRequest.builder()
 *     .agentId("agent-123")
 *     .recipientPhone("+1234567890")
 *     .body("Hello! Thank you for contacting us.")
 *     .addMetadata("template", "customer_support")
 *     .build();
 * }</pre>
 *
 * <p><b>Phone Number Format:</b></p>
 * <ul>
 *   <li><b>Required:</b> E.164 format (e.g., "+1234567890")</li>
 *   <li><b>Structure:</b> + [country code] [subscriber number]</li>
 *   <li><b>Must include:</b> Plus sign (+) and country code</li>
 *   <li><b>No spaces/dashes:</b> Numbers only after the +</li>
 * </ul>
 *
 * <p><b>WhatsApp Business API Notes:</b></p>
 * <ul>
 *   <li><b>Phone Verification:</b> Recipient must have WhatsApp installed</li>
 *   <li><b>Opt-in Required:</b> Recipient must have consented to receive messages</li>
 *   <li><b>24-Hour Window:</b> Free-form messages allowed within 24 hours of user message</li>
 *   <li><b>Templates:</b> Pre-approved templates required for messages outside 24-hour window</li>
 *   <li><b>Message Length:</b> Up to 4096 characters (much longer than SMS)</li>
 * </ul>
 *
 * <p><b>Template Messages:</b></p>
 * If using WhatsApp templates, specify template information in metadata:
 * <pre>{@code
 * WhatsAppRequest request = WhatsAppRequest.builder()
 *     .recipientPhone("+1234567890")
 *     .body("Fallback message text")
 *     .addMetadata("template_name", "order_confirmation")
 *     .addMetadata("template_language", "en")
 *     .addMetadata("template_params", "OrderID-12345,Total-$99.99")
 *     .build();
 * }</pre>
 *
 * @since 1.0
 * @see CommunicationRequest
 * @see CommunicationChannel#WHATSAPP
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WhatsAppRequest extends CommunicationRequest {

    /**
     * The phone number of the recipient in E.164 format.
     * <p>
     * This is a type-safe alternative to the generic {@link #getRecipient()} field.
     * When building a WhatsAppRequest, this field is automatically synchronized with
     * the parent's recipient field.
     * </p>
     *
     * <p><b>Required Format:</b> E.164 (e.g., "+1234567890")</p>
     * <p><b>Important:</b> WhatsApp strictly requires the E.164 format with + prefix</p>
     */
    private String recipientPhone;

    /**
     * Maximum allowed message length for WhatsApp.
     * WhatsApp supports much longer messages than SMS.
     */
    private static final int MAX_MESSAGE_LENGTH = 4096;

    /**
     * Validates WhatsApp-specific requirements in addition to base validation.
     * <p>
     * This method also handles field synchronization between recipientPhone and recipient.
     * </p>
     *
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public void validate() {
        // Set channel before parent validation
        if (this.getChannel() == null) {
            this.setChannel(CommunicationChannel.WHATSAPP);
        }

        // Sync recipientPhone with recipient field (bi-directional)
        if (this.recipientPhone != null && this.getRecipient() == null) {
            this.setRecipient(this.recipientPhone);
        } else if (this.getRecipient() != null && this.recipientPhone == null) {
            this.recipientPhone = this.getRecipient();
        }

        super.validate();

        if (this.getChannel() != CommunicationChannel.WHATSAPP) {
            throw new IllegalArgumentException("WhatsAppRequest must use WHATSAPP channel");
        }

        // Validate phone number format - WhatsApp requires E.164 with + prefix
        String phone = this.getRecipient();
        if (!phone.startsWith("+")) {
            throw new IllegalArgumentException(
                "WhatsApp phone number must start with + (E.164 format required): " + phone
            );
        }

        if (!phone.matches("^\\+[1-9]\\d{1,14}$")) {
            throw new IllegalArgumentException(
                "Invalid WhatsApp phone number format. Use E.164 format (e.g., +1234567890): " + phone
            );
        }

        // Validate message length
        if (this.getBody() != null && this.getBody().length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException(
                String.format("WhatsApp message exceeds maximum length of %d characters (current: %d)",
                    MAX_MESSAGE_LENGTH, this.getBody().length())
            );
        }
    }

    /**
     * Checks if this request is using a WhatsApp template.
     * <p>
     * Template messages are identified by the presence of template metadata.
     * </p>
     *
     * @return true if template_name metadata is present
     */
    public boolean isTemplateMessage() {
        return hasMetadata("template_name") || hasMetadata("templateName");
    }

    /**
     * Gets the template name if this is a template message.
     *
     * @return template name or null if not a template message
     */
    public String getTemplateName() {
        String name = getMetadata("template_name");
        return name != null ? name : getMetadata("templateName");
    }

    /**
     * Gets the template language code if specified.
     * <p>
     * Common language codes: "en" (English), "es" (Spanish), "pt" (Portuguese), etc.
     * </p>
     *
     * @return language code or "en" as default
     */
    public String getTemplateLanguage() {
        String lang = getMetadata("template_language");
        if (lang == null) {
            lang = getMetadata("templateLanguage");
        }
        return lang != null ? lang : "en";
    }

    /**
     * Checks if the message is within WhatsApp's length limit.
     *
     * @return true if message length is acceptable
     */
    public boolean isWithinLengthLimit() {
        return this.getBody() == null || this.getBody().length() <= MAX_MESSAGE_LENGTH;
    }

    /**
     * Gets the number of characters remaining before hitting the length limit.
     *
     * @return remaining character count
     */
    public int getRemainingCharacters() {
        if (this.getBody() == null) {
            return MAX_MESSAGE_LENGTH;
        }
        return Math.max(0, MAX_MESSAGE_LENGTH - this.getBody().length());
    }

    /**
     * Gets the maximum allowed message length for WhatsApp.
     *
     * @return maximum message length (4096 characters)
     */
    public static int getMaxMessageLength() {
        return MAX_MESSAGE_LENGTH;
    }

    /**
     * Normalizes the phone number to ensure it's in proper E.164 format.
     * <p>
     * This is a convenience method that adds the + prefix if missing.
     * Use with caution - it's always better to require proper format from input.
     * </p>
     *
     * @param phoneNumber the phone number to normalize
     * @return normalized phone number with + prefix
     */
    public static String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return phoneNumber;
        }

        String trimmed = phoneNumber.trim().replaceAll("[\\s-]", "");

        if (!trimmed.startsWith("+")) {
            return "+" + trimmed;
        }

        return trimmed;
    }
}
