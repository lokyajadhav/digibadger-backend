package com.taashee.badger.controllers;

import com.taashee.badger.models.*;
import com.taashee.badger.repositories.*;
import com.taashee.badger.services.UserService;
import com.taashee.badger.services.EmailVerificationService;
import org.springframework.mail.SimpleMailMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// DTO for user data to avoid serialization issues
class UserDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private Set<String> roles;
    
    public UserDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.enabled = user.isEnabled();
        this.roles = user.getRoles();
    }
    
    // Getters
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public boolean isEnabled() { return enabled; }
    public Set<String> getRoles() { return roles; }
}

@Tag(name = "Organization User Management", description = "APIs for ISSUERs to manage users in their organization. OWNER permission required. Author: Lokya Naik")
@RestController
@RequestMapping("/api/organization/users")
public class OrganizationUserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private OrganizationStaffRepository organizationStaffRepository;
    
    @Autowired
    private OrganizationUserRepository organizationUserRepository;
    
    @Autowired
    private UserInvitationRepository invitationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailVerificationService emailVerificationService;

    @Operation(summary = "List users in organization", description = "ISSUER: Get all users mapped to the organization.")
    @PreAuthorize("hasRole('ISSUER')")
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<UserDto>>> getOrganizationUsers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        // Get user
        User issuer = userService.findByEmail(email);
        if (issuer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found", null, "User not found")
            );
        }
        
        // Find organizations where user has OWNER permission
        List<OrganizationStaff> ownerStaff = organizationStaffRepository.findByUserIdAndStaffRole(issuer.getId(), "owner");
        if (ownerStaff.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ApiResponse<>(HttpStatus.FORBIDDEN.value(), "You don't have OWNER permission in any organization", null, "No OWNER permission")
            );
        }
        
        // Get all users from all organizations where user has OWNER permission
        Set<User> organizationUsers = new HashSet<>();
        for (OrganizationStaff staff : ownerStaff) {
            List<OrganizationUser> orgUsers = organizationUserRepository.findByOrganizationId(staff.getOrganization().getId());
            for (OrganizationUser orgUser : orgUsers) {
                organizationUsers.add(orgUser.getUser());
            }
        }
        
        // Convert to DTOs to avoid serialization issues
        List<UserDto> userDtos = organizationUsers.stream()
            .map(UserDto::new)
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", userDtos, null));
    }

    @Operation(summary = "Invite user to organization", description = "ISSUER: Invite a user to their organization.")
    @PreAuthorize("hasRole('ISSUER')")
    @PostMapping("/invite")
    public ResponseEntity<ApiResponse<Object>> inviteUserToOrganization(@RequestBody Map<String, String> body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        String userEmail = body.get("email");
        String organizationIdStr = body.get("organizationId");
        
        if (userEmail == null || organizationIdStr == null) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(400, "Email and organizationId required", null, null)
            );
        }
        
        Long organizationId;
        try {
            organizationId = Long.parseLong(organizationIdStr);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(400, "Invalid organizationId", null, null)
            );
        }
        
        // Get issuer
        User issuer = userService.findByEmail(email);
        if (issuer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found", null, "User not found")
            );
        }
        
        // Check if issuer has OWNER permission in this organization
        Optional<OrganizationStaff> ownerStaff = organizationStaffRepository.findByUserIdAndOrganizationIdAndStaffRole(
            issuer.getId(), organizationId, "owner");
        if (ownerStaff.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ApiResponse<>(HttpStatus.FORBIDDEN.value(), "You don't have OWNER permission in this organization", null, "No OWNER permission")
            );
        }
        
        // Check if user already exists
        if (userRepository.findByEmail(userEmail).isPresent()) {
            return ResponseEntity.status(409).body(
                new ApiResponse<>(409, "User already exists: " + userEmail, null, null)
            );
        }
        
        // Check if pending invitation already exists
        Optional<UserInvitation> existing = invitationRepository.findByEmailAndStatus(userEmail, UserInvitation.Status.PENDING);
        if (existing.isPresent()) {
            return ResponseEntity.status(409).body(
                new ApiResponse<>(409, "Pending invitation already exists", null, null)
            );
        }
        
        // Create invitation
        UserInvitation invitation = new UserInvitation();
        invitation.setEmail(userEmail);
        invitation.setRole("USER"); // Always USER role for organization users
        invitation.setStatus(UserInvitation.Status.PENDING);
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setCreatedBy(issuer);
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setOrganizationId(organizationId); // Store organization ID for mapping
        invitationRepository.save(invitation);
        
        // Send invitation email with acceptance link
        try {
            String acceptUrl = System.getenv().getOrDefault("UI_BASE_URL", "http://localhost:5173") + "/accept-invitation?token=" + invitation.getToken();
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(userEmail);
            message.setSubject("You're invited to join our organization on Badger Management");
            message.setText("Hello,\n\nYou have been invited to join our organization on Badger Management as a student.\nPlease click the link below to accept the invitation and activate your account:\n" + acceptUrl + "\n\nIf you did not expect this invitation, you can ignore this email.\n");
            message.setFrom("appadmin@taashee.com");
            emailVerificationService.sendCustomEmail(message);
            System.out.println("Single invitation email sent successfully to: " + userEmail); // Debug log
        } catch (Exception ex) {
            System.err.println("Failed to send single invitation email to " + userEmail + ": " + ex.getMessage()); // Debug log
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Failed to send invitation email: " + ex.getMessage(), null, null));
        }
        
        // Return only essential invitation data to avoid serialization issues
        Map<String, Object> invitationData = new HashMap<>();
        invitationData.put("id", invitation.getId());
        invitationData.put("email", invitation.getEmail());
        invitationData.put("status", invitation.getStatus());
        invitationData.put("createdAt", invitation.getCreatedAt());
        
        return ResponseEntity.ok(new ApiResponse<>(200, "Invitation sent", invitationData, null));
    }

    @Operation(summary = "Bulk invite users to organization", description = "ISSUER: Bulk invite users to their organization.")
    @PreAuthorize("hasRole('ISSUER')")
    @PostMapping("/bulk-invite")
    public ResponseEntity<ApiResponse<Map<String, Object>>> bulkInviteUsers(@RequestBody List<Map<String, String>> users, @RequestParam Long organizationId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        // Get issuer
        User issuer = userService.findByEmail(email);
        if (issuer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found", null, "User not found")
            );
        }
        
        // Check if issuer has OWNER permission in this organization
        Optional<OrganizationStaff> ownerStaff = organizationStaffRepository.findByUserIdAndOrganizationIdAndStaffRole(
            issuer.getId(), organizationId, "owner");
        if (ownerStaff.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ApiResponse<>(HttpStatus.FORBIDDEN.value(), "You don't have OWNER permission in this organization", null, "No OWNER permission")
            );
        }
        
        int successCount = 0;
        int errorCount = 0;
        StringBuilder errorDetails = new StringBuilder();
        List<String> alreadyRegistered = new ArrayList<>();
        
        for (Map<String, String> userRow : users) {
            String userEmail = userRow.get("email");
            if (userEmail == null) {
                errorCount++;
                errorDetails.append("Missing email field\n");
                continue;
            }
            
            if (userRepository.findByEmail(userEmail).isPresent()) {
                alreadyRegistered.add(userEmail);
                continue;
            }
            
            if (invitationRepository.findByEmailAndStatus(userEmail, UserInvitation.Status.PENDING).isPresent()) {
                errorCount++;
                errorDetails.append("Pending invitation already exists: ").append(userEmail).append("\n");
                continue;
            }
            
            try {
                UserInvitation invitation = new UserInvitation();
                invitation.setEmail(userEmail);
                invitation.setRole("USER");
                invitation.setStatus(UserInvitation.Status.PENDING);
                invitation.setToken(UUID.randomUUID().toString());
                invitation.setCreatedBy(issuer);
                invitation.setCreatedAt(LocalDateTime.now());
                invitation.setOrganizationId(organizationId);
                invitationRepository.save(invitation);
                
                // Send invitation email with acceptance link
                try {
                    String acceptUrl = System.getenv().getOrDefault("UI_BASE_URL", "http://localhost:5173") + "/accept-invitation?token=" + invitation.getToken();
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setTo(userEmail);
                    message.setSubject("You're invited to join our organization on Badger Management");
                    message.setText("Hello,\n\nYou have been invited to join our organization on Badger Management as a student.\nPlease click the link below to accept the invitation and activate your account:\n" + acceptUrl + "\n\nIf you did not expect this invitation, you can ignore this email.\n");
                    message.setFrom("appadmin@taashee.com");
                    emailVerificationService.sendCustomEmail(message);
                    System.out.println("Email sent successfully to: " + userEmail); // Debug log
                } catch (Exception emailEx) {
                    System.err.println("Failed to send email to " + userEmail + ": " + emailEx.getMessage()); // Debug log
                    errorCount++;
                    errorDetails.append("Failed to send email to: ").append(userEmail).append(" (Reason: ").append(emailEx.getMessage()).append(")\n");
                    continue; // Skip this user if email fails
                }
                
                successCount++;
            } catch (Exception ex) {
                errorCount++;
                errorDetails.append("Failed to create invitation for: ").append(userEmail).append(" (Reason: ").append(ex.getMessage()).append(")\n");
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("errorCount", errorCount);
        result.put("errorDetails", errorDetails.toString());
        result.put("alreadyRegistered", alreadyRegistered);
        
        return ResponseEntity.ok(new ApiResponse<>(200, 
            "Bulk invite completed. Success: " + successCount + ", Errors: " + errorCount + ", Already registered: " + alreadyRegistered.size(), 
            result, null));
    }

    @Operation(summary = "Delete user from organization", description = "ISSUER: Remove a user from their organization.")
    @PreAuthorize("hasRole('ISSUER')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Object>> removeUserFromOrganization(@PathVariable Long userId, @RequestParam Long organizationId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        // Get issuer
        User issuer = userService.findByEmail(email);
        if (issuer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found", null, "User not found")
            );
        }
        
        // Check if issuer has OWNER permission in this organization
        Optional<OrganizationStaff> ownerStaff = organizationStaffRepository.findByUserIdAndOrganizationIdAndStaffRole(
            issuer.getId(), organizationId, "owner");
        if (ownerStaff.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ApiResponse<>(HttpStatus.FORBIDDEN.value(), "You don't have OWNER permission in this organization", null, "No OWNER permission")
            );
        }
        
        // Remove user from organization
        Optional<OrganizationUser> orgUser = organizationUserRepository.findByOrganizationIdAndUserId(organizationId, userId);
        if (orgUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found in organization", null, "User not in organization")
            );
        }
        
        organizationUserRepository.delete(orgUser.get());
        
        return ResponseEntity.ok(new ApiResponse<>(200, "User removed from organization", null, null));
    }
} 