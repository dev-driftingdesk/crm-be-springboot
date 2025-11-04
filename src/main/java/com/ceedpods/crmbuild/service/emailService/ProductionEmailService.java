package com.ceedpods.crmbuild.service.emailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Production-ready email service with HTML templates and proper error handling.
 * This service uses Spring Mail (JavaMailSender) to send emails via SMTP.
 *
 * Supports multiple email providers:
 * - Gmail SMTP
 * - SendGrid SMTP
 * - AWS SES SMTP
 * - Custom SMTP servers
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductionEmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${app.email.from:noreply@ceedpods.com}")
    private String fromEmail;

    @Value("${app.email.from-name:CRM System}")
    private String fromName;

    @Value("${app.url:http://localhost:8084}")
    private String appUrl;

    @Value("${app.password-reset.token-expiration-minutes:15}")
    private int tokenExpirationMinutes;

    /**
     * Send an email with HTML content
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlContent HTML content of the email
     * @throws RuntimeException if email sending fails
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        if (!emailEnabled) {
            log.info("Email sending disabled. Would send email to: {}, Subject: {}", to, subject);
            log.debug("HTML Content: {}", htmlContent);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(message);

            log.info("Email sent successfully to: {} with subject: {}", to, subject);

        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    /**
     * Send password reset email with 6-digit verification code
     *
     * @param to Recipient email address
     * @param verificationCode 6-digit verification code
     */
    public void sendPasswordResetEmail(String to, String verificationCode) {
        String subject = "Password Reset Request - " + fromName;

        Context context = new Context();
        context.setVariable("verificationCode", verificationCode);
        context.setVariable("expirationMinutes", tokenExpirationMinutes);
        context.setVariable("appUrl", appUrl);
        context.setVariable("companyName", fromName);
        context.setVariable("year", LocalDateTime.now().getYear());

        String htmlContent = templateEngine.process("emails/password-reset", context);

        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Send password reset confirmation email
     *
     * @param to Recipient email address
     * @param firstName User's first name
     */
    public void sendPasswordResetConfirmationEmail(String to, String firstName) {
        String subject = "Password Reset Successful - " + fromName;

        Context context = new Context();
        context.setVariable("firstName", firstName);
        context.setVariable("resetTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss")));
        context.setVariable("appUrl", appUrl);
        context.setVariable("companyName", fromName);
        context.setVariable("year", LocalDateTime.now().getYear());

        String htmlContent = templateEngine.process("emails/password-reset-confirmation", context);

        sendHtmlEmail(to, subject, htmlContent);
    }
}
