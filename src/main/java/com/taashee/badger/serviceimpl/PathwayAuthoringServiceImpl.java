package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.*;
import com.taashee.badger.repositories.*;
import com.taashee.badger.services.PathwayAuthoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PathwayAuthoringServiceImpl implements PathwayAuthoringService {

    @Autowired
    private PathwayRepository pathwayRepository;
    
    @Autowired
    private PathwayElementRepository pathwayElementRepository;
    
    @Autowired
    private PathwayElementBadgeRepository pathwayElementBadgeRepository;
    
    @Autowired
    private BadgeClassRepository badgeClassRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrganizationStaffRepository organizationStaffRepository;

    @Override
    public Map<String, Object> getPathwayStructure(Long pathwayId, String userEmail) {
        // Verify user has access to this pathway
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new RuntimeException("Pathway not found"));
        
        // Check if user has access to this pathway's organization
        if (!hasAccessToPathway(user, pathway)) {
            throw new RuntimeException("Access denied to pathway");
        }
        
        // Get all elements for this pathway
        List<PathwayElement> allElements = pathwayElementRepository.findByPathwayId(pathwayId);
        
        // Build hierarchical structure
        Map<Long, PathwayElement> elementMap = new HashMap<>();
        List<PathwayElement> rootElements = new ArrayList<>();
        
        // Create map of all elements
        for (PathwayElement element : allElements) {
            elementMap.put(element.getId(), element);
        }
        
        // Build hierarchy
        for (PathwayElement element : allElements) {
            if (element.getParentElement() == null) {
                rootElements.add(element);
            } else {
                PathwayElement parent = elementMap.get(element.getParentElement().getId());
                if (parent != null) {
                    if (parent.getChildElements() == null) {
                        parent.setChildElements(new ArrayList<>());
                    }
                    parent.getChildElements().add(element);
                }
            }
        }
        
        // Sort elements by order index
        sortElementsByOrder(rootElements);
        
        Map<String, Object> structure = new HashMap<>();
        structure.put("pathway", pathway);
        structure.put("elements", rootElements);
        structure.put("totalElements", allElements.size());
        structure.put("completionStatus", calculatePathwayCompletionStatus(pathway, allElements));
        
        return structure;
    }

    @Override
    public PathwayElement createElement(Long pathwayId, PathwayElement element, String userEmail) {
        // Verify access
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new RuntimeException("Pathway not found"));
        
        if (!hasAccessToPathway(user, pathway)) {
            throw new RuntimeException("Access denied to pathway");
        }
        
        // Set pathway and timestamps
        element.setPathway(pathway);
        element.setCreatedAt(LocalDateTime.now());
        element.setUpdatedAt(LocalDateTime.now());
        
        // Set default values if not provided
        if (element.getOrderIndex() == null) {
            element.setOrderIndex(getNextOrderIndex(pathwayId, element.getParentElement() != null ? element.getParentElement().getId() : null));
        }
        
        if (element.getCompletionRule() == null) {
            element.setCompletionRule(PathwayElement.CompletionRule.ALL);
        }
        
        if (element.getRequiredCount() == null) {
            element.setRequiredCount(1);
        }
        
        if (element.getIsOptional() == null) {
            element.setIsOptional(false);
        }
        
        if (element.getCountsTowardsParent() == null) {
            element.setCountsTowardsParent(true);
        }
        
        // Validate element
        validateElement(element);
        
        PathwayElement savedElement = pathwayElementRepository.save(element);
        
        // Update parent's child elements if needed
        if (element.getParentElement() != null) {
            PathwayElement parent = pathwayElementRepository.findById(element.getParentElement().getId()).orElse(null);
            if (parent != null) {
                if (parent.getChildElements() == null) {
                    parent.setChildElements(new ArrayList<>());
                }
                parent.getChildElements().add(savedElement);
                pathwayElementRepository.save(parent);
            }
        }
        
        return savedElement;
    }

    @Override
    public PathwayElement updateElement(Long pathwayId, Long elementId, PathwayElement element, String userEmail) {
        // Verify access
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new RuntimeException("Pathway not found"));
        
        if (!hasAccessToPathway(user, pathway)) {
            throw new RuntimeException("Access denied to pathway");
        }
        
        PathwayElement existingElement = pathwayElementRepository.findById(elementId)
                .orElseThrow(() -> new RuntimeException("Element not found"));
        
        // Verify element belongs to this pathway
        if (!existingElement.getPathway().getId().equals(pathwayId)) {
            throw new RuntimeException("Element does not belong to this pathway");
        }
        
        // Update fields
        existingElement.setName(element.getName());
        existingElement.setDescription(element.getDescription());
        existingElement.setShortCode(element.getShortCode());
        existingElement.setElementType(element.getElementType());
        existingElement.setOrderIndex(element.getOrderIndex());
        existingElement.setCompletionRule(element.getCompletionRule());
        existingElement.setRequiredCount(element.getRequiredCount());
        existingElement.setIsOptional(element.getIsOptional());
        existingElement.setCountsTowardsParent(element.getCountsTowardsParent());
        existingElement.setUpdatedAt(LocalDateTime.now());
        
        // Validate element
        validateElement(existingElement);
        
        return pathwayElementRepository.save(existingElement);
    }

    @Override
    public void deleteElement(Long pathwayId, Long elementId, String userEmail) {
        // Verify access
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new RuntimeException("Pathway not found"));
        
        if (!hasAccessToPathway(user, pathway)) {
            throw new RuntimeException("Access denied to pathway");
        }
        
        PathwayElement element = pathwayElementRepository.findById(elementId)
                .orElseThrow(() -> new RuntimeException("Element not found"));
        
        // Verify element belongs to this pathway
        if (!element.getPathway().getId().equals(pathwayId)) {
            throw new RuntimeException("Element does not belong to this pathway");
        }
        
        // Check if element has children
        if (element.getChildElements() != null && !element.getChildElements().isEmpty()) {
            throw new RuntimeException("Cannot delete element with children. Please delete children first.");
        }
        
        // Check if element has badges
        List<PathwayElementBadge> badges = pathwayElementBadgeRepository.findByElementId(elementId);
        if (!badges.isEmpty()) {
            throw new RuntimeException("Cannot delete element with badges. Please remove badges first.");
        }
        
        // Remove from parent's children list
        if (element.getParentElement() != null) {
            PathwayElement parent = pathwayElementRepository.findById(element.getParentElement().getId()).orElse(null);
            if (parent != null && parent.getChildElements() != null) {
                parent.getChildElements().removeIf(child -> child.getId().equals(elementId));
                pathwayElementRepository.save(parent);
            }
        }
        
        pathwayElementRepository.delete(element);
    }

    @Override
    public PathwayElement moveElement(Long pathwayId, Long elementId, Long newParentId, Integer newOrderIndex, String userEmail) {
        // Verify access
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new RuntimeException("Pathway not found"));
        
        if (!hasAccessToPathway(user, pathway)) {
            throw new RuntimeException("Access denied to pathway");
        }
        
        PathwayElement element = pathwayElementRepository.findById(elementId)
                .orElseThrow(() -> new RuntimeException("Element not found"));
        
        // Verify element belongs to this pathway
        if (!element.getPathway().getId().equals(pathwayId)) {
            throw new RuntimeException("Element does not belong to this pathway");
        }
        
        // Remove from current parent
        if (element.getParentElement() != null) {
            PathwayElement currentParent = pathwayElementRepository.findById(element.getParentElement().getId()).orElse(null);
            if (currentParent != null && currentParent.getChildElements() != null) {
                currentParent.getChildElements().removeIf(child -> child.getId().equals(elementId));
                pathwayElementRepository.save(currentParent);
            }
        }
        
        // Set new parent
        if (newParentId != null) {
            PathwayElement newParent = pathwayElementRepository.findById(newParentId)
                    .orElseThrow(() -> new RuntimeException("Parent element not found"));
            
            // Verify parent belongs to same pathway
            if (!newParent.getPathway().getId().equals(pathwayId)) {
                throw new RuntimeException("Parent element does not belong to this pathway");
            }
            
            // Check for circular reference
            if (wouldCreateCircularReference(element, newParent)) {
                throw new RuntimeException("Cannot move element: would create circular reference");
            }
            
            element.setParentElement(newParent);
            
            // Add to parent's children
            if (newParent.getChildElements() == null) {
                newParent.setChildElements(new ArrayList<>());
            }
            newParent.getChildElements().add(element);
            pathwayElementRepository.save(newParent);
        } else {
            element.setParentElement(null);
        }
        
        // Set new order index
        if (newOrderIndex != null) {
            element.setOrderIndex(newOrderIndex);
        } else {
            element.setOrderIndex(getNextOrderIndex(pathwayId, newParentId));
        }
        
        element.setUpdatedAt(LocalDateTime.now());
        
        return pathwayElementRepository.save(element);
    }

    @Override
    public List<PathwayElement> reorderElements(Long pathwayId, List<Map<String, Object>> reorderRequest, String userEmail) {
        // Verify access
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new RuntimeException("Pathway not found"));
        
        if (!hasAccessToPathway(user, pathway)) {
            throw new RuntimeException("Access denied to pathway");
        }
        
        List<PathwayElement> updatedElements = new ArrayList<>();
        
        for (Map<String, Object> request : reorderRequest) {
            Long elementId = Long.valueOf(request.get("elementId").toString());
            Integer newOrderIndex = Integer.valueOf(request.get("orderIndex").toString());
            
            PathwayElement element = pathwayElementRepository.findById(elementId)
                    .orElseThrow(() -> new RuntimeException("Element not found"));
            
            // Verify element belongs to this pathway
            if (!element.getPathway().getId().equals(pathwayId)) {
                throw new RuntimeException("Element does not belong to this pathway");
            }
            
            element.setOrderIndex(newOrderIndex);
            element.setUpdatedAt(LocalDateTime.now());
            
            updatedElements.add(pathwayElementRepository.save(element));
        }
        
        return updatedElements;
    }

    @Override
    public PathwayElementBadge addBadgeToElement(Long pathwayId, Long elementId, PathwayElementBadge badge, String userEmail) {
        // Verify access
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new RuntimeException("Pathway not found"));
        
        if (!hasAccessToPathway(user, pathway)) {
            throw new RuntimeException("Access denied to pathway");
        }
        
        PathwayElement element = pathwayElementRepository.findById(elementId)
                .orElseThrow(() -> new RuntimeException("Element not found"));
        
        // Verify element belongs to this pathway
        if (!element.getPathway().getId().equals(pathwayId)) {
            throw new RuntimeException("Element does not belong to this pathway");
        }
        
        // Set element and timestamps
        badge.setElement(element);
        badge.setCreatedAt(LocalDateTime.now());
        badge.setUpdatedAt(LocalDateTime.now());
        
        // Set default values
        if (badge.getIsRequired() == null) {
            badge.setIsRequired(true);
        }
        
        if (badge.getBadgeSource() == null) {
            badge.setBadgeSource(PathwayElementBadge.BadgeSource.BADGR);
        }
        
        // Validate badge
        validateBadge(badge);
        
        return pathwayElementBadgeRepository.save(badge);
    }

    @Override
    public void removeBadgeFromElement(Long pathwayId, Long elementId, Long badgeId, String userEmail) {
        // Verify access
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new RuntimeException("Pathway not found"));
        
        if (!hasAccessToPathway(user, pathway)) {
            throw new RuntimeException("Access denied to pathway");
        }
        
        PathwayElement element = pathwayElementRepository.findById(elementId)
                .orElseThrow(() -> new RuntimeException("Element not found"));
        
        // Verify element belongs to this pathway
        if (!element.getPathway().getId().equals(pathwayId)) {
            throw new RuntimeException("Element does not belong to this pathway");
        }
        
        PathwayElementBadge badge = pathwayElementBadgeRepository.findById(badgeId)
                .orElseThrow(() -> new RuntimeException("Badge not found"));
        
        // Verify badge belongs to this element
        if (!badge.getElement().getId().equals(elementId)) {
            throw new RuntimeException("Badge does not belong to this element");
        }
        
        pathwayElementBadgeRepository.delete(badge);
    }

    @Override
    public List<Map<String, Object>> searchBadges(String query, String source, String framework, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Map<String, Object>> results = new ArrayList<>();
        
        // Search internal badges
        if (source == null || "badgr".equalsIgnoreCase(source) || "internal".equalsIgnoreCase(source)) {
            List<BadgeClass> internalBadges = badgeClassRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
            
            for (BadgeClass badge : internalBadges) {
                Map<String, Object> badgeInfo = new HashMap<>();
                badgeInfo.put("id", badge.getId());
                badgeInfo.put("name", badge.getName());
                badgeInfo.put("description", badge.getDescription());
                badgeInfo.put("imageUrl", badge.getImage());
                badgeInfo.put("issuerName", badge.getOrganization().getName());
                badgeInfo.put("source", "badgr");
                badgeInfo.put("isInternal", true);
                badgeInfo.put("tags", badge.getTags());
                badgeInfo.put("criteria", badge.getCriteria());
                
                results.add(badgeInfo);
            }
        }
        
        // Search external badges (simulated BadgeRank integration)
        if (source == null || "external".equalsIgnoreCase(source) || "badgerank".equalsIgnoreCase(source)) {
            // This would integrate with BadgeRank API in real implementation
            List<Map<String, Object>> externalBadges = searchExternalBadges(query, framework);
            results.addAll(externalBadges);
        }
        
        return results;
    }

    @Override
    public List<BadgeClass> getInternalBadges(Long organizationId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (organizationId != null) {
            return badgeClassRepository.findByOrganizationId(organizationId);
        } else {
            // Get badges from all organizations user has access to
            List<OrganizationStaff> staffRoles = organizationStaffRepository.findByUserEmail(userEmail);
            List<Long> organizationIds = staffRoles.stream()
                    .map(staff -> staff.getOrganization().getId())
                    .collect(Collectors.toList());
            
            if (organizationIds.isEmpty()) {
                return new ArrayList<>();
            }
            
            return badgeClassRepository.findByOrganizationIdIn(organizationIds);
        }
    }

    @Override
    public PathwayElementBadge addExternalBadge(Long pathwayId, Long elementId, Map<String, Object> externalBadgeData, String userEmail) {
        // Verify access
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new RuntimeException("Pathway not found"));
        
        if (!hasAccessToPathway(user, pathway)) {
            throw new RuntimeException("Access denied to pathway");
        }
        
        PathwayElement element = pathwayElementRepository.findById(elementId)
                .orElseThrow(() -> new RuntimeException("Element not found"));
        
        // Verify element belongs to this pathway
        if (!element.getPathway().getId().equals(pathwayId)) {
            throw new RuntimeException("Element does not belong to this pathway");
        }
        
        // Create external badge
        PathwayElementBadge externalBadge = new PathwayElementBadge();
        externalBadge.setElement(element);
        externalBadge.setBadgeSource(PathwayElementBadge.BadgeSource.EXTERNAL);
        externalBadge.setExternalBadgeId((String) externalBadgeData.get("externalBadgeId"));
        externalBadge.setExternalIssuerName((String) externalBadgeData.get("issuerName"));
        externalBadge.setExternalIssuerUrl((String) externalBadgeData.get("issuerUrl"));
        externalBadge.setBadgeName((String) externalBadgeData.get("name"));
        externalBadge.setBadgeDescription((String) externalBadgeData.get("description"));
        externalBadge.setBadgeImageUrl((String) externalBadgeData.get("imageUrl"));
        externalBadge.setIsRequired((Boolean) externalBadgeData.getOrDefault("isRequired", true));
        externalBadge.setIsVerified(false);
        externalBadge.setCreatedAt(LocalDateTime.now());
        externalBadge.setUpdatedAt(LocalDateTime.now());
        
        return pathwayElementBadgeRepository.save(externalBadge);
    }

    @Override
    public PathwayElementBadge verifyExternalBadge(Long badgeId, String notes, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        PathwayElementBadge badge = pathwayElementBadgeRepository.findById(badgeId)
                .orElseThrow(() -> new RuntimeException("Badge not found"));
        
        // Verify user has access to the pathway
        if (!hasAccessToPathway(user, badge.getElement().getPathway())) {
            throw new RuntimeException("Access denied to pathway");
        }
        
        badge.setIsVerified(true);
        badge.setVerifiedAt(LocalDateTime.now());
        badge.setVerifiedBy(user);
        badge.setVerificationNotes(notes);
        badge.setUpdatedAt(LocalDateTime.now());
        
        return pathwayElementBadgeRepository.save(badge);
    }

    @Override
    public PathwayElement addCompetencyAlignment(Long pathwayId, Long elementId, PathwayElement.CompetencyAlignment competency, String userEmail) {
        // Verify access
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new RuntimeException("Pathway not found"));
        
        if (!hasAccessToPathway(user, pathway)) {
            throw new RuntimeException("Access denied to pathway");
        }
        
        PathwayElement element = pathwayElementRepository.findById(elementId)
                .orElseThrow(() -> new RuntimeException("Element not found"));
        
        // Verify element belongs to this pathway
        if (!element.getPathway().getId().equals(pathwayId)) {
            throw new RuntimeException("Element does not belong to this pathway");
        }
        
        // Add competency alignment
        if (element.getCompetencyAlignments() == null) {
            element.setCompetencyAlignments(new ArrayList<>());
        }
        element.getCompetencyAlignments().add(competency);
        element.setUpdatedAt(LocalDateTime.now());
        
        return pathwayElementRepository.save(element);
    }

    @Override
    public List<Map<String, Object>> getCompetencyFrameworks() {
        List<Map<String, Object>> frameworks = new ArrayList<>();
        
        // CASE Framework
        Map<String, Object> caseFramework = new HashMap<>();
        caseFramework.put("id", "case");
        caseFramework.put("name", "CASE (Credential Engine)");
        caseFramework.put("description", "Comprehensive framework for credential transparency");
        caseFramework.put("url", "https://credentialengine.org/");
        caseFramework.put("version", "1.0");
        frameworks.add(caseFramework);
        
        // Common Core
        Map<String, Object> commonCore = new HashMap<>();
        commonCore.put("id", "common-core");
        commonCore.put("name", "Common Core State Standards");
        commonCore.put("description", "K-12 education standards");
        commonCore.put("url", "https://www.corestandards.org/");
        commonCore.put("version", "1.0");
        frameworks.add(commonCore);
        
        // ISTE Standards
        Map<String, Object> iste = new HashMap<>();
        iste.put("id", "iste");
        iste.put("name", "ISTE Standards");
        iste.put("description", "Technology standards for students and educators");
        iste.put("url", "https://www.iste.org/standards");
        iste.put("version", "1.0");
        frameworks.add(iste);
        
        return frameworks;
    }

    @Override
    public Map<String, Object> validatePathway(Long pathwayId, String userEmail) {
        // Verify access
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new RuntimeException("Pathway not found"));
        
        if (!hasAccessToPathway(user, pathway)) {
            throw new RuntimeException("Access denied to pathway");
        }
        
        List<PathwayElement> allElements = pathwayElementRepository.findByPathwayId(pathwayId);
        
        Map<String, Object> validation = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        boolean isValid = true;
        
        // Check for elements without completion criteria
        for (PathwayElement element : allElements) {
            if (!element.getIsOptional() && !hasCompletionCriteria(element)) {
                errors.add("Element '" + element.getName() + "' has no completion criteria");
                isValid = false;
            }
        }
        
        // Check for circular references
        if (hasCircularReferences(allElements)) {
            errors.add("Pathway contains circular references");
            isValid = false;
        }
        
        // Check for orphaned elements
        List<PathwayElement> orphanedElements = findOrphanedElements(allElements);
        if (!orphanedElements.isEmpty()) {
            warnings.add("Found " + orphanedElements.size() + " orphaned elements");
        }
        
        // Check completion badge
        if (pathway.getCompletionBadge() == null) {
            warnings.add("No completion badge specified");
        }
        
        validation.put("isValid", isValid);
        validation.put("errors", errors);
        validation.put("warnings", warnings);
        validation.put("totalElements", allElements.size());
        validation.put("completableElements", countCompletableElements(allElements));
        
        return validation;
    }

    @Override
    public Pathway publishPathway(Long pathwayId, String userEmail) {
        // Verify access
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new RuntimeException("Pathway not found"));
        
        if (!hasAccessToPathway(user, pathway)) {
            throw new RuntimeException("Access denied to pathway");
        }
        
        // Validate pathway before publishing
        Map<String, Object> validation = validatePathway(pathwayId, userEmail);
        if (!(Boolean) validation.get("isValid")) {
            throw new RuntimeException("Cannot publish pathway with errors: " + validation.get("errors"));
        }
        
        pathway.setStatus(Pathway.PathwayStatus.PUBLISHED);
        pathway.setPublishedAt(LocalDateTime.now());
        pathway.setPublishedBy(user);
        pathway.setUpdatedAt(LocalDateTime.now());
        
        return pathwayRepository.save(pathway);
    }

    @Override
    public Pathway unpublishPathway(Long pathwayId, String userEmail) {
        // Verify access
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new RuntimeException("Pathway not found"));
        
        if (!hasAccessToPathway(user, pathway)) {
            throw new RuntimeException("Access denied to pathway");
        }
        
        pathway.setStatus(Pathway.PathwayStatus.DRAFT);
        pathway.setPublishedAt(null);
        pathway.setPublishedBy(null);
        pathway.setUpdatedAt(LocalDateTime.now());
        
        return pathwayRepository.save(pathway);
    }

    // Helper methods
    private boolean hasAccessToPathway(User user, Pathway pathway) {
        // Check if user is admin
        if (user.getRole() == User.Role.ADMIN) {
            return true;
        }
        
        // Check if user is staff of the pathway's organization
        return organizationStaffRepository.findByUserEmailAndOrganizationId(user.getEmail(), pathway.getOrganization().getId()).isPresent();
    }
    
    private void validateElement(PathwayElement element) {
        if (element.getName() == null || element.getName().trim().isEmpty()) {
            throw new RuntimeException("Element name is required");
        }
        
        if (element.getCompletionRule() != null && element.getRequiredCount() != null) {
            if (element.getCompletionRule() == PathwayElement.CompletionRule.SOME && element.getRequiredCount() <= 0) {
                throw new RuntimeException("Required count must be greater than 0 for SOME completion rule");
            }
        }
    }
    
    private void validateBadge(PathwayElementBadge badge) {
        if (badge.getBadgeSource() == PathwayElementBadge.BadgeSource.BADGR) {
            if (badge.getBadgeClass() == null) {
                throw new RuntimeException("Badge class is required for internal badges");
            }
        } else if (badge.getBadgeSource() == PathwayElementBadge.BadgeSource.EXTERNAL) {
            if (badge.getBadgeName() == null || badge.getBadgeName().trim().isEmpty()) {
                throw new RuntimeException("Badge name is required for external badges");
            }
        }
    }
    
    private Integer getNextOrderIndex(Long pathwayId, Long parentElementId) {
        List<PathwayElement> siblings = pathwayElementRepository.findByPathwayIdAndParentElementId(pathwayId, parentElementId);
        if (siblings.isEmpty()) {
            return 1;
        }
        return siblings.stream().mapToInt(PathwayElement::getOrderIndex).max().orElse(0) + 1;
    }
    
    private void sortElementsByOrder(List<PathwayElement> elements) {
        elements.sort(Comparator.comparing(PathwayElement::getOrderIndex, Comparator.nullsLast(Comparator.naturalOrder())));
        for (PathwayElement element : elements) {
            if (element.getChildElements() != null) {
                sortElementsByOrder(element.getChildElements());
            }
        }
    }
    
    private boolean wouldCreateCircularReference(PathwayElement element, PathwayElement newParent) {
        if (element.getId().equals(newParent.getId())) {
            return true;
        }
        
        PathwayElement current = newParent;
        while (current.getParentElement() != null) {
            if (current.getParentElement().getId().equals(element.getId())) {
                return true;
            }
            current = current.getParentElement();
        }
        
        return false;
    }
    
    private boolean hasCompletionCriteria(PathwayElement element) {
        // Check if element has badges
        List<PathwayElementBadge> badges = pathwayElementBadgeRepository.findByElementId(element.getId());
        if (!badges.isEmpty()) {
            return true;
        }
        
        // Check if element has children
        if (element.getChildElements() != null && !element.getChildElements().isEmpty()) {
            return true;
        }
        
        return false;
    }
    
    private boolean hasCircularReferences(List<PathwayElement> elements) {
        Set<Long> visited = new HashSet<>();
        Set<Long> recursionStack = new HashSet<>();
        
        for (PathwayElement element : elements) {
            if (isCyclicUtil(element, visited, recursionStack)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isCyclicUtil(PathwayElement element, Set<Long> visited, Set<Long> recursionStack) {
        if (recursionStack.contains(element.getId())) {
            return true;
        }
        
        if (visited.contains(element.getId())) {
            return false;
        }
        
        visited.add(element.getId());
        recursionStack.add(element.getId());
        
        if (element.getChildElements() != null) {
            for (PathwayElement child : element.getChildElements()) {
                if (isCyclicUtil(child, visited, recursionStack)) {
                    return true;
                }
            }
        }
        
        recursionStack.remove(element.getId());
        return false;
    }
    
    private List<PathwayElement> findOrphanedElements(List<PathwayElement> allElements) {
        Set<Long> allIds = allElements.stream().map(PathwayElement::getId).collect(Collectors.toSet());
        Set<Long> childIds = allElements.stream()
                .filter(e -> e.getParentElement() != null)
                .map(e -> e.getParentElement().getId())
                .collect(Collectors.toSet());
        
        return allElements.stream()
                .filter(e -> e.getParentElement() != null && !allIds.contains(e.getParentElement().getId()))
                .collect(Collectors.toList());
    }
    
    private int countCompletableElements(List<PathwayElement> elements) {
        int count = 0;
        for (PathwayElement element : elements) {
            if (hasCompletionCriteria(element)) {
                count++;
            }
        }
        return count;
    }
    
    private Map<String, Object> calculatePathwayCompletionStatus(Pathway pathway, List<PathwayElement> elements) {
        Map<String, Object> status = new HashMap<>();
        int totalElements = elements.size();
        int completableElements = countCompletableElements(elements);
        
        status.put("totalElements", totalElements);
        status.put("completableElements", completableElements);
        status.put("completionPercentage", totalElements > 0 ? (double) completableElements / totalElements * 100 : 0);
        
        return status;
    }
    
    private List<Map<String, Object>> searchExternalBadges(String query, String framework) {
        // Simulated external badge search (BadgeRank integration)
        List<Map<String, Object>> externalBadges = new ArrayList<>();
        
        // Simulate some external badges
        if (query.toLowerCase().contains("math")) {
            Map<String, Object> badge1 = new HashMap<>();
            badge1.put("id", "ext_math_1");
            badge1.put("name", "Mathematics Fundamentals");
            badge1.put("description", "Core mathematics skills and concepts");
            badge1.put("imageUrl", "https://example.com/math-badge.png");
            badge1.put("issuerName", "Canvas Learning");
            badge1.put("source", "canvas");
            badge1.put("isInternal", false);
            badge1.put("externalUrl", "https://canvas.instructure.com/badges/math-fundamentals");
            externalBadges.add(badge1);
            
            Map<String, Object> badge2 = new HashMap<>();
            badge2.put("id", "ext_math_2");
            badge2.put("name", "Advanced Mathematics");
            badge2.put("description", "Advanced mathematical concepts and problem solving");
            badge2.put("imageUrl", "https://example.com/advanced-math.png");
            badge2.put("issuerName", "Acclaim");
            badge2.put("source", "acclaim");
            badge2.put("isInternal", false);
            badge2.put("externalUrl", "https://youracclaim.com/badges/advanced-math");
            externalBadges.add(badge2);
        }
        
        if (query.toLowerCase().contains("communication")) {
            Map<String, Object> badge3 = new HashMap<>();
            badge3.put("id", "ext_comm_1");
            badge3.put("name", "Effective Communication");
            badge3.put("description", "Professional communication skills");
            badge3.put("imageUrl", "https://example.com/communication.png");
            badge3.put("issuerName", "Digital Promise");
            badge3.put("source", "digitalpromise");
            badge3.put("isInternal", false);
            badge3.put("externalUrl", "https://digitalpromise.org/badges/communication");
            externalBadges.add(badge3);
        }
        
        return externalBadges;
    }

    // Additional methods for comprehensive pathway authoring
    @Override
    public List<PathwayElement> getElementPrerequisites(Long elementId, String userEmail) {
        // Implementation for getting element prerequisites
        return new ArrayList<>();
    }

    @Override
    public void addElementPrerequisite(Long elementId, Long prerequisiteElementId, String userEmail) {
        // Implementation for adding element prerequisites
    }

    @Override
    public void removeElementPrerequisite(Long elementId, Long prerequisiteElementId, String userEmail) {
        // Implementation for removing element prerequisites
    }

    @Override
    public void updateCompletionRule(Long elementId, PathwayElement.CompletionRule rule, Integer requiredCount, String userEmail) {
        // Implementation for updating completion rules
    }

    @Override
    public void setElementOptional(Long elementId, boolean isOptional, String userEmail) {
        // Implementation for setting element optional status
    }

    @Override
    public void setElementCountsTowardsParent(Long elementId, boolean countsTowardsParent, String userEmail) {
        // Implementation for setting element counts towards parent
    }

    @Override
    public void setBadgeRequired(Long badgeId, boolean isRequired, String userEmail) {
        // Implementation for setting badge required status
    }

    @Override
    public List<PathwayElementBadge> getElementBadges(Long elementId, String userEmail) {
        // Implementation for getting element badges
        return pathwayElementBadgeRepository.findByElementId(elementId);
    }

    @Override
    public Map<String, Object> getPathwayAuthoringAnalytics(Long pathwayId, String userEmail) {
        // Implementation for pathway authoring analytics
        return new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> getElementCompletionStats(Long elementId, String userEmail) {
        // Implementation for element completion statistics
        return new ArrayList<>();
    }

    @Override
    public void createPathwayVersion(Long pathwayId, String versionName, String userEmail) {
        // Implementation for creating pathway versions
    }

    @Override
    public List<Map<String, Object>> getPathwayVersions(Long pathwayId, String userEmail) {
        // Implementation for getting pathway versions
        return new ArrayList<>();
    }

    @Override
    public Pathway revertToVersion(Long pathwayId, String versionId, String userEmail) {
        // Implementation for reverting to pathway version
        return null;
    }

    @Override
    public void sharePathway(Long pathwayId, String collaboratorEmail, String permission, String userEmail) {
        // Implementation for sharing pathway
    }

    @Override
    public List<Map<String, Object>> getPathwayCollaborators(Long pathwayId, String userEmail) {
        // Implementation for getting pathway collaborators
        return new ArrayList<>();
    }

    @Override
    public void removePathwayCollaborator(Long pathwayId, String collaboratorEmail, String userEmail) {
        // Implementation for removing pathway collaborator
    }

    @Override
    public Map<String, Object> validateElementCompleteness(Long elementId, String userEmail) {
        // Implementation for validating element completeness
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> validatePathwayCompleteness(Long pathwayId, String userEmail) {
        // Implementation for validating pathway completeness
        return new HashMap<>();
    }

    @Override
    public List<String> getPathwayWarnings(Long pathwayId, String userEmail) {
        // Implementation for getting pathway warnings
        return new ArrayList<>();
    }

    @Override
    public List<String> getPathwayErrors(Long pathwayId, String userEmail) {
        // Implementation for getting pathway errors
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getCompatibleBadges(Long elementId, String userEmail) {
        // Implementation for getting compatible badges
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> checkBadgeCompatibility(Long elementId, Long badgeId, String userEmail) {
        // Implementation for checking badge compatibility
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> optimizePathwayStructure(Long pathwayId, String userEmail) {
        // Implementation for optimizing pathway structure
        return new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> suggestPathwayImprovements(Long pathwayId, String userEmail) {
        // Implementation for suggesting pathway improvements
        return new ArrayList<>();
    }

    @Override
    public void bulkUpdateElements(Long pathwayId, List<Map<String, Object>> updates, String userEmail) {
        // Implementation for bulk updating elements
    }

    @Override
    public void bulkAddBadges(Long pathwayId, Long elementId, List<Map<String, Object>> badges, String userEmail) {
        // Implementation for bulk adding badges
    }

    @Override
    public void bulkRemoveBadges(Long pathwayId, Long elementId, List<Long> badgeIds, String userEmail) {
        // Implementation for bulk removing badges
    }

    @Override
    public List<Pathway> getPathwayTemplates(String userEmail) {
        // Implementation for getting pathway templates
        return new ArrayList<>();
    }

    @Override
    public Pathway saveAsTemplate(Long pathwayId, String templateName, String templateCategory, String userEmail) {
        // Implementation for saving pathway as template
        return null;
    }

    @Override
    public Map<String, Object> exportPathway(Long pathwayId, String userEmail) {
        // Implementation for exporting pathway
        return new HashMap<>();
    }

    @Override
    public Pathway importPathway(Map<String, Object> pathwayData, Long organizationId, String userEmail) {
        // Implementation for importing pathway
        return null;
    }
} 