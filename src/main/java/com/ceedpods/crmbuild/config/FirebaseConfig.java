package com.ceedpods.crmbuild.config;

import com.ceedpods.crmbuild.entity.FirebaseConfiguration;
import com.ceedpods.crmbuild.repository.FirebaseConfigurationRepository;
import com.ceedpods.crmbuild.util.EncryptionUtil;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Configuration
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = true)
public class FirebaseConfig {

    private final FirebaseConfigurationRepository configRepository;
    private final EncryptionUtil encryptionUtil;
    private static boolean initialized = false;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseConfiguration config = configRepository.findByActiveTrue()
                        .orElse(null);

                if (config == null) {
                    log.warn("No active Firebase configuration found in database. Firebase features will be disabled. " +
                            "Use POST /admin/firebase-config to configure Firebase credentials.");
                    return;
                }

                String credentialsJson = buildCredentialsJson(config);

                GoogleCredentials credentials = GoogleCredentials.fromStream(
                        new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8))
                );

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .setProjectId(config.getProjectId())
                        .build();

                FirebaseApp.initializeApp(options);
                initialized = true;
                log.info("Firebase initialized successfully from MongoDB for project: {}", config.getProjectId());
            } else {
                initialized = true;
            }
        } catch (Exception e) {
            log.error("Failed to initialize Firebase from MongoDB: {}", e.getMessage(), e);
            // Don't throw - allow app to start, Firebase features will be disabled
        }
    }

    private String buildCredentialsJson(FirebaseConfiguration config) {
        // Decrypt encrypted fields
        String decryptedPrivateKeyId = encryptionUtil.decrypt(config.getPrivateKeyId());
        String decryptedPrivateKey = encryptionUtil.decrypt(config.getPrivateKey());
        String decryptedClientEmail = encryptionUtil.decrypt(config.getClientEmail());
        String decryptedClientId = encryptionUtil.decrypt(config.getClientId());

        return String.format("""
                {
                  "type": "%s",
                  "project_id": "%s",
                  "private_key_id": "%s",
                  "private_key": "%s",
                  "client_email": "%s",
                  "client_id": "%s",
                  "auth_uri": "%s",
                  "token_uri": "%s",
                  "auth_provider_x509_cert_url": "%s",
                  "client_x509_cert_url": "%s",
                  "universe_domain": "%s"
                }
                """,
                config.getType(),
                config.getProjectId(),
                decryptedPrivateKeyId,
                decryptedPrivateKey,
                decryptedClientEmail,
                decryptedClientId,
                config.getAuthUri(),
                config.getTokenUri(),
                config.getAuthProviderX509CertUrl(),
                config.getClientX509CertUrl(),
                config.getUniverseDomain()
        );
    }

    /**
     * Get FirebaseMessaging instance. Call this dynamically when needed.
     * @return FirebaseMessaging instance
     * @throws IllegalStateException if Firebase is not initialized
     */
    public FirebaseMessaging getFirebaseMessaging() {
        if (!initialized) {
            throw new IllegalStateException("Firebase is not initialized. Please configure Firebase credentials via POST /admin/firebase-config");
        }
        return FirebaseMessaging.getInstance();
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
