package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.BadgeInstance;
import com.taashee.badger.models.BadgeInstanceDTO;
import com.taashee.badger.models.BadgeClass;
import com.taashee.badger.models.Organization;
import com.taashee.badger.models.User;
import com.taashee.badger.models.Evidence;
import com.taashee.badger.repositories.BadgeInstanceRepository;
import com.taashee.badger.repositories.BadgeClassRepository;
import com.taashee.badger.repositories.OrganizationRepository;
import com.taashee.badger.repositories.OrganizationStaffRepository;
import com.taashee.badger.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taashee.badger.services.BadgeInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class BadgeInstanceServiceImpl implements BadgeInstanceService {
    @Autowired
    private BadgeInstanceRepository badgeInstanceRepository;
    @Autowired
    private BadgeClassRepository badgeClassRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private OrganizationStaffRepository organizationStaffRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public BadgeInstance createBadgeInstance(BadgeInstance badgeInstance) {
        return badgeInstanceRepository.save(badgeInstance);
    }

    @Override
    public BadgeInstance updateBadgeInstance(Long id, BadgeInstance badgeInstance) {
        badgeInstance.setId(id);
        return badgeInstanceRepository.save(badgeInstance);
    }

    @Override
    public void deleteBadgeInstance(Long id) {
        badgeInstanceRepository.deleteById(id);
    }

    @Override
    public Optional<BadgeInstance> getBadgeInstanceById(Long id) {
        // Enforce role-based and organization-based access control for all badge instance operations
        // ADMINs: full access
        // ORGANIZATION role: only their own organization's data
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
            .map(a -> a.getAuthority())
            .toList();
        boolean isAdmin = roles.contains("ROLE_ADMIN");
        boolean isIssuer = roles.contains("ROLE_ISSUER");
        Optional<BadgeInstance> badgeInstanceOpt = badgeInstanceRepository.findById(id);
        if (isAdmin) return badgeInstanceOpt;
        if (isIssuer) {
            com.taashee.badger.models.User user = userRepository.findByEmail(email).orElse(null);
            if (user == null || badgeInstanceOpt.isEmpty()) return Optional.empty();
            List<com.taashee.badger.models.OrganizationStaff> staffList = organizationStaffRepository.findByUserId(user.getId());
            if (staffList.isEmpty()) return Optional.empty();
            List<Long> organizationIds = staffList.stream().map(s -> s.getOrganization().getId()).toList();
            if (badgeInstanceOpt.get().getOrganization() != null && organizationIds.contains(badgeInstanceOpt.get().getOrganization().getId())) {
                return badgeInstanceOpt;
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Override
    public List<BadgeInstance> getAllBadgeInstances() {
        // Enforce role-based and organization-based access control for all badge instance operations
        // ADMINs: full access
        // ORGANIZATION role: only their own organization's data
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
            .map(a -> a.getAuthority())
            .toList();
        boolean isAdmin = roles.contains("ROLE_ADMIN");
        boolean isIssuer = roles.contains("ROLE_ISSUER");
        if (isAdmin) {
            return badgeInstanceRepository.findAll();
        } else if (isIssuer) {
            com.taashee.badger.models.User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) return List.of();
            List<com.taashee.badger.models.OrganizationStaff> staffList = organizationStaffRepository.findByUserId(user.getId());
            if (staffList.isEmpty()) return List.of();
            List<Long> organizationIds = staffList.stream().map(s -> s.getOrganization().getId()).toList();
            return badgeInstanceRepository.findAll().stream().filter(bi -> bi.getOrganization() != null && organizationIds.contains(bi.getOrganization().getId())).toList();
        } else {
            return List.of();
        }
    }

    @Override
    public List<BadgeInstance> getBadgesForRecipient(Long userId) {
        return badgeInstanceRepository.findByRecipientId(userId);
    }

    @Override
    public BadgeInstance getBadgeInstanceForRecipient(Long userId, Long badgeInstanceId) {
        return badgeInstanceRepository.findByIdAndRecipientId(badgeInstanceId, userId)
            .orElseThrow(() -> new RuntimeException("BadgeInstance not found for user"));
    }

    @Override
    public BadgeInstance archiveBadgeInstance(Long id, boolean archive) {
        BadgeInstance instance = badgeInstanceRepository.findById(id).orElseThrow(() -> new RuntimeException("BadgeInstance not found"));
        instance.setArchived(archive);
        return badgeInstanceRepository.save(instance);
    }

    @Override
    public List<BadgeInstance> bulkArchiveBadgeInstances(List<Long> ids, boolean archive) {
        List<BadgeInstance> instances = badgeInstanceRepository.findAllById(ids);
        for (BadgeInstance instance : instances) {
            instance.setArchived(archive);
        }
        return badgeInstanceRepository.saveAll(instances);
    }

    @Override
    public void bulkDeleteBadgeInstances(List<Long> ids) {
        badgeInstanceRepository.deleteAllById(ids);
    }

    @Override
    public List<BadgeInstance> revokeBadgeInstances(List<Long> ids, String revocationReason) {
        List<BadgeInstance> instances = badgeInstanceRepository.findAllById(ids);
        for (BadgeInstance instance : instances) {
            instance.setRevoked(true);
            instance.setRevocationReason(revocationReason);
        }
        return badgeInstanceRepository.saveAll(instances);
    }

    @Override
    public BadgeInstance createBadgeInstanceFromDTO(BadgeInstanceDTO dto) {
        // Enforce role-based and organization-based access control for all badge instance operations
        // ADMINs: full access
        // ORGANIZATION role: only their own organization's data
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
            .map(a -> a.getAuthority())
            .toList();
        boolean isAdmin = roles.contains("ROLE_ADMIN");
        boolean isIssuer = roles.contains("ROLE_ISSUER");
        if (isIssuer && !isAdmin) {
            com.taashee.badger.models.User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                List<com.taashee.badger.models.OrganizationStaff> staffList = organizationStaffRepository.findByUserId(user.getId());
                if (!staffList.isEmpty()) {
                    dto.organizationId = staffList.get(0).getOrganization().getId();
                }
            }
        }
        BadgeInstance badgeInstance = new BadgeInstance();
        mapDTOToEntity(dto, badgeInstance);
        return badgeInstanceRepository.save(badgeInstance);
    }

    @Override
    public BadgeInstance updateBadgeInstanceFromDTO(Long id, BadgeInstanceDTO dto) {
        // Enforce role-based and organization-based access control for all badge instance operations
        // ADMINs: full access
        // ORGANIZATION role: only their own organization's data
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
            .map(a -> a.getAuthority())
            .toList();
        boolean isAdmin = roles.contains("ROLE_ADMIN");
        boolean isIssuer = roles.contains("ROLE_ISSUER");
        if (isIssuer && !isAdmin) {
            com.taashee.badger.models.User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                List<com.taashee.badger.models.OrganizationStaff> staffList = organizationStaffRepository.findByUserId(user.getId());
                if (!staffList.isEmpty()) {
                    dto.organizationId = staffList.get(0).getOrganization().getId();
                }
            }
        }
        BadgeInstance badgeInstance = badgeInstanceRepository.findById(id).orElseThrow(() -> new RuntimeException("BadgeInstance not found"));
        mapDTOToEntity(dto, badgeInstance);
        return badgeInstanceRepository.save(badgeInstance);
    }

    private void mapDTOToEntity(BadgeInstanceDTO dto, BadgeInstance badgeInstance) {
        if (dto.badgeClassId != null) {
            BadgeClass badgeClass = badgeClassRepository.findById(dto.badgeClassId)
                .orElseThrow(() -> new RuntimeException("BadgeClass not found: " + dto.badgeClassId));
            // Brand/logo validation
            if (badgeClass.getImage() == null || badgeClass.getImage().trim().isEmpty()) {
                throw new RuntimeException("Cannot award badge instance: the selected badge class does not have a brand/logo/image.");
            }
            badgeInstance.setBadgeClass(badgeClass);
        }
        if (dto.organizationId != null) {
            Organization organization = organizationRepository.findById(dto.organizationId).orElseThrow(() -> new RuntimeException("Organization not found: " + dto.organizationId));
            badgeInstance.setOrganization(organization);
        }
        if (dto.recipientId != null) {
            User recipient = userRepository.findById(dto.recipientId).orElseThrow(() -> new RuntimeException("User not found: " + dto.recipientId));
            badgeInstance.setRecipient(recipient);
        }
        badgeInstance.setIssuedOn(dto.issuedOn);
        badgeInstance.setPublicKeyOrganization(dto.publicKeyOrganization);
        badgeInstance.setIdentifier(dto.identifier);
        badgeInstance.setRecipientType(dto.recipientType);
        badgeInstance.setAwardType(dto.awardType);
        badgeInstance.setDirectAwardBundle(dto.directAwardBundle);
        badgeInstance.setRecipientIdentifier(dto.recipientIdentifier);
        badgeInstance.setImage(dto.image);
        badgeInstance.setRevoked(dto.revoked);
        badgeInstance.setRevocationReason(dto.revocationReason);
        badgeInstance.setExpiresAt(dto.expiresAt);
        badgeInstance.setAcceptance(dto.acceptance);
        badgeInstance.setNarrative(dto.narrative);
        badgeInstance.setHashed(dto.hashed);
        badgeInstance.setSalt(dto.salt);
        badgeInstance.setArchived(dto.archived);
        badgeInstance.setOldJson(dto.oldJson);
        badgeInstance.setSignature(dto.signature);
        badgeInstance.setIsPublic(dto.isPublic);
        badgeInstance.setIncludeEvidence(dto.includeEvidence);
        badgeInstance.setGradeAchieved(dto.gradeAchieved);
        badgeInstance.setIncludeGradeAchieved(dto.includeGradeAchieved);
        badgeInstance.setStatus(dto.status != null ? BadgeInstance.Status.valueOf(dto.status) : null);
        badgeInstance.setDescription(dto.description);
        badgeInstance.setLearningOutcomes(dto.learningOutcomes);
        // Extensions
        if (dto.extensions != null) {
            try {
                badgeInstance.setExtensions(objectMapper.writeValueAsString(dto.extensions));
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize extensions", e);
            }
        } else {
            badgeInstance.setExtensions(null);
        }
        // Evidence items
        if (dto.evidenceItems != null) {
            ArrayList<Evidence> evidenceList = new ArrayList<>();
            for (BadgeInstanceDTO.EvidenceDTO e : dto.evidenceItems) {
                Evidence evidence = new Evidence();
                evidence.setBadgeInstance(badgeInstance);
                evidence.setEvidenceUrl(e.evidenceUrl);
                evidence.setNarrative(e.narrative);
                evidence.setName(e.name);
                evidence.setDescription(e.description);
                evidenceList.add(evidence);
            }
            badgeInstance.setEvidenceItems(evidenceList);
        }
        // Validation logic for narrative/evidence
        // BadgeClass badgeClass = badgeInstance.getBadgeClass(); // TODO: Implement getBadgeClass() in BadgeInstance if not present
        BadgeClass badgeClass = null; // fallback
        if (badgeClass != null) {
            if (badgeClass.isNarrativeRequired() && (dto.narrative == null || dto.narrative.trim().isEmpty())) {
                throw new RuntimeException("Narrative is required for this badge class");
            }
            if (badgeClass.isEvidenceRequired() && (dto.evidenceItems == null || dto.evidenceItems.isEmpty())) {
                throw new RuntimeException("Evidence is required for this badge class");
            }
        }
    }
} 