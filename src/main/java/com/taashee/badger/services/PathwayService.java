package com.taashee.badger.services;

import com.taashee.badger.models.Pathway;
import com.taashee.badger.models.PathwayStep;
import com.taashee.badger.models.PathwayVersion;
import com.taashee.badger.models.Organization;
import com.taashee.badger.models.User;
import java.util.List;

public interface PathwayService {
    Pathway createPathway(Organization organization, String name, String description);
    List<Pathway> listPathways(Organization organization);
    Pathway getPathway(Long pathwayId);
    Pathway updatePathway(Long pathwayId, String name, String description);
    void deletePathway(Long pathwayId);
    
    PathwayStep createStep(Long pathwayId, Long parentStepId, String name, String description, String shortCode, boolean optionalStep, boolean milestone);
    List<PathwayStep> listSteps(Long pathwayId);
    PathwayStep getStep(Long stepId);
    PathwayStep updateStep(Long pathwayId, Long stepId, String name, String description, String shortCode, String alignmentUrl, String targetCode, String frameworkName, Boolean optional);
    void deleteStep(Long pathwayId, Long stepId);
    PathwayStep rearrangeStep(Long pathwayId, Long stepId, Long newParentId, Integer newOrderIndex);
    
    // Version management
    PathwayVersion createVersion(Long pathwayId);
    List<PathwayVersion> listVersions(Long pathwayId);
    
    // Audit logging
    void logAction(Organization organization, Pathway pathway, User user, String action, String entityType, Long entityId, String details);
}


