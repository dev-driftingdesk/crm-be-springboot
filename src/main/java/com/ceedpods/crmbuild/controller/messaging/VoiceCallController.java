package com.ceedpods.crmbuild.controller.messaging;

import com.ceedpods.crmbuild.dto.messaging.MessageDTO;
import com.ceedpods.crmbuild.dto.request.InitiateCallRequest;
import com.ceedpods.crmbuild.dto.request.SaveTwilioVoiceCredentialRequest;
import com.ceedpods.crmbuild.dto.response.ApiResponse;
import com.ceedpods.crmbuild.entity.messaging.AgentCredential;
import com.ceedpods.crmbuild.entity.messaging.Message;
import com.ceedpods.crmbuild.mapper.MessageMapper;
import com.ceedpods.crmbuild.repository.AgentCredentialRepository;
import com.ceedpods.crmbuild.security.RequirePermission;
import com.ceedpods.crmbuild.service.messaging.EncryptionService;
import com.ceedpods.crmbuild.service.messaging.MessageDispatchService;
import com.ceedpods.crmbuild.service.messaging.TwilioVoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/voice")
@RequiredArgsConstructor
@Slf4j
public class VoiceCallController {

    private final MessageDispatchService messageDispatchService;
    private final AgentCredentialRepository credentialRepository;
    private final EncryptionService encryptionService;
    private final MessageMapper messageMapper;
    private final TwilioVoiceService twilioVoiceService;

    @Value("${app.url}")
    private String appUrl;

