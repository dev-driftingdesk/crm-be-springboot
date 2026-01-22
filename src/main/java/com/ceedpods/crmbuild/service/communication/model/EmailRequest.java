package com.ceedpods.crmbuild.service.communication.model;

import com.ceedpods.crmbuild.service.communication.enums.CommunicationChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Specialized communication request for sending emails.
 * <p>
 * This class extends {@link CommunicationRequest} to provide email-specific features
 * such as subject lines and HTML content support. It automatically sets the channel
 * to {@link CommunicationChannel#EMAIL}.
 * </p>
 *
 * <p><b>Features:</b></p>
 * <ul>
 *   <li><b>Subject Line:</b> Required for all emails</li>
 *   <li><b>HTML Support:</b> Optional HTML body for rich formatting</li>
 *   <li><b>Type-Safe Recipient:</b> Explicitly typed as email address</li>
 *   <li><b>Plain Text Fallback:</b> Body field serves as plain text version</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * EmailRequest request = EmailRequest.builder()
 *     .agentId("agent-123")
 *     .recipientEmail("customer@example.com")
 *     .subject("Welcome to our CRM!")
 *     .body("Welcome! We're glad to have you.")
 *     .htmlBody("<h1>Welcome!</h1><p>We're glad to have you.</p>")
 *     .addMetadata("campaign", "onboarding")
 *     .build();
 * }</pre>
 *
 * <p><b>Plain Text vs HTML:</b></p>
 * <ul>
 *   <li><b>body:</b> Always required, serves as plain text version</li>
 *   <li><b>htmlBody:</b> Optional, provides rich HTML formatting</li>
 *   <li>If htmlBody is provided, most email clients will prefer it over plain text</li>
 *   <li>Always include meaningful plain text for accessibility and email client compatibility</li>
 * </ul>
 *
 * @since 1.0
 * @see CommunicationRequest
 * @see CommunicationChannel#EMAIL
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EmailRequest extends CommunicationRequest {

    /**
     * The email address of the recipient.
     * <p>
     * This is a type-safe alternative to the generic {@link #getRecipient()} field.
     * When building an EmailRequest, this field is automatically synchronized with
     * the parent's recipient field.
     * </p>
     *
     * <p><b>Format:</b> Must be a valid email address (e.g., "user@example.com")</p>
     */
    private String recipientEmail;

    /**
     * The email subject line.
     * <p>
     * This is a required field for all emails. A clear, concise subject line
     * improves email deliverability and user engagement.
     * </p>
     *
     * <p><b>Best Practices:</b></p>
     * <ul>
     *   <li>Keep it under 50 characters for better mobile display</li>
     *   <li>Avoid spam trigger words (FREE, URGENT, etc.)</li>
     *   <li>Make it descriptive and relevant to the content</li>
     * </ul>
     */
    private String subject;

    /**
     * Optional HTML version of the email body.
     * <p>
     * If provided, this will be used as the primary email content for email clients
     * that support HTML. The plain text {@link #getBody()} field should always be
     * populated as a fallback.
     * </p>
     *
     * <p><b>Security Note:</b></p>
     * Ensure HTML content is properly sanitized to prevent XSS attacks when
     * constructing emails from user-provided data.
     *
     * <p><b>Example HTML:</b></p>
     * <pre>{@code
     * String html = """
     *     <html>
     *       <body>
     *         <h1>Welcome to CRM!</h1>
     *         <p>Thank you for joining us.</p>
     *       </body>
     *     </html>
     *     """;
     * }</pre>
     */
    private String htmlBody;

    /**
     * Validates email-specific requirements in addition to base validation.
     * <p>
     * This method also handles field synchronization between recipientEmail and recipient.
     * </p>
     *
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public void validate() {
        // Set channel before parent validation
        if (this.getChannel() == null) {
            this.setChannel(CommunicationChannel.EMAIL);
        }

        // Sync recipientEmail with recipient field (bi-directional)
        if (this.recipientEmail != null && this.getRecipient() == null) {
            this.setRecipient(this.recipientEmail);
        } else if (this.getRecipient() != null && this.recipientEmail == null) {
            this.recipientEmail = this.getRecipient();
        }

        super.validate();

        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Email subject is required");
        }

        if (this.getChannel() != CommunicationChannel.EMAIL) {
            throw new IllegalArgumentException("EmailRequest must use EMAIL channel");
        }

        // Validate email format
        String email = this.getRecipient();
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email address format: " + email);
        }
    }

    /**
     * Checks if this email has HTML content.
     *
     * @return true if htmlBody is not null and not empty
     */
    public boolean hasHtmlBody() {
        return htmlBody != null && !htmlBody.trim().isEmpty();
    }

    /**
     * Gets the effective body content (HTML if available, otherwise plain text).
     *
     * @return HTML body if present, otherwise plain text body
     */
    public String getEffectiveBody() {
        return hasHtmlBody() ? htmlBody : this.getBody();
    }
}
