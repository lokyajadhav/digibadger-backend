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
import com.taashee.badger.exceptions.ApiException;
import java.util.Map;

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
        List<BadgeInstance> badgeInstances = badgeInstanceRepository.findAllById(ids);
        for (BadgeInstance bi : badgeInstances) {
            bi.setRevoked(true);
            bi.setRevocationReason(revocationReason);
        }
        return badgeInstanceRepository.saveAll(badgeInstances);
    }

    @Override
    public BadgeInstance revokeBadgeInstance(Long id, String revocationReason) {
        BadgeInstance badgeInstance = badgeInstanceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Badge instance not found"));
        
        // Check if user has permission to revoke this badge instance
        if (!hasPermissionToModifyBadgeInstance(badgeInstance)) {
            throw new IllegalStateException("You don't have permission to modify this badge instance");
        }
        
        // Check if badge instance is already revoked
        if (badgeInstance.isRevoked()) {
            throw new IllegalStateException("Badge instance is already revoked");
        }
        
        // Revoke the badge instance
        badgeInstance.setRevoked(true);
        badgeInstance.setRevocationReason(revocationReason);
        
        return badgeInstanceRepository.save(badgeInstance);
    }

    @Override
    public Map<String, Object> getUserBadges(int page, int size, String search) {
        // Get current user
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get all badge instances for this user
        List<BadgeInstance> allInstances = badgeInstanceRepository.findByRecipientId(user.getId());
        
        // Apply search filter
        List<BadgeInstance> filteredInstances = allInstances.stream()
            .filter(instance -> {
                if (search != null && !search.trim().isEmpty()) {
                    String searchLower = search.toLowerCase();
                    String badgeName = instance.getBadgeClass() != null ? 
                        instance.getBadgeClass().getName().toLowerCase() : "";
                    String orgName = instance.getOrganization() != null ? 
                        instance.getOrganization().getNameEnglish().toLowerCase() : "";
                    return badgeName.contains(searchLower) || orgName.contains(searchLower);
                }
                return true;
            })
            .collect(java.util.stream.Collectors.toList());
        
        // Apply pagination
        int total = filteredInstances.size();
        int totalPages = (int) Math.ceil((double) total / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, total);
        
        List<BadgeInstance> paginatedInstances = filteredInstances.subList(startIndex, endIndex);
        
        // Convert to DTOs
        List<Map<String, Object>> badges = paginatedInstances.stream()
            .map(instance -> {
                Map<String, Object> badgeData = new java.util.HashMap<>();
                badgeData.put("id", instance.getId());
                badgeData.put("badgeClassName", instance.getBadgeClass() != null ? 
                    instance.getBadgeClass().getName() : "Unknown Badge");
                badgeData.put("description", instance.getBadgeClass() != null ? 
                    instance.getBadgeClass().getDescription() : null);
                badgeData.put("image", instance.getBadgeClass() != null ? 
                    instance.getBadgeClass().getImage() : null);
                badgeData.put("organizationName", instance.getOrganization() != null ? 
                    instance.getOrganization().getNameEnglish() : "Unknown Organization");
                badgeData.put("issuedOn", instance.getIssuedOn());
                badgeData.put("status", instance.getCurrentStatus());
                badgeData.put("revoked", instance.isRevoked());
                badgeData.put("expiresAt", instance.getExpiresAt());
                badgeData.put("narrative", instance.getNarrative());
                badgeData.put("evidenceItems", instance.getEvidenceItems() != null ? 
                    instance.getEvidenceItems().stream().map(e -> {
                        Map<String, Object> evidence = new java.util.HashMap<>();
                        evidence.put("narrative", e.getNarrative());
                        evidence.put("url", e.getEvidenceUrl());
                        evidence.put("name", e.getName());
                        evidence.put("description", e.getDescription());
                        return evidence;
                    }).toList() : null);
                return badgeData;
            })
            .collect(java.util.stream.Collectors.toList());
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("content", badges);
        result.put("totalElements", total);
        result.put("totalPages", totalPages);
        result.put("currentPage", page);
        result.put("size", size);
        result.put("first", page == 0);
        result.put("last", page >= totalPages - 1);
        
        return result;
    }

    /**
     * Check if the current user has permission to modify the badge instance
     * Only badge class owners (ISSUER with OWNER permission) can modify
     */
    private boolean hasPermissionToModifyBadgeInstance(BadgeInstance badgeInstance) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
            .map(a -> a.getAuthority())
            .toList();
        
        // Only ISSUER role can modify badge instances
        if (!roles.contains("ROLE_ISSUER")) {
            return false;
        }
        
        // Check if user belongs to the badge instance organization and has OWNER permission
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || badgeInstance.getOrganization() == null) {
            return false;
        }
        
        Optional<com.taashee.badger.models.OrganizationStaff> staffOptional = organizationStaffRepository
            .findByOrganizationIdAndUserId(badgeInstance.getOrganization().getId(), user.getId());
        
        if (!staffOptional.isPresent()) {
            return false;
        }
        
        com.taashee.badger.models.OrganizationStaff staff = staffOptional.get();
        String staffRole = staff.getStaffRole();
        
        // Check if staff role indicates owner-like permissions
        return staffRole != null && (
            staffRole.equalsIgnoreCase("OWNER") || 
            staffRole.equalsIgnoreCase("owner") ||
            staffRole.equalsIgnoreCase("ADMIN") ||
            staffRole.equalsIgnoreCase("admin")
        );
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
                throw new ApiException("You must add a logo/image to this badge before it can be issued.", 400);
            }
            badgeInstance.setBadgeClass(badgeClass);
        }
        if (dto.organizationId != null) {
            Organization organization = organizationRepository.findById(dto.organizationId).orElseThrow(() -> new RuntimeException("Organization not found: " + dto.organizationId));
            badgeInstance.setOrganization(organization);
        }
        
        // Handle recipient by email if recipientId is not provided
        if (dto.recipientId != null) {
            User recipient = userRepository.findById(dto.recipientId).orElseThrow(() -> new RuntimeException("User not found: " + dto.recipientId));
            badgeInstance.setRecipient(recipient);
        } else if (dto.recipientEmail != null && !dto.recipientEmail.trim().isEmpty()) {
            User recipient = userRepository.findByEmail(dto.recipientEmail).orElse(null);
            if (recipient != null) {
                badgeInstance.setRecipient(recipient);
            }
        }
        
        // Handle issue date from ISO string
        if (dto.issueDate != null && !dto.issueDate.trim().isEmpty()) {
            try {
                badgeInstance.setIssuedOn(java.time.LocalDateTime.parse(dto.issueDate.replace("Z", "")));
            } catch (Exception e) {
                throw new RuntimeException("Invalid issue date format: " + dto.issueDate);
            }
        } else {
            badgeInstance.setIssuedOn(dto.issuedOn);
        }
        
        badgeInstance.setPublicKeyOrganization(dto.publicKeyOrganization);
        badgeInstance.setIdentifier(dto.identifier);
        badgeInstance.setRecipientType(dto.recipientType);
        badgeInstance.setAwardType(dto.awardType);
        badgeInstance.setDirectAwardBundle(dto.directAwardBundle);
        badgeInstance.setRecipientIdentifier(dto.recipientIdentifier);
        badgeInstance.setImage(dto.image);
        badgeInstance.setRevoked(dto.revoked);
        badgeInstance.setRevocationReason(dto.revocationReason);
        
        // Handle expiration date from ISO string
        if (dto.expiresAt != null && !dto.expiresAt.trim().isEmpty()) {
            try {
                badgeInstance.setExpiresAt(java.time.LocalDateTime.parse(dto.expiresAt.replace("Z", "")));
            } catch (Exception e) {
                throw new RuntimeException("Invalid expiration date format: " + dto.expiresAt);
            }
        } else {
            badgeInstance.setExpiresAt(dto.expirationDateTime);
        }
        
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
        if (dto.evidenceItems != null && !dto.evidenceItems.isEmpty()) {
            ArrayList<Evidence> evidenceList = new ArrayList<>();
            for (BadgeInstanceDTO.EvidenceDTO e : dto.evidenceItems) {
                // Only add evidence items that have content
                if ((e.narrative != null && !e.narrative.trim().isEmpty()) || 
                    (e.url != null && !e.url.trim().isEmpty())) {
                    Evidence evidence = new Evidence();
                    evidence.setBadgeInstance(badgeInstance);
                    evidence.setEvidenceUrl(e.url); // Use url field from frontend
                    evidence.setNarrative(e.narrative);
                    evidence.setName(e.name);
                    evidence.setDescription(e.description);
                    evidenceList.add(evidence);
                }
            }
            badgeInstance.setEvidenceItems(evidenceList);
        }
        
        // Validation logic for narrative/evidence
        BadgeClass badgeClass = badgeInstance.getBadgeClass();
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