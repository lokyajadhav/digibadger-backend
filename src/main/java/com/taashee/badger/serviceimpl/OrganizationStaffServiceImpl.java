package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.OrganizationStaff;
import com.taashee.badger.models.User;
import com.taashee.badger.models.Organization;
import com.taashee.badger.repositories.OrganizationStaffRepository;
import com.taashee.badger.repositories.UserRepository;
import com.taashee.badger.repositories.OrganizationRepository;
import com.taashee.badger.services.OrganizationStaffService;
import com.taashee.badger.services.EmailVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashSet;
import com.taashee.badger.repositories.OrganizationStaffInvitationRepository;
import com.taashee.badger.models.OrganizationStaffInvitation;

@Service
public class OrganizationStaffServiceImpl implements OrganizationStaffService {
    @Autowired
    private OrganizationStaffRepository organizationStaffRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private EmailVerificationService emailVerificationService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private OrganizationStaffInvitationRepository invitationRepository;

    @Value("${staff.default.password}")
    private String defaultStaffPassword;

    @Override
    public List<OrganizationStaff> getStaffByOrganizationId(Long organizationId) {
        return organizationStaffRepository.findByOrganizationId(organizationId);
    }

    @Override
    public OrganizationStaff getStaffByOrganizationIdAndUserId(Long organizationId, Long userId) {
        return organizationStaffRepository.findByOrganizationIdAndUserId(organizationId, userId).orElse(null);
    }

    @Override
    @Transactional
    public OrganizationStaff addStaffToOrganization(Long organizationId, OrganizationStaff staff) {
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
        // Always add ISSUER role if not present
        if (user.getRoles() == null) user.setRoles(new HashSet<>());
        if (!user.getRoles().contains("ISSUER")) {
            user.getRoles().add("ISSUER");
        }
        // If user has any other application role (ADMIN, USER, etc.) and is being assigned as main organization, throw exception
        if (!isNewUser && staff.getStaffRole() != null && staff.getStaffRole().equalsIgnoreCase("owner")) {
            for (String role : user.getRoles()) {
                if (!role.equals("ISSUER")) {
                    throw new RuntimeException("User already has another application role and cannot be assigned as organization.");
                }
            }
        }
        userRepository.save(user);
        staff.setUser(user);
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new RuntimeException("Organization not found"));
        staff.setOrganization(organization);
        OrganizationStaff savedStaff = organizationStaffRepository.save(staff);
        if (isNewUser) {
            emailVerificationService.sendStaffInvitationEmail(user, defaultStaffPassword);
        }
        return savedStaff;
    }

    @Override
    @Transactional
    public OrganizationStaff updateStaff(Long staffId, OrganizationStaff staff) {
        OrganizationStaff existing = organizationStaffRepository.findById(staffId)
            .orElseThrow(() -> new RuntimeException("OrganizationStaff not found"));
        existing.setStaffRole(staff.getStaffRole());
        existing.setSigner(staff.isSigner());
        return organizationStaffRepository.save(existing);
    }

    @Override
    @Transactional
    public void removeStaff(Long staffId) {
        organizationStaffRepository.deleteById(staffId);
    }

    @Override
    @Transactional
    public void removeStaffCompletely(Long organizationId, Long staffId, boolean fullDelete) {
        OrganizationStaff staff = organizationStaffRepository.findById(staffId)
            .orElseThrow(() -> new RuntimeException("OrganizationStaff not found"));
        User user = staff.getUser();
        organizationStaffRepository.deleteById(staffId);
        if (fullDelete && user != null) {
            // Check if user is staff for any other organization
            List<OrganizationStaff> otherStaff = organizationStaffRepository.findByUserId(user.getId());
            if (otherStaff.isEmpty()) {
                // Check if user has any other roles (besides ISSUER)
                if (user.getRoles() == null || user.getRoles().isEmpty() || (user.getRoles().size() == 1 && user.getRoles().contains("ISSUER"))) {
                    // Delete all invitations for this user
                    List<com.taashee.badger.models.OrganizationStaffInvitation> invites = invitationRepository.findByEmailAndStatus(user.getEmail(), com.taashee.badger.models.OrganizationStaffInvitation.Status.PENDING);
                    invites.addAll(invitationRepository.findByEmailAndStatus(user.getEmail(), com.taashee.badger.models.OrganizationStaffInvitation.Status.ACCEPTED));
                    for (com.taashee.badger.models.OrganizationStaffInvitation inv : invites) {
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