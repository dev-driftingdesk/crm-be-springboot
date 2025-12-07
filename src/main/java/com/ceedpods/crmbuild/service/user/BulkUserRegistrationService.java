package com.ceedpods.crmbuild.service.user;

import com.ceedpods.crmbuild.constants.AppConstants;
import com.ceedpods.crmbuild.dto.request.BulkUserRegistrationRequest;
import com.ceedpods.crmbuild.dto.response.BulkUserRegistrationResponse;
import com.ceedpods.crmbuild.dto.response.BulkUserRegistrationResponse.Summary;
import com.ceedpods.crmbuild.dto.response.BulkUserRegistrationResponse.UserRegistrationResult;
import com.ceedpods.crmbuild.entity.user.User;
import com.ceedpods.crmbuild.enums.UserRole;
import com.ceedpods.crmbuild.repository.UserRepository;
import com.ceedpods.crmbuild.service.auditLogService.AuditLogService;
import com.ceedpods.crmbuild.service.emailService.EmailService;
import com.ceedpods.crmbuild.service.keycloakService.KeycloakAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service for bulk registration of sales users
 * Handles user creation in Keycloak and MongoDB, password generation, and welcome email sending
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BulkUserRegistrationService {

    private final UserRepository userRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final String WEB_APP_URL = AppConstants.Urls.WEB_APP_URL;

    /**
     * Perform bulk registration of sales users from a list of email addresses
     *
     * @param request BulkUserRegistrationRequest containing list of emails
     * @param adminEmail Email of the admin performing the operation
     * @param adminId ID of the admin performing the operation
     * @return BulkUserRegistrationResponse with results for each email
     */
    @Transactional
    public BulkUserRegistrationResponse bulkRegisterUsers(
            BulkUserRegistrationRequest request,
            String adminEmail,
            String adminId) {

        log.info("Starting bulk user registration by admin: {} for {} emails",
                adminEmail, request.getEmails().size());

        List<UserRegistrationResult> results = new ArrayList<>();
        int created = 0;
        int alreadyExists = 0;
        int failed = 0;
        int emailsSent = 0;
        int emailsFailed = 0;

        for (String email : request.getEmails()) {
            String trimmedEmail = email.trim().toLowerCase();
            UserRegistrationResult result = processEmail(trimmedEmail);
            results.add(result);

            switch (result.getStatus()) {
                case "created":
                    created++;
                    if ("sent".equals(result.getEmailStatus())) {
                        emailsSent++;
                    } else if ("failed".equals(result.getEmailStatus())) {
                        emailsFailed++;
                    }
                    break;
                case "already_exists":
                    alreadyExists++;
                    break;
                case "failed":
                    failed++;
                    break;
            }
        }

        // Log audit event
        String details = String.format("Emails processed: %s", String.join(", ",
                results.stream()
                        .filter(r -> "created".equals(r.getStatus()))
                        .map(UserRegistrationResult::getEmail)
                        .toList()));
        auditLogService.logBulkUserCreated(adminEmail, adminId, created, failed, details);

        log.info("Bulk registration completed by admin: {}. Created: {}, Already exists: {}, Failed: {}",
                adminEmail, created, alreadyExists, failed);

        return BulkUserRegistrationResponse.builder()
                .summary(Summary.builder()
                        .totalProcessed(request.getEmails().size())
                        .created(created)
                        .alreadyExists(alreadyExists)
                        .failed(failed)
                        .emailsSent(emailsSent)
                        .emailsFailed(emailsFailed)
                        .build())
                .results(results)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Process a single email for registration
     */
    private UserRegistrationResult processEmail(String email) {
        // Validate email format
        if (!isValidEmail(email)) {
            log.warn("Invalid email format: {}", email);
            return UserRegistrationResult.builder()
                    .email(email)
                    .status("failed")
                    .emailStatus("not_applicable")
                    .errorMessage("Invalid email format")
                    .build();
        }

        // Check if user already exists in MongoDB
        if (userRepository.existsByEmail(email)) {
            log.info("User already exists: {}", email);
            return UserRegistrationResult.builder()
                    .email(email)
                    .status("already_exists")
                    .emailStatus("not_applicable")
                    .build();
        }

        // Check if user exists in Keycloak (might exist there but not in MongoDB)
        String existingKeycloakId = keycloakAdminService.getUserByEmail(email);
        if (existingKeycloakId != null) {
            log.info("User already exists in Keycloak: {}", email);
            return UserRegistrationResult.builder()
                    .email(email)
                    .status("already_exists")
                    .emailStatus("not_applicable")
                    .build();
        }

        try {
            // Generate password from email local part
            String temporaryPassword = generatePassword(email);

            // Extract first name from email (use local part before @ as placeholder)
            String localPart = email.substring(0, email.indexOf('@'));
            String firstName = capitalizeFirstLetter(localPart);
            String lastName = "User"; // Default last name

            // Create user in Keycloak
            String keycloakId = keycloakAdminService.createUser(email, firstName, lastName, temporaryPassword);
            log.info("User created in Keycloak: {} with ID: {}", email, keycloakId);

            // Assign SALES_REP role in Keycloak
            keycloakAdminService.assignRealmRoleToUser(keycloakId, AppConstants.Keycloak.ROLE_SALES_REP);
            log.info("SALES_REP role assigned to user: {}", email);

            // Create user in MongoDB
            User user = User.builder()
                    .keycloakId(keycloakId)
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .role(UserRole.SALES_REP)
                    .enabled(true)
                    .emailVerified(false)
                    .mustChangePassword(true) // Flag to require password change on first login
                    .build();

            user = userRepository.save(user);
            log.info("User saved in MongoDB: {} with ID: {}", email, user.getId());

            // Send welcome email
            String emailStatus = sendWelcomeEmail(email, temporaryPassword);

            return UserRegistrationResult.builder()
                    .email(email)
                    .status("created")
                    .temporaryPassword(temporaryPassword)
                    .emailStatus(emailStatus)
                    .userId(user.getId())
                    .build();

        } catch (Exception e) {
            log.error("Failed to register user {}: {}", email, e.getMessage(), e);
            return UserRegistrationResult.builder()
                    .email(email)
                    .status("failed")
                    .emailStatus("not_applicable")
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * Generate one-time password from email local part
     * Format: localPart + "123@"
     * Example: lahiru@gmail.com -> lahiru123@
     */
    private String generatePassword(String email) {
        String localPart = email.substring(0, email.indexOf('@'));
        return localPart + "123@";
    }

    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Capitalize first letter of a string
     */
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Send welcome email with login credentials
     * Includes retry logic for failed attempts
     */
    private String sendWelcomeEmail(String email, String temporaryPassword) {
        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                emailService.sendWelcomeEmail(email, email, temporaryPassword, WEB_APP_URL);
                log.info("Welcome email sent successfully to: {}", email);
                return "sent";
            } catch (Exception e) {
                retryCount++;
                log.warn("Failed to send welcome email to {} (attempt {}/{}): {}",
                        email, retryCount, maxRetries, e.getMessage());

                if (retryCount >= maxRetries) {
                    log.error("All retry attempts exhausted for sending welcome email to: {}", email);
                    return "failed";
                }

                // Wait before retry (exponential backoff)
                try {
                    Thread.sleep((long) Math.pow(2, retryCount) * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return "failed";
                }
            }
        }
        return "failed";
    }
}
