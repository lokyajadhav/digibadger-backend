package com.taashee.badger.controllers;

import com.taashee.badger.models.UserInvitation;
import com.taashee.badger.models.User;
import com.taashee.badger.models.ApiResponse;
import com.taashee.badger.repositories.UserInvitationRepository;
import com.taashee.badger.repositories.UserRepository;
import com.taashee.badger.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import com.taashee.badger.services.EmailVerificationService;
import org.springframework.mail.SimpleMailMessage;
import java.util.HashMap;

@Tag(name = "User Invitations", description = "APIs for user invitations. Author: Lokya Naik")
@RestController
@RequestMapping("/api")
public class UserInvitationController {
    @Autowired
    private UserInvitationRepository invitationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailVerificationService emailVerificationService;
    @Value("${staff.default.password}")
    private String defaultPassword;

    @Operation(summary = "Invite a user", description = "ADMIN only: Invite a user by email and role. Author: Lokya Naik")
    @PostMapping("/admin/invitations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserInvitation>> inviteUser(@RequestBody Map<String, String> body, @RequestParam Long adminId) {
        String email = body.get("email");
        String role = body.get("role");
        if (email == null || role == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Email and role required", null, null));
        }
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(409).body(new ApiResponse<>(409, "User already exists: " + email, null, null));
        }
        Optional<UserInvitation> existing = invitationRepository.findByEmailAndStatus(email, UserInvitation.Status.PENDING);
        if (existing.isPresent()) {
            return ResponseEntity.status(409).body(new ApiResponse<>(409, "Pending invitation already exists", null, null));
        }
        User admin = userRepository.findById(adminId).orElse(null);
        if (admin == null) {
            return ResponseEntity.status(404).body(new ApiResponse<>(404, "Admin not found", null, null));
        }
        UserInvitation invitation = new UserInvitation();
        invitation.setEmail(email);
        invitation.setRole(role);
        invitation.setStatus(UserInvitation.Status.PENDING);
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setCreatedBy(admin);
        invitation.setCreatedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
        // Send invitation email with acceptance link
        try {
            String acceptUrl = System.getenv().getOrDefault("UI_BASE_URL", "http://localhost:5173") + "/accept-invitation?token=" + invitation.getToken();
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("You're invited to Badger Management");
            message.setText("Hello,\n\nYou have been invited to join Badger Management as a " + role + ".\nPlease click the link below to accept the invitation and activate your account:\n" + acceptUrl + "\n\nIf you did not expect this invitation, you can ignore this email.\n");
            message.setFrom("appadmin@taashee.com");
            emailVerificationService.sendCustomEmail(message);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Failed to send invitation email: " + ex.getMessage(), null, null));
        }
        return ResponseEntity.ok(new ApiResponse<>(200, "Invitation sent", invitation, null));
    }

    @Operation(summary = "Accept invitation", description = "Accept a user invitation by token. Author: Lokya Naik")
    @GetMapping("/invitations/accept")
    public ResponseEntity<ApiResponse<String>> acceptInvitation(@RequestParam String token, @RequestParam String firstName, @RequestParam String lastName) {
        Optional<UserInvitation> invitationOpt = invitationRepository.findByToken(token);
        if (invitationOpt.isEmpty()) {
            return ResponseEntity.status(404).body(new ApiResponse<>(404, "Invitation not found", null, null));
        }
        UserInvitation invitation = invitationOpt.get();
        if (invitation.getStatus() != UserInvitation.Status.PENDING) {
            return ResponseEntity.status(409).body(new ApiResponse<>(409, "Invitation already used or rejected", null, null));
        }
        // Create user with default password
        User user = new User();
        user.setEmail(invitation.getEmail());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(passwordEncoder.encode(defaultPassword));
        user.setEnabled(true);
        user.setRoles(java.util.Set.of(invitation.getRole()));
        userService.saveUser(user);
        // Mark invitation as accepted
        invitation.setStatus(UserInvitation.Status.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
        // Send credentials email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Your Badger Management Account is Ready");
        message.setText("Congratulations! Your account has been created.\n\nLogin details:\nEmail: " + user.getEmail() + "\nPassword: " + defaultPassword + "\nRole: " + invitation.getRole() + "\n\nPlease log in and change your password after your first login.\n\nIf you have any questions, contact your administrator.");
        message.setFrom("appadmin@taashee.com");
        emailVerificationService.sendCustomEmail(message);
        return ResponseEntity.ok(new ApiResponse<>(200, "Invitation accepted, user created. Credentials sent to email.", null, null));
    }

    @Operation(summary = "Bulk import users", description = "ADMIN only: Bulk invite users by Excel upload. Author: Lokya Naik")
    @PostMapping("/admin/users/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importUsersBulk(@RequestBody java.util.List<Map<String, String>> users, @RequestParam Long adminId) {
        User admin = userRepository.findById(adminId).orElse(null);
        if (admin == null) {
            return ResponseEntity.status(404).body(new ApiResponse<>(404, "Admin not found", null, null));
        }
        int successCount = 0;
        int errorCount = 0;
        StringBuilder errorDetails = new StringBuilder();
        java.util.List<String> alreadyRegistered = new java.util.ArrayList<>();
        for (Map<String, String> userRow : users) {
            String email = userRow.get("email");
            String role = userRow.getOrDefault("role", "USER");
            if (email == null || role == null) {
                errorCount++;
                errorDetails.append("Missing required fields for: ").append(email).append("\n");
                continue;
            }
            if (userRepository.findByEmail(email).isPresent()) {
                alreadyRegistered.add(email);
                continue;
            }
            if (invitationRepository.findByEmailAndStatus(email, UserInvitation.Status.PENDING).isPresent()) {
                errorCount++;
                errorDetails.append("Pending invitation already exists: ").append(email).append("\n");
                continue;
            }
            try {
                UserInvitation invitation = new UserInvitation();
                invitation.setEmail(email);
                invitation.setRole(role);
                invitation.setStatus(UserInvitation.Status.PENDING);
                invitation.setToken(UUID.randomUUID().toString());
                invitation.setCreatedBy(admin);
                invitation.setCreatedAt(LocalDateTime.now());
                invitationRepository.save(invitation);
                // Send invitation email with acceptance link
                String acceptUrl = System.getenv().getOrDefault("UI_BASE_URL", "http://localhost:5173") + "/accept-invitation?token=" + invitation.getToken();
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("You're invited to Badger Management");
                message.setText("Hello,\n\nYou have been invited to join Badger Management as a " + role + ".\nPlease click the link below to accept the invitation and activate your account:\n" + acceptUrl + "\n\nIf you did not expect this invitation, you can ignore this email.\n");
                message.setFrom("appadmin@taashee.com");
                emailVerificationService.sendCustomEmail(message);
                successCount++;
            } catch (Exception ex) {
                errorCount++;
                errorDetails.append("Failed to invite: ").append(email).append(" (Reason: ").append(ex.getMessage()).append(")\n");
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("errorCount", errorCount);
        result.put("errorDetails", errorDetails.toString());
        result.put("alreadyRegistered", alreadyRegistered);
        return ResponseEntity.ok(new ApiResponse<>(200, "Bulk invite completed. Success: " + successCount + ", Errors: " + errorCount + ", Already registered: " + alreadyRegistered.size(), result, null));
    }
} 