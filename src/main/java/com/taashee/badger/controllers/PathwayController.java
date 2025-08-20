package com.taashee.badger.controllers;

import com.taashee.badger.models.PathwayDTO;
import com.taashee.badger.models.Pathway;
import com.taashee.badger.services.PathwayService;
import com.taashee.badger.models.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pathways")
public class PathwayController {

    @Autowired
    private PathwayService pathwayService;

    // Create a new pathway for the current user's organization
    @PostMapping
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<PathwayDTO>> createPathway(@RequestBody PathwayDTO pathwayDTO) {
        
        try {
            // Get current user's email from security context
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            PathwayDTO createdPathway = pathwayService.createPathwayForCurrentUser(pathwayDTO, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway created successfully", createdPathway, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to create pathway", null, e.getMessage()));
        }
    }

    // Get all pathways for the current user's organization
    @GetMapping
    @PreAuthorize("hasRole('ISSUER')")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<PathwayDTO>>> getPathwaysForCurrentUser() {
        
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<PathwayDTO> pathways = pathwayService.getPathwaysForCurrentUser(userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathways retrieved successfully", pathways, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve pathways", null, e.getMessage()));
        }
    }

    // Get a specific pathway by ID for the current user's organization
    @GetMapping("/{pathwayId}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<PathwayDTO>> getPathwayById(@PathVariable Long pathwayId) {
        
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Optional<PathwayDTO> pathway = pathwayService.getPathwayByIdForCurrentUser(pathwayId, userEmail);
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

    // Update a pathway for the current user's organization
    @PutMapping("/{pathwayId}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<PathwayDTO>> updatePathway(
            @PathVariable Long pathwayId,
            @RequestBody PathwayDTO pathwayDTO) {
        
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            PathwayDTO updatedPathway = pathwayService.updatePathwayForCurrentUser(pathwayId, pathwayDTO, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway updated successfully", updatedPathway, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to update pathway", null, e.getMessage()));
        }
    }

    // Delete a pathway for the current user's organization
    @DeleteMapping("/{pathwayId}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<Void>> deletePathway(@PathVariable Long pathwayId) {
        
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            pathwayService.deletePathwayForCurrentUser(pathwayId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway deleted successfully", null, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to delete pathway", null, e.getMessage()));
        }
    }

    // === ENTERPRISE-GRADE PUBLISHING ENDPOINTS ===

    // Publish a pathway
    @PostMapping("/{pathwayId}/publish")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<PathwayDTO>> publishPathway(@PathVariable Long pathwayId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            PathwayDTO publishedPathway = pathwayService.publishPathway(pathwayId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway published successfully", publishedPathway, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to publish pathway", null, e.getMessage()));
        }
    }

    // Unpublish a pathway
    @PostMapping("/{pathwayId}/unpublish")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<PathwayDTO>> unpublishPathway(@PathVariable Long pathwayId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            PathwayDTO unpublishedPathway = pathwayService.unpublishPathway(pathwayId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway unpublished successfully", unpublishedPathway, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to unpublish pathway", null, e.getMessage()));
        }
    }

    // Archive a pathway
    @PostMapping("/{pathwayId}/archive")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<PathwayDTO>> archivePathway(@PathVariable Long pathwayId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            PathwayDTO archivedPathway = pathwayService.archivePathway(pathwayId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway archived successfully", archivedPathway, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to archive pathway", null, e.getMessage()));
        }
    }

    // Validate pathway for publishing
    @GetMapping("/{pathwayId}/validate")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<List<String>>> validatePathway(@PathVariable Long pathwayId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<String> validationErrors = pathwayService.validatePathwayForPublishing(pathwayId, userEmail);
            if (validationErrors.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(200, "Pathway is valid for publishing", validationErrors, null));
            } else {
                return ResponseEntity.ok(new ApiResponse<>(400, "Pathway validation failed", validationErrors, null));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to validate pathway", null, e.getMessage()));
        }
    }

    // Get pathway validation status
    @GetMapping("/{pathwayId}/validation-status")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<PathwayDTO>> getPathwayValidationStatus(@PathVariable Long pathwayId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            PathwayDTO pathway = pathwayService.getPathwayValidationStatus(pathwayId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway validation status retrieved", pathway, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to get pathway validation status", null, e.getMessage()));
        }
    }

    // === ORGANIZATION-SCOPED ACCESS CONTROL ENDPOINTS ===

    // Get pathways by status for current user's organization
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<List<PathwayDTO>>> getPathwaysByStatus(@PathVariable String status) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long organizationId = pathwayService.getUserOrganizationId(userEmail);
            Pathway.PathwayStatus pathwayStatus = Pathway.PathwayStatus.valueOf(status.toUpperCase());
            List<PathwayDTO> pathways = pathwayService.getPathwaysByStatus(organizationId, pathwayStatus);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathways retrieved by status", pathways, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve pathways by status", null, e.getMessage()));
        }
    }

    // Get pathway statistics for current user's organization
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<PathwayDTO>> getPathwayStatistics() {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long organizationId = pathwayService.getUserOrganizationId(userEmail);
            PathwayDTO statistics = pathwayService.getPathwayStatistics(organizationId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway statistics retrieved", statistics, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve pathway statistics", null, e.getMessage()));
        }
    }

    // === STUDENT ACCESS ENDPOINTS ===

    // Get published pathways for student
    @GetMapping("/student/published")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<PathwayDTO>>> getPublishedPathwaysForStudent() {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<PathwayDTO> pathways = pathwayService.getPublishedPathwaysForStudent(userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Published pathways retrieved for student", pathways, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve published pathways", null, e.getMessage()));
        }
    }

    // Check if student can enroll in pathway
    @GetMapping("/{pathwayId}/can-enroll")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Boolean>> canEnrollInPathway(@PathVariable Long pathwayId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            boolean canEnroll = pathwayService.canStudentEnrollInPathway(pathwayId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Enrollment check completed", canEnroll, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to check enrollment eligibility", null, e.getMessage()));
        }
    }
} 