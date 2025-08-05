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
    
    // Get published pathways for students in an organization
    List<PathwayDTO> getPublishedPathwaysForOrganization(Long organizationId);
    
    // Get a specific pathway by ID (with organization validation)
    Optional<PathwayDTO> getPathwayById(Long pathwayId, Long organizationId);
    
    // Get a specific pathway by ID for the current user's organization
    Optional<PathwayDTO> getPathwayByIdForCurrentUser(Long pathwayId, String userEmail);
    
    // Get a published pathway by ID for students
    Optional<PathwayDTO> getPublishedPathwayById(Long pathwayId, Long organizationId);
    
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
    
    // === ENTERPRISE-GRADE PUBLISHING FUNCTIONALITY ===
    
    // Publish a pathway (Draft -> Published)
    PathwayDTO publishPathway(Long pathwayId, String userEmail);
    
    // Unpublish a pathway (Published -> Draft)
    PathwayDTO unpublishPathway(Long pathwayId, String userEmail);
    
    // Archive a pathway (Published -> Archived)
    PathwayDTO archivePathway(Long pathwayId, String userEmail);
    
    // Validate pathway before publishing
    List<String> validatePathwayForPublishing(Long pathwayId, String userEmail);
    
    // Get pathway validation status
    PathwayDTO getPathwayValidationStatus(Long pathwayId, String userEmail);
    
    // === ORGANIZATION-SCOPED ACCESS CONTROL ===
    
    // Check if user has permission to manage pathway
    boolean hasPermissionToManagePathway(Long pathwayId, String userEmail);
    
    // Check if user has permission to view pathway
    boolean hasPermissionToViewPathway(Long pathwayId, String userEmail);
    
    // Get user's organization ID
    Long getUserOrganizationId(String userEmail);
    
    // === PATHWAY STATUS MANAGEMENT ===
    
    // Get pathways by status for organization
    List<PathwayDTO> getPathwaysByStatus(Long organizationId, Pathway.PathwayStatus status);
    
    // Get pathway statistics for organization
    PathwayDTO getPathwayStatistics(Long organizationId);
    
    // === STUDENT ACCESS ===
    
    // Get published pathways for student
    List<PathwayDTO> getPublishedPathwaysForStudent(String userEmail);
    
    // Check if student can enroll in pathway
    boolean canStudentEnrollInPathway(Long pathwayId, String userEmail);
} 