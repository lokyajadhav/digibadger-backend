package com.taashee.badger.services;

import com.taashee.badger.models.Pathway;
import com.taashee.badger.models.PathwayDTO;
import java.util.List;
import java.util.Optional;

public interface PathwayService {
    
    // Create a new pathway for an organization
    PathwayDTO createPathway(PathwayDTO pathwayDTO, Long organizationId);
    
    // Get all pathways for an organization
    List<PathwayDTO> getPathwaysByOrganization(Long organizationId);
    
    // Get a specific pathway by ID (with organization validation)
    Optional<PathwayDTO> getPathwayById(Long pathwayId, Long organizationId);
    
    // Update a pathway
    PathwayDTO updatePathway(Long pathwayId, PathwayDTO pathwayDTO, Long organizationId);
    
    // Delete a pathway
    void deletePathway(Long pathwayId, Long organizationId);
    
    // Get available pathways for a user (based on their organization and subscriptions)
    List<PathwayDTO> getAvailablePathwaysForUser(Long userId);
    
    // Enroll a user in a pathway
    void enrollUserInPathway(Long pathwayId, Long userId);
    
    // Get pathway progress for a user
    PathwayDTO getPathwayProgress(Long pathwayId, Long userId);
    
    // Check if pathway exists and belongs to organization
    boolean pathwayExistsAndBelongsToOrganization(Long pathwayId, Long organizationId);
} 