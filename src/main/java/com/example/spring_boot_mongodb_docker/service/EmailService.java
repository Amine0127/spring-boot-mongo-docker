package com.example.spring_boot_mongodb_docker.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:noreply@example.com}")
    private String fromEmail;

    @Autowired
    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            logger.info("Sending simple email to: {}", to);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            logger.info("Simple email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send simple email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Async
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables)
            throws MessagingException {
        try {
            logger.info("Sending HTML email to: {}", to);

            // Prepare the context for Thymeleaf template
            Context context = new Context();
            variables.forEach(context::setVariable);

            // Process the template
            String htmlContent = templateEngine.process(templateName, context);

            // Create the email message
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            // Send the email
            mailSender.send(message);
            logger.info("HTML email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send HTML email to: {}", to, e);
            throw new MessagingException("Failed to send HTML email", e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String resetLink) throws MessagingException {
        logger.info("Sending password reset email to: {}", to);

        // For simple implementation without templates
        String subject = "Password Reset Request";
        String message = "Hello,\n\nYou have requested to reset your password. "
                + "Please click on the link below to reset your password:\n\n"
                + resetLink + "\n\n"
                + "This link will expire in 30 minutes.\n\n"
                + "If you did not request a password reset, please ignore this email.\n\n"
                + "Regards,\nThe Team";

        sendSimpleEmail(to, subject, message);
    }
}

