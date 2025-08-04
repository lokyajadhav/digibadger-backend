package com.taashee.badger.services;

import com.taashee.badger.models.Pathway;
import com.taashee.badger.models.PathwayProgress;
import com.taashee.badger.models.PathwayElementProgress;

import java.util.List;
import java.util.Optional;

public interface PathwayEnrollmentService {
    
    /**
     * Enroll a user in a pathway
     */
    PathwayProgress enrollUserInPathway(Long pathwayId, String userEmail);
    
    /**
     * Unenroll a user from a pathway
     */
    void unenrollUserFromPathway(Long pathwayId, String userEmail);
    
    /**
     * Get user's progress for a specific pathway
     */
    Optional<PathwayProgress> getUserPathwayProgress(Long pathwayId, String userEmail);
    
    /**
     * Get all pathways user is enrolled in
     */
    List<PathwayProgress> getUserEnrollments(String userEmail);
    
    /**
     * Complete a pathway element for a user
     */
    PathwayElementProgress completeElement(Long pathwayId, Long elementId, String userEmail);
    
    /**
     * Get available pathways for enrollment
     */
    List<Pathway> getAvailablePathways(String userEmail);
    
    /**
     * Get pathway analytics for organization staff
     */
    Object getPathwayAnalytics(Long pathwayId, String userEmail);
    
    // Removed bulkEnrollGroup - simplified approach
    
    /**
     * Update pathway progress when badges are earned
     */
    void updateProgressOnBadgeEarned(Long pathwayId, Long userId, Long badgeId);
    
    /**
     * Check if user can complete a pathway element
     */
    boolean canCompleteElement(Long pathwayId, Long elementId, String userEmail);
    
    /**
     * Get pathway completion status
     */
    boolean isPathwayCompleted(Long pathwayId, String userEmail);
    
    /**
     * Issue completion badge when pathway is finished
     */
    void issueCompletionBadge(Long pathwayId, String userEmail);
} 