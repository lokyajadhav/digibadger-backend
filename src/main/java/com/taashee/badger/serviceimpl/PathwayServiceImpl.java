package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.Pathway;
import com.taashee.badger.models.PathwayStep;
import com.taashee.badger.models.PathwayVersion;
import com.taashee.badger.models.Organization;
import com.taashee.badger.models.User;
import com.taashee.badger.models.AuditLog;
import com.taashee.badger.repositories.PathwayRepository;
import com.taashee.badger.repositories.PathwayStepRepository;
import com.taashee.badger.repositories.PathwayVersionRepository;
import com.taashee.badger.repositories.AuditLogRepository;
import com.taashee.badger.services.PathwayService;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PathwayServiceImpl implements PathwayService {
    private final PathwayRepository pathwayRepository;
    private final PathwayStepRepository stepRepository;
    private final PathwayVersionRepository versionRepository;
    private final AuditLogRepository auditRepository;

    public PathwayServiceImpl(PathwayRepository pathwayRepository, PathwayStepRepository stepRepository, 
                            PathwayVersionRepository versionRepository, AuditLogRepository auditRepository) {
        this.pathwayRepository = pathwayRepository;
        this.stepRepository = stepRepository;
        this.versionRepository = versionRepository;
        this.auditRepository = auditRepository;
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
        }
        step.setName(name);
        step.setDescription(description);
        step.setShortCode(shortCode);
        step.setOptionalStep(optionalStep);
        step.setMilestone(milestone);
        step.setOrderIndex(stepRepository.findByPathwayOrderByOrderIndexAsc(pathway).size());
        return stepRepository.save(step);
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
    public PathwayStep updateStep(Long pathwayId, Long stepId, String name, String description, String shortCode, String alignmentUrl, String targetCode, String frameworkName, Boolean optional) {
        PathwayStep step = stepRepository.findById(stepId).orElseThrow();
        // Validate that the step belongs to the specified pathway
        if (!step.getPathway().getId().equals(pathwayId)) {
            throw new IllegalArgumentException("Step does not belong to the specified pathway");
        }
        if (name != null) step.setName(name);
        if (description != null) step.setDescription(description);
        if (shortCode != null) step.setShortCode(shortCode);
        if (alignmentUrl != null) step.setAlignmentUrl(alignmentUrl);
        if (targetCode != null) step.setTargetCode(targetCode);
        if (frameworkName != null) step.setFrameworkName(frameworkName);
        if (optional != null) step.setOptionalStep(optional);
        return stepRepository.save(step);
    }

    @Override
    public Pathway getPathway(Long pathwayId) {
        return pathwayRepository.findById(pathwayId).orElseThrow();
    }

    @Override
    @Transactional
    public Pathway updatePathway(Long pathwayId, String name, String description) {
        Pathway pathway = pathwayRepository.findById(pathwayId).orElseThrow();
        if (name != null) pathway.setName(name);
        if (description != null) pathway.setDescription(description);
        return pathwayRepository.save(pathway);
    }

    @Override
    @Transactional
    public void deletePathway(Long pathwayId) {
        Pathway pathway = pathwayRepository.findById(pathwayId).orElseThrow();
        pathwayRepository.delete(pathway);
    }

    @Override
    public PathwayStep getStep(Long stepId) {
        return stepRepository.findById(stepId).orElseThrow();
    }

    @Override
    @Transactional
    public void deleteStep(Long pathwayId, Long stepId) {
        PathwayStep step = stepRepository.findById(stepId).orElseThrow();
        // Validate that the step belongs to the specified pathway
        if (!step.getPathway().getId().equals(pathwayId)) {
            throw new IllegalArgumentException("Step does not belong to the specified pathway");
        }
        stepRepository.delete(step);
    }

    @Override
    public PathwayVersion createVersion(Long pathwayId) {
        Pathway pathway = pathwayRepository.findById(pathwayId).orElseThrow();
        Optional<PathwayVersion> latestVersion = versionRepository.findTopByPathwayOrderByVersionNumberDesc(pathway);
        Long newVersionNumber = latestVersion.map(v -> v.getVersionNumber() + 1).orElse(1L);
        
        PathwayVersion version = new PathwayVersion();
        version.setPathway(pathway);
        version.setVersionNumber(newVersionNumber);
        version.setName(pathway.getName());
        version.setDescription(pathway.getDescription());
        version.setStatus(pathway.getStatus());
        version.setCompletionBadgeId(pathway.getCompletionBadgeId());
        
        return versionRepository.save(version);
    }

    @Override
    public List<PathwayVersion> listVersions(Long pathwayId) {
        Pathway pathway = pathwayRepository.findById(pathwayId).orElseThrow();
        return versionRepository.findByPathwayOrderByVersionNumberDesc(pathway);
    }

    @Override
    public void logAction(Organization organization, Pathway pathway, User user, String action, String entityType, Long entityId, String details) {
        AuditLog log = new AuditLog();
        log.setOrganization(organization);
        log.setPathway(pathway);
        log.setUser(user);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        auditRepository.save(log);
    }
}


