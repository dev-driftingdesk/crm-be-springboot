package com.ceedpods.crmbuild.service.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MetaWhatsAppService {

    private static final String WHATSAPP_API_URL = "https://graph.facebook.com/v18.0/";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String sendMessage(String recipientPhone, String messageBody, Map<String, String> credentials) throws Exception {
        log.info("Sending WhatsApp message to: {}", recipientPhone);

        String accessToken = credentials.get("accessToken");
        String phoneNumberId = credentials.get("phoneNumberId");

        String url = WHATSAPP_API_URL + phoneNumberId + "/messages";

        // Build request payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("to", recipientPhone);
        payload.put("type", "text");

        Map<String, String> textContent = new HashMap<>();
        textContent.put("body", messageBody);
        payload.put("text", textContent);

        String jsonPayload = objectMapper.writeValueAsString(payload);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonPayload, JSON))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error details";
                log.error("Meta WhatsApp API error. Status: {}, Body: {}", response.code(), errorBody);
                throw new Exception("Meta WhatsApp API error: " + errorBody);
            }

            String responseBody = response.body() != null ? response.body().string() : "{}";
            JsonNode responseJson = objectMapper.readTree(responseBody);

            String messageId = responseJson.path("messages").get(0).path("id").asText();
            log.info("WhatsApp message sent successfully. Message ID: {}", messageId);

            return messageId;
        }
    }
}
