package com.ceedpods.crmbuild.service.messaging;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Map;

@Service
@Slf4j
public class TwilioVoiceService {

    @Value("${app.url}")
    private String appUrl;

    /**
     * Initiate a conference call between two users
     * This method calls the first user (caller) and connects them to a conference room
     */
    public String initiateConferenceCall(String callerPhone, String conferenceName,
                                        Map<String, String> credentials,
                                        Boolean record,
                                        String statusCallbackUrl) throws Exception {
        log.info("Initiating conference call for caller: {}, conference: {}", callerPhone, conferenceName);

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

            log.debug("Calling user at {} to join conference {}", callerPhone, conferenceName);

            // Build the TwiML URL that will connect the user to the conference
            String twimlUrl = appUrl + "/api/v1/voice/twiml/join-conference?conferenceName=" +
                             escapeUrlParam(conferenceName) + "&record=" + (record != null && record);

            // Build call creator
            var callCreator = Call.creator(
                new PhoneNumber(callerPhone),     // To - user's phone
                new PhoneNumber(fromPhoneNumber), // From - Twilio number
                URI.create(twimlUrl)              // TwiML URL to join conference
            );

            // Add status callback URL if provided
            if (statusCallbackUrl != null && !statusCallbackUrl.isEmpty()) {
                callCreator.setStatusCallback(URI.create(statusCallbackUrl));
                log.debug("Status callback URL set: {}", statusCallbackUrl);
            }

            // Create the call
            Call call = callCreator.create();

            String callSid = call.getSid();
            String status = call.getStatus().toString();

            log.info("Conference call initiated successfully. Call SID: {}, Status: {}", callSid, status);

            // Check if call failed immediately
            if ("failed".equalsIgnoreCase(status) || "canceled".equalsIgnoreCase(status)) {
                String errorMessage = "Call failed with status: " + status;
                log.error("Twilio call failed. Call SID: {}, Error: {}", callSid, errorMessage);
                throw new Exception("Call initiation failed: " + errorMessage);
            }

            return callSid;

        } catch (Exception e) {
            log.error("Error initiating conference call via Twilio: {}", e.getMessage(), e);
            throw new Exception("Twilio error: " + e.getMessage(), e);
        } finally {
            // Clean up Twilio initialization to avoid memory leaks
            Twilio.destroy();
        }
    }

    /**
     * Get TwiML to connect a user to a conference room
     * This is called by Twilio when the user answers the phone
     */
    public String getConferenceTwiML(String conferenceName, boolean record, boolean startOnEnter, boolean endOnExit) {
        StringBuilder twiml = new StringBuilder();
        twiml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        twiml.append("<Response>");
        twiml.append("<Dial>");
        twiml.append("<Conference");

        // Conference attributes
        if (startOnEnter) {
            twiml.append(" startConferenceOnEnter=\"true\"");
        } else {
            twiml.append(" startConferenceOnEnter=\"false\"");
        }

        if (endOnExit) {
            twiml.append(" endConferenceOnExit=\"true\"");
        } else {
            twiml.append(" endConferenceOnExit=\"false\"");
        }

        // Enable recording with transcription
        if (record) {
            twiml.append(" record=\"record-from-start\"");
            twiml.append(" recordingStatusCallback=\"").append(appUrl).append("/api/v1/voice/webhook/recording-status\"");
            twiml.append(" recordingStatusCallbackEvent=\"completed\"");
            // Enable automatic transcription
            twiml.append(" transcribe=\"true\"");
            twiml.append(" transcribeCallback=\"").append(appUrl).append("/api/v1/voice/webhook/transcription\"");
        }

        twiml.append(" statusCallback=\"").append(appUrl).append("/api/v1/voice/webhook/conference-status\"");
        twiml.append(" statusCallbackEvent=\"start end join leave\"");

        twiml.append(">");
        twiml.append(escapeXml(conferenceName));
        twiml.append("</Conference>");
        twiml.append("</Dial>");
        twiml.append("</Response>");

        return twiml.toString();
    }

    /**
     * Escape XML special characters to prevent TwiML injection
     */
    private String escapeXml(String input) {
        if (input == null) {
            return "";
        }
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }

    /**
     * Escape URL parameters
     */
    private String escapeUrlParam(String input) {
        if (input == null) {
            return "";
        }
        try {
            return java.net.URLEncoder.encode(input, "UTF-8");
        } catch (Exception e) {
            return input;
        }
    }

    /**
     * Terminate an ongoing call
     */
    public void terminateCall(String callSid, Map<String, String> credentials) throws Exception {
        log.info("Terminating call: {}", callSid);

        String accountSid = credentials.get("accountSid");
        String authToken = credentials.get("authToken");

        if (accountSid == null || accountSid.isEmpty()) {
            throw new Exception("Twilio Account SID is missing");
        }

        if (authToken == null || authToken.isEmpty()) {
            throw new Exception("Twilio Auth Token is missing");
        }

        try {
            // Initialize Twilio client
            Twilio.init(accountSid, authToken);

            // Update call status to completed (hangs up the call)
            Call call = Call.updater(callSid)
                .setStatus(Call.UpdateStatus.COMPLETED)
                .update();

            log.info("Call terminated successfully. Call SID: {}, Final Status: {}",
                callSid, call.getStatus());

        } catch (Exception e) {
            log.error("Error terminating call via Twilio: {}", e.getMessage(), e);
            throw new Exception("Twilio error: " + e.getMessage(), e);
        } finally {
            // Clean up Twilio initialization
            Twilio.destroy();
        }
    }

    /**
     * Fetch call details from Twilio
     */
    public Call getCallDetails(String callSid, Map<String, String> credentials) throws Exception {
        log.info("Fetching call details for: {}", callSid);

        String accountSid = credentials.get("accountSid");
        String authToken = credentials.get("authToken");

        if (accountSid == null || accountSid.isEmpty()) {
            throw new Exception("Twilio Account SID is missing");
        }

        if (authToken == null || authToken.isEmpty()) {
            throw new Exception("Twilio Auth Token is missing");
        }

        try {
            // Initialize Twilio client
            Twilio.init(accountSid, authToken);

            Call call = Call.fetcher(callSid).fetch();

            log.info("Call details fetched successfully. Call SID: {}, Status: {}, Duration: {}",
                callSid, call.getStatus(), call.getDuration());

            return call;

        } catch (Exception e) {
            log.error("Error fetching call details from Twilio: {}", e.getMessage(), e);
            throw new Exception("Twilio error: " + e.getMessage(), e);
        } finally {
            // Clean up Twilio initialization
            Twilio.destroy();
        }
    }
}
