package com.taashee.badger.controllers;

import com.taashee.badger.models.PathwayDTO;
import com.taashee.badger.services.PathwayService;
import com.taashee.badger.models.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/organizations/{organizationId}/pathways")
@CrossOrigin(origins = "*")
public class PathwayController {

    @Autowired
    private PathwayService pathwayService;

    // Create a new pathway for an organization
    @PostMapping
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<PathwayDTO>> createPathway(
            @PathVariable Long organizationId,
            @RequestBody PathwayDTO pathwayDTO) {
        
        try {
            PathwayDTO createdPathway = pathwayService.createPathway(pathwayDTO, organizationId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway created successfully", createdPathway, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to create pathway", null, e.getMessage()));
        }
    }

    // Get all pathways for an organization
    @GetMapping
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<List<PathwayDTO>>> getPathwaysByOrganization(
            @PathVariable Long organizationId) {
        
        try {
            List<PathwayDTO> pathways = pathwayService.getPathwaysByOrganization(organizationId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathways retrieved successfully", pathways, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve pathways", null, e.getMessage()));
        }
    }

    // Get a specific pathway by ID
    @GetMapping("/{pathwayId}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<PathwayDTO>> getPathwayById(
            @PathVariable Long organizationId,
            @PathVariable Long pathwayId) {
        
        try {
            Optional<PathwayDTO> pathway = pathwayService.getPathwayById(pathwayId, organizationId);
            if (pathway.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(200, "Pathway retrieved successfully", pathway.get(), null));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve pathway", null, e.getMessage()));
        }
    }

    // Update a pathway
    @PutMapping("/{pathwayId}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<PathwayDTO>> updatePathway(
            @PathVariable Long organizationId,
            @PathVariable Long pathwayId,
            @RequestBody PathwayDTO pathwayDTO) {
        
        try {
            PathwayDTO updatedPathway = pathwayService.updatePathway(pathwayId, pathwayDTO, organizationId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway updated successfully", updatedPathway, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to update pathway", null, e.getMessage()));
        }
    }

    // Delete a pathway
    @DeleteMapping("/{pathwayId}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<Void>> deletePathway(
            @PathVariable Long organizationId,
            @PathVariable Long pathwayId) {
        
        try {
            pathwayService.deletePathway(pathwayId, organizationId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway deleted successfully", null, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to delete pathway", null, e.getMessage()));
        }
    }

    // Get available pathways for users
    @GetMapping("/available")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<PathwayDTO>>> getAvailablePathways(
            @PathVariable Long organizationId) {
        
        try {
            // TODO: Get current user ID from security context
            Long userId = 1L; // Placeholder
            List<PathwayDTO> pathways = pathwayService.getAvailablePathwaysForUser(userId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Available pathways retrieved successfully", pathways, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve available pathways", null, e.getMessage()));
        }
    }

    // Enroll user in a pathway
    @PostMapping("/{pathwayId}/enroll")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> enrollInPathway(
            @PathVariable Long organizationId,
            @PathVariable Long pathwayId) {
        
        try {
            // TODO: Get current user ID from security context
            Long userId = 1L; // Placeholder
            pathwayService.enrollUserInPathway(pathwayId, userId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Successfully enrolled in pathway", null, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to enroll in pathway", null, e.getMessage()));
        }
    }

    // Get pathway progress for a user
    @GetMapping("/{pathwayId}/progress")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PathwayDTO>> getPathwayProgress(
            @PathVariable Long organizationId,
            @PathVariable Long pathwayId) {
        
        try {
            // TODO: Get current user ID from security context
            Long userId = 1L; // Placeholder
            PathwayDTO progress = pathwayService.getPathwayProgress(pathwayId, userId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway progress retrieved successfully", progress, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve pathway progress", null, e.getMessage()));
        }
    }
} 