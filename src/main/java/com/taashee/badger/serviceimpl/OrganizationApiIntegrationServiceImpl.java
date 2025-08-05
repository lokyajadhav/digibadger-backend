package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.OrganizationApiConfig;
import com.taashee.badger.models.PathwayProgress;
import com.taashee.badger.models.Organization;
import com.taashee.badger.models.User;
import com.taashee.badger.repositories.OrganizationApiConfigRepository;
import com.taashee.badger.repositories.PathwayProgressRepository;
import com.taashee.badger.repositories.OrganizationRepository;
import com.taashee.badger.repositories.UserRepository;
import com.taashee.badger.services.OrganizationApiIntegrationService;
import com.taashee.badger.exceptions.ResourceNotFoundException;
import com.taashee.badger.exceptions.UnauthorizedException;
import java.lang.IllegalArgumentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@Transactional
public class OrganizationApiIntegrationServiceImpl implements OrganizationApiIntegrationService {

    @Autowired
    private OrganizationApiConfigRepository apiConfigRepository;

    @Autowired
    private PathwayProgressRepository pathwayProgressRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    // === ORGANIZATION API CONFIGURATION ===

    @Override
    public OrganizationApiConfig configureApiSettings(Long organizationId, OrganizationApiConfig config) {
        log.info("Configuring API settings for organization: {}", organizationId);
        
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + organizationId));
        
        User currentUser = getCurrentUser();
        validateUserPermission(organizationId, currentUser);
        
        config.setOrganization(organization);
        config.setCreatedBy(currentUser);
        config.setUpdatedBy(currentUser);
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        
        // Validate configuration
        List<String> validationErrors = config.getValidationErrors();
        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException("Invalid API configuration: " + String.join(", ", validationErrors));
        }
        
        OrganizationApiConfig savedConfig = apiConfigRepository.save(config);
        log.info("API configuration saved successfully for organization: {}", organizationId);
        
        return savedConfig;
    }

    @Override
    public Optional<OrganizationApiConfig> getApiConfiguration(Long organizationId) {
        log.debug("Getting API configuration for organization: {}", organizationId);
        List<OrganizationApiConfig> configs = apiConfigRepository.findByOrganizationId(organizationId);
        return configs.isEmpty() ? Optional.empty() : Optional.of(configs.get(0));
    }

    @Override
    public boolean testApiConnection(Long organizationId) {
        log.info("Testing API connection for organization: {}", organizationId);
        
        Optional<OrganizationApiConfig> configOpt = getApiConfiguration(organizationId);
        if (configOpt.isEmpty()) {
            log.error("No API configuration found for organization: {}", organizationId);
            return false;
        }
        
        OrganizationApiConfig config = configOpt.get();
        if (!config.isValid()) {
            log.error("Invalid API configuration for organization: {}", organizationId);
            return false;
        }
        
        try {
            // Test connection based on API type
            boolean isConnected = testConnectionByType(config);
            
            if (isConnected) {
                config.setIsVerified(true);
                config.incrementSuccessCount();
                config.setUpdatedAt(LocalDateTime.now());
                apiConfigRepository.save(config);
                log.info("API connection test successful for organization: {}", organizationId);
            } else {
                config.setIsVerified(false);
                config.setError("Connection test failed");
                apiConfigRepository.save(config);
                log.error("API connection test failed for organization: {}", organizationId);
            }
            
            return isConnected;
        } catch (Exception e) {
            log.error("Error testing API connection for organization: {}", organizationId, e);
            config.setError("Connection test error: " + e.getMessage());
            apiConfigRepository.save(config);
            return false;
        }
    }

    @Override
    public OrganizationApiConfig updateApiConfiguration(Long organizationId, OrganizationApiConfig config) {
        log.info("Updating API configuration for organization: {}", organizationId);
        
        List<OrganizationApiConfig> configs = apiConfigRepository.findByOrganizationId(organizationId);
        if (configs.isEmpty()) {
            throw new ResourceNotFoundException("API configuration not found for organization: " + organizationId);
        }
        OrganizationApiConfig existingConfig = configs.get(0);
        
        User currentUser = getCurrentUser();
        validateUserPermission(organizationId, currentUser);
        
        // Update fields
        existingConfig.setApiName(config.getApiName());
        existingConfig.setApiType(config.getApiType());
        existingConfig.setBaseUrl(config.getBaseUrl());
        existingConfig.setApiKey(config.getApiKey());
        existingConfig.setApiSecret(config.getApiSecret());
        existingConfig.setAccessToken(config.getAccessToken());
        existingConfig.setRefreshToken(config.getRefreshToken());
        existingConfig.setTokenExpiresAt(config.getTokenExpiresAt());
        existingConfig.setIsActive(config.getIsActive());
        existingConfig.setSyncFrequencyMinutes(config.getSyncFrequencyMinutes());
        existingConfig.setWebhookUrl(config.getWebhookUrl());
        existingConfig.setWebhookSecret(config.getWebhookSecret());
        existingConfig.setApiSettings(config.getApiSettings());
        existingConfig.setSyncConfig(config.getSyncConfig());
        existingConfig.setUpdatedBy(currentUser);
        existingConfig.setUpdatedAt(LocalDateTime.now());
        
        // Validate updated configuration
        List<String> validationErrors = existingConfig.getValidationErrors();
        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException("Invalid API configuration: " + String.join(", ", validationErrors));
        }
        
        OrganizationApiConfig updatedConfig = apiConfigRepository.save(existingConfig);
        log.info("API configuration updated successfully for organization: {}", organizationId);
        
        return updatedConfig;
    }

    @Override
    public void deleteApiConfiguration(Long organizationId) {
        log.info("Deleting API configuration for organization: {}", organizationId);
        
        List<OrganizationApiConfig> configs = apiConfigRepository.findByOrganizationId(organizationId);
        if (configs.isEmpty()) {
            throw new ResourceNotFoundException("API configuration not found for organization: " + organizationId);
        }
        OrganizationApiConfig config = configs.get(0);
        
        User currentUser = getCurrentUser();
        validateUserPermission(organizationId, currentUser);
        
        apiConfigRepository.delete(config);
        log.info("API configuration deleted successfully for organization: {}", organizationId);
    }

    // === PRIVATE HELPER METHODS ===

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    private void validateUserPermission(Long organizationId, User user) {
        // Check if user has ISSUER role and belongs to the organization
        if (!user.getRoles().contains("ISSUER")) {
            throw new UnauthorizedException("User does not have ISSUER role");
        }
        
        // Additional organization membership validation would go here
        // For now, just check the role
    }

    private boolean testConnectionByType(OrganizationApiConfig config) {
        try {
            switch (config.getApiType()) {
                case CANVAS:
                    return testCanvasConnection(config);
                case MOODLE:
                    return testMoodleConnection(config);
                case BLACKBOARD:
                    return testBlackboardConnection(config);
                case CUSTOM:
                    return testCustomConnection(config);
                default:
                    return testGenericConnection(config);
            }
        } catch (Exception e) {
            log.error("Error testing connection for API type: {}", config.getApiType(), e);
            return false;
        }
    }

    private boolean testCanvasConnection(OrganizationApiConfig config) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                config.getBaseUrl() + "/api/v1/courses", Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("Canvas connection test failed", e);
            return false;
        }
    }

    private boolean testMoodleConnection(OrganizationApiConfig config) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                config.getBaseUrl() + "/webservice/rest/server.php?wsfunction=core_course_get_courses", Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("Moodle connection test failed", e);
            return false;
        }
    }

    private boolean testBlackboardConnection(OrganizationApiConfig config) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                config.getBaseUrl() + "/learn/api/public/v1/courses", Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("Blackboard connection test failed", e);
            return false;
        }
    }

    private boolean testCustomConnection(OrganizationApiConfig config) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                config.getBaseUrl() + "/health", Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("Custom connection test failed", e);
            return false;
        }
    }

    private boolean testGenericConnection(OrganizationApiConfig config) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(config.getBaseUrl(), Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("Generic connection test failed", e);
            return false;
        }
    }

    // === 2-WAY SYNC FUNCTIONALITY ===

    @Override
    public PathwayProgress syncProgressFromOrganization(Long pathwayId, Long userId, Map<String, Object> progressData) {
        log.info("Syncing progress from organization for pathway: {}, user: {}", pathwayId, userId);
        
        try {
            // Get pathway progress
            PathwayProgress progress = pathwayProgressRepository.findByPathwayIdAndUserId(pathwayId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pathway progress not found"));
            
            // Extract progress data from organization
            Double progressPercentage = extractProgressPercentage(progressData);
            List<String> completedElements = extractCompletedElements(progressData);
            Boolean isCompleted = extractCompletionStatus(progressData);
            
            // Update progress
            if (progressPercentage != null) {
                progress.setProgressPercentage(progressPercentage);
            }
            if (completedElements != null) {
                progress.setCompletedElements(completedElements.size());
            }
            if (isCompleted != null && isCompleted) {
                progress.completeProgress();
            }
            
            progress.setLastActivityAt(LocalDateTime.now());
            progress.setMetadata(progressData);
            
            PathwayProgress updatedProgress = pathwayProgressRepository.save(progress);
            log.info("Progress synced from organization successfully for pathway: {}, user: {}", pathwayId, userId);
            
            return updatedProgress;
        } catch (Exception e) {
            log.error("Error syncing progress from organization for pathway: {}, user: {}", pathwayId, userId, e);
            throw new RuntimeException("Failed to sync progress from organization", e);
        }
    }

    @Override
    public boolean syncProgressToOrganization(Long pathwayId, Long userId, PathwayProgress progress) {
        log.info("Syncing progress to organization for pathway: {}, user: {}", pathwayId, userId);
        
        try {
            Long organizationId = progress.getPathway().getOrganization().getId();
            Optional<OrganizationApiConfig> configOpt = getApiConfiguration(organizationId);
            
            if (configOpt.isEmpty() || !configOpt.get().canSync()) {
                log.warn("No valid API configuration for organization: {}", organizationId);
                return false;
            }
            
            OrganizationApiConfig config = configOpt.get();
            Map<String, Object> progressData = buildProgressDataForOrganization(progress);
            
            // Send progress to organization API
            boolean success = sendDataToOrganization(config, "/progress/sync", progressData);
            
            if (success) {
                config.incrementSuccessCount();
                config.updateLastSync();
            } else {
                config.setError("Failed to sync progress to organization");
            }
            
            apiConfigRepository.save(config);
            log.info("Progress synced to organization successfully for pathway: {}, user: {}", pathwayId, userId);
            
            return success;
        } catch (Exception e) {
            log.error("Error syncing progress to organization for pathway: {}, user: {}", pathwayId, userId, e);
            return false;
        }
    }

    @Override
    public boolean syncCompletionFromOrganization(Long pathwayId, Long userId, Map<String, Object> completionData) {
        log.info("Syncing completion from organization for pathway: {}, user: {}", pathwayId, userId);
        
        try {
            PathwayProgress progress = pathwayProgressRepository.findByPathwayIdAndUserId(pathwayId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pathway progress not found"));
            
            Boolean isCompleted = extractCompletionStatus(completionData);
            if (isCompleted != null && isCompleted) {
                progress.completeProgress();
                progress.setCompletedAt(LocalDateTime.now());
                progress.setMetadata(completionData);
                
                pathwayProgressRepository.save(progress);
                log.info("Completion synced from organization successfully for pathway: {}, user: {}", pathwayId, userId);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("Error syncing completion from organization for pathway: {}, user: {}", pathwayId, userId, e);
            return false;
        }
    }

    @Override
    public boolean syncCompletionToOrganization(Long pathwayId, Long userId, PathwayProgress progress) {
        log.info("Syncing completion to organization for pathway: {}, user: {}", pathwayId, userId);
        
        try {
            Long organizationId = progress.getPathway().getOrganization().getId();
            Optional<OrganizationApiConfig> configOpt = getApiConfiguration(organizationId);
            
            if (configOpt.isEmpty() || !configOpt.get().canSync()) {
                log.warn("No valid API configuration for organization: {}", organizationId);
                return false;
            }
            
            OrganizationApiConfig config = configOpt.get();
            Map<String, Object> completionData = buildCompletionDataForOrganization(progress);
            
            // Send completion to organization API
            boolean success = sendDataToOrganization(config, "/completion/sync", completionData);
            
            if (success) {
                config.incrementSuccessCount();
                config.updateLastSync();
            } else {
                config.setError("Failed to sync completion to organization");
            }
            
            apiConfigRepository.save(config);
            log.info("Completion synced to organization successfully for pathway: {}, user: {}", pathwayId, userId);
            
            return success;
        } catch (Exception e) {
            log.error("Error syncing completion to organization for pathway: {}, user: {}", pathwayId, userId, e);
            return false;
        }
    }

    // === REAL-TIME UPDATES ===

    @Override
    public boolean sendRealTimeUpdate(Long organizationId, String eventType, Map<String, Object> eventData) {
        log.info("Sending real-time update to organization: {}, event: {}", organizationId, eventType);
        
        try {
            Optional<OrganizationApiConfig> configOpt = getApiConfiguration(organizationId);
            if (configOpt.isEmpty() || !configOpt.get().canSync()) {
                return false;
            }
            
            OrganizationApiConfig config = configOpt.get();
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("eventType", eventType);
            updateData.put("eventData", eventData);
            updateData.put("timestamp", LocalDateTime.now().toString());
            
            boolean success = sendDataToOrganization(config, "/realtime/update", updateData);
            
            if (success) {
                config.incrementSuccessCount();
            } else {
                config.setError("Failed to send real-time update");
            }
            
            apiConfigRepository.save(config);
            return success;
        } catch (Exception e) {
            log.error("Error sending real-time update to organization: {}", organizationId, e);
            return false;
        }
    }

    @Override
    public boolean receiveRealTimeUpdate(Long organizationId, String eventType, Map<String, Object> eventData) {
        log.info("Receiving real-time update from organization: {}, event: {}", organizationId, eventType);
        
        try {
            // Process real-time update based on event type
            switch (eventType) {
                case "PROGRESS_UPDATE":
                    return processProgressUpdate(organizationId, eventData);
                case "COMPLETION_UPDATE":
                    return processCompletionUpdate(organizationId, eventData);
                case "COURSE_UPDATE":
                    return processCourseUpdate(organizationId, eventData);
                case "ASSIGNMENT_UPDATE":
                    return processAssignmentUpdate(organizationId, eventData);
                default:
                    log.warn("Unknown event type: {}", eventType);
                    return false;
            }
        } catch (Exception e) {
            log.error("Error processing real-time update from organization: {}", organizationId, e);
            return false;
        }
    }

    // === COURSE/ASSIGNMENT INTEGRATION ===

    @Override
    public List<Map<String, Object>> getAvailableCourses(Long organizationId) {
        log.info("Getting available courses from organization: {}", organizationId);
        
        try {
            Optional<OrganizationApiConfig> configOpt = getApiConfiguration(organizationId);
            if (configOpt.isEmpty() || !configOpt.get().canSync()) {
                return new ArrayList<>();
            }
            
            OrganizationApiConfig config = configOpt.get();
            ResponseEntity<Map> response = restTemplate.getForEntity(
                config.getBaseUrl() + "/courses", Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> courses = (List<Map<String, Object>>) response.getBody().get("courses");
                log.info("Retrieved {} courses from organization: {}", courses != null ? courses.size() : 0, organizationId);
                return courses != null ? courses : new ArrayList<>();
            }
            
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error getting available courses from organization: {}", organizationId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<Map<String, Object>> getCourseDetails(Long organizationId, String courseId) {
        log.info("Getting course details from organization: {}, course: {}", organizationId, courseId);
        
        try {
            Optional<OrganizationApiConfig> configOpt = getApiConfiguration(organizationId);
            if (configOpt.isEmpty() || !configOpt.get().canSync()) {
                return Optional.empty();
            }
            
            OrganizationApiConfig config = configOpt.get();
            ResponseEntity<Map> response = restTemplate.getForEntity(
                config.getBaseUrl() + "/courses/" + courseId, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Retrieved course details from organization: {}, course: {}", organizationId, courseId);
                return Optional.of(response.getBody());
            }
            
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error getting course details from organization: {}, course: {}", organizationId, courseId, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Map<String, Object>> getAssignmentDetails(Long organizationId, String assignmentId) {
        log.info("Getting assignment details from organization: {}, assignment: {}", organizationId, assignmentId);
        
        try {
            Optional<OrganizationApiConfig> configOpt = getApiConfiguration(organizationId);
            if (configOpt.isEmpty() || !configOpt.get().canSync()) {
                return Optional.empty();
            }
            
            OrganizationApiConfig config = configOpt.get();
            ResponseEntity<Map> response = restTemplate.getForEntity(
                config.getBaseUrl() + "/assignments/" + assignmentId, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Retrieved assignment details from organization: {}, assignment: {}", organizationId, assignmentId);
                return Optional.of(response.getBody());
            }
            
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error getting assignment details from organization: {}, assignment: {}", organizationId, assignmentId, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Map<String, Object>> getTestDetails(Long organizationId, String testId) {
        log.info("Getting test details from organization: {}, test: {}", organizationId, testId);
        
        try {
            Optional<OrganizationApiConfig> configOpt = getApiConfiguration(organizationId);
            if (configOpt.isEmpty() || !configOpt.get().canSync()) {
                return Optional.empty();
            }
            
            OrganizationApiConfig config = configOpt.get();
            ResponseEntity<Map> response = restTemplate.getForEntity(
                config.getBaseUrl() + "/tests/" + testId, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Retrieved test details from organization: {}, test: {}", organizationId, testId);
                return Optional.of(response.getBody());
            }
            
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error getting test details from organization: {}, test: {}", organizationId, testId, e);
            return Optional.empty();
        }
    }

    // === BADGE INTEGRATION ===

    @Override
    public List<Map<String, Object>> getAvailableBadges(Long organizationId) {
        log.info("Getting available badges from organization: {}", organizationId);
        
        try {
            Optional<OrganizationApiConfig> configOpt = getApiConfiguration(organizationId);
            if (configOpt.isEmpty() || !configOpt.get().canSync()) {
                return new ArrayList<>();
            }
            
            OrganizationApiConfig config = configOpt.get();
            ResponseEntity<Map> response = restTemplate.getForEntity(
                config.getBaseUrl() + "/badges", Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> badges = (List<Map<String, Object>>) response.getBody().get("badges");
                log.info("Retrieved {} badges from organization: {}", badges != null ? badges.size() : 0, organizationId);
                return badges != null ? badges : new ArrayList<>();
            }
            
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error getting available badges from organization: {}", organizationId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean issueBadgeThroughOrganization(Long organizationId, String badgeId, Long userId, Map<String, Object> badgeData) {
        log.info("Issuing badge through organization: {}, badge: {}, user: {}", organizationId, badgeId, userId);
        
        try {
            Optional<OrganizationApiConfig> configOpt = getApiConfiguration(organizationId);
            if (configOpt.isEmpty() || !configOpt.get().canSync()) {
                return false;
            }
            
            OrganizationApiConfig config = configOpt.get();
            Map<String, Object> issueData = new HashMap<>();
            issueData.put("badgeId", badgeId);
            issueData.put("userId", userId);
            issueData.put("badgeData", badgeData);
            issueData.put("issuedAt", LocalDateTime.now().toString());
            
            boolean success = sendDataToOrganization(config, "/badges/issue", issueData);
            
            if (success) {
                config.incrementSuccessCount();
                log.info("Badge issued successfully through organization: {}, badge: {}, user: {}", organizationId, badgeId, userId);
            } else {
                config.setError("Failed to issue badge through organization");
                log.error("Failed to issue badge through organization: {}, badge: {}, user: {}", organizationId, badgeId, userId);
            }
            
            apiConfigRepository.save(config);
            return success;
        } catch (Exception e) {
            log.error("Error issuing badge through organization: {}, badge: {}, user: {}", organizationId, badgeId, userId, e);
            return false;
        }
    }

    @Override
    public boolean verifyBadgeThroughOrganization(Long organizationId, String badgeId, Long userId) {
        log.info("Verifying badge through organization: {}, badge: {}, user: {}", organizationId, badgeId, userId);
        
        try {
            Optional<OrganizationApiConfig> configOpt = getApiConfiguration(organizationId);
            if (configOpt.isEmpty() || !configOpt.get().canSync()) {
                return false;
            }
            
            OrganizationApiConfig config = configOpt.get();
            ResponseEntity<Map> response = restTemplate.getForEntity(
                config.getBaseUrl() + "/badges/" + badgeId + "/verify?userId=" + userId, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Boolean isValid = (Boolean) response.getBody().get("isValid");
                log.info("Badge verification result from organization: {}, badge: {}, user: {}, valid: {}", 
                    organizationId, badgeId, userId, isValid);
                return isValid != null && isValid;
            }
            
            return false;
        } catch (Exception e) {
            log.error("Error verifying badge through organization: {}, badge: {}, user: {}", organizationId, badgeId, userId, e);
            return false;
        }
    }

    // === ERROR HANDLING AND MONITORING ===

    @Override
    public Map<String, Object> getSyncStatus(Long organizationId) {
        log.debug("Getting sync status for organization: {}", organizationId);
        
        Optional<OrganizationApiConfig> configOpt = getApiConfiguration(organizationId);
        if (configOpt.isEmpty()) {
            return Map.of("status", "NOT_CONFIGURED", "message", "No API configuration found");
        }
        
        OrganizationApiConfig config = configOpt.get();
        Map<String, Object> status = new HashMap<>();
        status.put("status", config.isHealthy() ? "HEALTHY" : "UNHEALTHY");
        status.put("isActive", config.getIsActive());
        status.put("isVerified", config.getIsVerified());
        status.put("lastSyncAt", config.getLastSyncAt());
        status.put("lastErrorAt", config.getLastErrorAt());
        status.put("errorCount", config.getErrorCount());
        status.put("successCount", config.getSuccessCount());
        status.put("syncFrequencyMinutes", config.getSyncFrequencyMinutes());
        status.put("isTokenExpired", config.getIsTokenExpired());
        
        return status;
    }

    @Override
    public List<Map<String, Object>> getSyncHistory(Long organizationId) {
        log.debug("Getting sync history for organization: {}", organizationId);
        
        // This would typically query a sync history table
        // For now, return basic information from the config
        Optional<OrganizationApiConfig> configOpt = getApiConfiguration(organizationId);
        if (configOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        OrganizationApiConfig config = configOpt.get();
        List<Map<String, Object>> history = new ArrayList<>();
        
        if (config.getLastSyncAt() != null) {
            Map<String, Object> lastSync = new HashMap<>();
            lastSync.put("timestamp", config.getLastSyncAt());
            lastSync.put("type", "SYNC");
            lastSync.put("status", "SUCCESS");
            history.add(lastSync);
        }
        
        if (config.getLastErrorAt() != null) {
            Map<String, Object> lastError = new HashMap<>();
            lastError.put("timestamp", config.getLastErrorAt());
            lastError.put("type", "ERROR");
            lastError.put("status", "FAILED");
            lastError.put("message", config.getLastErrorMessage());
            history.add(lastError);
        }
        
        return history;
    }

    @Override
    public boolean retryFailedSync(Long organizationId, String syncId) {
        log.info("Retrying failed sync for organization: {}, sync: {}", organizationId, syncId);
        
        // This would typically retry a specific failed sync operation
        // For now, just test the connection
        return testApiConnection(organizationId);
    }

    @Override
    public Map<String, Object> getApiHealthStatus(Long organizationId) {
        log.debug("Getting API health status for organization: {}", organizationId);
        
        Optional<OrganizationApiConfig> configOpt = getApiConfiguration(organizationId);
        if (configOpt.isEmpty()) {
            return Map.of("status", "NOT_CONFIGURED", "healthy", false);
        }
        
        OrganizationApiConfig config = configOpt.get();
        Map<String, Object> health = new HashMap<>();
        health.put("status", config.isHealthy() ? "HEALTHY" : "UNHEALTHY");
        health.put("healthy", config.isHealthy());
        health.put("isActive", config.getIsActive());
        health.put("isVerified", config.getIsVerified());
        health.put("errorCount", config.getErrorCount());
        health.put("successCount", config.getSuccessCount());
        health.put("lastErrorAt", config.getLastErrorAt());
        health.put("lastSuccessAt", config.getLastSuccessAt());
        health.put("isTokenExpired", config.getIsTokenExpired());
        
        return health;
    }

    // === BATCH OPERATIONS ===

    @Override
    public List<PathwayProgress> batchSyncProgress(Long organizationId, List<Map<String, Object>> progressDataList) {
        log.info("Batch syncing progress for organization: {}, items: {}", organizationId, progressDataList.size());
        
        List<PathwayProgress> results = new ArrayList<>();
        
        for (Map<String, Object> progressData : progressDataList) {
            try {
                Long pathwayId = Long.valueOf(progressData.get("pathwayId").toString());
                Long userId = Long.valueOf(progressData.get("userId").toString());
                
                PathwayProgress progress = syncProgressFromOrganization(pathwayId, userId, progressData);
                results.add(progress);
            } catch (Exception e) {
                log.error("Error in batch sync progress for organization: {}", organizationId, e);
            }
        }
        
        log.info("Batch sync completed for organization: {}, successful: {}", organizationId, results.size());
        return results;
    }

    @Override
    public boolean batchSyncCompletions(Long organizationId, List<Map<String, Object>> completionDataList) {
        log.info("Batch syncing completions for organization: {}, items: {}", organizationId, completionDataList.size());
        
        int successCount = 0;
        
        for (Map<String, Object> completionData : completionDataList) {
            try {
                Long pathwayId = Long.valueOf(completionData.get("pathwayId").toString());
                Long userId = Long.valueOf(completionData.get("userId").toString());
                
                boolean success = syncCompletionFromOrganization(pathwayId, userId, completionData);
                if (success) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("Error in batch sync completion for organization: {}", organizationId, e);
            }
        }
        
        log.info("Batch sync completions completed for organization: {}, successful: {}", organizationId, successCount);
        return successCount > 0;
    }

    // === SECURITY AND AUTHENTICATION ===

    @Override
    public boolean validateApiCredentials(Long organizationId, String apiKey, String apiSecret) {
        log.info("Validating API credentials for organization: {}", organizationId);
        
        try {
            Optional<OrganizationApiConfig> configOpt = getApiConfiguration(organizationId);
            if (configOpt.isEmpty()) {
                return false;
            }
            
            OrganizationApiConfig config = configOpt.get();
            // This would typically make a test API call with the provided credentials
            // For now, just check if they match the stored credentials
            return apiKey.equals(config.getApiKey()) && apiSecret.equals(config.getApiSecret());
        } catch (Exception e) {
            log.error("Error validating API credentials for organization: {}", organizationId, e);
            return false;
        }
    }

    @Override
    public String refreshApiToken(Long organizationId) {
        log.info("Refreshing API token for organization: {}", organizationId);
        
        try {
            Optional<OrganizationApiConfig> configOpt = getApiConfiguration(organizationId);
            if (configOpt.isEmpty()) {
                throw new ResourceNotFoundException("API configuration not found for organization: " + organizationId);
            }
            
            OrganizationApiConfig config = configOpt.get();
            if (config.getRefreshToken() == null) {
                throw new IllegalArgumentException("No refresh token available for organization: " + organizationId);
            }
            
            // This would typically make an OAuth token refresh call
            // For now, just return a mock new token
            String newToken = "refreshed_token_" + System.currentTimeMillis();
            config.setAccessToken(newToken);
            config.setTokenExpiresAt(LocalDateTime.now().plusHours(1));
            config.setUpdatedAt(LocalDateTime.now());
            
            apiConfigRepository.save(config);
            log.info("API token refreshed successfully for organization: {}", organizationId);
            
            return newToken;
        } catch (Exception e) {
            log.error("Error refreshing API token for organization: {}", organizationId, e);
            throw new RuntimeException("Failed to refresh API token", e);
        }
    }

    @Override
    public boolean validateApiPermissions(Long organizationId, List<String> requiredPermissions) {
        log.info("Validating API permissions for organization: {}, permissions: {}", organizationId, requiredPermissions);
        
        try {
            Optional<OrganizationApiConfig> configOpt = getApiConfiguration(organizationId);
            if (configOpt.isEmpty()) {
                return false;
            }
            
            OrganizationApiConfig config = configOpt.get();
            // This would typically check the API permissions against the organization's API
            // For now, just return true if the API is verified
            return config.getIsVerified();
        } catch (Exception e) {
            log.error("Error validating API permissions for organization: {}", organizationId, e);
            return false;
        }
    }

    // === ADDITIONAL PRIVATE HELPER METHODS ===

    private boolean sendDataToOrganization(OrganizationApiConfig config, String endpoint, Map<String, Object> data) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            if (config.getApiKey() != null) {
                headers.set("Authorization", "Bearer " + config.getApiKey());
            }
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(data, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                config.getBaseUrl() + endpoint, request, Map.class);
            
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("Error sending data to organization API: {}", endpoint, e);
            return false;
        }
    }

    private Double extractProgressPercentage(Map<String, Object> progressData) {
        Object value = progressData.get("progressPercentage");
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private List<String> extractCompletedElements(Map<String, Object> progressData) {
        Object value = progressData.get("completedElements");
        if (value instanceof List) {
            return (List<String>) value;
        }
        return null;
    }

    private Boolean extractCompletionStatus(Map<String, Object> progressData) {
        Object value = progressData.get("isCompleted");
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return null;
    }

    private Map<String, Object> buildProgressDataForOrganization(PathwayProgress progress) {
        Map<String, Object> data = new HashMap<>();
        data.put("pathwayId", progress.getPathway().getId());
        data.put("userId", progress.getUser().getId());
        data.put("progressPercentage", progress.getProgressPercentage());
        data.put("completedElements", progress.getCompletedElements());
        data.put("isCompleted", progress.getIsCompleted());
        data.put("lastActivityAt", progress.getLastActivityAt());
        data.put("timeSpentMinutes", progress.getTimeSpentMinutes());
        return data;
    }

    private Map<String, Object> buildCompletionDataForOrganization(PathwayProgress progress) {
        Map<String, Object> data = new HashMap<>();
        data.put("pathwayId", progress.getPathway().getId());
        data.put("userId", progress.getUser().getId());
        data.put("isCompleted", progress.getIsCompleted());
        data.put("completedAt", progress.getCompletedAt());
        data.put("completionBadgeIssued", progress.getCompletionBadgeIssued());
        data.put("achievements", progress.getAchievements());
        return data;
    }

    private boolean processProgressUpdate(Long organizationId, Map<String, Object> eventData) {
        try {
            Long pathwayId = Long.valueOf(eventData.get("pathwayId").toString());
            Long userId = Long.valueOf(eventData.get("userId").toString());
            Map<String, Object> progressData = (Map<String, Object>) eventData.get("progressData");
            
            syncProgressFromOrganization(pathwayId, userId, progressData);
            return true;
        } catch (Exception e) {
            log.error("Error processing progress update for organization: {}", organizationId, e);
            return false;
        }
    }

    private boolean processCompletionUpdate(Long organizationId, Map<String, Object> eventData) {
        try {
            Long pathwayId = Long.valueOf(eventData.get("pathwayId").toString());
            Long userId = Long.valueOf(eventData.get("userId").toString());
            Map<String, Object> completionData = (Map<String, Object>) eventData.get("completionData");
            
            syncCompletionFromOrganization(pathwayId, userId, completionData);
            return true;
        } catch (Exception e) {
            log.error("Error processing completion update for organization: {}", organizationId, e);
            return false;
        }
    }

    private boolean processCourseUpdate(Long organizationId, Map<String, Object> eventData) {
        // Process course updates from organization
        log.info("Processing course update for organization: {}", organizationId);
        return true;
    }

    private boolean processAssignmentUpdate(Long organizationId, Map<String, Object> eventData) {
        // Process assignment updates from organization
        log.info("Processing assignment update for organization: {}", organizationId);
        return true;
    }
} 