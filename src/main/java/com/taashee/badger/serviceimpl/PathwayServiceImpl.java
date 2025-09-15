package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.Pathway;
import com.taashee.badger.models.PathwayStep;
import com.taashee.badger.models.PathwayVersion;
import com.taashee.badger.models.StepVersion;
import com.taashee.badger.models.Organization;
import com.taashee.badger.models.User;
import com.taashee.badger.models.AuditLog;
import com.taashee.badger.models.StepRequirement;
import com.taashee.badger.models.StepRequirementType;
import com.taashee.badger.repositories.PathwayRepository;
import com.taashee.badger.repositories.PathwayStepRepository;
import com.taashee.badger.repositories.PathwayVersionRepository;
import com.taashee.badger.repositories.AuditLogRepository;
import com.taashee.badger.repositories.StepRequirementRepository;
import com.taashee.badger.repositories.StepVersionRepository;
import com.taashee.badger.repositories.UserRepository;
import com.taashee.badger.services.PathwayService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class PathwayServiceImpl implements PathwayService {
    private final PathwayRepository pathwayRepository;
    private final PathwayStepRepository stepRepository;
    private final PathwayVersionRepository versionRepository;
    private final AuditLogRepository auditRepository;
    private final StepRequirementRepository stepRequirementRepository;
    private final StepVersionRepository stepVersionRepository;
    private final UserRepository userRepository;

    public PathwayServiceImpl(PathwayRepository pathwayRepository, PathwayStepRepository stepRepository, 
                            PathwayVersionRepository versionRepository, AuditLogRepository auditRepository,
                            StepRequirementRepository stepRequirementRepository, StepVersionRepository stepVersionRepository,
                            UserRepository userRepository) {
        this.pathwayRepository = pathwayRepository;
        this.stepRepository = stepRepository;
        this.versionRepository = versionRepository;
        this.auditRepository = auditRepository;
        this.stepRequirementRepository = stepRequirementRepository;
        this.stepVersionRepository = stepVersionRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return null; // Return null if no user is authenticated
        }
        String email = auth.getPrincipal().toString();
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    @Transactional
    public Pathway createPathway(Organization organization, String name, String description) {
        Pathway p = new Pathway();
        p.setOrganization(organization);
        p.setName(name);
        p.setDescription(description);
        Pathway savedPathway = pathwayRepository.save(p);
        
        // Automatically create milestone step for the pathway
        PathwayStep milestoneStep = new PathwayStep();
        milestoneStep.setPathway(savedPathway);
        milestoneStep.setParentStep(null); // Milestone has no parent
        milestoneStep.setName("End of Pathway");
        milestoneStep.setDescription("Completion milestone for " + name);
        milestoneStep.setShortCode("EOP");
        milestoneStep.setOptionalStep(false);
        milestoneStep.setMilestone(true); // This is the milestone step
        milestoneStep.setOrderIndex(0);
        stepRepository.save(milestoneStep);
        
        // Get current user and log the pathway creation
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            logAction(organization, savedPathway, currentUser, "CREATE", "PATHWAY", savedPathway.getId(), 
                "Created pathway: " + name + " with description: " + (description != null ? description : ""));
            
            // Log the milestone step creation
            logAction(organization, savedPathway, currentUser, "CREATE", "STEP", milestoneStep.getId(), 
                "Created milestone step: " + milestoneStep.getName());
        }
        
        return savedPathway;
    }

    @Override
    public List<Pathway> listPathways(Organization organization) {
        return pathwayRepository.findByOrganization(organization);
    }

    @Override
    @Transactional
    public PathwayStep createStep(Long pathwayId, Long parentStepId, String name, String description, String shortCode, boolean optionalStep, boolean milestone) {
        Pathway pathway = pathwayRepository.findById(pathwayId).orElseThrow();

        PathwayStep step = new PathwayStep();
        step.setPathway(pathway);
        if (parentStepId != null) {
            PathwayStep parent = stepRepository.findById(parentStepId).orElseThrow();
            step.setParentStep(parent);
            
            // DAG Validation: Prevent cycles
            validateNoCycles(step, pathway);
        }
        step.setName(name);
        step.setDescription(description);
        step.setShortCode(shortCode);
        step.setOptionalStep(optionalStep);
        step.setMilestone(milestone);
        step.setOrderIndex(stepRepository.findByPathwayOrderByOrderIndexAsc(pathway).size());
        
        // DAG Validation: Validate order index
        validateOrderIndex(step, pathway);
        
        PathwayStep savedStep = stepRepository.save(step);
        
        // Get current user and log the step creation
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            String stepType = milestone ? "milestone" : "step";
            String parentInfo = parentStepId != null ? " under parent step ID: " + parentStepId : "";
            logAction(pathway.getOrganization(), pathway, currentUser, "CREATE", "STEP", savedStep.getId(), 
                "Created " + stepType + ": " + name + " (Short code: " + shortCode + ")" + parentInfo);
        }
        
        return savedStep;
    }
    
    /**
     * DAG Validation: Prevent cycles in pathway structure
     */
    private void validateNoCycles(PathwayStep newStep, Pathway pathway) {
        if (newStep.getParentStep() == null) return;
        
        // Check if adding this step would create a cycle
        PathwayStep current = newStep.getParentStep();
        while (current != null) {
            if (current.getId().equals(newStep.getId())) {
                throw new IllegalArgumentException("Cycle detected: Cannot create step that would form a circular dependency");
            }
            current = current.getParentStep();
        }
    }
    
    /**
     * DAG Validation: Validate order index consistency
     */
    private void validateOrderIndex(PathwayStep newStep, Pathway pathway) {
        List<PathwayStep> existingSteps = stepRepository.findByPathwayOrderByOrderIndexAsc(pathway);
        
        // Check for duplicate order indexes
        Set<Integer> usedIndexes = existingSteps.stream()
            .map(PathwayStep::getOrderIndex)
            .collect(Collectors.toSet());
            
        if (usedIndexes.contains(newStep.getOrderIndex())) {
            // Auto-adjust order index to avoid conflicts
            int maxIndex = existingSteps.stream()
                .mapToInt(PathwayStep::getOrderIndex)
                .max()
                .orElse(-1);
            newStep.setOrderIndex(maxIndex + 1);
        }
        
        // Ensure order index is not negative
        if (newStep.getOrderIndex() < 0) {
            newStep.setOrderIndex(0);
        }
    }

    @Override
    public List<PathwayStep> listSteps(Long pathwayId) {
        Pathway pathway = pathwayRepository.findById(pathwayId).orElseThrow();
        return stepRepository.findByPathwayOrderByOrderIndexAsc(pathway);
    }

    @Override
    @Transactional
    public PathwayStep rearrangeStep(Long pathwayId, Long stepId, Long newParentId, Integer newOrderIndex) {
        PathwayStep step = stepRepository.findById(stepId).orElseThrow();
        // Validate that the step belongs to the specified pathway
        if (!step.getPathway().getId().equals(pathwayId)) {
            throw new IllegalArgumentException("Step does not belong to the specified pathway");
        }
        
        PathwayStep newParent = null;
        if (newParentId != null) {
            newParent = stepRepository.findById(newParentId).orElseThrow();
            // Validate that the new parent also belongs to the same pathway
            if (!newParent.getPathway().getId().equals(pathwayId)) {
                throw new IllegalArgumentException("New parent step does not belong to the specified pathway");
            }
            // DAG guard: prevent setting a parent to its own descendant
            PathwayStep cursor = newParent;
            while (cursor != null) {
                if (cursor.getId().equals(step.getId())) {
                    throw new IllegalArgumentException("Cycle detected: cannot set a step's descendant as its parent");
                }
                cursor = cursor.getParentStep();
            }
        }
        step.setParentStep(newParent);
        if (newOrderIndex != null) {
            step.setOrderIndex(Math.max(0, newOrderIndex));
        }
        return stepRepository.save(step);
    }

    @Override
    @Transactional
    public PathwayStep updateStep(Long pathwayId, Long stepId, String name, String description, String shortCode, String alignmentUrl, String targetCode, String frameworkName, Boolean optional, String prerequisiteRule, String prerequisiteSteps) {
        PathwayStep step = stepRepository.findById(stepId).orElseThrow();
        // Validate that the step belongs to the specified pathway
        if (!step.getPathway().getId().equals(pathwayId)) {
            throw new IllegalArgumentException("Step does not belong to the specified pathway");
        }
        
        // Store old values for audit logging
        String oldName = step.getName();
        String oldDescription = step.getDescription();
        String oldShortCode = step.getShortCode();
        String oldAlignmentUrl = step.getAlignmentUrl();
        String oldTargetCode = step.getTargetCode();
        String oldFrameworkName = step.getFrameworkName();
        Boolean oldOptional = step.isOptionalStep();
        String oldPrerequisiteRule = step.getPrerequisiteRule();
        String oldPrerequisiteSteps = step.getPrerequisiteSteps();
        
        if (name != null) step.setName(name);
        if (description != null) step.setDescription(description);
        if (shortCode != null) step.setShortCode(shortCode);
        if (alignmentUrl != null) step.setAlignmentUrl(alignmentUrl);
        if (targetCode != null) step.setTargetCode(targetCode);
        if (frameworkName != null) step.setFrameworkName(frameworkName);
        if (optional != null) step.setOptionalStep(optional);
        if (prerequisiteRule != null) step.setPrerequisiteRule(prerequisiteRule);
        if (prerequisiteSteps != null) step.setPrerequisiteSteps(prerequisiteSteps);
        
        PathwayStep savedStep = stepRepository.save(step);
        
        // Get current user and log configuration changes
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            StringBuilder changes = new StringBuilder();
            
            if (name != null && !name.equals(oldName)) {
                changes.append("Name: '").append(oldName).append("' → '").append(name).append("'; ");
            }
            if (description != null && !description.equals(oldDescription)) {
                changes.append("Description updated; ");
            }
            if (shortCode != null && !shortCode.equals(oldShortCode)) {
                changes.append("Short code: '").append(oldShortCode).append("' → '").append(shortCode).append("'; ");
            }
            if (alignmentUrl != null && !alignmentUrl.equals(oldAlignmentUrl)) {
                changes.append("Alignment URL updated; ");
            }
            if (targetCode != null && !targetCode.equals(oldTargetCode)) {
                changes.append("Target code: '").append(oldTargetCode).append("' → '").append(targetCode).append("'; ");
            }
            if (frameworkName != null && !frameworkName.equals(oldFrameworkName)) {
                changes.append("Framework: '").append(oldFrameworkName).append("' → '").append(frameworkName).append("'; ");
            }
            if (optional != null && !optional.equals(oldOptional)) {
                changes.append("Optional: ").append(oldOptional).append(" → ").append(optional).append("; ");
            }
            if (prerequisiteRule != null && !prerequisiteRule.equals(oldPrerequisiteRule)) {
                changes.append("Prerequisite rule updated; ");
            }
            if (prerequisiteSteps != null && !prerequisiteSteps.equals(oldPrerequisiteSteps)) {
                changes.append("Prerequisite steps updated; ");
            }
            
            if (changes.length() > 0) {
                logAction(step.getPathway().getOrganization(), step.getPathway(), currentUser, "UPDATE", "STEP", stepId, 
                    "Updated step configuration: " + changes.toString().trim());
            }
        }
        
        return savedStep;
    }

    @Override
    @Transactional
    public PathwayStep updateStepAchievement(Long pathwayId, Long stepId, Long achievementBadgeId, Boolean achievementExternal) {
        PathwayStep step = stepRepository.findById(stepId).orElseThrow();
        // Validate that the step belongs to the specified pathway
        if (!step.getPathway().getId().equals(pathwayId)) {
            throw new IllegalArgumentException("Step does not belong to the specified pathway");
        }
        
        // Store old values for audit logging
        Long oldAchievementBadgeId = step.getAchievementBadgeId();
        Boolean oldAchievementExternal = step.getAchievementExternal();
        
        // Always update these fields, allowing null values to clear them
        step.setAchievementBadgeId(achievementBadgeId);
        step.setAchievementExternal(achievementExternal);
        
        PathwayStep savedStep = stepRepository.save(step);
        
        // Get current user and log achievement changes
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            StringBuilder changes = new StringBuilder();
            
            if (!java.util.Objects.equals(achievementBadgeId, oldAchievementBadgeId)) {
                changes.append("Achievement Badge ID: ").append(oldAchievementBadgeId).append(" → ").append(achievementBadgeId).append("; ");
            }
            if (!java.util.Objects.equals(achievementExternal, oldAchievementExternal)) {
                changes.append("External Achievement: ").append(oldAchievementExternal).append(" → ").append(achievementExternal).append("; ");
            }
            
            if (changes.length() > 0) {
                logAction(step.getPathway().getOrganization(), step.getPathway(), currentUser, "UPDATE", "STEP", stepId, 
                    "Updated step achievement settings: " + changes.toString().trim());
            }
        }
        
        return savedStep;
    }

    @Override
    public Pathway getPathway(Long pathwayId) {
        return pathwayRepository.findById(pathwayId).orElseThrow();
    }

    @Override
    @Transactional
    public Pathway updatePathway(Long pathwayId, String name, String description) {
        Pathway pathway = pathwayRepository.findById(pathwayId).orElseThrow();
        String oldName = pathway.getName();
        String oldDescription = pathway.getDescription();
        
        if (name != null) pathway.setName(name);
        if (description != null) pathway.setDescription(description);
        
        Pathway savedPathway = pathwayRepository.save(pathway);
        
        // Get current user and log the pathway update
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            String changes = "";
            if (name != null && !name.equals(oldName)) {
                changes += "Name changed from '" + oldName + "' to '" + name + "'. ";
            }
            if (description != null && !description.equals(oldDescription)) {
                changes += "Description updated. ";
            }
            
            if (!changes.isEmpty()) {
                logAction(pathway.getOrganization(), pathway, currentUser, "UPDATE", "PATHWAY", pathwayId, 
                    "Updated pathway: " + changes.trim());
            }
        }
        
        return savedPathway;
    }

    @Override
    @Transactional
    public Pathway updatePathwayConfiguration(Long pathwayId, String shortCode, String alignmentUrl, String targetCode, String frameworkName, 
                                            Long completionBadgeId, Boolean completionBadgeExternal, String prerequisiteRule, String prerequisiteSteps) {
        Pathway pathway = pathwayRepository.findById(pathwayId).orElseThrow();
        // Always update these fields, allowing null values to clear them
        pathway.setShortCode(shortCode);
        pathway.setAlignmentUrl(alignmentUrl);
        pathway.setTargetCode(targetCode);
        pathway.setFrameworkName(frameworkName);
        pathway.setCompletionBadgeId(completionBadgeId);
        pathway.setCompletionBadgeExternal(completionBadgeExternal);
        pathway.setPrerequisiteRule(prerequisiteRule);
        pathway.setPrerequisiteSteps(prerequisiteSteps);
        return pathwayRepository.save(pathway);
    }

    @Override
    @Transactional
    public void deletePathway(Long pathwayId) {
        Pathway pathway = pathwayRepository.findById(pathwayId).orElseThrow();
        
        // Get current user and log pathway deletion
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            String status = pathway.getStatus() != null ? pathway.getStatus().name() : "UNKNOWN";
            logAction(pathway.getOrganization(), pathway, currentUser, "DELETE", "PATHWAY", pathwayId, 
                "Deleted " + status + " pathway: " + pathway.getName() + " with description: " + 
                (pathway.getDescription() != null ? pathway.getDescription() : ""));
        }
        
        // Delete all audit logs for this pathway first to avoid foreign key constraint violations
        List<AuditLog> auditLogs = auditRepository.findByPathway(pathway);
        auditRepository.deleteAll(auditLogs);
        
        // Delete all step versions for this pathway first to avoid foreign key constraint violations
        List<StepVersion> stepVersions = stepVersionRepository.findByPathway(pathway);
        stepVersionRepository.deleteAll(stepVersions);
        
        // Delete all steps for this pathway (which will also delete step requirements)
        List<PathwayStep> steps = stepRepository.findByPathwayOrderByOrderIndexAsc(pathway);
        stepRepository.deleteAll(steps);
        
        // Delete all pathway versions for this pathway
        List<PathwayVersion> versions = versionRepository.findByPathwayOrderByVersionDesc(pathway);
        versionRepository.deleteAll(versions);
        
        // Finally delete the pathway itself
        pathwayRepository.delete(pathway);
    }

    @Override
    public PathwayStep getStep(Long pathwayId, Long stepId) {
        PathwayStep step = stepRepository.findById(stepId).orElseThrow();
        // Validate that the step belongs to the specified pathway
        if (!step.getPathway().getId().equals(pathwayId)) {
            throw new IllegalArgumentException("Step does not belong to the specified pathway");
        }
        return step;
    }

    @Override
    @Transactional
    public void deleteStep(Long pathwayId, Long stepId) {
        PathwayStep step = stepRepository.findById(stepId).orElseThrow();
        // Validate that the step belongs to the specified pathway
        if (!step.getPathway().getId().equals(pathwayId)) {
            throw new IllegalArgumentException("Step does not belong to the specified pathway");
        }
        
        // Get current user and log step deletion
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            String stepType = step.isMilestone() ? "milestone" : "step";
            logAction(step.getPathway().getOrganization(), step.getPathway(), currentUser, "DELETE", "STEP", stepId, 
                "Deleted " + stepType + ": " + step.getName() + " (Short code: " + step.getShortCode() + ")");
        }
        
        stepRepository.delete(step);
    }

    @Override
    public PathwayVersion createVersion(Long pathwayId) {
        Pathway pathway = pathwayRepository.findById(pathwayId).orElseThrow();
        Optional<Integer> maxVersion = versionRepository.findMaxVersionByPathway(pathway);
        Integer newVersionNumber = maxVersion.orElse(0) + 1;
        
        PathwayVersion version = new PathwayVersion(pathway, newVersionNumber, 
            PathwayVersion.VersionStatus.DRAFT, null); // TODO: Get current user
        version.setCompletionBadgeId(pathway.getCompletionBadgeId());
        version.setCompletionBadgeExternal(pathway.getCompletionBadgeExternal());
        version.setPrerequisiteRule(pathway.getPrerequisiteRule());
        version.setPrerequisiteSteps(pathway.getPrerequisiteSteps());
        
        return versionRepository.save(version);
    }

    @Override
    public List<PathwayVersion> listVersions(Long pathwayId) {
        Pathway pathway = pathwayRepository.findById(pathwayId).orElseThrow();
        return versionRepository.findByPathwayOrderByVersionDesc(pathway);
    }

    @Override
    public void logAction(Organization organization, Pathway pathway, User user, String action, String entityType, Long entityId, String details) {
        AuditLog log = new AuditLog();
        log.setPathway(pathway);
        log.setUser(user);
        log.setAction(AuditLog.AuditAction.valueOf(action.toUpperCase()));
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDescription(details);
        auditRepository.save(log);
    }

    // Step Requirements Management
    @Override
    @Transactional
    public StepRequirement createStepRequirement(Long pathwayId, Long stepId, String type, Long badgeClassId, String thirdPartyUrl, String thirdPartyJson, String experienceName, String experienceDescription, String groupKey) {
        PathwayStep step = stepRepository.findById(stepId).orElseThrow(() -> new RuntimeException("Step not found"));
        
        // Verify the step belongs to the pathway
        if (!step.getPathway().getId().equals(pathwayId)) {
            throw new RuntimeException("Step does not belong to the specified pathway");
        }
        
        StepRequirement requirement = new StepRequirement();
        requirement.setStep(step);
        requirement.setType(StepRequirementType.valueOf(type));
        requirement.setBadgeClassId(badgeClassId);
        requirement.setThirdPartyUrl(thirdPartyUrl);
        requirement.setThirdPartyJson(thirdPartyJson);
        requirement.setExperienceName(experienceName);
        requirement.setExperienceDescription(experienceDescription);
        requirement.setGroupKey(groupKey);
        
        StepRequirement savedRequirement = stepRequirementRepository.save(requirement);
        
        // Get current user and log requirement creation
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            logAction(step.getPathway().getOrganization(), step.getPathway(), currentUser, "CREATE", "REQUIREMENT", savedRequirement.getId(), 
                "Created requirement for step '" + step.getName() + "': Type=" + type + 
                (experienceName != null ? ", Experience=" + experienceName : "") +
                (groupKey != null ? ", Group=" + groupKey : ""));
        }
        
        return savedRequirement;
    }

    @Override
    @Transactional
    public StepRequirement updateStepRequirement(Long pathwayId, Long stepId, Long requirementId, String type, Long badgeClassId, String thirdPartyUrl, String thirdPartyJson, String experienceName, String experienceDescription, String groupKey) {
        PathwayStep step = stepRepository.findById(stepId).orElseThrow(() -> new RuntimeException("Step not found"));
        
        // Verify the step belongs to the pathway
        if (!step.getPathway().getId().equals(pathwayId)) {
            throw new RuntimeException("Step does not belong to the specified pathway");
        }
        
        StepRequirement requirement = step.getRequirements().stream()
            .filter(req -> req.getId().equals(requirementId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Requirement not found"));
        
        // Store old values for audit logging
        StepRequirementType oldType = requirement.getType();
        Long oldBadgeClassId = requirement.getBadgeClassId();
        String oldThirdPartyUrl = requirement.getThirdPartyUrl();
        String oldExperienceName = requirement.getExperienceName();
        String oldExperienceDescription = requirement.getExperienceDescription();
        String oldGroupKey = requirement.getGroupKey();
        
        requirement.setType(StepRequirementType.valueOf(type));
        requirement.setBadgeClassId(badgeClassId);
        requirement.setThirdPartyUrl(thirdPartyUrl);
        requirement.setThirdPartyJson(thirdPartyJson);
        requirement.setExperienceName(experienceName);
        requirement.setExperienceDescription(experienceDescription);
        requirement.setGroupKey(groupKey);
        
        StepRequirement savedRequirement = stepRequirementRepository.save(requirement);
        
        // Get current user and log requirement changes
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            StringBuilder changes = new StringBuilder();
            
            if (!type.equals(oldType.name())) {
                changes.append("Type: ").append(oldType).append(" → ").append(type).append("; ");
            }
            if (!java.util.Objects.equals(badgeClassId, oldBadgeClassId)) {
                changes.append("Badge Class ID: ").append(oldBadgeClassId).append(" → ").append(badgeClassId).append("; ");
            }
            if (!java.util.Objects.equals(thirdPartyUrl, oldThirdPartyUrl)) {
                changes.append("Third Party URL updated; ");
            }
            if (!java.util.Objects.equals(experienceName, oldExperienceName)) {
                changes.append("Experience Name: '").append(oldExperienceName).append("' → '").append(experienceName).append("'; ");
            }
            if (!java.util.Objects.equals(experienceDescription, oldExperienceDescription)) {
                changes.append("Experience Description updated; ");
            }
            if (!java.util.Objects.equals(groupKey, oldGroupKey)) {
                changes.append("Group Key: '").append(oldGroupKey).append("' → '").append(groupKey).append("'; ");
            }
            
            if (changes.length() > 0) {
                logAction(step.getPathway().getOrganization(), step.getPathway(), currentUser, "UPDATE", "REQUIREMENT", requirementId, 
                    "Updated requirement for step '" + step.getName() + "': " + changes.toString().trim());
            }
        }
        
        return savedRequirement;
    }

    @Override
    @Transactional
    public void deleteStepRequirement(Long pathwayId, Long stepId, Long requirementId) {
        PathwayStep step = stepRepository.findById(stepId).orElseThrow(() -> new RuntimeException("Step not found"));
        
        // Verify the step belongs to the pathway
        if (!step.getPathway().getId().equals(pathwayId)) {
            throw new RuntimeException("Step does not belong to the specified pathway");
        }
        
        StepRequirement requirement = stepRequirementRepository.findById(requirementId)
            .orElseThrow(() -> new RuntimeException("Requirement not found"));
        
        // Verify the requirement belongs to the step
        if (!requirement.getStep().getId().equals(stepId)) {
            throw new RuntimeException("Requirement does not belong to the specified step");
        }
        
        // Get current user and log requirement deletion
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            logAction(step.getPathway().getOrganization(), step.getPathway(), currentUser, "DELETE", "REQUIREMENT", requirementId, 
                "Deleted requirement from step '" + step.getName() + "': Type=" + requirement.getType() + 
                (requirement.getExperienceName() != null ? ", Experience=" + requirement.getExperienceName() : "") +
                (requirement.getGroupKey() != null ? ", Group=" + requirement.getGroupKey() : ""));
        }
        
        stepRequirementRepository.delete(requirement);
    }
}


