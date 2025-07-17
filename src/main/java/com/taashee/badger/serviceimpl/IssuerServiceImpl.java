package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.Issuer;
import com.taashee.badger.models.User;
import com.taashee.badger.repositories.IssuerRepository;
import com.taashee.badger.repositories.UserRepository;
import com.taashee.badger.services.IssuerService;
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
public class IssuerServiceImpl implements IssuerService {
    @Autowired
    private IssuerRepository issuerRepository;
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
    public Issuer createIssuer(Issuer issuer) {
        // Ensure main contact/owner exists as user with ISSUER role
        String ownerEmail = issuer.getEmail();
        if (ownerEmail == null || ownerEmail.isEmpty()) {
            throw new RuntimeException("Issuer email (main contact) is required");
        }
        User user = userRepository.findByEmail(ownerEmail).orElse(null);
        boolean isNewUser = false;
        if (user == null) {
            user = new User();
            user.setEmail(ownerEmail);
            user.setFirstName(issuer.getNameEnglish() != null ? issuer.getNameEnglish() : "");
            user.setLastName("");
            user.setEnabled(true);
            user.setPassword(passwordEncoder.encode(defaultStaffPassword));
            user.setRoles(new HashSet<>());
            isNewUser = true;
        }
        // Always add ISSUER application role if not present
        if (user.getRoles() == null) user.setRoles(new HashSet<>());
        if (!user.getRoles().contains("ISSUER")) {
            user.getRoles().add("ISSUER");
        }
        // If user has any other application role (ADMIN, USER, etc.), throw exception
        for (String role : user.getRoles()) {
            if (!role.equals("ISSUER")) {
                throw new RuntimeException("User already has another application role and cannot be assigned as issuer.");
            }
        }
        userRepository.save(user);
        if (isNewUser) {
            emailVerificationService.sendStaffInvitationEmail(user, defaultStaffPassword);
        }
        return issuerRepository.save(issuer);
    }

    @Override
    @Transactional
    public Issuer updateIssuer(Long id, Issuer issuer) {
        Issuer existing = issuerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Issuer not found"));
        // Preserve badges collection
        issuer.setBadges(existing.getBadges());
        issuer.setId(id);
        // Remove old staff processing logic
        return issuerRepository.save(issuer);
    }

    @Override
    public void deleteIssuer(Long id) {
        issuerRepository.deleteById(id);
    }

    @Override
    public Optional<Issuer> getIssuerById(Long id) {
        return issuerRepository.findById(id);
    }

    @Override
    public List<Issuer> getAllIssuers() {
        return issuerRepository.findAll();
    }

    @Override
    public Issuer archiveIssuer(Long id, boolean archive) {
        Issuer issuer = issuerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Issuer not found"));
        issuer.setArchived(archive);
        return issuerRepository.save(issuer);
    }

    @Override
    public List<Issuer> bulkArchiveIssuers(List<Long> ids, boolean archive) {
        List<Issuer> issuers = issuerRepository.findAllById(ids);
        for (Issuer issuer : issuers) {
            issuer.setArchived(archive);
        }
        return issuerRepository.saveAll(issuers);
    }

    @Override
    public void bulkDeleteIssuers(List<Long> ids) {
        issuerRepository.deleteAllById(ids);
    }
} 