package com.taashee.badger.controllers;

import com.taashee.badger.models.OrganizationApiConfig;
import com.taashee.badger.services.OrganizationApiIntegrationService;
import com.taashee.badger.models.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/organization/api-config")
@CrossOrigin(origins = "*")
@Tag(name = "Organization API Configuration", description = "APIs for managing organization API integrations for 2-way sync. ISSUER permission required. Author: Enterprise Badger")
public class OrganizationApiConfigController {

    @Autowired
    private OrganizationApiIntegrationService apiIntegrationService;

    // === API CONFIGURATION MANAGEMENT ===

    @Operation(summary = "Configure API settings for organization", description = "ISSUER: Configure API settings for 2-way sync with organization systems.")
    @PreAuthorize("hasRole('ISSUER')")
    @PostMapping("/configure")
    public ResponseEntity<ApiResponse<OrganizationApiConfig>> configureApiSettings(
            @RequestParam Long organizationId,
            @RequestBody OrganizationApiConfig config) {
        try {
            OrganizationApiConfig savedConfig = apiIntegrationService.configureApiSettings(organizationId, config);
            return ResponseEntity.ok(new ApiResponse<>(200, "API configuration saved successfully", savedConfig, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to configure API settings", null, e.getMessage()));
        }
    }

    @Operation(summary = "Get API configuration for organization", description = "ISSUER: Get API configuration for the organization.")
    @PreAuthorize("hasRole('ISSUER')")
    @GetMapping("/{organizationId}")
    public ResponseEntity<ApiResponse<OrganizationApiConfig>> getApiConfiguration(@PathVariable Long organizationId) {
        try {
            Optional<OrganizationApiConfig> config = apiIntegrationService.getApiConfiguration(organizationId);
            if (config.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(200, "API configuration retrieved successfully", config.get(), null));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve API configuration", null, e.getMessage()));
        }
    }

    @Operation(summary = "Test API connection", description = "ISSUER: Test the API connection for the organization.")
    @PreAuthorize("hasRole('ISSUER')")
    @PostMapping("/{organizationId}/test")
    public ResponseEntity<ApiResponse<Boolean>> testApiConnection(@PathVariable Long organizationId) {
        try {
            boolean isConnected = apiIntegrationService.testApiConnection(organizationId);
            String message = isConnected ? "API connection successful" : "API connection failed";
            return ResponseEntity.ok(new ApiResponse<>(200, message, isConnected, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to test API connection", null, e.getMessage()));
        }
    }

    @Operation(summary = "Update API configuration", description = "ISSUER: Update existing API configuration for the organization.")
    @PreAuthorize("hasRole('ISSUER')")
    @PutMapping("/{organizationId}")
    public ResponseEntity<ApiResponse<OrganizationApiConfig>> updateApiConfiguration(
            @PathVariable Long organizationId,
            @RequestBody OrganizationApiConfig config) {
        try {
            OrganizationApiConfig updatedConfig = apiIntegrationService.updateApiConfiguration(organizationId, config);
            return ResponseEntity.ok(new ApiResponse<>(200, "API configuration updated successfully", updatedConfig, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to update API configuration", null, e.getMessage()));
        }
    }

    @Operation(summary = "Delete API configuration", description = "ISSUER: Delete API configuration for the organization.")
    @PreAuthorize("hasRole('ISSUER')")
    @DeleteMapping("/{organizationId}")
    public ResponseEntity<ApiResponse<Void>> deleteApiConfiguration(@PathVariable Long organizationId) {
        try {
            apiIntegrationService.deleteApiConfiguration(organizationId);
            return ResponseEntity.ok(new ApiResponse<>(200, "API configuration deleted successfully", null, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to delete API configuration", null, e.getMessage()));
        }
    }

    // === SYNC STATUS AND MONITORING ===

    @Operation(summary = "Get sync status", description = "ISSUER: Get current sync status for the organization.")
    @PreAuthorize("hasRole('ISSUER')")
    @GetMapping("/{organizationId}/sync-status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSyncStatus(@PathVariable Long organizationId) {
        try {
            Map<String, Object> syncStatus = apiIntegrationService.getSyncStatus(organizationId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Sync status retrieved successfully", syncStatus, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve sync status", null, e.getMessage()));
        }
    }

    @Operation(summary = "Get sync history", description = "ISSUER: Get sync history for the organization.")
    @PreAuthorize("hasRole('ISSUER')")
    @GetMapping("/{organizationId}/sync-history")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSyncHistory(@PathVariable Long organizationId) {
        try {
            List<Map<String, Object>> syncHistory = apiIntegrationService.getSyncHistory(organizationId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Sync history retrieved successfully", syncHistory, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve sync history", null, e.getMessage()));
        }
    }

    @Operation(summary = "Get API health status", description = "ISSUER: Get API health status for the organization.")
    @PreAuthorize("hasRole('ISSUER')")
    @GetMapping("/{organizationId}/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getApiHealthStatus(@PathVariable Long organizationId) {
        try {
            Map<String, Object> healthStatus = apiIntegrationService.getApiHealthStatus(organizationId);
            return ResponseEntity.ok(new ApiResponse<>(200, "API health status retrieved successfully", healthStatus, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve API health status", null, e.getMessage()));
        }
    }

    @Operation(summary = "Retry failed sync", description = "ISSUER: Retry a failed sync operation.")
    @PreAuthorize("hasRole('ISSUER')")
    @PostMapping("/{organizationId}/retry-sync/{syncId}")
    public ResponseEntity<ApiResponse<Boolean>> retryFailedSync(
            @PathVariable Long organizationId,
            @PathVariable String syncId) {
        try {
            boolean retrySuccess = apiIntegrationService.retryFailedSync(organizationId, syncId);
            String message = retrySuccess ? "Sync retry initiated successfully" : "Sync retry failed";
            return ResponseEntity.ok(new ApiResponse<>(200, message, retrySuccess, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retry sync", null, e.getMessage()));
        }
    }

    // === COURSE AND ASSIGNMENT INTEGRATION ===

    @Operation(summary = "Get available courses", description = "ISSUER: Get available courses from organization system.")
    @PreAuthorize("hasRole('ISSUER')")
    @GetMapping("/{organizationId}/courses")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAvailableCourses(@PathVariable Long organizationId) {
        try {
            List<Map<String, Object>> courses = apiIntegrationService.getAvailableCourses(organizationId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Available courses retrieved successfully", courses, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve available courses", null, e.getMessage()));
        }
    }

    @Operation(summary = "Get course details", description = "ISSUER: Get details of a specific course from organization system.")
    @PreAuthorize("hasRole('ISSUER')")
    @GetMapping("/{organizationId}/courses/{courseId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCourseDetails(
            @PathVariable Long organizationId,
            @PathVariable String courseId) {
        try {
            Optional<Map<String, Object>> courseDetails = apiIntegrationService.getCourseDetails(organizationId, courseId);
            if (courseDetails.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(200, "Course details retrieved successfully", courseDetails.get(), null));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve course details", null, e.getMessage()));
        }
    }

    @Operation(summary = "Get assignment details", description = "ISSUER: Get details of a specific assignment from organization system.")
    @PreAuthorize("hasRole('ISSUER')")
    @GetMapping("/{organizationId}/assignments/{assignmentId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAssignmentDetails(
            @PathVariable Long organizationId,
            @PathVariable String assignmentId) {
        try {
            Optional<Map<String, Object>> assignmentDetails = apiIntegrationService.getAssignmentDetails(organizationId, assignmentId);
            if (assignmentDetails.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(200, "Assignment details retrieved successfully", assignmentDetails.get(), null));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve assignment details", null, e.getMessage()));
        }
    }

    @Operation(summary = "Get test details", description = "ISSUER: Get details of a specific test from organization system.")
    @PreAuthorize("hasRole('ISSUER')")
    @GetMapping("/{organizationId}/tests/{testId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTestDetails(
            @PathVariable Long organizationId,
            @PathVariable String testId) {
        try {
            Optional<Map<String, Object>> testDetails = apiIntegrationService.getTestDetails(organizationId, testId);
            if (testDetails.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(200, "Test details retrieved successfully", testDetails.get(), null));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve test details", null, e.getMessage()));
        }
    }

    // === BADGE INTEGRATION ===

    @Operation(summary = "Get available badges", description = "ISSUER: Get available badges from organization system.")
    @PreAuthorize("hasRole('ISSUER')")
    @GetMapping("/{organizationId}/badges")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAvailableBadges(@PathVariable Long organizationId) {
        try {
            List<Map<String, Object>> badges = apiIntegrationService.getAvailableBadges(organizationId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Available badges retrieved successfully", badges, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to retrieve available badges", null, e.getMessage()));
        }
    }

    @Operation(summary = "Issue badge through organization", description = "ISSUER: Issue a badge through the organization system.")
    @PreAuthorize("hasRole('ISSUER')")
    @PostMapping("/{organizationId}/badges/{badgeId}/issue")
    public ResponseEntity<ApiResponse<Boolean>> issueBadgeThroughOrganization(
            @PathVariable Long organizationId,
            @PathVariable String badgeId,
            @RequestParam Long userId,
            @RequestBody Map<String, Object> badgeData) {
        try {
            boolean issued = apiIntegrationService.issueBadgeThroughOrganization(organizationId, badgeId, userId, badgeData);
            String message = issued ? "Badge issued successfully" : "Failed to issue badge";
            return ResponseEntity.ok(new ApiResponse<>(200, message, issued, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to issue badge", null, e.getMessage()));
        }
    }

    @Operation(summary = "Verify badge through organization", description = "ISSUER: Verify a badge through the organization system.")
    @PreAuthorize("hasRole('ISSUER')")
    @PostMapping("/{organizationId}/badges/{badgeId}/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyBadgeThroughOrganization(
            @PathVariable Long organizationId,
            @PathVariable String badgeId,
            @RequestParam Long userId) {
        try {
            boolean verified = apiIntegrationService.verifyBadgeThroughOrganization(organizationId, badgeId, userId);
            String message = verified ? "Badge verified successfully" : "Failed to verify badge";
            return ResponseEntity.ok(new ApiResponse<>(200, message, verified, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to verify badge", null, e.getMessage()));
        }
    }

    // === SECURITY AND AUTHENTICATION ===

    @Operation(summary = "Validate API credentials", description = "ISSUER: Validate API credentials for the organization.")
    @PreAuthorize("hasRole('ISSUER')")
    @PostMapping("/{organizationId}/validate-credentials")
    public ResponseEntity<ApiResponse<Boolean>> validateApiCredentials(
            @PathVariable Long organizationId,
            @RequestParam String apiKey,
            @RequestParam String apiSecret) {
        try {
            boolean isValid = apiIntegrationService.validateApiCredentials(organizationId, apiKey, apiSecret);
            String message = isValid ? "API credentials are valid" : "API credentials are invalid";
            return ResponseEntity.ok(new ApiResponse<>(200, message, isValid, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to validate API credentials", null, e.getMessage()));
        }
    }

    @Operation(summary = "Refresh API token", description = "ISSUER: Refresh the API token for the organization.")
    @PreAuthorize("hasRole('ISSUER')")
    @PostMapping("/{organizationId}/refresh-token")
    public ResponseEntity<ApiResponse<String>> refreshApiToken(@PathVariable Long organizationId) {
        try {
            String newToken = apiIntegrationService.refreshApiToken(organizationId);
            return ResponseEntity.ok(new ApiResponse<>(200, "API token refreshed successfully", newToken, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Failed to refresh API token", null, e.getMessage()));
        }
    }
} 