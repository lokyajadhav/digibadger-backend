package com.taashee.badger.services;

import com.taashee.badger.models.Pathway;
import com.taashee.badger.models.PathwayDTO;
import java.util.List;
import java.util.Optional;

public interface PathwayService {
    
    // Create a new pathway for an organization
    PathwayDTO createPathway(PathwayDTO pathwayDTO, Long organizationId);
    
    // Create a new pathway for the current user's organization
    PathwayDTO createPathwayForCurrentUser(PathwayDTO pathwayDTO, String userEmail);
    
    // Get all pathways for an organization
    List<PathwayDTO> getPathwaysByOrganization(Long organizationId);
    
    // Get all pathways for the current user's organization
    List<PathwayDTO> getPathwaysForCurrentUser(String userEmail);
    
    // Get a specific pathway by ID (with organization validation)
    Optional<PathwayDTO> getPathwayById(Long pathwayId, Long organizationId);
    
    // Get a specific pathway by ID for the current user's organization
    Optional<PathwayDTO> getPathwayByIdForCurrentUser(Long pathwayId, String userEmail);
    
    // Update a pathway
    PathwayDTO updatePathway(Long pathwayId, PathwayDTO pathwayDTO, Long organizationId);
    
    // Update a pathway for the current user's organization
    PathwayDTO updatePathwayForCurrentUser(Long pathwayId, PathwayDTO pathwayDTO, String userEmail);
    
    // Delete a pathway
    void deletePathway(Long pathwayId, Long organizationId);
    
    // Delete a pathway for the current user's organization
    void deletePathwayForCurrentUser(Long pathwayId, String userEmail);
    
    // Get available pathways for a user (based on their organization and subscriptions)
    List<PathwayDTO> getAvailablePathwaysForUser(Long userId);
    
    // Enroll a user in a pathway
    void enrollUserInPathway(Long pathwayId, Long userId);
    
    // Get pathway progress for a user
    PathwayDTO getPathwayProgress(Long pathwayId, Long userId);
    
    // Check if pathway exists and belongs to organization
    boolean pathwayExistsAndBelongsToOrganization(Long pathwayId, Long organizationId);
} 