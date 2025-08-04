package com.taashee.badger.controllers;

import com.taashee.badger.models.*;
import com.taashee.badger.services.PathwayAnalyticsService;
import com.taashee.badger.models.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/pathway-analytics")
@CrossOrigin(origins = "*")
public class PathwayAnalyticsController {

    @Autowired
    private PathwayAnalyticsService pathwayAnalyticsService;

    // Get pathway overview analytics
    @GetMapping("/overview/{pathwayId}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPathwayOverview(@PathVariable Long pathwayId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Map<String, Object> overview = pathwayAnalyticsService.getPathwayOverview(pathwayId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway overview retrieved successfully", overview, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve pathway overview", null, e.getMessage()));
        }
    }

    // Get enrollment trends
    @GetMapping("/enrollment-trends/{pathwayId}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEnrollmentTrends(
            @PathVariable Long pathwayId,
            @RequestParam(required = false) String period) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Map<String, Object> trends = pathwayAnalyticsService.getEnrollmentTrends(pathwayId, period, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Enrollment trends retrieved successfully", trends, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve enrollment trends", null, e.getMessage()));
        }
    }

    // Get completion analytics
    @GetMapping("/completion-analytics/{pathwayId}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCompletionAnalytics(@PathVariable Long pathwayId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Map<String, Object> analytics = pathwayAnalyticsService.getCompletionAnalytics(pathwayId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Completion analytics retrieved successfully", analytics, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve completion analytics", null, e.getMessage()));
        }
    }

    // Get element performance analytics
    @GetMapping("/element-performance/{pathwayId}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getElementPerformance(@PathVariable Long pathwayId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Map<String, Object> performance = pathwayAnalyticsService.getElementPerformance(pathwayId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Element performance retrieved successfully", performance, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve element performance", null, e.getMessage()));
        }
    }

    // Get user engagement analytics
    @GetMapping("/user-engagement/{pathwayId}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserEngagement(@PathVariable Long pathwayId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Map<String, Object> engagement = pathwayAnalyticsService.getUserEngagement(pathwayId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "User engagement retrieved successfully", engagement, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve user engagement", null, e.getMessage()));
        }
    }

    // Get organization-wide pathway analytics
    @GetMapping("/organization/{organizationId}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrganizationPathwayAnalytics(@PathVariable Long organizationId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Map<String, Object> analytics = pathwayAnalyticsService.getOrganizationPathwayAnalytics(organizationId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Organization pathway analytics retrieved successfully", analytics, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve organization pathway analytics", null, e.getMessage()));
        }
    }

    // Export pathway analytics report
    @GetMapping("/export/{pathwayId}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<String>> exportPathwayReport(
            @PathVariable Long pathwayId,
            @RequestParam(required = false) String format) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String reportUrl = pathwayAnalyticsService.exportPathwayReport(pathwayId, format, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Pathway report exported successfully", reportUrl, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to export pathway report", null, e.getMessage()));
        }
    }

    // Get real-time pathway metrics
    @GetMapping("/realtime/{pathwayId}")
    @PreAuthorize("hasRole('ISSUER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRealTimeMetrics(@PathVariable Long pathwayId) {
        try {
            String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Map<String, Object> metrics = pathwayAnalyticsService.getRealTimeMetrics(pathwayId, userEmail);
            return ResponseEntity.ok(new ApiResponse<>(200, "Real-time metrics retrieved successfully", metrics, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve real-time metrics", null, e.getMessage()));
        }
    }
} 