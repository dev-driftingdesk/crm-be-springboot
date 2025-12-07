package com.ceedpods.crmbuild.service.emailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Email service that delegates to ProductionEmailService for actual email sending.
 * This service uses Spring Mail (JavaMailSender) to send emails via SMTP.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final ProductionEmailService productionEmailService;

    /**
     * Send a generic email with plain text or HTML content
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body (can be plain text or HTML)
     */
    public void sendEmail(String to, String subject, String body) {
        log.info("Sending email to: {} with subject: {}", to, subject);
        // Convert plain text to HTML by replacing newlines with <br> tags
        String htmlBody = body.replace("\n", "<br>");
        productionEmailService.sendHtmlEmail(to, subject, htmlBody);
    }

    /**
     * Send password reset email with verification code
     *
     * @param to Recipient email address
     * @param verificationCode 6-digit verification code
     */
    public void sendPasswordResetEmail(String to, String verificationCode) {
        log.info("Sending password reset email to: {}", to);
        productionEmailService.sendPasswordResetEmail(to, verificationCode);
    }

    /**
     * Send password reset confirmation email
     *
     * @param to Recipient email address
     * @param firstName User's first name
     */
    public void sendPasswordResetConfirmationEmail(String to, String firstName) {
        log.info("Sending password reset confirmation email to: {}", to);
        productionEmailService.sendPasswordResetConfirmationEmail(to, firstName);
    }

    /**
     * Send welcome email with login credentials for newly registered users
     *
     * @param to Recipient email address
     * @param username Username (email) for login
     * @param temporaryPassword One-time password for first login
     * @param loginUrl URL for the login page
     * @param mobileAppUrl Deep link URL for the mobile app
     */
    public void sendWelcomeEmail(String to, String username, String temporaryPassword, String loginUrl, String mobileAppUrl) {
        log.info("Sending welcome email to: {}", to);
        productionEmailService.sendWelcomeEmail(to, username, temporaryPassword, loginUrl, mobileAppUrl);
    }
}