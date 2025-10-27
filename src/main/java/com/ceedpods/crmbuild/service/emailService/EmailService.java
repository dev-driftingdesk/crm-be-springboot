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
            // TODO: Implement actual email sending logic here
            // This is a placeholder implementation
            
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
    
    public void sendWelcomeEmail(String to, String firstName) {
        String subject = "Welcome to CRM System";
        String body = String.format(
            "Dear %s,\n\n" +
            "Welcome to our CRM System! Your account has been successfully created.\n\n" +
            "You can now log in and start using the system.\n\n" +
            "If you have any questions, please contact your administrator.\n\n" +
            "Best regards,\n" +
            "CRM System Team",
            firstName
        );
        
        sendEmail(to, subject, body);
    }
    
    public void sendPasswordResetEmail(String to, String resetToken) {
        String subject = "Password Reset Request";
        String body = String.format(
            "Dear User,\n\n" +
            "You have requested a password reset for your CRM System account.\n\n" +
            "Please use the following token to reset your password:\n" +
            "%s\n\n" +
            "If you did not request this reset, please ignore this email.\n\n" +
            "Best regards,\n" +
            "CRM System Team",
            resetToken
        );
        
        sendEmail(to, subject, body);
    }
    
    public void sendAccountLockedEmail(String to, String firstName) {
        String subject = "Account Locked - CRM System";
        String body = String.format(
            "Dear %s,\n\n" +
            "Your CRM System account has been locked due to multiple failed login attempts.\n\n" +
            "Please contact your administrator to unlock your account.\n\n" +
            "Best regards,\n" +
            "CRM System Team",
            firstName
        );
        
        sendEmail(to, subject, body);
    }
}