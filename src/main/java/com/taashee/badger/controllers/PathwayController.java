package com.taashee.badger.controllers;

import com.taashee.badger.models.Organization;
import com.taashee.badger.models.Pathway;
import com.taashee.badger.models.PathwayStep;
import com.taashee.badger.models.StepRequirement;
import com.taashee.badger.repositories.OrganizationRepository;
import com.taashee.badger.services.PathwayService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;
import java.util.List;

@RestController
@RequestMapping("/api/organizations/{orgId}/pathways")
public class PathwayController {
    private final PathwayService pathwayService;
    private final OrganizationRepository organizationRepository;

    public PathwayController(PathwayService pathwayService, OrganizationRepository organizationRepository) {
        this.pathwayService = pathwayService;
        this.organizationRepository = organizationRepository;
    }

    private StepRequirementDto toStepRequirementDto(StepRequirement req) {
        return new StepRequirementDto(
            req.getId(),
            req.getType() != null ? req.getType().name() : null,
            req.getBadgeClassId(),
            req.getThirdPartyUrl(),
            req.getThirdPartyJson(),
            req.getExperienceName(),
            req.getExperienceDescription(),
            req.getGroupKey()
        );
    }

    private StepDto toStepDto(PathwayStep step) {
        List<StepRequirementDto> requirements = step.getRequirements().stream()
            .map(this::toStepRequirementDto)
            .collect(Collectors.toList());
            
        return new StepDto(
            step.getId(), 
            step.getParentStep() != null ? step.getParentStep().getId() : null, 
            step.getName(), 
            step.getDescription(),
            step.getShortCode(),
            step.isOptionalStep(),
            step.getOrderIndex(), 
            step.isMilestone(),
            step.getAchievementBadgeId(),
            step.getAchievementExternal(),
            requirements,
            step.getPrerequisiteRule(),
            step.getPrerequisiteSteps()
        );
    }

    @GetMapping
    public List<Pathway> list(@PathVariable Long orgId) {
        Organization org = organizationRepository.findById(orgId).orElseThrow();
        return pathwayService.listPathways(org);
    }

    public static record CreatePathwayRequest(@NotBlank String name, String description) {}

    @PostMapping
    public Pathway create(@PathVariable Long orgId, @RequestBody CreatePathwayRequest body) {
        Organization org = organizationRepository.findById(orgId).orElseThrow();
        return pathwayService.createPathway(org, body.name(), body.description());
    }

    @GetMapping("/{pathwayId}")
    public Pathway getPathway(@PathVariable Long orgId, @PathVariable Long pathwayId) {
        return pathwayService.getPathway(pathwayId);
    }

    @PutMapping("/{pathwayId}")
    public Pathway updatePathway(@PathVariable Long orgId, @PathVariable Long pathwayId, @RequestBody CreatePathwayRequest body) {
        return pathwayService.updatePathway(pathwayId, body.name(), body.description());
    }

    public static record PathwayConfigurationRequest(
        String shortCode, 
        String alignmentUrl, 
        String targetCode, 
        String frameworkName,
        Long completionBadgeId,
        Boolean completionBadgeExternal,
        String prerequisiteRule,
        String prerequisiteSteps
    ) {}

    @PutMapping("/{pathwayId}/configuration")
    public Pathway updatePathwayConfiguration(@PathVariable Long orgId, @PathVariable Long pathwayId, @RequestBody PathwayConfigurationRequest body) {
        return pathwayService.updatePathwayConfiguration(
            pathwayId, 
            body.shortCode(), 
            body.alignmentUrl(), 
            body.targetCode(), 
            body.frameworkName(),
            body.completionBadgeId(),
            body.completionBadgeExternal(),
            body.prerequisiteRule(),
            body.prerequisiteSteps()
        );
    }

    @DeleteMapping("/{pathwayId}")
    public void deletePathway(@PathVariable Long orgId, @PathVariable Long pathwayId) {
        pathwayService.deletePathway(pathwayId);
    }

    public static record CreateStepRequest(Long parentStepId, @NotBlank String name, String description, String shortCode, boolean optionalStep, boolean milestone) {}

    @PostMapping("/{pathwayId}/steps")
    public StepDto createStep(@PathVariable Long orgId, @PathVariable Long pathwayId, @RequestBody CreateStepRequest body) {
        // orgId used for scoping later; kept for parity
        var s = pathwayService.createStep(pathwayId, body.parentStepId(), body.name(), body.description(), body.shortCode(), body.optionalStep(), body.milestone());
        return toStepDto(s);
    }

    public static record StepRequirementDto(Long id, String type, Long badgeClassId, String thirdPartyUrl, String thirdPartyJson, String experienceName, String experienceDescription, String groupKey) {}
    
