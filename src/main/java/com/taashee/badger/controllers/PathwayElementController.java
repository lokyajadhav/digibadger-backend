package com.taashee.badger.controllers;

import com.taashee.badger.models.PathwayElementDTO;
import com.taashee.badger.services.PathwayElementService;
import com.taashee.badger.models.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/organizations/{organizationId}/pathways/{pathwayId}/elements")
@CrossOrigin(origins = "*")
public class PathwayElementController {

    @Autowired
    private PathwayElementService pathwayElementService;

    // Create a new pathway element
    @PostMapping
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<PathwayElementDTO>> createPathwayElement(
            @PathVariable Long organizationId,
            @PathVariable Long pathwayId,
            @RequestBody PathwayElementDTO elementDTO) {
        
        try {
            PathwayElementDTO createdElement = pathwayElementService.createPathwayElement(elementDTO, pathwayId, organizationId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway element created successfully", createdElement, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to create pathway element", null, e.getMessage()));
        }
    }

    // Get all elements for a pathway
    @GetMapping
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<List<PathwayElementDTO>>> getPathwayElements(
            @PathVariable Long organizationId,
            @PathVariable Long pathwayId) {
        
        try {
            List<PathwayElementDTO> elements = pathwayElementService.getPathwayElements(pathwayId, organizationId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway elements retrieved successfully", elements, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve pathway elements", null, e.getMessage()));
        }
    }

    // Get a specific pathway element by ID
    @GetMapping("/{elementId}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<PathwayElementDTO>> getPathwayElementById(
            @PathVariable Long organizationId,
            @PathVariable Long pathwayId,
            @PathVariable Long elementId) {
        
        try {
            Optional<PathwayElementDTO> element = pathwayElementService.getPathwayElementById(elementId, pathwayId, organizationId);
            if (element.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(200, "Pathway element retrieved successfully", element.get(), null));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve pathway element", null, e.getMessage()));
        }
    }

    // Update a pathway element
    @PutMapping("/{elementId}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<PathwayElementDTO>> updatePathwayElement(
            @PathVariable Long organizationId,
            @PathVariable Long pathwayId,
            @PathVariable Long elementId,
            @RequestBody PathwayElementDTO elementDTO) {
        
        try {
            PathwayElementDTO updatedElement = pathwayElementService.updatePathwayElement(elementId, elementDTO, pathwayId, organizationId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway element updated successfully", updatedElement, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to update pathway element", null, e.getMessage()));
        }
    }

    // Delete a pathway element
    @DeleteMapping("/{elementId}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<Void>> deletePathwayElement(
            @PathVariable Long organizationId,
            @PathVariable Long pathwayId,
            @PathVariable Long elementId) {
        
        try {
            pathwayElementService.deletePathwayElement(elementId, pathwayId, organizationId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway element deleted successfully", null, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to delete pathway element", null, e.getMessage()));
        }
    }

    // Add badge requirement to pathway element
    @PostMapping("/{elementId}/badge-requirements/{badgeClassId}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<Void>> addBadgeRequirement(
            @PathVariable Long organizationId,
            @PathVariable Long pathwayId,
            @PathVariable Long elementId,
            @PathVariable Long badgeClassId) {
        
        try {
            pathwayElementService.addBadgeRequirement(elementId, badgeClassId, pathwayId, organizationId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Badge requirement added successfully", null, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to add badge requirement", null, e.getMessage()));
        }
    }

    // Remove badge requirement from pathway element
    @DeleteMapping("/{elementId}/badge-requirements/{badgeClassId}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<Void>> removeBadgeRequirement(
            @PathVariable Long organizationId,
            @PathVariable Long pathwayId,
            @PathVariable Long elementId,
            @PathVariable Long badgeClassId) {
        
        try {
            pathwayElementService.removeBadgeRequirement(elementId, badgeClassId, pathwayId, organizationId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Badge requirement removed successfully", null, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to remove badge requirement", null, e.getMessage()));
        }
    }
} 