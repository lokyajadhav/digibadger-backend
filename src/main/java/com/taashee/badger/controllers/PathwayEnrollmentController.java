package com.taashee.badger.controllers;

import com.taashee.badger.models.*;
import com.taashee.badger.services.PathwayEnrollmentService;
import com.taashee.badger.models.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pathway-enrollment")
public class PathwayEnrollmentController {

    @Autowired
    private PathwayEnrollmentService pathwayEnrollmentService;

    // Enroll a user in a pathway
    @PostMapping("/enroll/{pathwayId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PathwayProgress>> enrollInPathway(@PathVariable Long pathwayId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            PathwayProgress enrollment = pathwayEnrollmentService.enrollUserInPathway(pathwayId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Successfully enrolled in pathway", enrollment, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to enroll in pathway", null, e.getMessage()));
        }
    }

    // Unenroll a user from a pathway
    @DeleteMapping("/unenroll/{pathwayId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> unenrollFromPathway(@PathVariable Long pathwayId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            pathwayEnrollmentService.unenrollUserFromPathway(pathwayId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Successfully unenrolled from pathway", null, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to unenroll from pathway", null, e.getMessage()));
        }
    }

    // Get user's pathway progress
    @GetMapping("/progress/{pathwayId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PathwayProgress>> getPathwayProgress(@PathVariable Long pathwayId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Optional<PathwayProgress> progress = pathwayEnrollmentService.getUserPathwayProgress(pathwayId, userEmail);
            if (progress.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(200, "Pathway progress retrieved successfully", progress.get(), null));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve pathway progress", null, e.getMessage()));
        }
    }

    // Get all pathways user is enrolled in
    @GetMapping("/my-enrollments")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<PathwayProgress>>> getMyEnrollments() {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<PathwayProgress> enrollments = pathwayEnrollmentService.getUserEnrollments(userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Enrollments retrieved successfully", enrollments, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve enrollments", null, e.getMessage()));
        }
    }

    // Update element progress
    @PostMapping("/progress/{pathwayId}/elements/{elementId}/complete")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PathwayElementProgress>> completeElement(
            @PathVariable Long pathwayId,
            @PathVariable Long elementId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            PathwayElementProgress progress = pathwayEnrollmentService.completeElement(pathwayId, elementId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Element completed successfully", progress, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to complete element", null, e.getMessage()));
        }
    }

    // Get available pathways for enrollment
    @GetMapping("/available")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<Pathway>>> getAvailablePathways() {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<Pathway> availablePathways = pathwayEnrollmentService.getAvailablePathways(userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Available pathways retrieved successfully", availablePathways, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve available pathways", null, e.getMessage()));
        }
    }

    // Get pathway analytics (for organization staff)
    @GetMapping("/analytics/{pathwayId}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<Object>> getPathwayAnalytics(@PathVariable Long pathwayId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Object analytics = pathwayEnrollmentService.getPathwayAnalytics(pathwayId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway analytics retrieved successfully", analytics, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve pathway analytics", null, e.getMessage()));
        }
    }

    // Removed bulk enroll endpoint - simplified approach
} 