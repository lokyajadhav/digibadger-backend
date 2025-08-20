package com.taashee.badger.controllers;

import com.taashee.badger.services.PathwayAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Temporarily disabled until repository issues are fixed
// @RestController
// @RequestMapping("/api/pathway-analytics")
public class PathwayAnalyticsController {

    @Autowired
    private PathwayAnalyticsService pathwayAnalyticsService;

    // All methods temporarily commented out
    /*
    @GetMapping("/overview/{pathwayId}")
    public ResponseEntity<?> getPathwayOverview(@PathVariable Long pathwayId) {
        try {
            Map<String, Object> overview = pathwayAnalyticsService.getPathwayOverview(pathwayId);
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/enrollment-trends/{pathwayId}")
    public ResponseEntity<?> getEnrollmentTrends(@PathVariable Long pathwayId) {
        try {
            Map<String, Object> trends = pathwayAnalyticsService.getEnrollmentTrends(pathwayId);
            return ResponseEntity.ok(trends);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/completion-analytics/{pathwayId}")
    public ResponseEntity<?> getCompletionAnalytics(@PathVariable Long pathwayId) {
        try {
            Map<String, Object> analytics = pathwayAnalyticsService.getCompletionAnalytics(pathwayId);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/element-performance/{pathwayId}")
    public ResponseEntity<?> getElementPerformance(@PathVariable Long pathwayId) {
        try {
            Map<String, Object> performance = pathwayAnalyticsService.getElementPerformance(pathwayId);
            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user-engagement/{pathwayId}")
    public ResponseEntity<?> getUserEngagement(@PathVariable Long pathwayId) {
        try {
            Map<String, Object> engagement = pathwayAnalyticsService.getUserEngagement(pathwayId);
            return ResponseEntity.ok(engagement);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<?> getOrganizationAnalytics(@PathVariable Long organizationId) {
        try {
            Map<String, Object> analytics = pathwayAnalyticsService.getOrganizationAnalytics(organizationId);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/export/{pathwayId}")
    public ResponseEntity<?> exportPathwayAnalytics(@PathVariable Long pathwayId) {
        try {
            Map<String, Object> export = pathwayAnalyticsService.exportPathwayAnalytics(pathwayId);
            return ResponseEntity.ok(export);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/realtime/{pathwayId}")
    public ResponseEntity<?> getRealtimeAnalytics(@PathVariable Long pathwayId) {
        try {
            Map<String, Object> realtime = pathwayAnalyticsService.getRealtimeAnalytics(pathwayId);
            return ResponseEntity.ok(realtime);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    */
} 