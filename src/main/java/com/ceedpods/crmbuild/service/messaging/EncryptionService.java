package com.ceedpods.crmbuild.service.messaging;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class EncryptionService {

    @Value("${app.encryption.secret:my-secret-key-change-in-production}")
    private String encryptionSecret;

    private StandardPBEStringEncryptor encryptor;

    @PostConstruct
    public void init() {
        encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(encryptionSecret);
        encryptor.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        encryptor.setIvGenerator(new RandomIvGenerator());
    }

    public String encrypt(String plainText) {
        return encryptor.encrypt(plainText);
    }

    public String decrypt(String encryptedText) {
        return encryptor.decrypt(encryptedText);
    }

    public Map<String, String> encryptMap(Map<String, String> plainMap) {
        Map<String, String> encrypted = new HashMap<>();
        for (Map.Entry<String, String> entry : plainMap.entrySet()) {
            encrypted.put(entry.getKey(), encrypt(entry.getValue()));
        }
        return encrypted;
    }

    public Map<String, String> decryptMap(Map<String, String> encryptedMap) {
        Map<String, String> decrypted = new HashMap<>();
        for (Map.Entry<String, String> entry : encryptedMap.entrySet()) {
            decrypted.put(entry.getKey(), decrypt(entry.getValue()));
        }
        return decrypted;
    }
}
