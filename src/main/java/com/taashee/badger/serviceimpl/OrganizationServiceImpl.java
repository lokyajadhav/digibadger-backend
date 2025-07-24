package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.Organization;
import com.taashee.badger.models.User;
import com.taashee.badger.repositories.OrganizationRepository;
import com.taashee.badger.repositories.UserRepository;
import com.taashee.badger.services.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.taashee.badger.services.EmailVerificationService;
import org.springframework.stereotype.Service;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class OrganizationServiceImpl implements OrganizationService {
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailVerificationService emailVerificationService;
    @Value("${staff.default.password}")
    private String defaultStaffPassword;

    @Override
    @Transactional
    public Organization createOrganization(Organization organization) {
        // Ensure main contact/owner exists as user with ORGANIZATION role
        String ownerEmail = organization.getEmail();
        if (ownerEmail == null || ownerEmail.isEmpty()) {
            throw new RuntimeException("Organization email (main contact) is required");
        }
        User user = userRepository.findByEmail(ownerEmail).orElse(null);
        boolean isNewUser = false;
        if (user == null) {
            user = new User();
            user.setEmail(ownerEmail);
            user.setFirstName(organization.getNameEnglish() != null ? organization.getNameEnglish() : "");
            user.setLastName("");
            user.setEnabled(true);
            user.setPassword(passwordEncoder.encode(defaultStaffPassword));
            user.setRoles(new HashSet<>());
            isNewUser = true;
        }
        // Always add ORGANIZATION application role if not present
        if (user.getRoles() == null) user.setRoles(new HashSet<>());
        if (!user.getRoles().contains("ORGANIZATION")) {
            user.getRoles().add("ORGANIZATION");
        }
        // If user has any other application role (ADMIN, USER, etc.), throw exception
        for (String role : user.getRoles()) {
            if (!role.equals("ORGANIZATION")) {
                throw new RuntimeException("User already has another application role and cannot be assigned as organization.");
            }
        }
        userRepository.save(user);
        if (isNewUser) {
            emailVerificationService.sendStaffInvitationEmail(user, defaultStaffPassword);
        }
        return organizationRepository.save(organization);
    }

    @Override
    @Transactional
    public Organization updateOrganization(Long id, Organization organization) {
        Organization existing = organizationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Organization not found"));
        // Preserve badges collection
        organization.setBadges(existing.getBadges());
        organization.setId(id);
        // Remove old staff processing logic
        return organizationRepository.save(organization);
    }

    @Override
    public void deleteOrganization(Long id) {
        organizationRepository.deleteById(id);
    }

    @Override
    public Optional<Organization> getOrganizationById(Long id) {
        return organizationRepository.findById(id);
    }

    @Override
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    @Override
    public Organization archiveOrganization(Long id, boolean archive) {
        Organization organization = organizationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Organization not found"));
        organization.setArchived(archive);
        return organizationRepository.save(organization);
    }

    @Override
    public List<Organization> bulkArchiveOrganizations(List<Long> ids, boolean archive) {
        List<Organization> organizations = organizationRepository.findAllById(ids);
        for (Organization organization : organizations) {
            organization.setArchived(archive);
        }
        return organizationRepository.saveAll(organizations);
    }

    @Override
    public void bulkDeleteOrganizations(List<Long> ids) {
        organizationRepository.deleteAllById(ids);
    }
} 