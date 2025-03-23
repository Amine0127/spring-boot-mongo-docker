package com.example.spring_boot_mongodb_docker.controller;

import com.example.spring_boot_mongodb_docker.model.PasswordResetToken;
import com.example.spring_boot_mongodb_docker.model.User;
import com.example.spring_boot_mongodb_docker.repository.PasswordResetTokenRepository;
import com.example.spring_boot_mongodb_docker.service.EmailService;
import com.example.spring_boot_mongodb_docker.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/password")
public class PasswordResetController {

    private final UserService userService;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Autowired
    public PasswordResetController(UserService userService,
                                   PasswordResetTokenRepository tokenRepository,
                                   EmailService emailService) {
        this.userService = userService;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Delete any existing tokens for this user
            tokenRepository.deleteByUsername(user.getUsername());

            // Create a new token
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(
                    token,
                    user.getUsername(),
                    Instant.now().plus(24, ChronoUnit.HOURS)
            );
            tokenRepository.save(resetToken);

            // Send email with reset link
            String resetLink = frontendUrl + "/reset-password?token=" + token;
            try {
                emailService.sendPasswordResetEmail(email, resetLink);
                return ResponseEntity.ok("Password reset email sent");
            } catch (MessagingException e) {
                return ResponseEntity.badRequest().body("Failed to send email: " + e.getMessage());
            }
        }

        // Always return success to prevent user enumeration
        return ResponseEntity.ok("If your email is registered, you will receive a password reset link");
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isPresent()) {
            PasswordResetToken resetToken = tokenOpt.get();

            if (resetToken.isExpired()) {
                return ResponseEntity.badRequest().body("Token has expired");
            }

            // Update password
            Optional<User> userOpt = userService.findByUsername(resetToken.getUsername());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                userService.updatePassword(user.getUsername(), newPassword);

                // Delete the used token
                tokenRepository.delete(resetToken);

                return ResponseEntity.ok("Password has been reset successfully");
            }
        }

        return ResponseEntity.badRequest().body("Invalid or expired token");
    }
}
