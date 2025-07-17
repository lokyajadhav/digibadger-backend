package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.IssuerStaffInvitation;
import com.taashee.badger.repositories.IssuerStaffInvitationRepository;
import com.taashee.badger.services.IssuerStaffInvitationService;
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
public class IssuerStaffInvitationServiceImpl implements IssuerStaffInvitationService {

    @Autowired
    private IssuerStaffInvitationRepository invitationRepository;

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

    public boolean hasActiveInvitation(String email, Long issuerId) {
        List<IssuerStaffInvitation> pending = invitationRepository.findByEmailAndIssuerIdAndStatus(email, issuerId, IssuerStaffInvitation.Status.PENDING);
        List<IssuerStaffInvitation> accepted = invitationRepository.findByEmailAndIssuerIdAndStatus(email, issuerId, IssuerStaffInvitation.Status.ACCEPTED);
        return (!pending.isEmpty() || !accepted.isEmpty());
    }

    @Override
    public IssuerStaffInvitation createInvitation(IssuerStaffInvitation invitation) {
        if (hasActiveInvitation(invitation.getEmail(), invitation.getIssuerId())) {
            throw new RuntimeException("Active invitation already exists for this email and issuer.");
        }
        // Generate token if not set
        if (invitation.getToken() == null || invitation.getToken().isEmpty()) {
            invitation.setToken(UUID.randomUUID().toString());
        }
        invitation.setStatus(IssuerStaffInvitation.Status.PENDING);
        IssuerStaffInvitation saved = invitationRepository.save(invitation);
        // Send invitation email
        String acceptUrl = uiBaseUrl + "/accept-issuer-invitation?token=" + saved.getToken();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(saved.getEmail());
        message.setSubject("You're invited as staff to an Issuer on Badger Management");
        message.setText("Hello,\n\nYou have been invited to join an Issuer as staff (role: " + saved.getStaffRole() + ").\nPlease click the link below to accept the invitation and activate your account:\n" + acceptUrl + "\n\nIf you did not expect this invitation, you can ignore this email.\n");
        message.setFrom("appadmin@taashee.com");
        emailVerificationService.sendCustomEmail(message);
        return saved;
    }

    @Override
    public Optional<IssuerStaffInvitation> findByToken(String token) {
        return invitationRepository.findByToken(token);
    }

    @Override
    public List<IssuerStaffInvitation> findByIssuerId(Long issuerId) {
        return invitationRepository.findByIssuerId(issuerId);
    }

    @Override
    public List<IssuerStaffInvitation> findByEmailAndStatus(String email, IssuerStaffInvitation.Status status) {
        return invitationRepository.findByEmailAndStatus(email, status);
    }

    @Override
    public List<IssuerStaffInvitation> findByIssuerIdAndStatus(Long issuerId, IssuerStaffInvitation.Status status) {
        return invitationRepository.findByIssuerIdAndStatus(issuerId, status);
    }

    @Override
    public IssuerStaffInvitation acceptInvitation(String token) {
        Optional<IssuerStaffInvitation> invitationOpt = invitationRepository.findByToken(token);
        if (invitationOpt.isPresent()) {
            IssuerStaffInvitation invitation = invitationOpt.get();
            if (invitation.getStatus() != IssuerStaffInvitation.Status.PENDING) {
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
                user.getRoles().add("ISSUER");
                isNewUser = true;
                userRepository.save(user);
            } else if (!user.getRoles().contains("ISSUER")) {
                user.getRoles().add("ISSUER");
                userRepository.save(user);
            }
            invitation.setStatus(IssuerStaffInvitation.Status.ACCEPTED);
            invitation.setAcceptedAt(java.time.LocalDateTime.now());
            invitationRepository.save(invitation);
            // Send credentials email if new user
            if (isNewUser) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("Your Badger Management Account is Ready");
                message.setText("Congratulations! Your account has been created.\n\nLogin details:\nEmail: " + user.getEmail() + "\nPassword: " + defaultStaffPassword + "\nRole: ISSUER\n\nPlease log in and change your password after your first login.\n\nIf you have any questions, contact your administrator.");
                message.setFrom("appadmin@taashee.com");
                emailVerificationService.sendCustomEmail(message);
            }
            return invitation;
        }
        throw new RuntimeException("Invitation not found");
    }

    @Override
    public IssuerStaffInvitation rejectInvitation(String token) {
        Optional<IssuerStaffInvitation> invitationOpt = invitationRepository.findByToken(token);
        if (invitationOpt.isPresent()) {
            IssuerStaffInvitation invitation = invitationOpt.get();
            invitationRepository.delete(invitation);
            return invitation; // Return the deleted invitation for response
        }
        throw new RuntimeException("Invitation not found");
    }

    @Override
    public Optional<IssuerStaffInvitation> findById(Long id) {
        return invitationRepository.findById(id);
    }
    @Override
    public IssuerStaffInvitation save(IssuerStaffInvitation invitation) {
        return invitationRepository.save(invitation);
    }
    public void sendCustomEmail(org.springframework.mail.SimpleMailMessage message) {
        emailVerificationService.sendCustomEmail(message);
    }
} 