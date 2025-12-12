package com.ceedpods.crmbuild.service.notification;

import com.ceedpods.crmbuild.config.FirebaseConfig;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = true)
public class FirebasePushNotificationService {

    private final FirebaseConfig firebaseConfig;

    private FirebaseMessaging getFirebaseMessaging() {
        return firebaseConfig.getFirebaseMessaging();
    }

    public boolean isAvailable() {
        return FirebaseConfig.isInitialized();
    }

    /**
     * Send notification to multiple device tokens
     */
    public BatchResponse sendToTokens(List<String> tokens, String title, String body,
                                       String imageUrl, Map<String, String> data) throws FirebaseMessagingException {

        MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .setImage(imageUrl)
                        .build())
                .addAllTokens(tokens);

        if (data != null && !data.isEmpty()) {
            messageBuilder.putAllData(data);
        }

        BatchResponse response = getFirebaseMessaging().sendEachForMulticast(messageBuilder.build());
        log.info("Sent notification to {} tokens. Success: {}, Failure: {}",
                tokens.size(), response.getSuccessCount(), response.getFailureCount());

        return response;
    }

    /**
     * Send notification to a single token
     */
    public String sendToToken(String token, String title, String body,
                              String imageUrl, Map<String, String> data) throws FirebaseMessagingException {

        Message.Builder messageBuilder = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .setImage(imageUrl)
                        .build());

        if (data != null && !data.isEmpty()) {
            messageBuilder.putAllData(data);
        }

        String response = getFirebaseMessaging().send(messageBuilder.build());
        log.info("Sent notification to token. Response: {}", response);

        return response;
    }

    /**
     * Send notification to a topic
     */
    public String sendToTopic(String topic, String title, String body,
                              String imageUrl, Map<String, String> data) throws FirebaseMessagingException {

        Message.Builder messageBuilder = Message.builder()
                .setTopic(topic)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .setImage(imageUrl)
                        .build());

        if (data != null && !data.isEmpty()) {
            messageBuilder.putAllData(data);
        }

        String response = getFirebaseMessaging().send(messageBuilder.build());
        log.info("Sent notification to topic '{}'. Response: {}", topic, response);

        return response;
    }

    /**
     * Subscribe tokens to a topic
     */
    public TopicManagementResponse subscribeToTopic(List<String> tokens, String topic)
            throws FirebaseMessagingException {
        TopicManagementResponse response = getFirebaseMessaging().subscribeToTopic(tokens, topic);
        log.info("Subscribed {} tokens to topic '{}'. Success: {}, Failure: {}",
                tokens.size(), topic, response.getSuccessCount(), response.getFailureCount());
        return response;
    }

    /**
     * Unsubscribe tokens from a topic
     */
    public TopicManagementResponse unsubscribeFromTopic(List<String> tokens, String topic)
            throws FirebaseMessagingException {
        TopicManagementResponse response = getFirebaseMessaging().unsubscribeFromTopic(tokens, topic);
        log.info("Unsubscribed {} tokens from topic '{}'. Success: {}, Failure: {}",
                tokens.size(), topic, response.getSuccessCount(), response.getFailureCount());
        return response;
    }
}
