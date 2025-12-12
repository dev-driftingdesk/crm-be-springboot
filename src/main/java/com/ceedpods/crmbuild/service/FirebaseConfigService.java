package com.ceedpods.crmbuild.service;

import com.ceedpods.crmbuild.dto.request.FirebaseConfigRequest;
import com.ceedpods.crmbuild.entity.FirebaseConfiguration;
import com.ceedpods.crmbuild.repository.FirebaseConfigurationRepository;
import com.ceedpods.crmbuild.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FirebaseConfigService {

    private final FirebaseConfigurationRepository configRepository;
    private final EncryptionUtil encryptionUtil;

    /**
     * Save or update Firebase configuration with encrypted sensitive fields
     */
    public FirebaseConfiguration saveConfiguration(FirebaseConfigRequest request) {
        log.info("Saving Firebase configuration for project: {}", request.getProjectId());

        // Deactivate any existing active configuration
        Optional<FirebaseConfiguration> existingConfig = configRepository.findByActiveTrue();
        existingConfig.ifPresent(config -> {
            config.setActive(false);
            config.setUpdatedAt(LocalDateTime.now());
            configRepository.save(config);
            log.info("Deactivated previous Firebase configuration: {}", config.getId());
        });

        // Encrypt sensitive fields
        String encryptedPrivateKeyId = encryptionUtil.encrypt(request.getPrivateKeyId());
        String encryptedPrivateKey = encryptionUtil.encrypt(request.getPrivateKey());
        String encryptedClientEmail = encryptionUtil.encrypt(request.getClientEmail());
        String encryptedClientId = encryptionUtil.encrypt(request.getClientId());

        // Create new configuration
        FirebaseConfiguration newConfig = FirebaseConfiguration.builder()
                .id(UUID.randomUUID().toString())
                .type(request.getType())
                .projectId(request.getProjectId())
                .privateKeyId(encryptedPrivateKeyId)
                .privateKey(encryptedPrivateKey)
                .clientEmail(encryptedClientEmail)
                .clientId(encryptedClientId)
                .authUri(request.getAuthUri())
                .tokenUri(request.getTokenUri())
                .authProviderX509CertUrl(request.getAuthProviderX509CertUrl())
                .clientX509CertUrl(request.getClientX509CertUrl())
                .universeDomain(request.getUniverseDomain())
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        FirebaseConfiguration saved = configRepository.save(newConfig);
        log.info("Firebase configuration saved successfully with ID: {}", saved.getId());

        return saved;
    }

    /**
     * Get current active configuration (with masked sensitive fields)
     */
    public Optional<FirebaseConfiguration> getActiveConfiguration() {
        return configRepository.findByActiveTrue();
    }

    /**
     * Mask sensitive field for display
     */
    public String maskSensitiveField(String value) {
        if (value == null || value.length() < 8) {
            return "****";
        }
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }
}