    /**
     * Save Twilio Voice credentials
     * POST /api/v1/voice/credentials
     */
    @PostMapping("/credentials")
    @RequirePermission("COMMUNICATION_SEND_SMS")
    public ResponseEntity<ApiResponse<String>> saveCredentials(
            @Valid @RequestBody SaveTwilioVoiceCredentialRequest request,
            Authentication authentication) {
        try {
            String agentId = getKeycloakId(authentication);
            log.info("Agent {} saving Twilio Voice credentials", agentId);

            // Build credentials map
            Map<String, String> credentials = new HashMap<>();
            credentials.put("accountSid", request.getAccountSid());
            credentials.put("authToken", request.getAuthToken());
            credentials.put("phoneNumber", request.getPhoneNumber());

            // Encrypt credentials
            Map<String, String> encrypted = encryptionService.encryptMap(credentials);

            // Check if credential exists
            AgentCredential credential = credentialRepository.findActiveByAgentIdAndChannel(agentId, "VOICE")
                    .orElse(AgentCredential.builder()
                            .id(UUID.randomUUID().toString())
                            .agentId(agentId)
                            .channel("VOICE")
                            .build());

            credential.setEncryptedCredentials(encrypted);
            credential.setActive(true);

            credentialRepository.save(credential);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Twilio Voice credentials saved successfully", null));

        } catch (Exception e) {
            log.error("Error saving Voice credentials: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to save Voice credentials: " + e.getMessage()));
        }
    }

    /**
     * Initiate a voice call between two system users via Twilio Conference
     * POST /api/v1/voice/call
     */
    @PostMapping("/call")
    @RequirePermission("COMMUNICATION_SEND_SMS")
    public ResponseEntity<ApiResponse<MessageDTO>> initiateCall(
            @Valid @RequestBody InitiateCallRequest request,
            Authentication authentication) {
        try {
            String callerUserId = getKeycloakId(authentication);
            log.info("User {} initiating voice call to user {}", callerUserId, request.getRecipientUserId());

            // Build status callback URL if not provided
            String statusCallbackUrl = request.getStatusCallbackUrl();
            if (statusCallbackUrl == null || statusCallbackUrl.isEmpty()) {
                statusCallbackUrl = appUrl + "/api/v1/voice/webhook/status";
            }

            Message message = messageDispatchService.initiateCall(
                    callerUserId,
                    request.getRecipientUserId(),
                    request.getRecord() != null ? request.getRecord() : false,
                    statusCallbackUrl
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Voice call initiated. Both users will receive calls.", messageMapper.toDTO(message)));

        } catch (Exception e) {
            log.error("Error initiating voice call: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to initiate call: " + e.getMessage()));
        }
    }

    /**
     * TwiML endpoint - Called by Twilio when a user answers the call
     * This endpoint returns TwiML that connects the user to the conference
     * GET /api/v1/voice/twiml/join-conference
     */
    @GetMapping(value = "/twiml/join-conference", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> joinConference(
            @RequestParam String conferenceName,
            @RequestParam(defaultValue = "false") boolean record,
            @RequestParam(required = false) String participant) {
        try {
            log.info("Generating TwiML for conference: {}, participant: {}", conferenceName, participant);

            // Determine conference settings based on participant
            // Caller starts the conference, recipient waits for caller
            boolean startOnEnter = "caller".equals(participant) || participant == null;
            boolean endOnExit = "caller".equals(participant);

            String twiml = twilioVoiceService.getConferenceTwiML(conferenceName, record, startOnEnter, endOnExit);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .body(twiml);

        } catch (Exception e) {
            log.error("Error generating conference TwiML: {}", e.getMessage(), e);
            // Return error TwiML
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .body("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Say>Sorry, an error occurred.</Say></Response>");
        }
    }

    /**
     * Get call details by message ID
     * GET /api/v1/voice/calls/{id}
     */
    @GetMapping("/calls/{id}")
    @RequirePermission("COMMUNICATION_SEND_SMS")
    public ResponseEntity<ApiResponse<MessageDTO>> getCall(
            @PathVariable String id,
            Authentication authentication) {
        try {
            String agentId = getKeycloakId(authentication);
            Message message = messageDispatchService.getMessageById(agentId, id);

            return ResponseEntity.ok(ApiResponse.success(messageMapper.toDTO(message)));

        } catch (Exception e) {
            log.error("Error fetching call: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to fetch call: " + e.getMessage()));
        }
    }

    /**
     * Terminate an ongoing call
     * POST /api/v1/voice/hangup/{callSid}
     */
    @PostMapping("/hangup/{callSid}")
    @RequirePermission("COMMUNICATION_SEND_SMS")
    public ResponseEntity<ApiResponse<String>> hangupCall(
            @PathVariable String callSid,
            Authentication authentication) {
        try {
            String agentId = getKeycloakId(authentication);
            log.info("Agent {} terminating call {}", agentId, callSid);

            messageDispatchService.terminateCall(agentId, callSid);

            return ResponseEntity.ok(ApiResponse.success("Call terminated successfully", null));

        } catch (Exception e) {
            log.error("Error terminating call: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to terminate call: " + e.getMessage()));
        }
    }

    /**
     * Webhook endpoint for Twilio call status updates
     * POST /api/v1/voice/webhook/status
     */
    @PostMapping("/webhook/status")
    public ResponseEntity<String> handleCallStatus(
            @RequestParam(required = false) String CallSid,
            @RequestParam(required = false) String CallStatus,
            @RequestParam(required = false) String CallDuration,
            @RequestParam(required = false) String RecordingUrl) {
        try {
            log.info("Received Twilio status update - CallSid: {}, Status: {}, Duration: {}",
                    CallSid, CallStatus, CallDuration);

            if (CallSid == null || CallSid.isEmpty()) {
                log.warn("CallSid is missing in webhook request");
                return ResponseEntity.ok("OK");
            }

            // Parse duration
            Long duration = null;
            if (CallDuration != null && !CallDuration.isEmpty()) {
                try {
                    duration = Long.parseLong(CallDuration);
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse CallDuration: {}", CallDuration);
                }
            }

            // Update call status in database
            messageDispatchService.updateCallStatus(CallSid, CallStatus, duration, RecordingUrl);

            return ResponseEntity.ok("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response></Response>");

        } catch (Exception e) {
            log.error("Error processing Twilio webhook: {}", e.getMessage(), e);
            return ResponseEntity.ok("OK");
        }
    }

    /**
     * Webhook endpoint for Twilio conference status updates
     * POST /api/v1/voice/webhook/conference-status
     */
    @PostMapping("/webhook/conference-status")
    public ResponseEntity<String> handleConferenceStatus(
            @RequestParam(required = false) String ConferenceSid,
            @RequestParam(required = false) String StatusCallbackEvent,
            @RequestParam(required = false) String FriendlyName,
            @RequestParam(required = false) String CallSid) {
        try {
            log.info("Conference event - Conference: {}, Event: {}, CallSid: {}",
                    ConferenceSid, StatusCallbackEvent, CallSid);

            // You can add logic here to update conference status in database
            // For example: when conference starts, when participants join/leave, when conference ends

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            log.error("Error processing conference webhook: {}", e.getMessage(), e);
            return ResponseEntity.ok("OK");
        }
    }

    /**
     * Webhook endpoint for recording status updates
     * POST /api/v1/voice/webhook/recording-status
     * Called by Twilio when recording is completed
     */
    @PostMapping("/webhook/recording-status")
    public ResponseEntity<String> handleRecordingStatus(
            @RequestParam(required = false) String RecordingSid,
            @RequestParam(required = false) String RecordingUrl,
            @RequestParam(required = false) String RecordingStatus,
            @RequestParam(required = false) String RecordingDuration,
            @RequestParam(required = false) String CallSid,
            @RequestParam(required = false) String ConferenceSid) {
        try {
            log.info("Recording completed - RecordingSid: {}, URL: {}, Status: {}, Duration: {}",
                    RecordingSid, RecordingUrl, RecordingStatus, RecordingDuration);

            if (RecordingSid != null && RecordingUrl != null) {
                // Update message with recording details
                messageDispatchService.updateRecordingInfo(RecordingSid, RecordingUrl, CallSid);
            }

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            log.error("Error processing recording webhook: {}", e.getMessage(), e);
            return ResponseEntity.ok("OK");
        }
    }

    /**
     * Webhook endpoint for transcription results
     * POST /api/v1/voice/webhook/transcription
     * Called by Twilio when transcription is completed
     */
    @PostMapping("/webhook/transcription")
    public ResponseEntity<String> handleTranscription(
            @RequestParam(required = false) String TranscriptionSid,
            @RequestParam(required = false) String TranscriptionText,
            @RequestParam(required = false) String TranscriptionStatus,
            @RequestParam(required = false) String TranscriptionUrl,
            @RequestParam(required = false) String RecordingSid,
            @RequestParam(required = false) String CallSid) {
        try {
            log.info("Transcription received - TranscriptionSid: {}, Status: {}, Text length: {}",
                    TranscriptionSid, TranscriptionStatus,
                    TranscriptionText != null ? TranscriptionText.length() : 0);

            if (TranscriptionSid != null && TranscriptionText != null) {
                // Store transcription in database
                messageDispatchService.updateTranscription(
                    TranscriptionSid,
                    TranscriptionText,
                    TranscriptionStatus,
                    TranscriptionUrl,
                    RecordingSid
                );

                log.info("Transcription saved successfully for RecordingSid: {}", RecordingSid);
            }

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            log.error("Error processing transcription webhook: {}", e.getMessage(), e);
            return ResponseEntity.ok("OK");
        }
    }

    /**
     * Get transcript for a specific call
     * GET /api/v1/voice/calls/{id}/transcript
     */
    @GetMapping("/calls/{id}/transcript")
    @RequirePermission("COMMUNICATION_SEND_SMS")
    public ResponseEntity<ApiResponse<String>> getTranscript(
            @PathVariable String id,
            Authentication authentication) {
        try {
            String agentId = getKeycloakId(authentication);
            Message message = messageDispatchService.getMessageById(agentId, id);

            if (message.getTranscriptionText() == null || message.getTranscriptionText().isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("Transcription not available yet. Please try again later."));
            }

            return ResponseEntity.ok(ApiResponse.success("Transcript retrieved successfully", message.getTranscriptionText()));

        } catch (Exception e) {
            log.error("Error fetching transcript: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to fetch transcript: " + e.getMessage()));
        }
    }

    private String getKeycloakId(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getSubject();
    }
}
