package com.taashee.badger.services;

import com.taashee.badger.models.Pathway;
import com.taashee.badger.models.PathwayElement;
import com.taashee.badger.models.PathwayElementBadge;
import com.taashee.badger.models.BadgeClass;

import java.util.List;
import java.util.Map;

public interface PathwayAuthoringService {
    
    // Pathway Structure Management
    Map<String, Object> getPathwayStructure(Long pathwayId, String userEmail);
    
    // Element Management
    PathwayElement createElement(Long pathwayId, PathwayElement element, String userEmail);
    PathwayElement updateElement(Long pathwayId, Long elementId, PathwayElement element, String userEmail);
    void deleteElement(Long pathwayId, Long elementId, String userEmail);
    
    // Element Hierarchy Management
    PathwayElement moveElement(Long pathwayId, Long elementId, Long newParentId, Integer newOrderIndex, String userEmail);
    List<PathwayElement> reorderElements(Long pathwayId, List<Map<String, Object>> reorderRequest, String userEmail);
    
    // Badge Integration
    PathwayElementBadge addBadgeToElement(Long pathwayId, Long elementId, PathwayElementBadge badge, String userEmail);
    void removeBadgeFromElement(Long pathwayId, Long elementId, Long badgeId, String userEmail);
    
    // Badge Search and Discovery
    List<Map<String, Object>> searchBadges(String query, String source, String framework, String userEmail);
    List<BadgeClass> getInternalBadges(Long organizationId, String userEmail);
    
    // External Badge Management
    PathwayElementBadge addExternalBadge(Long pathwayId, Long elementId, Map<String, Object> externalBadgeData, String userEmail);
    PathwayElementBadge verifyExternalBadge(Long badgeId, String notes, String userEmail);
    
    // Competency Alignment
    PathwayElement addCompetencyAlignment(Long pathwayId, Long elementId, PathwayElement.CompetencyAlignment competency, String userEmail);
    List<Map<String, Object>> getCompetencyFrameworks();
    
    // Pathway Validation
    Map<String, Object> validatePathway(Long pathwayId, String userEmail);
    
    // Pathway Publishing
    Pathway publishPathway(Long pathwayId, String userEmail);
    Pathway unpublishPathway(Long pathwayId, String userEmail);
    
    // Pathway Templates
    List<Pathway> getPathwayTemplates(String userEmail);
    Pathway saveAsTemplate(Long pathwayId, String templateName, String templateCategory, String userEmail);
    
    // Export/Import
    Map<String, Object> exportPathway(Long pathwayId, String userEmail);
    Pathway importPathway(Map<String, Object> pathwayData, Long organizationId, String userEmail);
    
    // Advanced Features
    List<PathwayElement> getElementPrerequisites(Long elementId, String userEmail);
    void addElementPrerequisite(Long elementId, Long prerequisiteElementId, String userEmail);
    void removeElementPrerequisite(Long elementId, Long prerequisiteElementId, String userEmail);
    
    // Completion Rules
    void updateCompletionRule(Long elementId, PathwayElement.CompletionRule rule, Integer requiredCount, String userEmail);
    void setElementOptional(Long elementId, boolean isOptional, String userEmail);
    void setElementCountsTowardsParent(Long elementId, boolean countsTowardsParent, String userEmail);
    
    // Badge Requirements
    void setBadgeRequired(Long badgeId, boolean isRequired, String userEmail);
    List<PathwayElementBadge> getElementBadges(Long elementId, String userEmail);
    
    // Pathway Analytics (for authoring)
    Map<String, Object> getPathwayAuthoringAnalytics(Long pathwayId, String userEmail);
    List<Map<String, Object>> getElementCompletionStats(Long elementId, String userEmail);
    
    // Version Control
    void createPathwayVersion(Long pathwayId, String versionName, String userEmail);
    List<Map<String, Object>> getPathwayVersions(Long pathwayId, String userEmail);
    Pathway revertToVersion(Long pathwayId, String versionId, String userEmail);
    
    // Collaboration
    void sharePathway(Long pathwayId, String collaboratorEmail, String permission, String userEmail);
    List<Map<String, Object>> getPathwayCollaborators(Long pathwayId, String userEmail);
    void removePathwayCollaborator(Long pathwayId, String collaboratorEmail, String userEmail);
    
    // Advanced Validation
    Map<String, Object> validateElementCompleteness(Long elementId, String userEmail);
    Map<String, Object> validatePathwayCompleteness(Long pathwayId, String userEmail);
    List<String> getPathwayWarnings(Long pathwayId, String userEmail);
    List<String> getPathwayErrors(Long pathwayId, String userEmail);
    
    // Badge Compatibility
    List<Map<String, Object>> getCompatibleBadges(Long elementId, String userEmail);
    Map<String, Object> checkBadgeCompatibility(Long elementId, Long badgeId, String userEmail);
    
    // Pathway Optimization
    Map<String, Object> optimizePathwayStructure(Long pathwayId, String userEmail);
    List<Map<String, Object>> suggestPathwayImprovements(Long pathwayId, String userEmail);
    
    // Bulk Operations
    void bulkUpdateElements(Long pathwayId, List<Map<String, Object>> updates, String userEmail);
    void bulkAddBadges(Long pathwayId, Long elementId, List<Map<String, Object>> badges, String userEmail);
    void bulkRemoveBadges(Long pathwayId, Long elementId, List<Long> badgeIds, String userEmail);
} 