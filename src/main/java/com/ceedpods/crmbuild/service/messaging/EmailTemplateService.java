package com.ceedpods.crmbuild.service.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for generating professional HTML email templates
 * using the CRM System brand colors and design tokens.
 */
@Service
@Slf4j
public class EmailTemplateService {

    // Brand colors
    private static final String COLOR_NIGHT = "#0F1010";
    private static final String COLOR_WHITE = "#FFFFFF";
    private static final String COLOR_ISABELLINE = "#F5F1F0";
    private static final String COLOR_DAVYSGREY = "#555555";
    private static final String COLOR_MIDNIGHTGREEN = "#0B6C6B";
    private static final String COLOR_TIMBERWOLF = "#DFD8D7";

    /**
     * Generate a professional HTML email with CRM System branding
     *
     * @param subject Email subject
     * @param messageBody Email body content (can include HTML or plain text)
     * @return Formatted HTML email
     */
    public String generateEmailTemplate(String subject, String messageBody) {
        log.debug("Generating branded email template for subject: {}", subject);

        return buildHtmlTemplate(subject, messageBody);
    }

    /**
     * Build the complete HTML email template
     */
    private String buildHtmlTemplate(String subject, String messageBody) {
        // Convert plain text line breaks to HTML if the message body doesn't contain HTML
        String formattedBody = messageBody;
        if (!messageBody.contains("<") && !messageBody.contains(">")) {
            formattedBody = messageBody.replace("\n", "<br>");
        }

        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "    <title>" + escapeHtml(subject) + "</title>\n" +
                "    <style>\n" +
                "        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap');\n" +
                "        \n" +
                "        body {\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;\n" +
                "            background-color: " + COLOR_ISABELLINE + ";\n" +
                "            -webkit-font-smoothing: antialiased;\n" +
                "            -moz-osx-font-smoothing: grayscale;\n" +
                "        }\n" +
                "        \n" +
                "        .email-container {\n" +
                "            max-width: 600px;\n" +
                "            margin: 0 auto;\n" +
                "            background-color: " + COLOR_ISABELLINE + ";\n" +
                "            padding: 40px 20px;\n" +
                "        }\n" +
                "        \n" +
                "        .email-card {\n" +
                "            background-color: " + COLOR_WHITE + ";\n" +
                "            border-radius: 16px;\n" +
                "            box-shadow: 0 4px 16px rgba(15, 16, 16, 0.08);\n" +
                "            overflow: hidden;\n" +
                "        }\n" +
                "        \n" +
                "        .email-header {\n" +
                "            background: linear-gradient(135deg, " + COLOR_MIDNIGHTGREEN + " 0%, #094948 100%);\n" +
                "            padding: 32px 40px;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        \n" +
                "        .email-logo {\n" +
                "            display: inline-block;\n" +
                "            background-color: " + COLOR_WHITE + ";\n" +
                "            padding: 12px 24px;\n" +
                "            border-radius: 12px;\n" +
                "            margin-bottom: 16px;\n" +
                "        }\n" +
                "        \n" +
                "        .email-logo-text {\n" +
                "            font-size: 24px;\n" +
                "            font-weight: 700;\n" +
                "            color: " + COLOR_MIDNIGHTGREEN + ";\n" +
                "            margin: 0;\n" +
                "            letter-spacing: -0.5px;\n" +
                "        }\n" +
                "        \n" +
                "        .email-title {\n" +
                "            color: " + COLOR_WHITE + ";\n" +
                "            font-size: 20px;\n" +
                "            font-weight: 600;\n" +
                "            margin: 0;\n" +
                "            line-height: 1.4;\n" +
                "        }\n" +
                "        \n" +
                "        .email-body {\n" +
                "            padding: 40px;\n" +
                "            color: " + COLOR_NIGHT + ";\n" +
                "            font-size: 15px;\n" +
                "            line-height: 1.7;\n" +
                "        }\n" +
                "        \n" +
                "        .email-body p {\n" +
                "            margin: 0 0 16px 0;\n" +
                "        }\n" +
                "        \n" +
                "        .email-body p:last-child {\n" +
                "            margin-bottom: 0;\n" +
                "        }\n" +
                "        \n" +
                "        .email-divider {\n" +
                "            height: 1px;\n" +
                "            background-color: " + COLOR_TIMBERWOLF + ";\n" +
                "            margin: 32px 40px;\n" +
                "        }\n" +
                "        \n" +
                "        .email-footer {\n" +
                "            padding: 0 40px 40px 40px;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        \n" +
                "        .email-footer-text {\n" +
                "            color: " + COLOR_DAVYSGREY + ";\n" +
                "            font-size: 13px;\n" +
                "            line-height: 1.6;\n" +
                "            margin: 8px 0;\n" +
                "        }\n" +
                "        \n" +
                "        .email-footer-brand {\n" +
                "            color: " + COLOR_MIDNIGHTGREEN + ";\n" +
                "            font-weight: 600;\n" +
                "            text-decoration: none;\n" +
                "        }\n" +
                "        \n" +
                "        .button {\n" +
                "            display: inline-block;\n" +
                "            background-color: " + COLOR_MIDNIGHTGREEN + ";\n" +
                "            color: " + COLOR_WHITE + " !important;\n" +
                "            padding: 14px 32px;\n" +
                "            border-radius: 12px;\n" +
                "            text-decoration: none;\n" +
                "            font-weight: 600;\n" +
                "            font-size: 15px;\n" +
                "            margin: 20px 0;\n" +
                "            transition: background-color 0.2s ease;\n" +
                "        }\n" +
                "        \n" +
                "        .button:hover {\n" +
                "            background-color: #094948;\n" +
                "        }\n" +
                "        \n" +
                "        .accent-text {\n" +
                "            color: " + COLOR_MIDNIGHTGREEN + ";\n" +
                "            font-weight: 600;\n" +
                "        }\n" +
                "        \n" +
                "        @media only screen and (max-width: 600px) {\n" +
                "            .email-container {\n" +
                "                padding: 20px 10px;\n" +
                "            }\n" +
                "            \n" +
                "            .email-header {\n" +
                "                padding: 24px 20px;\n" +
                "            }\n" +
                "            \n" +
                "            .email-body {\n" +
                "                padding: 24px 20px;\n" +
                "            }\n" +
                "            \n" +
                "            .email-footer {\n" +
                "                padding: 0 20px 24px 20px;\n" +
                "            }\n" +
                "            \n" +
                "            .email-divider {\n" +
                "                margin: 24px 20px;\n" +
                "            }\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"email-container\">\n" +
                "        <div class=\"email-card\">\n" +
                "            <!-- Header -->\n" +
                "            <div class=\"email-header\">\n" +
                "                <div class=\"email-logo\">\n" +
                "                    <h1 class=\"email-logo-text\">CRM System</h1>\n" +
                "                </div>\n" +
                "                <p class=\"email-title\">" + escapeHtml(subject) + "</p>\n" +
                "            </div>\n" +
                "            \n" +
                "            <!-- Body -->\n" +
                "            <div class=\"email-body\">\n" +
                "                " + formattedBody + "\n" +
                "            </div>\n" +
                "            \n" +
                "            <!-- Divider -->\n" +
                "            <div class=\"email-divider\"></div>\n" +
                "            \n" +
                "            <!-- Footer -->\n" +
                "            <div class=\"email-footer\">\n" +
                "                <p class=\"email-footer-text\">\n" +
                "                    This email was sent from <span class=\"email-footer-brand\">CRM System</span>\n" +
                "                </p>\n" +
                "                <p class=\"email-footer-text\">\n" +
                "                    &copy; " + java.time.Year.now().getValue() + " CRM System. All rights reserved.\n" +
                "                </p>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * Escape HTML special characters to prevent XSS
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Generate a welcome email template
     */
    public String generateWelcomeEmail(String recipientName) {
        String subject = "Welcome to CRM System";
        String body = "<p>Hello <span class=\"accent-text\">" + escapeHtml(recipientName) + "</span>,</p>" +
                "<p>Welcome to CRM System! We're excited to have you on board.</p>" +
                "<p>Our platform helps you manage customer relationships efficiently and effectively. " +
                "If you have any questions or need assistance, please don't hesitate to reach out.</p>" +
                "<p style=\"margin-top: 24px;\">Best regards,<br>The CRM System Team</p>";

        return generateEmailTemplate(subject, body);
    }

    /**
     * Generate a notification email template
     */
    public String generateNotificationEmail(String title, String message) {
        String subject = title;
        String body = "<p>" + message + "</p>" +
                "<p style=\"margin-top: 24px;\">Best regards,<br>The CRM System Team</p>";

        return generateEmailTemplate(subject, body);
    }
}
