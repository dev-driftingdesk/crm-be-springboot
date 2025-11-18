package com.ceedpods.crmbuild.service.messaging;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * SMTP-based email service for agent-initiated emails.
 * Creates dynamic JavaMailSender instances with per-agent SMTP credentials.
 * Sends professionally branded HTML emails using the CRM System design.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SmtpEmailService {

    private final EmailTemplateService emailTemplateService;

    /**
     * Send email using SMTP with provided credentials
     *
     * @param recipientEmail Recipient email address
     * @param subject Email subject
     * @param messageBody Email body (can be plain text or HTML)
     * @param credentials Map containing SMTP credentials (host, port, username, password, senderAddress)
     * @return Message ID (generated UUID)
     * @throws Exception if email sending fails
     */
    public String sendEmail(String recipientEmail, String subject, String messageBody,
                           Map<String, String> credentials) throws Exception {
        log.info("Sending branded HTML email to: {}", recipientEmail);

        // Extract SMTP credentials
        String smtpHost = credentials.get("smtpHost");
        String smtpPort = credentials.get("smtpPort");
        String smtpUsername = credentials.get("smtpUsername");
        String smtpPassword = credentials.get("smtpPassword");
        String senderAddress = credentials.get("senderAddress");

        // Validate required credentials
        if (smtpHost == null || smtpHost.isEmpty()) {
            throw new Exception("SMTP host is missing");
        }

        if (smtpPort == null || smtpPort.isEmpty()) {
            throw new Exception("SMTP port is missing");
        }

        if (smtpUsername == null || smtpUsername.isEmpty()) {
            throw new Exception("SMTP username is missing");
        }

        if (smtpPassword == null || smtpPassword.isEmpty()) {
            throw new Exception("SMTP password is missing");
        }

        if (senderAddress == null || senderAddress.isEmpty()) {
            throw new Exception("Sender email address is missing");
        }

        try {
            // Create dynamic JavaMailSender with agent's SMTP credentials
            JavaMailSender mailSender = createMailSender(smtpHost, smtpPort, smtpUsername, smtpPassword);

            // Generate professional HTML email using brand template
            String htmlContent = emailTemplateService.generateEmailTemplate(subject, messageBody);

            // Create and send email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderAddress);
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML content

            log.debug("Sending branded HTML email via SMTP: {}:{}", smtpHost, smtpPort);
            mailSender.send(message);

            // Generate message ID for tracking
            String messageId = UUID.randomUUID().toString();
            log.info("Branded email sent successfully via SMTP. Message ID: {}", messageId);

            return messageId;

        } catch (MessagingException e) {
            log.error("MessagingException sending email via SMTP: {}", e.getMessage(), e);
            throw new Exception("SMTP email error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error sending email via SMTP: {}", e.getMessage(), e);
            throw new Exception("SMTP error: " + e.getMessage(), e);
        }
    }

    /**
     * Create a JavaMailSender with dynamic SMTP configuration
     */
    private JavaMailSender createMailSender(String host, String port, String username, String password) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(host);
        mailSender.setPort(Integer.parseInt(port));
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");

        // SSL/TLS configuration based on port
        int portNumber = Integer.parseInt(port);
        if (portNumber == 465) {
            // SSL configuration for port 465
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.port", port);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        } else if (portNumber == 587) {
            // STARTTLS configuration for port 587
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        } else {
            // Default to STARTTLS for other ports
            props.put("mail.smtp.starttls.enable", "true");
        }

        // Connection timeouts
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        log.debug("Created JavaMailSender for SMTP server: {}:{}", host, port);

        return mailSender;
    }
}
