package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.BadgeClass;
import com.taashee.badger.repositories.BadgeClassRepository;
import com.taashee.badger.services.BadgeClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import com.taashee.badger.models.BadgeInstance;
import com.taashee.badger.models.BadgeInstanceAwardRequest;
import com.taashee.badger.repositories.BadgeInstanceRepository;
import com.taashee.badger.models.BadgeClassDTO;
import com.taashee.badger.models.Tag;
import com.taashee.badger.models.Alignment;
import com.taashee.badger.models.Institution;
import com.taashee.badger.repositories.TagRepository;
import com.taashee.badger.repositories.InstitutionRepository;
import java.util.HashSet;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Set;
import org.springframework.security.core.context.SecurityContextHolder;
import com.taashee.badger.repositories.OrganizationStaffRepository;
import com.taashee.badger.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class BadgeClassServiceImpl implements BadgeClassService {
    private static final Logger logger = LoggerFactory.getLogger(BadgeClassServiceImpl.class);
    @Autowired
    private BadgeClassRepository badgeClassRepository;

    @Autowired
    private BadgeInstanceRepository badgeInstanceRepository;

    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private InstitutionRepository institutionRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrganizationStaffRepository organizationStaffRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public BadgeClass createBadgeClass(BadgeClass badgeClass) {
        return badgeClassRepository.save(badgeClass);
    }

    @Override
    public BadgeClass updateBadgeClass(Long id, BadgeClass badgeClass) {
        badgeClass.setId(id);
        return badgeClassRepository.save(badgeClass);
    }

    @Override
    public void deleteBadgeClass(Long id) {
        badgeClassRepository.deleteById(id);
    }

    @Override
    public Optional<BadgeClass> getBadgeClassById(Long id) {
        // Enforce role-based and organization-based access control for all badge class operations
        // ADMINs: full access
        // ORGANIZATION role: only their own organization's data
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
            .map(a -> a.getAuthority())
            .toList();
        boolean isAdmin = roles.contains("ROLE_ADMIN");
        boolean isIssuer = roles.contains("ROLE_ISSUER");
        Optional<BadgeClass> badgeClassOpt = badgeClassRepository.findById(id);
        if (isAdmin) return badgeClassOpt;
        if (isIssuer) {
            com.taashee.badger.models.User user = userRepository.findByEmail(email).orElse(null);
            if (user == null || badgeClassOpt.isEmpty()) return Optional.empty();
            List<com.taashee.badger.models.OrganizationStaff> staffList = organizationStaffRepository.findByUserId(user.getId());
            if (staffList.isEmpty()) return Optional.empty();
            List<Long> organizationIds = staffList.stream().map(s -> s.getOrganization().getId()).toList();
            if (badgeClassOpt.get().getOrganization() != null && organizationIds.contains(badgeClassOpt.get().getOrganization().getId())) {
                return badgeClassOpt;
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Override
    public List<BadgeClass> getAllBadgeClasses() {
        // Get current user email and roles
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
            .map(a -> a.getAuthority())
            .toList();
        boolean isAdmin = roles.contains("ROLE_ADMIN");
        boolean isIssuer = roles.contains("ROLE_ISSUER");
        if (isAdmin) {
            logger.info("[getAllBadgeClasses] ADMIN user: {} roles: {} - returning all badge classes", email, roles);
            return badgeClassRepository.findAll();
        } else if (isIssuer) {
            com.taashee.badger.models.User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                logger.warn("[getAllBadgeClasses] ISSUER user not found: {}", email);
                return List.of();
            }
            List<com.taashee.badger.models.OrganizationStaff> staffList = organizationStaffRepository.findByUserId(user.getId());
            if (staffList.isEmpty()) {
                logger.warn("[getAllBadgeClasses] ISSUER user {} has no organization staff records", email);
                return List.of();
            }
            List<Long> organizationIds = staffList.stream().map(s -> s.getOrganization().getId()).toList();
            List<BadgeClass> filtered = badgeClassRepository.findAll().stream().filter(bc -> bc.getOrganization() != null && organizationIds.contains(bc.getOrganization().getId())).toList();
            logger.info("[getAllBadgeClasses] ISSUER user: {} roles: {} organizationIds: {} - returning {} badge classes", email, roles, organizationIds, filtered.size());
            return filtered;
        } else {
            logger.info("[getAllBadgeClasses] Non-admin/non-issuer user: {} roles: {} - returning empty list", email, roles);
            return List.of();
        }
    }

    @Override
    public BadgeClass archiveBadgeClass(Long id, boolean archive) {
        BadgeClass badgeClass = badgeClassRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("BadgeClass not found"));
        badgeClass.setArchived(archive);
        return badgeClassRepository.save(badgeClass);
    }

    @Override
    public List<BadgeInstance> awardEnrollments(Long badgeClassId, List<BadgeInstanceAwardRequest> requests) {
        BadgeClass badgeClass = badgeClassRepository.findById(badgeClassId)
            .orElseThrow(() -> new RuntimeException("BadgeClass not found"));
        List<BadgeInstance> awarded = new ArrayList<>();
        for (BadgeInstanceAwardRequest req : requests) {
            BadgeInstance instance = new BadgeInstance();
            instance.setBadgeClass(badgeClass);
            instance.setRecipientIdentifier(req.getRecipientIdentifier());
            instance.setRecipientType(req.getRecipientType());
            instance.setAwardType(req.getAwardType());
            instance.setNarrative(req.getNarrative());
            // Set other fields as needed (e.g., issuedOn, issuer, etc.)
            awarded.add(badgeInstanceRepository.save(instance));
        }
        return awarded;
    }

    @Override
    public List<BadgeClass> bulkArchiveBadgeClasses(List<Long> ids, boolean archive) {
        List<BadgeClass> badgeClasses = badgeClassRepository.findAllById(ids);
        for (BadgeClass bc : badgeClasses) {
            bc.setArchived(archive);
        }
        return badgeClassRepository.saveAll(badgeClasses);
    }

    @Override
    public void bulkDeleteBadgeClasses(List<Long> ids) {
        badgeClassRepository.deleteAllById(ids);
    }

    @Override
    public BadgeClass createBadgeClassFromDTO(BadgeClassDTO dto) {
        // Enforce role-based and organization-based access control for all badge class operations
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
                    // Use the first organizationId (or all, if supporting multiple)
                    dto.organizationId = staffList.get(0).getOrganization().getId();
                    logger.info("[createBadgeClassFromDTO] ISSUER user: {} roles: {} - setting organizationId to {}", email, roles, dto.organizationId);
                } else {
                    logger.warn("[createBadgeClassFromDTO] ISSUER user: {} roles: {} - has no organization staff records", email, roles);
                }
            } else {
                logger.warn("[createBadgeClassFromDTO] ISSUER user not found: {}", email);
            }
        } else {
            logger.info("[createBadgeClassFromDTO] ADMIN or other user: {} roles: {} - using provided organizationId: {}", email, roles, dto.organizationId);
        }
        BadgeClass badgeClass = new BadgeClass();
        mapDTOToEntity(dto, badgeClass);
        return badgeClassRepository.save(badgeClass);
    }

    @Override
    public BadgeClass updateBadgeClassFromDTO(Long id, BadgeClassDTO dto) {
        // Enforce role-based and organization-based access control for all badge class operations
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
        BadgeClass badgeClass = badgeClassRepository.findById(id).orElseThrow(() -> new RuntimeException("BadgeClass not found"));
        mapDTOToEntity(dto, badgeClass);
        return badgeClassRepository.save(badgeClass);
    }

    private void mapDTOToEntity(BadgeClassDTO dto, BadgeClass badgeClass) {
        // Validation logic
        if (dto.name == null || dto.name.trim().isEmpty()) {
            throw new RuntimeException("Badge name is required");
        }
        if (!dto.isPrivate) {
            if (dto.description == null || dto.description.trim().isEmpty()) {
                throw new RuntimeException("Description is required for public badges");
            }
            if (dto.criteriaText == null || dto.criteriaText.trim().isEmpty()) {
                throw new RuntimeException("Criteria text is required for public badges");
            }
        }
        if ("micro_credential".equals(dto.badgeClassType)) {
            if (dto.participation == null || dto.participation.trim().isEmpty()) {
                throw new RuntimeException("Participation is required for micro credentials");
            }
            if (dto.assessmentType == null || dto.assessmentType.trim().isEmpty()) {
                throw new RuntimeException("Assessment type is required for micro credentials");
            }
            if (dto.qualityAssuranceName == null || dto.qualityAssuranceName.trim().isEmpty()) {
                throw new RuntimeException("Quality assurance name is required for micro credentials");
            }
            if (dto.qualityAssuranceUrl == null || dto.qualityAssuranceUrl.trim().isEmpty()) {
                throw new RuntimeException("Quality assurance URL is required for micro credentials");
            }
            if (dto.qualityAssuranceDescription == null || dto.qualityAssuranceDescription.trim().isEmpty()) {
                throw new RuntimeException("Quality assurance description is required for micro credentials");
            }
            if (dto.extensions == null || !dto.extensions.containsKey("LearningOutcomeExtension")) {
                throw new RuntimeException("LearningOutcomeExtension is required for micro credentials");
            }
            if (dto.extensions == null || !dto.extensions.containsKey("EQFExtension")) {
                throw new RuntimeException("EQFExtension is required for micro credentials");
            }
        }
        if ("regular".equals(dto.badgeClassType)) {
            if (dto.criteriaText == null || dto.criteriaText.trim().isEmpty()) {
                throw new RuntimeException("Criteria text is required for regular badges");
            }
            if (dto.extensions == null || !dto.extensions.containsKey("LearningOutcomeExtension")) {
                throw new RuntimeException("LearningOutcomeExtension is required for regular badges");
            }
            if (dto.extensions == null || !dto.extensions.containsKey("EQFExtension")) {
                throw new RuntimeException("EQFExtension is required for regular badges");
            }
            if (dto.extensions == null || !dto.extensions.containsKey("EducationProgramIdentifierExtension")) {
                throw new RuntimeException("EducationProgramIdentifierExtension is required for regular badges");
            }
        }
        badgeClass.setName(dto.name);
        badgeClass.setImage(dto.image);
        badgeClass.setDescription(dto.description);
        badgeClass.setCriteriaText(dto.criteriaText);
        badgeClass.setFormal(dto.formal);
        badgeClass.setIsPrivate(dto.isPrivate);
        badgeClass.setNarrativeRequired(dto.narrativeRequired);
        badgeClass.setEvidenceRequired(dto.evidenceRequired);
        badgeClass.setAwardNonValidatedNameAllowed(dto.awardNonValidatedNameAllowed);
        badgeClass.setIsMicroCredentials(dto.isMicroCredentials);
        badgeClass.setDirectAwardingDisabled(dto.directAwardingDisabled);
        badgeClass.setSelfEnrollmentDisabled(dto.selfEnrollmentDisabled);
        badgeClass.setParticipation(dto.participation);
        badgeClass.setAssessmentType(dto.assessmentType);
        badgeClass.setAssessmentIdVerified(dto.assessmentIdVerified);
        badgeClass.setAssessmentSupervised(dto.assessmentSupervised);
        badgeClass.setQualityAssuranceName(dto.qualityAssuranceName);
        badgeClass.setQualityAssuranceUrl(dto.qualityAssuranceUrl);
        badgeClass.setQualityAssuranceDescription(dto.qualityAssuranceDescription);
        badgeClass.setGradeAchievedRequired(dto.gradeAchievedRequired);
        badgeClass.setStackable(dto.stackable);
        badgeClass.setEqfNlqfLevelVerified(dto.eqfNlqfLevelVerified);
        badgeClass.setBadgeClassType(dto.badgeClassType);
        badgeClass.setExpirationPeriod(dto.expirationPeriod);
        badgeClass.setArchived(dto.archived);
        // Set organization if provided
        if (dto.organizationId != null) {
            badgeClass.setOrganization(new com.taashee.badger.models.Organization());
            badgeClass.getOrganization().setId(dto.organizationId);
        }
        // Tags
        if (dto.tagNames != null) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : dto.tagNames) {
                Tag tag = tagRepository.findByName(tagName).orElseGet(() -> {
                    Tag t = new Tag();
                    t.setName(tagName);
                    return tagRepository.save(t);
                });
                tags.add(tag);
            }
            badgeClass.setTags(tags);
        }
        // Alignments
        if (dto.alignments != null) {
            if (badgeClass.getAlignments() != null) {
                // Remove all old alignments (orphan removal)
                badgeClass.getAlignments().forEach(a -> a.setBadgeClass(null));
                badgeClass.getAlignments().clear();
                // Add new alignments to the same list instance
                for (BadgeClassDTO.AlignmentDTO a : dto.alignments) {
                    Alignment alignment = new Alignment();
                    alignment.setBadgeClass(badgeClass);
                    alignment.setTargetName(a.targetName);
                    alignment.setTargetUrl(a.targetUrl);
                    alignment.setTargetDescription(a.targetDescription);
                    alignment.setTargetFramework(a.targetFramework);
                    alignment.setTargetCode(a.targetCode);
                    badgeClass.getAlignments().add(alignment);
                }
            }
        }
        // Institutions
        if (dto.institutionIds != null) {
            Set<Institution> institutions = dto.institutionIds.stream()
                .map(id -> institutionRepository.findById(id).orElseThrow(() -> new RuntimeException("Institution not found: " + id)))
                .collect(Collectors.toSet());
            badgeClass.setAwardAllowedInstitutions(institutions);
        }
        // Extensions
        if (dto.extensions != null) {
            try {
                badgeClass.setExtensions(objectMapper.writeValueAsString(dto.extensions));
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize extensions", e);
            }
        }
    }
} 