package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.IssuerStaff;
import com.taashee.badger.models.User;
import com.taashee.badger.models.Issuer;
import com.taashee.badger.repositories.IssuerStaffRepository;
import com.taashee.badger.repositories.UserRepository;
import com.taashee.badger.repositories.IssuerRepository;
import com.taashee.badger.services.IssuerStaffService;
import com.taashee.badger.services.EmailVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashSet;
import com.taashee.badger.repositories.IssuerStaffInvitationRepository;
import com.taashee.badger.models.IssuerStaffInvitation;

@Service
public class IssuerStaffServiceImpl implements IssuerStaffService {
    @Autowired
    private IssuerStaffRepository issuerStaffRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IssuerRepository issuerRepository;
    @Autowired
    private EmailVerificationService emailVerificationService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private IssuerStaffInvitationRepository invitationRepository;

    @Value("${staff.default.password}")
    private String defaultStaffPassword;

    @Override
    public List<IssuerStaff> getStaffByIssuerId(Long issuerId) {
        return issuerStaffRepository.findByIssuerId(issuerId);
    }

    @Override
    @Transactional
    public IssuerStaff addStaffToIssuer(Long issuerId, IssuerStaff staff) {
        // Ensure user exists
        User user = userRepository.findByEmail(staff.getUser().getEmail()).orElse(null);
        boolean isNewUser = false;
        if (user == null) {
            user = new User();
            user.setEmail(staff.getUser().getEmail());
            user.setFirstName(staff.getUser().getFirstName());
            user.setLastName(staff.getUser().getLastName());
            user.setEnabled(true);
            user.setPassword(passwordEncoder.encode(defaultStaffPassword));
            user.setRoles(new HashSet<>()); // Start with empty roles
            isNewUser = true;
        }
        // Always add ISSUER application role if not present
        if (user.getRoles() == null) user.setRoles(new HashSet<>());
        if (!user.getRoles().contains("ISSUER")) {
            user.getRoles().add("ISSUER");
        }
        // If user has any other application role (ADMIN, USER, etc.) and is being assigned as main issuer, throw exception
        if (!isNewUser && staff.getStaffRole() != null && staff.getStaffRole().equalsIgnoreCase("owner")) {
            for (String role : user.getRoles()) {
                if (!role.equals("ISSUER")) {
                    throw new RuntimeException("User already has another application role and cannot be assigned as issuer.");
                }
            }
        }
        userRepository.save(user);
        staff.setUser(user);
        Issuer issuer = issuerRepository.findById(issuerId)
            .orElseThrow(() -> new RuntimeException("Issuer not found"));
        staff.setIssuer(issuer);
        IssuerStaff savedStaff = issuerStaffRepository.save(staff);
        if (isNewUser) {
            emailVerificationService.sendStaffInvitationEmail(user, defaultStaffPassword);
        }
        return savedStaff;
    }

    @Override
    @Transactional
    public IssuerStaff updateStaff(Long staffId, IssuerStaff staff) {
        IssuerStaff existing = issuerStaffRepository.findById(staffId)
            .orElseThrow(() -> new RuntimeException("IssuerStaff not found"));
        existing.setStaffRole(staff.getStaffRole());
        existing.setSigner(staff.isSigner());
        return issuerStaffRepository.save(existing);
    }

    @Override
    @Transactional
    public void removeStaff(Long staffId) {
        issuerStaffRepository.deleteById(staffId);
    }

    @Override
    @Transactional
    public void removeStaffCompletely(Long issuerId, Long staffId, boolean fullDelete) {
        IssuerStaff staff = issuerStaffRepository.findById(staffId)
            .orElseThrow(() -> new RuntimeException("IssuerStaff not found"));
        User user = staff.getUser();
        issuerStaffRepository.deleteById(staffId);
        if (fullDelete && user != null) {
            // Check if user is staff for any other issuer
            List<IssuerStaff> otherStaff = issuerStaffRepository.findByUserId(user.getId());
            if (otherStaff.isEmpty()) {
                // Check if user has any other roles (besides ISSUER)
                if (user.getRoles() == null || user.getRoles().isEmpty() || (user.getRoles().size() == 1 && user.getRoles().contains("ISSUER"))) {
                    // Delete all invitations for this user
                    List<com.taashee.badger.models.IssuerStaffInvitation> invites = invitationRepository.findByEmailAndStatus(user.getEmail(), com.taashee.badger.models.IssuerStaffInvitation.Status.PENDING);
                    invites.addAll(invitationRepository.findByEmailAndStatus(user.getEmail(), com.taashee.badger.models.IssuerStaffInvitation.Status.ACCEPTED));
                    for (com.taashee.badger.models.IssuerStaffInvitation inv : invites) {
                        invitationRepository.delete(inv);
                    }
                    userRepository.deleteById(user.getId());
                }
            }
        }
    }

    @Override
    public boolean userExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
} 