package com.taashee.badger.services;

import com.taashee.badger.models.User;
import com.taashee.badger.models.EmailVerificationToken;
import com.taashee.badger.repositories.EmailVerificationTokenRepository;
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
public class EmailVerificationService {
    @Autowired
    private EmailVerificationTokenRepository tokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.emailVerification.expiryMinutes:60}")
    private int expiryMinutes;

    @Value("${app.uiBaseUrl:http://localhost:5173}")
    private String uiBaseUrl;

    public void createVerificationTokenForUser(User user) {
        tokenRepository.deleteByUser(user);
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(expiryMinutes);
        EmailVerificationToken verificationToken = new EmailVerificationToken(token, user, expiry);
        tokenRepository.save(verificationToken);
        sendVerificationEmail(user, token);
    }

    public void sendVerificationEmail(User user, String token) {
        String verifyUrl = uiBaseUrl + "/verify-email?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Verify Your Email Address");
        message.setText("Welcome! Please verify your email by clicking the link below:\n" + verifyUrl + "\nThis link will expire in " + expiryMinutes + " minutes.");
        message.setFrom("appadmin@taashee.com");
        mailSender.send(message);
    }

    public Optional<User> validateVerificationToken(String token) {
        Optional<EmailVerificationToken> verificationTokenOpt = tokenRepository.findByToken(token);
        if (verificationTokenOpt.isPresent()) {
            EmailVerificationToken verificationToken = verificationTokenOpt.get();
            if (verificationToken.getExpiryDate().isAfter(LocalDateTime.now())) {
                return Optional.of(verificationToken.getUser());
            }
        }
        return Optional.empty();
    }

    @Transactional
    public void activateUser(User user) {
        user.setEnabled(true);
        userRepository.save(user);
        tokenRepository.deleteByUser(user);
    }

    public void sendStaffInvitationEmail(User user, String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("You have been added as staff - Badger Management");
        message.setText("Hello " + user.getFirstName() + ",\n\n" +
            "You have been added as staff to an issuer in the Badger Management system.\n" +
            "You can log in with the following credentials:\n" +
            "Email: " + user.getEmail() + "\n" +
            "Password: " + password + "\n\n" +
            "Please log in and change your password after your first login.\n\n" +
            "If you have any questions, contact your administrator.\n");
        message.setFrom("appadmin@taashee.com");
        mailSender.send(message);
    }

    public void sendCustomEmail(SimpleMailMessage message) {
        mailSender.send(message);
    }
} 