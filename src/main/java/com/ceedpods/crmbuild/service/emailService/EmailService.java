package com.ceedpods.crmbuild.service.emailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;
    
    @Value("${app.email.from:noreply@ceedpods.com}")
    private String fromEmail;
    
    // Simple email service implementation
    // In production, you would integrate with your email provider (SendGrid, AWS SES, etc.)

    public void sendEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            log.info("Email sending disabled. Would send email to: {}, Subject: {}", to, subject);
            return;
        }

        try {
            log.info("Sending email to: {}", to);
            log.info("Subject: {}", subject);
            log.debug("Body: {}", body);

            // Simulate email sending
            Thread.sleep(100);

            log.info("Email sent successfully to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    public void sendPasswordResetEmail(String to, String resetToken) {
        String subject = "Password Reset Request";
        String body = String.format(
            "Dear User,\n\n" +
            "You have requested a password reset for your CRM System account.\n\n" +
            "Please use the following verification code to reset your password:\n\n" +
            "Verification Code: %s\n\n" +
            "This code will expire in 15 minutes.\n\n" +
            "If you did not request this reset, please ignore this email.\n\n" +
            "Best regards,\n" +
            "CRM System Team",
            resetToken
        );

        sendEmail(to, subject, body);
    }

    public void sendPasswordResetConfirmationEmail(String to, String firstName) {
        String subject = "Password Reset Successful";
        String body = String.format(
            "Dear %s,\n\n" +
            "Your password has been successfully reset.\n\n" +
            "If you did not make this change, please contact your administrator immediately.\n\n" +
            "Best regards,\n" +
            "CRM System Team",
            firstName
        );

        sendEmail(to, subject, body);
    }
}