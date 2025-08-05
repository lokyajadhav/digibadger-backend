package com.taashee.badger.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organization_api_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganizationApiConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    private Organization organization;

    @Column(name = "api_name", nullable = false, length = 100)
    private String apiName;

    @Column(name = "api_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ApiType apiType;

    @Column(name = "base_url", nullable = false, length = 500)
    private String baseUrl;

    @Column(name = "api_key", length = 255)
    private String apiKey;

    @Column(name = "api_secret", length = 255)
    private String apiSecret;

    @Column(name = "access_token", length = 1000)
    private String accessToken;

    @Column(name = "refresh_token", length = 1000)
    private String refreshToken;

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    @Column(name = "sync_frequency_minutes")
    @Builder.Default
    private Integer syncFrequencyMinutes = 30;

    @Column(name = "webhook_url", length = 500)
    private String webhookUrl;

    @Column(name = "webhook_secret", length = 255)
    private String webhookSecret;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "api_settings", columnDefinition = "JSON")
    private Map<String, Object> apiSettings;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sync_config", columnDefinition = "JSON")
    private Map<String, Object> syncConfig;

    @Column(name = "error_count")
    @Builder.Default
    private Integer errorCount = 0;

    @Column(name = "last_error_message", columnDefinition = "TEXT")
    private String lastErrorMessage;

    @Column(name = "last_error_at")
    private LocalDateTime lastErrorAt;

    @Column(name = "success_count")
    @Builder.Default
    private Integer successCount = 0;

    @Column(name = "last_success_at")
    private LocalDateTime lastSuccessAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonIgnore
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    @JsonIgnore
    private User updatedBy;

    // JSON Properties for API responses
    @JsonProperty("organizationId")
    public Long getOrganizationId() {
        return organization != null ? organization.getId() : null;
    }

    @JsonProperty("organizationName")
    public String getOrganizationName() {
        return organization != null ? organization.getNameEnglish() : null;
    }

    @JsonProperty("createdByEmail")
    public String getCreatedByEmail() {
        return createdBy != null ? createdBy.getEmail() : null;
    }

    @JsonProperty("updatedByEmail")
    public String getUpdatedByEmail() {
        return updatedBy != null ? updatedBy.getEmail() : null;
    }

    @JsonProperty("isTokenExpired")
    public Boolean getIsTokenExpired() {
        return tokenExpiresAt != null && LocalDateTime.now().isAfter(tokenExpiresAt);
    }

    @JsonProperty("daysSinceLastSync")
    public Long getDaysSinceLastSync() {
        if (lastSyncAt == null) {
            return null;
        }
        return java.time.Duration.between(lastSyncAt, LocalDateTime.now()).toDays();
    }

    @JsonProperty("daysSinceLastError")
    public Long getDaysSinceLastError() {
        if (lastErrorAt == null) {
            return null;
        }
        return java.time.Duration.between(lastErrorAt, LocalDateTime.now()).toDays();
    }

    @JsonProperty("daysSinceLastSuccess")
    public Long getDaysSinceLastSuccess() {
        if (lastSuccessAt == null) {
            return null;
        }
        return java.time.Duration.between(lastSuccessAt, LocalDateTime.now()).toDays();
    }

    // Enums
    public enum ApiType {
        CANVAS("Canvas LMS"),
        MOODLE("Moodle LMS"),
        BLACKBOARD("Blackboard Learn"),
        D2L("D2L Brightspace"),
        SAKAI("Sakai LMS"),
        CUSTOM("Custom API"),
        BADGR("Badgr Platform"),
        ACCLAIM("Credly Acclaim"),
        OPEN_BADGES("Open Badges");

        private final String displayName;

        ApiType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Business Logic Methods
    public boolean needsTokenRefresh() {
        return tokenExpiresAt != null && 
               LocalDateTime.now().isAfter(tokenExpiresAt.minusMinutes(5)); // Refresh 5 minutes before expiry
    }

    public boolean isHealthy() {
        return isActive && isVerified && errorCount < 5 && 
               (lastErrorAt == null || getDaysSinceLastError() > 1);
    }

    public void incrementErrorCount() {
        this.errorCount++;
        this.lastErrorAt = LocalDateTime.now();
    }

    public void incrementSuccessCount() {
        this.successCount++;
        this.lastSuccessAt = LocalDateTime.now();
        this.errorCount = 0; // Reset error count on success
    }

    public void resetErrorCount() {
        this.errorCount = 0;
        this.lastErrorMessage = null;
    }

    public boolean canSync() {
        return isActive && isVerified && !getIsTokenExpired() && isHealthy();
    }

    // Validation Methods
    public boolean isValid() {
        return organization != null && 
               apiName != null && !apiName.trim().isEmpty() &&
               apiType != null &&
               baseUrl != null && !baseUrl.trim().isEmpty() &&
               createdBy != null;
    }

    public List<String> getValidationErrors() {
        List<String> errors = new ArrayList<>();
        
        if (organization == null) {
            errors.add("Organization is required");
        }
        
        if (apiName == null || apiName.trim().isEmpty()) {
            errors.add("API name is required");
        }
        
        if (apiType == null) {
            errors.add("API type is required");
        }
        
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            errors.add("Base URL is required");
        }
        
        if (createdBy == null) {
            errors.add("Created by user is required");
        }
        
        if (syncFrequencyMinutes != null && syncFrequencyMinutes < 1) {
            errors.add("Sync frequency must be at least 1 minute");
        }
        
        return errors;
    }

    // Utility Methods
    public void setApiSetting(String key, Object value) {
        if (apiSettings == null) {
            apiSettings = new java.util.HashMap<>();
        }
        apiSettings.put(key, value);
    }

    public Object getApiSetting(String key) {
        return apiSettings != null ? apiSettings.get(key) : null;
    }

    public void setSyncConfig(String key, Object value) {
        if (syncConfig == null) {
            syncConfig = new java.util.HashMap<>();
        }
        syncConfig.put(key, value);
    }

    public Object getSyncConfig(String key) {
        return syncConfig != null ? syncConfig.get(key) : null;
    }

    public void updateLastSync() {
        this.lastSyncAt = LocalDateTime.now();
    }

    public void setError(String errorMessage) {
        this.lastErrorMessage = errorMessage;
        this.lastErrorAt = LocalDateTime.now();
        incrementErrorCount();
    }

    public void clearError() {
        this.lastErrorMessage = null;
        resetErrorCount();
    }
} 