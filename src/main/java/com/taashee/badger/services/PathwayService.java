package com.taashee.badger.services;

import com.taashee.badger.models.Pathway;
import com.taashee.badger.models.PathwayStep;
import com.taashee.badger.models.PathwayVersion;
import com.taashee.badger.models.Organization;
import com.taashee.badger.models.User;
import com.taashee.badger.models.StepRequirement;
import java.util.List;

public interface PathwayService {
    Pathway createPathway(Organization organization, String name, String description);
    List<Pathway> listPathways(Organization organization);
    Pathway getPathway(Long pathwayId);
    Pathway updatePathway(Long pathwayId, String name, String description);
    void deletePathway(Long pathwayId);
    
    PathwayStep createStep(Long pathwayId, Long parentStepId, String name, String description, String shortCode, boolean optionalStep, boolean milestone);
    List<PathwayStep> listSteps(Long pathwayId);
    PathwayStep getStep(Long pathwayId, Long stepId);
    PathwayStep updateStep(Long pathwayId, Long stepId, String name, String description, String shortCode, String alignmentUrl, String targetCode, String frameworkName, Boolean optional, String prerequisiteRule, String prerequisiteSteps);
    PathwayStep updateStepAchievement(Long pathwayId, Long stepId, Long achievementBadgeId, Boolean achievementExternal);
    void deleteStep(Long pathwayId, Long stepId);
    PathwayStep rearrangeStep(Long pathwayId, Long stepId, Long newParentId, Integer newOrderIndex);
    
    // Pathway configuration methods
    Pathway updatePathwayConfiguration(Long pathwayId, String shortCode, String alignmentUrl, String targetCode, String frameworkName, 
                                    Long completionBadgeId, Boolean completionBadgeExternal, String prerequisiteRule, String prerequisiteSteps);
    
    // Version management
    PathwayVersion createVersion(Long pathwayId);
    List<PathwayVersion> listVersions(Long pathwayId);
    
    // Step Requirements Management
    StepRequirement createStepRequirement(Long pathwayId, Long stepId, String type, Long badgeClassId, String thirdPartyUrl, String thirdPartyJson, String experienceName, String experienceDescription, String groupKey);
    StepRequirement updateStepRequirement(Long pathwayId, Long stepId, Long requirementId, String type, Long badgeClassId, String thirdPartyUrl, String thirdPartyJson, String experienceName, String experienceDescription, String groupKey);
    void deleteStepRequirement(Long pathwayId, Long stepId, Long requirementId);
    
    // Audit logging
    void logAction(Organization organization, Pathway pathway, User user, String action, String entityType, Long entityId, String details);
}


