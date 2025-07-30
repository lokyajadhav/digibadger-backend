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
import com.taashee.badger.repositories.OrganizationStaffRepository;
import com.taashee.badger.repositories.OrganizationUserRepository;
import com.taashee.badger.repositories.BadgeInstanceRepository;
import com.taashee.badger.repositories.BadgeClassRepository;

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
    @Autowired
    private OrganizationStaffRepository organizationStaffRepository;
    @Autowired
    private OrganizationUserRepository organizationUserRepository;
    @Autowired
    private BadgeInstanceRepository badgeInstanceRepository;
    @Autowired
    private BadgeClassRepository badgeClassRepository;
    @Value("${staff.default.password}")
    private String defaultStaffPassword;

    @Override
    @Transactional
    public Organization createOrganization(Organization organization) {
        // Ensure main contact/owner exists as user with ISSUER role
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
        // Always add ISSUER role if not present
        if (user.getRoles() == null) user.setRoles(new HashSet<>());
        if (!user.getRoles().contains("ISSUER")) {
            user.getRoles().add("ISSUER");
        }
        userRepository.save(user);
        if (isNewUser) {
            emailVerificationService.sendStaffInvitationEmail(user, defaultStaffPassword);
        }
        Organization savedOrg = organizationRepository.save(organization);
        // Map user as main owner staff
        com.taashee.badger.models.OrganizationStaff staff = new com.taashee.badger.models.OrganizationStaff();
        staff.setOrganization(savedOrg);
        staff.setUser(user);
        staff.setStaffRole("owner");
        staff.setSigner(true);
        // Save staff mapping
        organizationStaffRepository.save(staff);
        return savedOrg;
    }

    @Override
    @Transactional
    public Organization updateOrganization(Long id, Organization organization) {
        Organization existing = organizationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Organization not found"));
        // Only update allowed fields, do not touch staff or badges collections
        existing.setNameEnglish(organization.getNameEnglish());
        existing.setDescriptionEnglish(organization.getDescriptionEnglish());
        existing.setImageEnglish(organization.getImageEnglish());
        existing.setUrlEnglish(organization.getUrlEnglish());
        existing.setEmail(organization.getEmail());
        existing.setFaculty(organization.getFaculty());
        existing.setInstitutionName(organization.getInstitutionName());
        existing.setInstitutionIdentifier(organization.getInstitutionIdentifier());
        existing.setGradingTableUrl(organization.getGradingTableUrl());
        existing.setArchived(organization.getArchived());
        existing.setBadgrApp(organization.getBadgrApp());
        existing.setOldJson(organization.getOldJson());
        // Do NOT set staff or badges collections!
        return organizationRepository.save(existing);
    }

    @Override
    public void deleteOrganization(Long id) {
        // Remove all staff mappings for this organization
        organizationStaffRepository.deleteByOrganizationId(id);
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
    @Transactional
    public void bulkDeleteOrganizations(List<Long> ids) {
        for (Long id : ids) {
            // First delete organization users
            organizationUserRepository.deleteByOrganizationId(id);
            // Then delete organization staff
            organizationStaffRepository.deleteByOrganizationId(id);
            // Delete badge instances for this organization's badge classes
            badgeInstanceRepository.deleteByBadgeClassOrganizationId(id);
            // Delete badge classes for this organization
            badgeClassRepository.deleteByOrganizationId(id);
            // Finally delete the organization
            organizationRepository.deleteById(id);
        }
    }

    @Override
    public List<Organization> getOrganizationsForUser(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return List.of();
        List<com.taashee.badger.models.OrganizationStaff> staffList = organizationStaffRepository.findByUserId(user.getId());
        if (staffList.isEmpty()) return List.of();
        // Return only minimal organization info to avoid proxy issues
        return staffList.stream()
            .map(s -> {
                Organization org = new Organization();
                org.setId(s.getOrganization().getId());
                org.setNameEnglish(s.getOrganization().getNameEnglish());
                org.setEmail(s.getOrganization().getEmail());
                org.setArchived(s.getOrganization().getArchived());
                return org;
            })
            .distinct()
            .toList();
    }
} 