package com.taashee.badger.services;

import com.taashee.badger.models.OrganizationApiConfig;
import com.taashee.badger.models.PathwayProgress;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrganizationApiIntegrationService {
    
    // === ORGANIZATION API CONFIGURATION ===
    
    // Configure API settings for an organization
    OrganizationApiConfig configureApiSettings(Long organizationId, OrganizationApiConfig config);
    
    // Get API configuration for an organization
    Optional<OrganizationApiConfig> getApiConfiguration(Long organizationId);
    
    // Test API connection for an organization
    boolean testApiConnection(Long organizationId);
    
    // Update API configuration
    OrganizationApiConfig updateApiConfiguration(Long organizationId, OrganizationApiConfig config);
    
    // Delete API configuration
    void deleteApiConfiguration(Long organizationId);
    
    // === 2-WAY SYNC FUNCTIONALITY ===
    
    // Sync pathway progress from organization to Badger
    PathwayProgress syncProgressFromOrganization(Long pathwayId, Long userId, Map<String, Object> progressData);
    
    // Sync pathway progress from Badger to organization
    boolean syncProgressToOrganization(Long pathwayId, Long userId, PathwayProgress progress);
    
    // Sync pathway completion from organization to Badger
    boolean syncCompletionFromOrganization(Long pathwayId, Long userId, Map<String, Object> completionData);
    
    // Sync pathway completion from Badger to organization
    boolean syncCompletionToOrganization(Long pathwayId, Long userId, PathwayProgress progress);
    
    // === REAL-TIME UPDATES ===
    
    // Send real-time update to organization
    boolean sendRealTimeUpdate(Long organizationId, String eventType, Map<String, Object> eventData);
    
    // Receive real-time update from organization
    boolean receiveRealTimeUpdate(Long organizationId, String eventType, Map<String, Object> eventData);
    
    // === COURSE/ASSIGNMENT INTEGRATION ===
    
    // Get available courses from organization
    List<Map<String, Object>> getAvailableCourses(Long organizationId);
    
    // Get course details from organization
    Optional<Map<String, Object>> getCourseDetails(Long organizationId, String courseId);
    
    // Get assignment details from organization
    Optional<Map<String, Object>> getAssignmentDetails(Long organizationId, String assignmentId);
    
    // Get test details from organization
    Optional<Map<String, Object>> getTestDetails(Long organizationId, String testId);
    
    // === BADGE INTEGRATION ===
    
    // Get available badges from organization
    List<Map<String, Object>> getAvailableBadges(Long organizationId);
    
    // Issue badge through organization API
    boolean issueBadgeThroughOrganization(Long organizationId, String badgeId, Long userId, Map<String, Object> badgeData);
    
    // Verify badge through organization API
    boolean verifyBadgeThroughOrganization(Long organizationId, String badgeId, Long userId);
    
    // === ERROR HANDLING AND MONITORING ===
    
    // Get sync status for an organization
    Map<String, Object> getSyncStatus(Long organizationId);
    
    // Get sync history for an organization
    List<Map<String, Object>> getSyncHistory(Long organizationId);
    
    // Retry failed sync operations
    boolean retryFailedSync(Long organizationId, String syncId);
    
    // Get API health status
    Map<String, Object> getApiHealthStatus(Long organizationId);
    
    // === BATCH OPERATIONS ===
    
    // Batch sync multiple pathway progress
    List<PathwayProgress> batchSyncProgress(Long organizationId, List<Map<String, Object>> progressDataList);
    
    // Batch sync multiple completions
    boolean batchSyncCompletions(Long organizationId, List<Map<String, Object>> completionDataList);
    
    // === SECURITY AND AUTHENTICATION ===
    
    // Validate API credentials
    boolean validateApiCredentials(Long organizationId, String apiKey, String apiSecret);
    
    // Refresh API token
    String refreshApiToken(Long organizationId);
    
    // Validate API permissions
    boolean validateApiPermissions(Long organizationId, List<String> requiredPermissions);
} 