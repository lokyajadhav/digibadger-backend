package com.taashee.badger.services;

import com.taashee.badger.models.PathwayElementBadgeDTO;
import java.util.List;
import java.util.Optional;

public interface PathwayElementBadgeService {
    
    /**
     * Add a badge to a pathway element
     */
    PathwayElementBadgeDTO addBadgeToElement(Long elementId, Long badgeClassId, Long organizationId, PathwayElementBadgeDTO badgeDTO);
    
    /**
     * Remove a badge from a pathway element
     */
    void removeBadgeFromElement(Long elementId, Long badgeClassId, Long organizationId);
    
    /**
     * Get all badges for a pathway element
     */
    List<PathwayElementBadgeDTO> getBadgesForElement(Long elementId, Long organizationId);
    
    /**
     * Get required badges for a pathway element
     */
    List<PathwayElementBadgeDTO> getRequiredBadgesForElement(Long elementId, Long organizationId);
    
    /**
     * Get a specific badge-element relationship
     */
    Optional<PathwayElementBadgeDTO> getBadgeElementRelationship(Long elementId, Long badgeClassId, Long organizationId);
    
    /**
     * Update a badge-element relationship
     */
    PathwayElementBadgeDTO updateBadgeElementRelationship(Long elementId, Long badgeClassId, Long organizationId, PathwayElementBadgeDTO badgeDTO);
    
    /**
     * Add an external badge to a pathway element
     */
    PathwayElementBadgeDTO addExternalBadgeToElement(Long elementId, Long organizationId, PathwayElementBadgeDTO externalBadgeDTO);
    
    /**
     * Check if a badge is associated with an element
     */
    boolean isBadgeAssociatedWithElement(Long elementId, Long badgeClassId, Long organizationId);
    
    /**
     * Get all badges for a pathway
     */
    List<PathwayElementBadgeDTO> getBadgesForPathway(Long pathwayId, Long organizationId);
    
    /**
     * Count required badges for an element
     */
    long countRequiredBadgesForElement(Long elementId, Long organizationId);
    
    /**
     * Validate a badge-element relationship
     */
    boolean validateBadgeElementRelationship(Long elementId, Long badgeClassId, Long organizationId);
} 