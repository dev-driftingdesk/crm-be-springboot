package com.ceedpods.crmbuild.service.communication;

import com.ceedpods.crmbuild.service.communication.enums.CommunicationChannel;
import com.ceedpods.crmbuild.service.communication.exception.ChannelNotSupportedException;
import com.ceedpods.crmbuild.service.communication.exception.CommunicationException;
import com.ceedpods.crmbuild.service.communication.exception.CredentialsNotFoundException;
import com.ceedpods.crmbuild.service.communication.exception.MessageSendException;
import com.ceedpods.crmbuild.service.communication.model.*;

/**
 * Core communication service interface defining the vendor-agnostic API contract
 * for all outbound communications in the CRM system.
 * <p>
 * This interface serves as the single, standardized point of reference for all
 * communication operations. It abstracts away vendor-specific implementations
 * (Twilio, Meta, SendGrid, etc.) allowing the system to remain vendor-neutral
 * and enabling easy provider switching without code changes.
 * </p>
 *
 * <p><b>Design Goals:</b></p>
 * <ul>
 *   <li><b>Vendor Independence:</b> No vendor-specific types in method signatures</li>
 *   <li><b>Consistency:</b> Uniform interface across all communication channels</li>
 *   <li><b>Type Safety:</b> Strong typing with channel-specific request objects</li>
 *   <li><b>Extensibility:</b> Easy to add new channels and vendors</li>
 *   <li><b>Traceability:</b> All operations return standardized result objects</li>
 * </ul>
 *
 * <p><b>Supported Channels:</b></p>
 * <ul>
 *   <li>{@link CommunicationChannel#EMAIL} - Email via SMTP or API providers</li>
 *   <li>{@link CommunicationChannel#SMS} - SMS via gateway providers</li>
 *   <li>{@link CommunicationChannel#WHATSAPP} - WhatsApp via Business API</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * // Sending an email
 * EmailRequest emailRequest = EmailRequest.builder()
 *     .agentId("agent-123")
 *     .recipientEmail("customer@example.com")
 *     .subject("Welcome to our CRM")
 *     .body("Thank you for signing up!")
 *     .build();
 *
 * CommunicationResult result = communicationService.sendEmail(emailRequest);
 *
 * if (result.isSuccess()) {
 *     logger.info("Email sent. Message ID: {}", result.getMessageId());
 * } else {
 *     logger.error("Email failed: {}", result.getErrorDetails().getMessage());
 * }
 *
 * // Sending an SMS
 * SmsRequest smsRequest = SmsRequest.builder()
 *     .agentId("agent-123")
 *     .recipientPhone("+1234567890")
 *     .body("Your verification code is 123456")
 *     .build();
 *
 * CommunicationResult smsResult = communicationService.sendSms(smsRequest);
 *
 * // Generic send (channel determined by request)
 * CommunicationRequest request = CommunicationRequest.builder()
 *     .channel(CommunicationChannel.WHATSAPP)
 *     .agentId("agent-123")
 *     .recipient("+1234567890")
 *     .body("Hello from CRM!")
 *     .build();
 *
 * CommunicationResult genericResult = communicationService.sendMessage(request);
 * }</pre>
 *
 * <p><b>Implementation Notes for Vendors:</b></p>
 * <ul>
 *   <li>Implementations should validate requests before sending</li>
 *   <li>All exceptions should be wrapped in {@link CommunicationException} subtypes</li>
 *   <li>Message IDs should be generated and tracked in the CRM database</li>
 *   <li>Vendor message IDs should be captured in {@link CommunicationResult#getVendorMessageId()}</li>
 *   <li>Failed messages should include detailed error information</li>
 *   <li>Retryable errors should be marked as such in {@link CommunicationResult.ErrorDetails}</li>
 * </ul>
 *
 * <p><b>Integration with CRMDD-118 (Outbound Dispatch Service):</b></p>
 * This interface is designed to be consumed by the Outbound Dispatch Service,
 * which will handle routing, retry logic, and orchestration. Individual service
 * implementations should focus on vendor-specific communication logic only.
 *
 * @since 1.0
 * @see CommunicationRequest
 * @see CommunicationResult
 * @see CommunicationChannel
 * @see EmailRequest
 * @see SmsRequest
 * @see WhatsAppRequest
 */
public interface CommunicationService {

    /**
     * Sends a message through the specified communication channel.
     * <p>
     * This is the generic send method that routes to the appropriate channel
     * based on the request's {@link CommunicationRequest#getChannel()} field.
     * For type-safe sending, consider using channel-specific methods like
     * {@link #sendEmail(EmailRequest)} or {@link #sendSms(SmsRequest)}.
     * </p>
     *
     * <p><b>Process Flow:</b></p>
     * <ol>
     *   <li>Validate the communication request</li>
     *   <li>Retrieve agent credentials for the specified channel</li>
     *   <li>Create message record in database (PENDING status)</li>
     *   <li>Send message via appropriate vendor service</li>
     *   <li>Update message status (SENT or FAILED)</li>
     *   <li>Return standardized result</li>
     * </ol>
     *
     * @param request the communication request containing all necessary information
     * @return result object containing message ID, status, and any error details
     * @throws IllegalArgumentException if request validation fails
     * @throws CredentialsNotFoundException if agent credentials are not found for the channel
     * @throws ChannelNotSupportedException if the requested channel is not supported
     * @throws MessageSendException if message sending fails
     * @throws CommunicationException for other communication-related errors
     */
    CommunicationResult sendMessage(CommunicationRequest request);

