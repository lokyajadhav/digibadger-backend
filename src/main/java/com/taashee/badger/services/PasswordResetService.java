package com.taashee.badger.services;

import com.taashee.badger.models.User;
import com.taashee.badger.models.PasswordResetToken;
import com.taashee.badger.repositories.PasswordResetTokenRepository;
import com.taashee.badger.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.resetPassword.expiryMinutes:30}")
    private int expiryMinutes;

    @Value("${app.uiBaseUrl:http://localhost:5173}")
    private String uiBaseUrl;

    @Transactional
    public void createPasswordResetTokenForUser(User user, String appUrl) {
        // Remove any existing tokens for this user
        tokenRepository.deleteByUser(user);
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(expiryMinutes);
        PasswordResetToken resetToken = new PasswordResetToken(token, user, expiry);
        tokenRepository.save(resetToken);
        sendResetEmail(user, token);
    }

    public void sendResetEmail(User user, String token) {
        String resetUrl = uiBaseUrl + "/reset-password?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Password Reset Request");
        message.setText("To reset your password, click the link below:\n" + resetUrl + "\nThis link will expire in " + expiryMinutes + " minutes.");
        message.setFrom("appadmin@taashee.com");
        mailSender.send(message);
    }

    public Optional<User> validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> resetTokenOpt = tokenRepository.findByToken(token);
        if (resetTokenOpt.isPresent()) {
            PasswordResetToken resetToken = resetTokenOpt.get();
            if (resetToken.getExpiryDate().isAfter(LocalDateTime.now())) {
                return Optional.of(resetToken.getUser());
            }
        }
        return Optional.empty();
    }

    @Transactional
    public void resetPassword(User user, String newPassword) {
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from the old password.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.deleteByUser(user);
    }
} 