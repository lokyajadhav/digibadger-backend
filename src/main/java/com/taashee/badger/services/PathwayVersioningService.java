package com.taashee.badger.services;

import com.taashee.badger.models.*;
import com.taashee.badger.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class PathwayVersioningService {
    
    @Autowired
    private PathwayVersionRepository pathwayVersionRepository;
    
    @Autowired
    private StepVersionRepository stepVersionRepository;
    
    @Autowired
    private RequirementVersionRepository requirementVersionRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private PathwayRepository pathwayRepository;
    
    @Autowired
    private PathwayStepRepository pathwayStepRepository;
    
    @Autowired
    private StepRequirementRepository stepRequirementRepository;
    
    /**
     * Publishes a pathway by creating immutable snapshots
     */
    public PathwayVersion publishPathway(Long pathwayId, User user) {
        // Get the pathway with all its steps and requirements
        Pathway pathway = pathwayRepository.findById(pathwayId)
            .orElseThrow(() -> new RuntimeException("Pathway not found: " + pathwayId));
        
        // Get all steps for this pathway
        List<PathwayStep> steps = pathwayStepRepository.findByPathwayOrderByOrderIndexAsc(pathway);
        
        // Determine the next version number
        Integer nextVersion = getNextVersionNumber(pathway);
        
        // Create pathway version snapshot
        PathwayVersion pathwayVersion = new PathwayVersion(pathway, nextVersion, 
            PathwayVersion.VersionStatus.PUBLISHED, user);
        pathwayVersion = pathwayVersionRepository.save(pathwayVersion);
        
        // Mark the original pathway as PUBLISHED (read-only)
        pathway.setStatus(PathwayStatus.PUBLISHED);
        pathwayRepository.save(pathway);
        
        // Create step version snapshots
        for (PathwayStep step : steps) {
            StepVersion stepVersion = new StepVersion(pathwayVersion, step);
            stepVersion = stepVersionRepository.save(stepVersion);
            
            // Create requirement version snapshots
            List<StepRequirement> requirements = stepRequirementRepository.findByStep(step);
            for (StepRequirement requirement : requirements) {
                RequirementVersion requirementVersion = new RequirementVersion(
                    stepVersion, requirement);
                requirementVersionRepository.save(requirementVersion);
            }
        }
        
        // Update pathway status to indicate it has published versions
        pathway.setStatus(PathwayStatus.PUBLISHED);
        pathwayRepository.save(pathway);
        
        // Create audit log
        AuditLog auditLog = AuditLog.createPublishLog(pathway, user, nextVersion);
        auditLogRepository.save(auditLog);
        
        return pathwayVersion;
    }
    
    /**
     * Gets the next version number for a pathway
     */
    private Integer getNextVersionNumber(Pathway pathway) {
        Optional<Integer> maxVersion = pathwayVersionRepository.findMaxVersionByPathway(pathway);
        return maxVersion.orElse(0) + 1;
    }
    
    /**
     * Gets all versions of a pathway
     */
    public List<PathwayVersion> getPathwayVersions(Long pathwayId) {
        Pathway pathway = pathwayRepository.findById(pathwayId)
            .orElseThrow(() -> new RuntimeException("Pathway not found: " + pathwayId));
        return pathwayVersionRepository.findByPathwayOrderByVersionDesc(pathway);
    }
    
    /**
     * Gets a specific version of a pathway
     */
    public Optional<PathwayVersion> getPathwayVersion(Long pathwayId, Integer version) {
        Pathway pathway = pathwayRepository.findById(pathwayId)
            .orElseThrow(() -> new RuntimeException("Pathway not found: " + pathwayId));
        return pathwayVersionRepository.findByPathwayAndVersion(pathway, version);
    }
    
    /**
     * Gets the latest published version of a pathway
     */
    public Optional<PathwayVersion> getLatestPublishedVersion(Long pathwayId) {
        Pathway pathway = pathwayRepository.findById(pathwayId)
            .orElseThrow(() -> new RuntimeException("Pathway not found: " + pathwayId));
        return pathwayVersionRepository.findByPathwayAndStatus(pathway, PathwayVersion.VersionStatus.PUBLISHED);
    }
    
    /**
     * Gets the latest draft version of a pathway
     */
    public Optional<PathwayVersion> getLatestDraftVersion(Long pathwayId) {
        Pathway pathway = pathwayRepository.findById(pathwayId)
            .orElseThrow(() -> new RuntimeException("Pathway not found: " + pathwayId));
        return pathwayVersionRepository.findLatestDraftByPathway(pathway);
    }
    
    /**
     * Gets audit logs for a pathway
     */
    public List<AuditLog> getPathwayAuditLogs(Long pathwayId) {
        Pathway pathway = pathwayRepository.findById(pathwayId)
            .orElseThrow(() -> new RuntimeException("Pathway not found: " + pathwayId));
        return auditLogRepository.findByPathwayOrderByTimestampDesc(pathway);
    }
    
    /**
     * Creates a new draft version after publishing
     */
    public PathwayVersion createNewDraftVersion(Long pathwayId, User user) {
        Pathway pathway = pathwayRepository.findById(pathwayId)
            .orElseThrow(() -> new RuntimeException("Pathway not found: " + pathwayId));
        
        // Determine the next version number
        Integer nextVersion = getNextVersionNumber(pathway);
        
        // Create new draft version
        PathwayVersion draftVersion = new PathwayVersion(pathway, nextVersion, 
            PathwayVersion.VersionStatus.DRAFT, user);
        draftVersion = pathwayVersionRepository.save(draftVersion);
        
        // Update pathway to point to new draft
        pathway.setCurrentDraftVersionId(draftVersion.getId());
        pathway.setStatus(PathwayStatus.DRAFT);
        pathwayRepository.save(pathway);
        
        // Create audit log
        AuditLog auditLog = new AuditLog(AuditLog.AuditAction.CREATE, "PathwayVersion", 
            draftVersion.getId(), user);
        auditLog.setEntityName("PathwayVersion");
        auditLog.setDescription("Created new draft version " + nextVersion);
        auditLog.setPathway(pathway);
        auditLogRepository.save(auditLog);
        
        return draftVersion;
    }
    
    /**
     * Gets version statistics for a pathway
     */
    public PathwayVersionStats getVersionStats(Long pathwayId) {
        Pathway pathway = pathwayRepository.findById(pathwayId)
            .orElseThrow(() -> new RuntimeException("Pathway not found: " + pathwayId));
        
        Long publishedCount = pathwayVersionRepository.countPublishedVersionsByPathway(pathway);
        Optional<Integer> maxVersion = pathwayVersionRepository.findMaxVersionByPathway(pathway);
        Optional<PathwayVersion> latestDraft = pathwayVersionRepository.findLatestDraftByPathway(pathway);
        Optional<PathwayVersion> latestPublished = pathwayVersionRepository.findByPathwayAndStatus(
            pathway, PathwayVersion.VersionStatus.PUBLISHED);
        
        return new PathwayVersionStats(
            maxVersion.orElse(0),
            publishedCount.intValue(),
            latestDraft.isPresent(),
            latestPublished.isPresent(),
            latestDraft.map(PathwayVersion::getVersion).orElse(null),
            latestPublished.map(PathwayVersion::getVersion).orElse(null)
        );
    }
    
    /**
     * Data class for version statistics
     */
    public static class PathwayVersionStats {
        public final Integer totalVersions;
        public final Integer publishedVersions;
        public final Boolean hasDraft;
        public final Boolean hasPublished;
        public final Integer latestDraftVersion;
        public final Integer latestPublishedVersion;
        
        public PathwayVersionStats(Integer totalVersions, Integer publishedVersions, 
                                 Boolean hasDraft, Boolean hasPublished,
                                 Integer latestDraftVersion, Integer latestPublishedVersion) {
            this.totalVersions = totalVersions;
            this.publishedVersions = publishedVersions;
            this.hasDraft = hasDraft;
            this.hasPublished = hasPublished;
            this.latestDraftVersion = latestDraftVersion;
            this.latestPublishedVersion = latestPublishedVersion;
        }
    }
    
    /**
     * Duplicates a pathway with automatic version naming
     */
    public Pathway duplicatePathway(Long originalPathwayId, User user) {
        try {
            // Get the original pathway with all its data
            Pathway originalPathway = pathwayRepository.findById(originalPathwayId)
                .orElseThrow(() -> new RuntimeException("Original pathway not found: " + originalPathwayId));
            
            // Get all steps and requirements for the original pathway
            List<PathwayStep> originalSteps = pathwayStepRepository.findByPathwayOrderByOrderIndexAsc(originalPathway);
            
            // Generate version name
            String baseName = originalPathway.getName().replaceAll("\\s+v\\d+$", ""); // Remove existing version suffix
            List<Pathway> existingPathways = pathwayRepository.findByOrganizationIdAndNameContaining(
                originalPathway.getOrganization().getId(), baseName);
            
            int nextVersion = 1;
            for (Pathway existing : existingPathways) {
                String name = existing.getName();
                if (name.matches(".*\\s+v\\d+$")) {
                    String versionStr = name.replaceAll(".*\\s+v(\\d+)$", "$1");
                    try {
                        int version = Integer.parseInt(versionStr);
                        nextVersion = Math.max(nextVersion, version + 1);
                    } catch (NumberFormatException e) {
                        // Ignore if version parsing fails
                    }
                }
            }
            
            String newName = baseName + " v" + nextVersion;
            
            // Create new pathway
            Pathway newPathway = new Pathway();
            newPathway.setOrganization(originalPathway.getOrganization());
            newPathway.setName(newName);
            newPathway.setDescription(originalPathway.getDescription());
            newPathway.setShortCode(originalPathway.getShortCode());
            newPathway.setAlignmentUrl(originalPathway.getAlignmentUrl());
            newPathway.setTargetCode(originalPathway.getTargetCode());
            newPathway.setFrameworkName(originalPathway.getFrameworkName());
            newPathway.setCompletionBadgeId(originalPathway.getCompletionBadgeId());
            newPathway.setCompletionBadgeExternal(originalPathway.getCompletionBadgeExternal());
            newPathway.setPrerequisiteRule(originalPathway.getPrerequisiteRule());
            newPathway.setPrerequisiteSteps(originalPathway.getPrerequisiteSteps());
            newPathway.setStatus(PathwayStatus.DRAFT); // Always create as draft
            
            newPathway = pathwayRepository.save(newPathway);
            
            // Duplicate all steps
            Map<Long, PathwayStep> stepIdMap = new HashMap<>();
            
            for (PathwayStep originalStep : originalSteps) {
                PathwayStep newStep = new PathwayStep();
                newStep.setPathway(newPathway);
                newStep.setName(originalStep.getName());
                newStep.setDescription(originalStep.getDescription());
                newStep.setShortCode(originalStep.getShortCode());
                newStep.setOptionalStep(originalStep.isOptionalStep());
                newStep.setOrderIndex(originalStep.getOrderIndex());
                newStep.setMilestone(originalStep.isMilestone());
                newStep.setAchievementBadgeId(originalStep.getAchievementBadgeId());
                newStep.setAchievementExternal(originalStep.getAchievementExternal());
                newStep.setPrerequisiteRule(originalStep.getPrerequisiteRule());
                newStep.setPrerequisiteSteps(originalStep.getPrerequisiteSteps());
                
                newStep = pathwayStepRepository.save(newStep);
                stepIdMap.put(originalStep.getId(), newStep);
            }
            
            // Update parent-child relationships
            for (PathwayStep originalStep : originalSteps) {
                if (originalStep.getParentStep() != null) {
                    PathwayStep newStep = stepIdMap.get(originalStep.getId());
                    PathwayStep newParentStep = stepIdMap.get(originalStep.getParentStep().getId());
                    newStep.setParentStep(newParentStep);
                    pathwayStepRepository.save(newStep);
                }
            }
            
            // Duplicate all requirements
            for (PathwayStep originalStep : originalSteps) {
                List<StepRequirement> originalRequirements = stepRequirementRepository.findByStep(originalStep);
                PathwayStep newStep = stepIdMap.get(originalStep.getId());
                
                for (StepRequirement originalReq : originalRequirements) {
                    StepRequirement newRequirement = new StepRequirement();
                    newRequirement.setStep(newStep);
                    newRequirement.setType(originalReq.getType());
                    newRequirement.setBadgeClassId(originalReq.getBadgeClassId());
                    newRequirement.setThirdPartyUrl(originalReq.getThirdPartyUrl());
                    newRequirement.setThirdPartyJson(originalReq.getThirdPartyJson());
                    newRequirement.setExperienceName(originalReq.getExperienceName());
                    newRequirement.setExperienceDescription(originalReq.getExperienceDescription());
                    newRequirement.setGroupKey(originalReq.getGroupKey());
                    
                    stepRequirementRepository.save(newRequirement);
                }
            }
        
            // Log the duplication action
            AuditLog auditLog = AuditLog.createPathwayLog("CREATE", newPathway, user, 
                "Duplicated from pathway: " + originalPathway.getName());
            auditLogRepository.save(auditLog);
            
            return newPathway;
        } catch (Exception e) {
            System.err.println("Error duplicating pathway: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to duplicate pathway: " + e.getMessage(), e);
        }
    }
}
