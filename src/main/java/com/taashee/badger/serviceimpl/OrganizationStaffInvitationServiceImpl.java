package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.OrganizationStaffInvitation;
import com.taashee.badger.repositories.OrganizationStaffInvitationRepository;
import com.taashee.badger.services.OrganizationStaffInvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.taashee.badger.services.EmailVerificationService;
import org.springframework.mail.SimpleMailMessage;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import com.taashee.badger.models.User;
import com.taashee.badger.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class OrganizationStaffInvitationServiceImpl implements OrganizationStaffInvitationService {

    @Autowired
    private OrganizationStaffInvitationRepository invitationRepository;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Value("${staff.default.password}")
    private String defaultStaffPassword;

    @Value("${app.uiBaseUrl:http://localhost:5173}")
    private String uiBaseUrl;

    public boolean hasActiveInvitation(String email, Long organizationId) {
        List<OrganizationStaffInvitation> pending = invitationRepository.findByEmailAndOrganizationIdAndStatus(email, organizationId, OrganizationStaffInvitation.Status.PENDING);
        List<OrganizationStaffInvitation> accepted = invitationRepository.findByEmailAndOrganizationIdAndStatus(email, organizationId, OrganizationStaffInvitation.Status.ACCEPTED);
        return (!pending.isEmpty() || !accepted.isEmpty());
    }

    @Override
    public OrganizationStaffInvitation createInvitation(OrganizationStaffInvitation invitation) {
        if (hasActiveInvitation(invitation.getEmail(), invitation.getOrganizationId())) {
            throw new RuntimeException("Active invitation already exists for this email and organization.");
        }
        // Generate token if not set
        if (invitation.getToken() == null || invitation.getToken().isEmpty()) {
            invitation.setToken(UUID.randomUUID().toString());
        }
        invitation.setStatus(OrganizationStaffInvitation.Status.PENDING);
        OrganizationStaffInvitation saved = invitationRepository.save(invitation);
        // Send invitation email
        String acceptUrl = uiBaseUrl + "/accept-organization-invitation?token=" + saved.getToken();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(saved.getEmail());
        message.setSubject("You're invited as staff to an Organization on Badger Management");
        message.setText("Hello,\n\nYou have been invited to join an Organization as staff (role: " + saved.getStaffRole() + ").\nPlease click the link below to accept the invitation and activate your account:\n" + acceptUrl + "\n\nIf you did not expect this invitation, you can ignore this email.\n");
        message.setFrom("appadmin@taashee.com");
        emailVerificationService.sendCustomEmail(message);
        return saved;
    }

    @Override
    public Optional<OrganizationStaffInvitation> findByToken(String token) {
        return invitationRepository.findByToken(token);
    }

    @Override
    public List<OrganizationStaffInvitation> findByOrganizationId(Long organizationId) {
        return invitationRepository.findByOrganizationId(organizationId);
    }

    @Override
    public List<OrganizationStaffInvitation> findByEmailAndStatus(String email, OrganizationStaffInvitation.Status status) {
        return invitationRepository.findByEmailAndStatus(email, status);
    }

    @Override
    public List<OrganizationStaffInvitation> findByOrganizationIdAndStatus(Long organizationId, OrganizationStaffInvitation.Status status) {
        return invitationRepository.findByOrganizationIdAndStatus(organizationId, status);
    }

    @Override
    public OrganizationStaffInvitation acceptInvitation(String token) {
        Optional<OrganizationStaffInvitation> invitationOpt = invitationRepository.findByToken(token);
        if (invitationOpt.isPresent()) {
            OrganizationStaffInvitation invitation = invitationOpt.get();
            if (invitation.getStatus() != OrganizationStaffInvitation.Status.PENDING) {
                throw new RuntimeException("Invitation already used or expired");
            }
            // Check if user exists
            User user = userRepository.findByEmail(invitation.getEmail()).orElse(null);
            boolean isNewUser = false;
            if (user == null) {
                user = new User();
                user.setEmail(invitation.getEmail());
                user.setFirstName("");
                user.setLastName("");
                user.setEnabled(true);
                user.setPassword(passwordEncoder.encode(defaultStaffPassword));
                user.setRoles(new java.util.HashSet<>());
                user.getRoles().add("ORGANIZATION");
                isNewUser = true;
                userRepository.save(user);
            } else if (!user.getRoles().contains("ORGANIZATION")) {
                user.getRoles().add("ORGANIZATION");
                userRepository.save(user);
            }
            invitation.setStatus(OrganizationStaffInvitation.Status.ACCEPTED);
            invitation.setAcceptedAt(java.time.LocalDateTime.now());
            invitationRepository.save(invitation);
            // Send credentials email if new user
            if (isNewUser) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("Your Badger Management Account is Ready");
                message.setText("Congratulations! Your account has been created.\n\nLogin details:\nEmail: " + user.getEmail() + "\nPassword: " + defaultStaffPassword + "\nRole: ORGANIZATION\n\nPlease log in and change your password after your first login.\n\nIf you have any questions, contact your administrator.");
                message.setFrom("appadmin@taashee.com");
                emailVerificationService.sendCustomEmail(message);
            }
            return invitation;
        }
        throw new RuntimeException("Invitation not found");
    }

    @Override
    public OrganizationStaffInvitation rejectInvitation(String token) {
        Optional<OrganizationStaffInvitation> invitationOpt = invitationRepository.findByToken(token);
        if (invitationOpt.isPresent()) {
            OrganizationStaffInvitation invitation = invitationOpt.get();
            invitationRepository.delete(invitation);
            return invitation; // Return the deleted invitation for response
        }
        throw new RuntimeException("Invitation not found");
    }

    @Override
    public Optional<OrganizationStaffInvitation> findById(Long id) {
        return invitationRepository.findById(id);
    }
    @Override
    public OrganizationStaffInvitation save(OrganizationStaffInvitation invitation) {
        return invitationRepository.save(invitation);
    }
    public void sendCustomEmail(org.springframework.mail.SimpleMailMessage message) {
        emailVerificationService.sendCustomEmail(message);
    }
} 