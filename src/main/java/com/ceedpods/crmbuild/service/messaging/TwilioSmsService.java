package com.ceedpods.crmbuild.service.messaging;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class TwilioSmsService {

    public String sendSms(String recipientPhone, String messageBody,
                         Map<String, String> credentials) throws Exception {
        log.info("Sending SMS to: {}", recipientPhone);

        String accountSid = credentials.get("accountSid");
        String authToken = credentials.get("authToken");
        String fromPhoneNumber = credentials.get("phoneNumber");

        if (accountSid == null || accountSid.isEmpty()) {
            throw new Exception("Twilio Account SID is missing");
        }

        if (authToken == null || authToken.isEmpty()) {
            throw new Exception("Twilio Auth Token is missing");
        }

        if (fromPhoneNumber == null || fromPhoneNumber.isEmpty()) {
            throw new Exception("Twilio phone number is missing");
        }

        try {
            // Initialize Twilio client
            Twilio.init(accountSid, authToken);

            log.debug("Initiating SMS send operation from {} to {}", fromPhoneNumber, recipientPhone);

            // Send SMS
            Message message = Message.creator(
                    new PhoneNumber(recipientPhone),  // To
                    new PhoneNumber(fromPhoneNumber), // From
                    messageBody                        // Body
            ).create();

            String messageSid = message.getSid();
            String status = message.getStatus().toString();

            log.info("SMS sent successfully. Message SID: {}, Status: {}", messageSid, status);

            // Check if message failed
            if ("failed".equalsIgnoreCase(status) || "undelivered".equalsIgnoreCase(status)) {
                String errorMessage = message.getErrorMessage() != null
                        ? message.getErrorMessage()
                        : "Unknown error";
                log.error("Twilio SMS failed. Message SID: {}, Error: {}", messageSid, errorMessage);
                throw new Exception("SMS send failed: " + errorMessage);
            }

            return messageSid;

        } catch (Exception e) {
            log.error("Error sending SMS via Twilio: {}", e.getMessage(), e);
            throw new Exception("Twilio error: " + e.getMessage(), e);
        } finally {
            // Clean up Twilio initialization to avoid memory leaks
            Twilio.destroy();
        }
    }
}