    public static record StepDto(Long id, Long parentStepId, String name, String description, String shortCode, boolean optionalStep, int orderIndex, boolean milestone, Long achievementBadgeId, Boolean achievementExternal, List<StepRequirementDto> requirements, String prerequisiteRule, String prerequisiteSteps) {}

    @GetMapping("/{pathwayId}/steps")
    public List<StepDto> listSteps(@PathVariable Long orgId, @PathVariable Long pathwayId) {
        return pathwayService
            .listSteps(pathwayId)
            .stream()
            .map(this::toStepDto)
            .collect(Collectors.toList());
    }

    @GetMapping("/{pathwayId}/steps/{stepId}")
    public StepDto getStep(@PathVariable Long orgId, @PathVariable Long pathwayId, @PathVariable Long stepId) {
        var step = pathwayService.getStep(pathwayId, stepId);
        return toStepDto(step);
    }

    public static record RearrangeRequest(Long newParentId, Integer newOrderIndex) {}

    @PostMapping("/{pathwayId}/steps/{stepId}:rearrange")
    public StepDto rearrange(@PathVariable Long orgId, @PathVariable Long pathwayId, @PathVariable Long stepId, @RequestBody RearrangeRequest body) {
        var s = pathwayService.rearrangeStep(pathwayId, stepId, body.newParentId(), body.newOrderIndex());
        return toStepDto(s);
    }

    public static record UpdateStepRequest(String name, String description, String shortCode, String alignmentUrl, String targetCode, String frameworkName, Boolean optional, String prerequisiteRule, String prerequisiteSteps) {}

    @PutMapping("/{pathwayId}/steps/{stepId}")
    public StepDto updateStep(@PathVariable Long orgId, @PathVariable Long pathwayId, @PathVariable Long stepId, @RequestBody UpdateStepRequest body) {
        var s = pathwayService.updateStep(pathwayId, stepId, body.name(), body.description(), body.shortCode(), body.alignmentUrl(), body.targetCode(), body.frameworkName(), body.optional(), body.prerequisiteRule(), body.prerequisiteSteps());
        return toStepDto(s);
    }

    public static record UpdateStepAchievementRequest(Long achievementBadgeId, Boolean achievementExternal) {}

    @PutMapping("/{pathwayId}/steps/{stepId}/achievement")
    public StepDto updateStepAchievement(@PathVariable Long orgId, @PathVariable Long pathwayId, @PathVariable Long stepId, @RequestBody UpdateStepAchievementRequest body) {
        var s = pathwayService.updateStepAchievement(pathwayId, stepId, body.achievementBadgeId(), body.achievementExternal());
        return toStepDto(s);
    }

    @DeleteMapping("/{pathwayId}/steps/{stepId}")
    public void deleteStep(@PathVariable Long orgId, @PathVariable Long pathwayId, @PathVariable Long stepId) {
        pathwayService.deleteStep(pathwayId, stepId);
    }

    // Step Requirements Management
    public static record CreateStepRequirementRequest(String type, Long badgeClassId, String thirdPartyUrl, String thirdPartyJson, String experienceName, String experienceDescription, String groupKey) {}

    @PostMapping("/{pathwayId}/steps/{stepId}/requirements")
    public StepRequirementDto createStepRequirement(@PathVariable Long orgId, @PathVariable Long pathwayId, @PathVariable Long stepId, @RequestBody CreateStepRequirementRequest body) {
        var req = pathwayService.createStepRequirement(pathwayId, stepId, body.type(), body.badgeClassId(), body.thirdPartyUrl(), body.thirdPartyJson(), body.experienceName(), body.experienceDescription(), body.groupKey());
        return toStepRequirementDto(req);
    }

    public static record UpdateStepRequirementRequest(String type, Long badgeClassId, String thirdPartyUrl, String thirdPartyJson, String experienceName, String experienceDescription, String groupKey) {}

    @PutMapping("/{pathwayId}/steps/{stepId}/requirements/{requirementId}")
    public StepRequirementDto updateStepRequirement(@PathVariable Long orgId, @PathVariable Long pathwayId, @PathVariable Long stepId, @PathVariable Long requirementId, @RequestBody UpdateStepRequirementRequest body) {
        var req = pathwayService.updateStepRequirement(pathwayId, stepId, requirementId, body.type(), body.badgeClassId(), body.thirdPartyUrl(), body.thirdPartyJson(), body.experienceName(), body.experienceDescription(), body.groupKey());
        return toStepRequirementDto(req);
    }

    @DeleteMapping("/{pathwayId}/steps/{stepId}/requirements/{requirementId}")
    public void deleteStepRequirement(@PathVariable Long orgId, @PathVariable Long pathwayId, @PathVariable Long stepId, @PathVariable Long requirementId) {
        pathwayService.deleteStepRequirement(pathwayId, stepId, requirementId);
    }
}


