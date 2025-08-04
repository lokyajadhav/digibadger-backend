package com.taashee.badger.controllers;

import com.taashee.badger.models.*;
import com.taashee.badger.services.PathwayAuthoringService;
import com.taashee.badger.models.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pathway-authoring")
@CrossOrigin(origins = "*")
public class PathwayAuthoringController {

    @Autowired
    private PathwayAuthoringService pathwayAuthoringService;

    // Pathway Structure Management
    @GetMapping("/{pathwayId}/structure")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPathwayStructure(@PathVariable Long pathwayId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Map<String, Object> structure = pathwayAuthoringService.getPathwayStructure(pathwayId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway structure retrieved successfully", structure, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve pathway structure", null, e.getMessage()));
        }
    }

    // Element Management
    @PostMapping("/{pathwayId}/elements")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<PathwayElement>> createElement(
            @PathVariable Long pathwayId,
            @RequestBody PathwayElement element) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            PathwayElement createdElement = pathwayAuthoringService.createElement(pathwayId, element, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Element created successfully", createdElement, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to create element", null, e.getMessage()));
        }
    }

    @PutMapping("/{pathwayId}/elements/{elementId}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<PathwayElement>> updateElement(
            @PathVariable Long pathwayId,
            @PathVariable Long elementId,
            @RequestBody PathwayElement element) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            PathwayElement updatedElement = pathwayAuthoringService.updateElement(pathwayId, elementId, element, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Element updated successfully", updatedElement, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to update element", null, e.getMessage()));
        }
    }

    @DeleteMapping("/{pathwayId}/elements/{elementId}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<Void>> deleteElement(
            @PathVariable Long pathwayId,
            @PathVariable Long elementId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            pathwayAuthoringService.deleteElement(pathwayId, elementId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Element deleted successfully", null, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to delete element", null, e.getMessage()));
        }
    }

    // Element Hierarchy Management
    @PostMapping("/{pathwayId}/elements/{elementId}/move")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<PathwayElement>> moveElement(
            @PathVariable Long pathwayId,
            @PathVariable Long elementId,
            @RequestBody Map<String, Object> moveRequest) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long newParentId = moveRequest.get("newParentId") != null ? Long.valueOf(moveRequest.get("newParentId").toString()) : null;
            Integer newOrderIndex = moveRequest.get("newOrderIndex") != null ? Integer.valueOf(moveRequest.get("newOrderIndex").toString()) : null;
            
            PathwayElement movedElement = pathwayAuthoringService.moveElement(pathwayId, elementId, newParentId, newOrderIndex, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Element moved successfully", movedElement, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to move element", null, e.getMessage()));
        }
    }

    @PostMapping("/{pathwayId}/elements/reorder")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<List<PathwayElement>>> reorderElements(
            @PathVariable Long pathwayId,
            @RequestBody List<Map<String, Object>> reorderRequest) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<PathwayElement> reorderedElements = pathwayAuthoringService.reorderElements(pathwayId, reorderRequest, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Elements reordered successfully", reorderedElements, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to reorder elements", null, e.getMessage()));
        }
    }

    // Badge Integration
    @PostMapping("/{pathwayId}/elements/{elementId}/badges")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<PathwayElementBadge>> addBadgeToElement(
            @PathVariable Long pathwayId,
            @PathVariable Long elementId,
            @RequestBody PathwayElementBadge badge) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            PathwayElementBadge addedBadge = pathwayAuthoringService.addBadgeToElement(pathwayId, elementId, badge, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Badge added to element successfully", addedBadge, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to add badge to element", null, e.getMessage()));
        }
    }

    @DeleteMapping("/{pathwayId}/elements/{elementId}/badges/{badgeId}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<Void>> removeBadgeFromElement(
            @PathVariable Long pathwayId,
            @PathVariable Long elementId,
            @PathVariable Long badgeId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            pathwayAuthoringService.removeBadgeFromElement(pathwayId, elementId, badgeId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Badge removed from element successfully", null, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to remove badge from element", null, e.getMessage()));
        }
    }

    // Badge Search and Discovery
    @GetMapping("/badges/search")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> searchBadges(
            @RequestParam String query,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String framework) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<Map<String, Object>> badges = pathwayAuthoringService.searchBadges(query, source, framework, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Badge search completed successfully", badges, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to search badges", null, e.getMessage()));
        }
    }

    @GetMapping("/badges/internal")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<List<BadgeClass>>> getInternalBadges(
            @RequestParam(required = false) Long organizationId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<BadgeClass> badges = pathwayAuthoringService.getInternalBadges(organizationId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Internal badges retrieved successfully", badges, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve internal badges", null, e.getMessage()));
        }
    }

    // External Badge Management
    @PostMapping("/{pathwayId}/elements/{elementId}/badges/external")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<PathwayElementBadge>> addExternalBadge(
            @PathVariable Long pathwayId,
            @PathVariable Long elementId,
            @RequestBody Map<String, Object> externalBadgeData) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            PathwayElementBadge externalBadge = pathwayAuthoringService.addExternalBadge(pathwayId, elementId, externalBadgeData, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "External badge added successfully", externalBadge, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to add external badge", null, e.getMessage()));
        }
    }

    @PostMapping("/badges/external/verify/{badgeId}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<PathwayElementBadge>> verifyExternalBadge(
            @PathVariable Long badgeId,
            @RequestBody Map<String, Object> verificationData) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String notes = (String) verificationData.get("notes");
            PathwayElementBadge verifiedBadge = pathwayAuthoringService.verifyExternalBadge(badgeId, notes, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "External badge verified successfully", verifiedBadge, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to verify external badge", null, e.getMessage()));
        }
    }

    // Competency Alignment
    @PostMapping("/{pathwayId}/elements/{elementId}/competencies")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<PathwayElement>> addCompetencyAlignment(
            @PathVariable Long pathwayId,
            @PathVariable Long elementId,
            @RequestBody PathwayElement.CompetencyAlignment competency) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            PathwayElement updatedElement = pathwayAuthoringService.addCompetencyAlignment(pathwayId, elementId, competency, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Competency alignment added successfully", updatedElement, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to add competency alignment", null, e.getMessage()));
        }
    }

    @GetMapping("/competency-frameworks")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCompetencyFrameworks() {
        try {
            List<Map<String, Object>> frameworks = pathwayAuthoringService.getCompetencyFrameworks();
            return ResponseEntity.ok(new ApiResponse<>(200, "Competency frameworks retrieved successfully", frameworks, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve competency frameworks", null, e.getMessage()));
        }
    }

    // Pathway Validation
    @GetMapping("/{pathwayId}/validation")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validatePathway(@PathVariable Long pathwayId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Map<String, Object> validation = pathwayAuthoringService.validatePathway(pathwayId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway validation completed", validation, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to validate pathway", null, e.getMessage()));
        }
    }

    // Pathway Publishing
    @PostMapping("/{pathwayId}/publish")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<Pathway>> publishPathway(@PathVariable Long pathwayId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Pathway publishedPathway = pathwayAuthoringService.publishPathway(pathwayId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway published successfully", publishedPathway, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to publish pathway", null, e.getMessage()));
        }
    }

    @PostMapping("/{pathwayId}/unpublish")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<Pathway>> unpublishPathway(@PathVariable Long pathwayId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Pathway unpublishedPathway = pathwayAuthoringService.unpublishPathway(pathwayId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway unpublished successfully", unpublishedPathway, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to unpublish pathway", null, e.getMessage()));
        }
    }

    // Pathway Templates
    @GetMapping("/templates")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<List<Pathway>>> getPathwayTemplates() {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<Pathway> templates = pathwayAuthoringService.getPathwayTemplates(userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway templates retrieved successfully", templates, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve pathway templates", null, e.getMessage()));
        }
    }

    @PostMapping("/{pathwayId}/save-as-template")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<Pathway>> saveAsTemplate(
            @PathVariable Long pathwayId,
            @RequestBody Map<String, Object> templateData) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String templateName = (String) templateData.get("templateName");
            String templateCategory = (String) templateData.get("templateCategory");
            
            Pathway template = pathwayAuthoringService.saveAsTemplate(pathwayId, templateName, templateCategory, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway saved as template successfully", template, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to save pathway as template", null, e.getMessage()));
        }
    }

    // Export/Import
    @GetMapping("/{pathwayId}/export")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> exportPathway(@PathVariable Long pathwayId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Map<String, Object> exportedPathway = pathwayAuthoringService.exportPathway(pathwayId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway exported successfully", exportedPathway, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to export pathway", null, e.getMessage()));
        }
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<Pathway>> importPathway(
            @RequestBody Map<String, Object> pathwayData,
            @RequestParam Long organizationId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Pathway importedPathway = pathwayAuthoringService.importPathway(pathwayData, organizationId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway imported successfully", importedPathway, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to import pathway", null, e.getMessage()));
        }
    }
} 