    /**
     * Sends an email message.
     * <p>
     * Type-safe method for sending emails with subject lines and optional HTML content.
     * This method automatically routes to email-specific providers (SMTP, SendGrid, etc.).
     * </p>
     *
     * <p><b>Email Features:</b></p>
     * <ul>
     *   <li>Subject line support</li>
     *   <li>HTML body support with plain text fallback</li>
     *   <li>Email-specific validation (address format, etc.)</li>
     *   <li>Professional email templates (if configured)</li>
     * </ul>
     *
     * @param request the email request with recipient, subject, and body
     * @return result object containing email ID, send status, and any error details
     * @throws IllegalArgumentException if email request validation fails
     * @throws CredentialsNotFoundException if email credentials are not configured for the agent
     * @throws MessageSendException if email sending fails
     * @throws CommunicationException for other email-related errors
     */
    CommunicationResult sendEmail(EmailRequest request);

    /**
     * Sends an SMS message.
     * <p>
     * Type-safe method for sending SMS messages to phone numbers.
     * This method automatically routes to SMS gateway providers (Twilio, AWS SNS, etc.).
     * </p>
     *
     * <p><b>SMS Considerations:</b></p>
     * <ul>
     *   <li>Phone numbers should be in E.164 format (e.g., +1234567890)</li>
     *   <li>Message length limits apply (160 chars for GSM-7, 70 for Unicode)</li>
     *   <li>Long messages will be split into multiple segments</li>
     *   <li>Delivery confirmation may not be available from all providers</li>
     * </ul>
     *
     * @param request the SMS request with recipient phone number and message body
     * @return result object containing SMS ID, send status, and any error details
     * @throws IllegalArgumentException if SMS request validation fails (invalid phone format, etc.)
     * @throws CredentialsNotFoundException if SMS credentials are not configured for the agent
     * @throws MessageSendException if SMS sending fails
     * @throws CommunicationException for other SMS-related errors
     */
    CommunicationResult sendSms(SmsRequest request);

    /**
     * Sends a WhatsApp message.
     * <p>
     * Type-safe method for sending WhatsApp messages via WhatsApp Business API.
     * This method automatically routes to WhatsApp API providers (Meta, Twilio, etc.).
     * </p>
     *
     * <p><b>WhatsApp Requirements:</b></p>
     * <ul>
     *   <li>Recipient must have WhatsApp installed on their phone</li>
     *   <li>Phone numbers must be in E.164 format with + prefix</li>
     *   <li>Opt-in required: Recipients must consent to receive messages</li>
     *   <li>24-hour window: Free-form messages allowed within 24h of user message</li>
     *   <li>Templates: Pre-approved templates required outside 24-hour window</li>
     * </ul>
     *
     * @param request the WhatsApp request with recipient phone number and message body
     * @return result object containing WhatsApp message ID, send status, and any error details
     * @throws IllegalArgumentException if WhatsApp request validation fails
     * @throws CredentialsNotFoundException if WhatsApp credentials are not configured for the agent
     * @throws MessageSendException if WhatsApp message sending fails
     * @throws CommunicationException for other WhatsApp-related errors
     */
    CommunicationResult sendWhatsApp(WhatsAppRequest request);

    /**
     * Retrieves the current status of a previously sent message.
     * <p>
     * This method allows tracking the lifecycle of a message after it has been sent.
     * The returned result may include updated status (e.g., DELIVERED) if the
     * vendor has provided delivery confirmation.
     * </p>
     *
     * <p><b>Status Updates:</b></p>
     * <ul>
     *   <li>Initial status is typically PENDING or SENT</li>
     *   <li>Status may update to DELIVERED when vendor confirms delivery</li>
     *   <li>Status may update to FAILED if vendor reports failure</li>
     *   <li>Not all vendors support delivery confirmation</li>
     * </ul>
     *
     * @param messageId the internal CRM message ID (from {@link CommunicationResult#getMessageId()})
     * @return result object with current status and metadata
     * @throws IllegalArgumentException if messageId is null or empty
     * @throws CommunicationException if message is not found or status check fails
     */
    CommunicationResult getMessageStatus(String messageId);

    /**
     * Checks if a specific communication channel is supported by this service.
     * <p>
     * This method can be used to verify channel availability before attempting
     * to send messages, allowing for graceful degradation or alternative channel selection.
     * </p>
     *
     * @param channel the communication channel to check
     * @return true if the channel is supported and available
     */
    boolean isChannelSupported(CommunicationChannel channel);

    /**
     * Checks if credentials are configured for a specific agent and channel.
     * <p>
     * This method allows proactive checking of credential availability before
     * attempting to send messages, enabling better error handling and user guidance.
     * </p>
     *
     * @param agentId the agent identifier
     * @param channel the communication channel
     * @return true if credentials exist and are active for the agent/channel combination
     */
    boolean hasCredentials(String agentId, CommunicationChannel channel);
}
