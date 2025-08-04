package com.taashee.badger.controllers;

import com.taashee.badger.models.ApiResponse;
import com.taashee.badger.models.PathwayElementBadgeDTO;
import com.taashee.badger.services.PathwayElementBadgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations/{organizationId}/pathways/{pathwayId}/elements/{elementId}/badges")
public class PathwayElementBadgeController {

    @Autowired
    private PathwayElementBadgeService pathwayElementBadgeService;

    /**
     * Get all badges for a pathway element
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PathwayElementBadgeDTO>>> getBadgesForElement(
            @PathVariable Long organizationId,
            @PathVariable Long pathwayId,
            @PathVariable Long elementId) {
        try {
            List<PathwayElementBadgeDTO> badges = pathwayElementBadgeService.getBadgesForElement(elementId, organizationId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Badges retrieved successfully", badges, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Failed to retrieve badges", null, e.getMessage()));
        }
    }

    /**
     * Get required badges for a pathway element
     */
    @GetMapping("/required")
    public ResponseEntity<ApiResponse<List<PathwayElementBadgeDTO>>> getRequiredBadgesForElement(
            @PathVariable Long organizationId,
            @PathVariable Long pathwayId,
            @PathVariable Long elementId) {
        try {
            List<PathwayElementBadgeDTO> requiredBadges = pathwayElementBadgeService.getRequiredBadgesForElement(elementId, organizationId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Required badges retrieved successfully", requiredBadges, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Failed to retrieve required badges", null, e.getMessage()));
        }
    }

    /**
     * Add a badge to a pathway element
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PathwayElementBadgeDTO>> addBadgeToElement(
            @PathVariable Long organizationId,
            @PathVariable Long pathwayId,
            @PathVariable Long elementId,
            @RequestBody PathwayElementBadgeDTO badgeDTO) {
        try {
            PathwayElementBadgeDTO addedBadge = pathwayElementBadgeService.addBadgeToElement(
                    elementId, badgeDTO.getBadgeClassId(), organizationId, badgeDTO);
            return ResponseEntity.ok(new ApiResponse<>(200, "Badge added to element successfully", addedBadge, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Failed to add badge to element", null, e.getMessage()));
        }
    }

    /**
     * Update a badge-element relationship
     */
    @PutMapping("/{badgeClassId}")
    public ResponseEntity<ApiResponse<PathwayElementBadgeDTO>> updateBadgeElementRelationship(
            @PathVariable Long organizationId,
            @PathVariable Long pathwayId,
            @PathVariable Long elementId,
            @PathVariable Long badgeClassId,
            @RequestBody PathwayElementBadgeDTO badgeDTO) {
        try {
            PathwayElementBadgeDTO updatedBadge = pathwayElementBadgeService.updateBadgeElementRelationship(
                    elementId, badgeClassId, organizationId, badgeDTO);
            return ResponseEntity.ok(new ApiResponse<>(200, "Badge-element relationship updated successfully", updatedBadge, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Failed to update badge-element relationship", null, e.getMessage()));
        }
    }

    /**
     * Remove a badge from a pathway element
     */
    @DeleteMapping("/{badgeClassId}")
    public ResponseEntity<ApiResponse<Void>> removeBadgeFromElement(
            @PathVariable Long organizationId,
            @PathVariable Long pathwayId,
            @PathVariable Long elementId,
            @PathVariable Long badgeClassId) {
        try {
            pathwayElementBadgeService.removeBadgeFromElement(elementId, badgeClassId, organizationId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Badge removed from element successfully", null, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Failed to remove badge from element", null, e.getMessage()));
        }
    }

    /**
     * Add an external badge to a pathway element
     */
    @PostMapping("/external")
    public ResponseEntity<ApiResponse<PathwayElementBadgeDTO>> addExternalBadgeToElement(
            @PathVariable Long organizationId,
            @PathVariable Long pathwayId,
            @PathVariable Long elementId,
            @RequestBody PathwayElementBadgeDTO externalBadgeDTO) {
        try {
            PathwayElementBadgeDTO addedExternalBadge = pathwayElementBadgeService.addExternalBadgeToElement(
                    elementId, organizationId, externalBadgeDTO);
            return ResponseEntity.ok(new ApiResponse<>(200, "External badge added to element successfully", addedExternalBadge, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Failed to add external badge to element", null, e.getMessage()));
        }
    }

    /**
     * Check if a badge is associated with an element
     */
    @GetMapping("/{badgeClassId}/check")
    public ResponseEntity<ApiResponse<Boolean>> isBadgeAssociatedWithElement(
            @PathVariable Long organizationId,
            @PathVariable Long pathwayId,
            @PathVariable Long elementId,
            @PathVariable Long badgeClassId) {
        try {
            boolean isAssociated = pathwayElementBadgeService.isBadgeAssociatedWithElement(elementId, badgeClassId, organizationId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Association check completed", isAssociated, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Failed to check badge association", null, e.getMessage()));
        }
    }

    /**
     * Get badge count for an element
     */
    @GetMapping("/count/required")
    public ResponseEntity<ApiResponse<Long>> countRequiredBadgesForElement(
            @PathVariable Long organizationId,
            @PathVariable Long pathwayId,
            @PathVariable Long elementId) {
        try {
            long requiredCount = pathwayElementBadgeService.countRequiredBadgesForElement(elementId, organizationId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Required badge count retrieved", requiredCount, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Failed to get required badge count", null, e.getMessage()));
        }
    }

    /**
     * Validate a badge-element relationship
     */
    @GetMapping("/{badgeClassId}/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateBadgeElementRelationship(
            @PathVariable Long organizationId,
            @PathVariable Long pathwayId,
            @PathVariable Long elementId,
            @PathVariable Long badgeClassId) {
        try {
            boolean isValid = pathwayElementBadgeService.validateBadgeElementRelationship(elementId, badgeClassId, organizationId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Validation completed", isValid, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Failed to validate relationship", null, e.getMessage()));
        }
    }
} 