package com.ceedpods.crmbuild.service.messaging;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailAddress;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.communication.email.models.EmailSendStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
@Slf4j
public class AzureEmailService {

    private static final int POLLING_TIMEOUT_SECONDS = 120;

    public String sendEmail(String recipientEmail, String subject, String messageBody,
                           Map<String, String> credentials) throws Exception {
        log.info("Sending email to: {}", recipientEmail);

        String connectionString = credentials.get("connectionString");
        String senderAddress = credentials.get("senderAddress");

        if (connectionString == null || connectionString.isEmpty()) {
            throw new Exception("Azure Communication Services connection string is missing");
        }

        if (senderAddress == null || senderAddress.isEmpty()) {
            throw new Exception("Sender email address is missing");
        }

        try {
            // Create email client
            EmailClient emailClient = new EmailClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();

            // Build email message
            EmailMessage emailMessage = new EmailMessage()
                    .setSenderAddress(senderAddress)
                    .setToRecipients(recipientEmail)
                    .setSubject(subject)
                    .setBodyPlainText(messageBody);

            log.debug("Initiating email send operation");

            // Send email and get poller
            SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(emailMessage);

            // Wait for completion with timeout
            PollResponse<EmailSendResult> response = poller.waitForCompletion(
                    Duration.ofSeconds(POLLING_TIMEOUT_SECONDS)
            );

            EmailSendResult result = response.getValue();
            String messageId = result.getId();
            EmailSendStatus status = result.getStatus();

            log.info("Email sent successfully. Message ID: {}, Status: {}", messageId, status);

            // Check final status
            if (status == EmailSendStatus.FAILED) {
                String errorMessage = result.getError() != null
                        ? result.getError().getMessage()
                        : "Unknown error";
                log.error("Azure Communication Services email failed. Message ID: {}, Error: {}",
                         messageId, errorMessage);
                throw new Exception("Email send failed: " + errorMessage);
            }

            return messageId;

        } catch (Exception e) {
            log.error("Error sending email via Azure Communication Services: {}", e.getMessage(), e);
            throw new Exception("Azure Communication Services error: " + e.getMessage(), e);
        }
    }
}
