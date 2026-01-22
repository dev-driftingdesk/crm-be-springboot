package com.ceedpods.crmbuild.service.communication.model;

import com.ceedpods.crmbuild.service.communication.enums.CommunicationChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Specialized communication request for sending SMS messages.
 * <p>
 * This class extends {@link CommunicationRequest} to provide SMS-specific features
 * and phone number validation. It automatically sets the channel to
 * {@link CommunicationChannel#SMS}.
 * </p>
 *
 * <p><b>Features:</b></p>
 * <ul>
 *   <li><b>Type-Safe Phone Number:</b> Explicitly typed recipient field</li>
 *   <li><b>E.164 Format Support:</b> International phone number standard</li>
 *   <li><b>Character Length Awareness:</b> Provides utilities for SMS length limits</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * SmsRequest request = SmsRequest.builder()
 *     .agentId("agent-123")
 *     .recipientPhone("+1234567890")
 *     .body("Your verification code is 123456")
 *     .addMetadata("type", "verification")
 *     .build();
 * }</pre>
 *
 * <p><b>Phone Number Format:</b></p>
 * <ul>
 *   <li><b>Recommended:</b> E.164 format (e.g., "+1234567890")</li>
 *   <li><b>Structure:</b> + [country code] [subscriber number]</li>
 *   <li><b>Max Length:</b> 15 digits (excluding the + sign)</li>
 *   <li><b>Example:</b> +1 555 123 4567 → "+15551234567"</li>
 * </ul>
 *
 * <p><b>SMS Length Considerations:</b></p>
 * <ul>
 *   <li><b>Single SMS:</b> Up to 160 characters (GSM-7 encoding)</li>
 *   <li><b>Unicode SMS:</b> Up to 70 characters (if special characters are used)</li>
 *   <li><b>Concatenated SMS:</b> Messages longer than limits are split into multiple parts</li>
 *   <li>Use {@link #getEstimatedSegmentCount()} to check message segments</li>
 * </ul>
 *
 * @since 1.0
 * @see CommunicationRequest
 * @see CommunicationChannel#SMS
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SmsRequest extends CommunicationRequest {

    /**
     * The phone number of the recipient in E.164 format.
     * <p>
     * This is a type-safe alternative to the generic {@link #getRecipient()} field.
     * When building an SmsRequest, this field is automatically synchronized with
     * the parent's recipient field.
     * </p>
     *
     * <p><b>Recommended Format:</b> E.164 (e.g., "+1234567890")</p>
     * <p><b>Country Code Examples:</b></p>
     * <ul>
     *   <li>United States/Canada: +1</li>
     *   <li>United Kingdom: +44</li>
     *   <li>India: +91</li>
     *   <li>Australia: +61</li>
     * </ul>
     */
    private String recipientPhone;

    /**
     * SMS character encoding limits.
     */
    private static final int GSM_7_SINGLE_LIMIT = 160;
    private static final int UNICODE_SINGLE_LIMIT = 70;
    private static final int GSM_7_CONCAT_LIMIT = 153;
    private static final int UNICODE_CONCAT_LIMIT = 67;

    /**
     * Validates SMS-specific requirements in addition to base validation.
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
            this.setChannel(CommunicationChannel.SMS);
        }

        // Sync recipientPhone with recipient field (bi-directional)
        if (this.recipientPhone != null && this.getRecipient() == null) {
            this.setRecipient(this.recipientPhone);
        } else if (this.getRecipient() != null && this.recipientPhone == null) {
            this.recipientPhone = this.getRecipient();
        }

        super.validate();

        if (this.getChannel() != CommunicationChannel.SMS) {
            throw new IllegalArgumentException("SmsRequest must use SMS channel");
        }

        // Validate phone number format (basic E.164 validation)
        String phone = this.getRecipient();
        if (!phone.matches("^\\+?[1-9]\\d{1,14}$")) {
            throw new IllegalArgumentException(
                "Invalid phone number format. Use E.164 format (e.g., +1234567890): " + phone
            );
        }
    }

    /**
     * Checks if the message body contains non-GSM-7 characters.
     * <p>
     * Messages with Unicode characters (emojis, certain accented characters, etc.)
     * have a lower character limit per SMS segment.
     * </p>
     *
     * @return true if the message requires Unicode encoding
     */
    public boolean requiresUnicodeEncoding() {
        if (this.getBody() == null) {
            return false;
        }

        // GSM-7 character set (simplified check)
        String gsm7Pattern = "^[@£$¥èéùìòÇØøÅåΔ_ΦΓΛΩΠΨΣΘΞÆæßÉ !\"#¤%&'()*+,\\-./0-9:;<=>?¡A-ZÄÖÑÜ§¿a-zäöñüà\\s\\r\\n]*$";
        return !this.getBody().matches(gsm7Pattern);
    }

    /**
     * Estimates the number of SMS segments this message will use.
     * <p>
     * This is an approximation based on standard SMS encoding rules.
     * Actual segment count may vary depending on the SMS provider and network.
     * </p>
     *
     * @return estimated number of SMS segments (1 or more)
     */
    public int getEstimatedSegmentCount() {
        if (this.getBody() == null || this.getBody().isEmpty()) {
            return 0;
        }

        int length = this.getBody().length();
        boolean isUnicode = requiresUnicodeEncoding();

        if (isUnicode) {
            if (length <= UNICODE_SINGLE_LIMIT) {
                return 1;
            }
            return (int) Math.ceil((double) length / UNICODE_CONCAT_LIMIT);
        } else {
            if (length <= GSM_7_SINGLE_LIMIT) {
                return 1;
            }
            return (int) Math.ceil((double) length / GSM_7_CONCAT_LIMIT);
        }
    }

    /**
     * Gets the character limit for a single SMS segment based on encoding.
     *
     * @return 160 for GSM-7 encoding, 70 for Unicode encoding
     */
    public int getSingleSegmentLimit() {
        return requiresUnicodeEncoding() ? UNICODE_SINGLE_LIMIT : GSM_7_SINGLE_LIMIT;
    }

    /**
     * Checks if the message will fit in a single SMS segment.
     *
     * @return true if the message fits in one SMS
     */
    public boolean isSingleSegment() {
        return getEstimatedSegmentCount() <= 1;
    }

    /**
     * Gets remaining characters before the next segment boundary.
     *
     * @return number of characters that can be added before a new segment is needed
     */
    public int getRemainingCharacters() {
        if (this.getBody() == null) {
            return getSingleSegmentLimit();
        }

        int length = this.getBody().length();
        int limit = getSingleSegmentLimit();

        if (length <= limit) {
            return limit - length;
        }

        // For multi-segment messages
        int concatLimit = requiresUnicodeEncoding() ? UNICODE_CONCAT_LIMIT : GSM_7_CONCAT_LIMIT;
        int segments = getEstimatedSegmentCount();
        return (segments * concatLimit) - length;
    }
}
