package com.taashee.badger.controllers;

import com.taashee.badger.models.Organization;
import com.taashee.badger.models.Pathway;
import com.taashee.badger.models.PathwayStep;
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

    @DeleteMapping("/{pathwayId}")
    public void deletePathway(@PathVariable Long orgId, @PathVariable Long pathwayId) {
        pathwayService.deletePathway(pathwayId);
    }

    public static record CreateStepRequest(Long parentStepId, @NotBlank String name, String description, String shortCode, boolean optionalStep, boolean milestone) {}

    @PostMapping("/{pathwayId}/steps")
    public StepDto createStep(@PathVariable Long orgId, @PathVariable Long pathwayId, @RequestBody CreateStepRequest body) {
        // orgId used for scoping later; kept for parity
        var s = pathwayService.createStep(pathwayId, body.parentStepId(), body.name(), body.description(), body.shortCode(), body.optionalStep(), body.milestone());
        return new StepDto(
            s.getId(), 
            s.getParentStep() != null ? s.getParentStep().getId() : null, 
            s.getName(), 
            s.getDescription(),
            s.getShortCode(),
            s.isOptionalStep(),
            s.getOrderIndex(), 
            s.isMilestone()
        );
    }

    public static record StepDto(Long id, Long parentStepId, String name, String description, String shortCode, boolean optionalStep, int orderIndex, boolean milestone) {}

    @GetMapping("/{pathwayId}/steps")
    public List<StepDto> listSteps(@PathVariable Long orgId, @PathVariable Long pathwayId) {
        return pathwayService
            .listSteps(pathwayId)
            .stream()
            .map(s -> new StepDto(
                s.getId(), 
                s.getParentStep() != null ? s.getParentStep().getId() : null, 
                s.getName(), 
                s.getDescription(),
                s.getShortCode(),
                s.isOptionalStep(),
                s.getOrderIndex(), 
                s.isMilestone()
            ))
            .collect(Collectors.toList());
    }

    public static record RearrangeRequest(Long newParentId, Integer newOrderIndex) {}

    @PostMapping("/{pathwayId}/steps/{stepId}:rearrange")
    public StepDto rearrange(@PathVariable Long orgId, @PathVariable Long pathwayId, @PathVariable Long stepId, @RequestBody RearrangeRequest body) {
        var s = pathwayService.rearrangeStep(pathwayId, stepId, body.newParentId(), body.newOrderIndex());
        return new StepDto(
            s.getId(), 
            s.getParentStep() != null ? s.getParentStep().getId() : null, 
            s.getName(), 
            s.getDescription(),
            s.getShortCode(),
            s.isOptionalStep(),
            s.getOrderIndex(), 
            s.isMilestone()
        );
    }

    public static record UpdateStepRequest(String name, String description, String shortCode, String alignmentUrl, String targetCode, String frameworkName, Boolean optional) {}

    @PutMapping("/steps/{stepId}")
    public StepDto updateStep(@PathVariable Long orgId, @PathVariable Long pathwayId, @PathVariable Long stepId, @RequestBody UpdateStepRequest body) {
        var s = pathwayService.updateStep(pathwayId, stepId, body.name(), body.description(), body.shortCode(), body.alignmentUrl(), body.targetCode(), body.frameworkName(), body.optional());
        return new StepDto(
            s.getId(), 
            s.getParentStep() != null ? s.getParentStep().getId() : null, 
            s.getName(), 
            s.getDescription(),
            s.getShortCode(),
            s.isOptionalStep(),
            s.getOrderIndex(), 
            s.isMilestone()
        );
    }

    @DeleteMapping("/steps/{stepId}")
    public void deleteStep(@PathVariable Long orgId, @PathVariable Long pathwayId, @PathVariable Long stepId) {
        pathwayService.deleteStep(pathwayId, stepId);
    }
}


