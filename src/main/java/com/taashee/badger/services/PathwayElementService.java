package com.taashee.badger.services;

import com.taashee.badger.models.PathwayElementDTO;
import java.util.List;
import java.util.Optional;

public interface PathwayElementService {
    
    // Create a new pathway element
    PathwayElementDTO createPathwayElement(PathwayElementDTO elementDTO, Long pathwayId, Long organizationId);
    
    // Create a new pathway element for current user's organization
    PathwayElementDTO createPathwayElementForCurrentUser(PathwayElementDTO elementDTO, Long pathwayId, String userEmail);
    
    // Get all elements for a pathway
    List<PathwayElementDTO> getPathwayElements(Long pathwayId, Long organizationId);
    
    // Get all elements for a pathway for current user's organization
    List<PathwayElementDTO> getPathwayElementsForCurrentUser(Long pathwayId, String userEmail);
    
    // Get a specific pathway element by ID
    Optional<PathwayElementDTO> getPathwayElementById(Long elementId, Long pathwayId, Long organizationId);
    
    // Get a specific pathway element by ID for current user's organization
    Optional<PathwayElementDTO> getPathwayElementByIdForCurrentUser(Long elementId, Long pathwayId, String userEmail);
    
    // Update a pathway element
    PathwayElementDTO updatePathwayElement(Long elementId, PathwayElementDTO elementDTO, Long pathwayId, Long organizationId);
    
    // Update a pathway element for current user's organization
    PathwayElementDTO updatePathwayElementForCurrentUser(Long elementId, PathwayElementDTO elementDTO, Long pathwayId, String userEmail);
    
    // Delete a pathway element
    void deletePathwayElement(Long elementId, Long pathwayId, Long organizationId);
    
    // Delete a pathway element for current user's organization
    void deletePathwayElementForCurrentUser(Long elementId, Long pathwayId, String userEmail);
    
    // Add badge requirement to pathway element
    void addBadgeRequirement(Long elementId, Long badgeClassId, Long pathwayId, Long organizationId);
    
    // Remove badge requirement from pathway element
    void removeBadgeRequirement(Long elementId, Long badgeClassId, Long pathwayId, Long organizationId);
    
    // Get next order index for pathway element
    Integer getNextOrderIndex(Long pathwayId);
    
    // Check if pathway element exists and belongs to organization
    boolean pathwayElementExistsAndBelongsToOrganization(Long elementId, Long pathwayId, Long organizationId);
} 