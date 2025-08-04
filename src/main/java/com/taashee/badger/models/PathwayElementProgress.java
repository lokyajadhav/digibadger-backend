package com.taashee.badger.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Entity
@Table(name = "pathway_element_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PathwayElementProgress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pathway_progress_id", nullable = false)
    @JsonIgnore
    private PathwayProgress pathwayProgress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "element_id", nullable = false)
    @JsonIgnore
    private PathwayElement element;

    @Column(name = "is_completed")
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "progress_percentage")
    @Builder.Default
    private Double progressPercentage = 0.00;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "completed_badges", columnDefinition = "JSON")
    private List<Long> completedBadges;

    @Column(name = "time_spent_minutes")
    @Builder.Default
    private Integer timeSpentMinutes = 0;

    @Column(name = "attempts_count")
    @Builder.Default
    private Integer attemptsCount = 0;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "JSON")
    private Map<String, Object> metadata;

    // JSON Properties for API responses
    @JsonProperty("pathwayProgressId")
    public Long getPathwayProgressId() {
        return pathwayProgress != null ? pathwayProgress.getId() : null;
    }

    @JsonProperty("elementId")
    public Long getElementId() {
        return element != null ? element.getId() : null;
    }

    @JsonProperty("elementName")
    public String getElementName() {
        return element != null ? element.getName() : null;
    }

    @JsonProperty("elementDescription")
    public String getElementDescription() {
        return element != null ? element.getDescription() : null;
    }

    @JsonProperty("elementType")
    public String getElementType() {
        return element != null ? element.getElementType().name() : null;
    }

    // Business Logic Methods
    public void startProgress() {
        if (this.startedAt == null) {
            this.startedAt = LocalDateTime.now();
        }
        this.lastAttemptAt = LocalDateTime.now();
        this.attemptsCount++;
    }

    public void updateProgress(Double percentage) {
        this.progressPercentage = Math.max(0.0, Math.min(100.0, percentage));
        this.lastAttemptAt = LocalDateTime.now();
    }

    public void completeProgress() {
        this.isCompleted = true;
        this.completedAt = LocalDateTime.now();
        this.progressPercentage = 100.0;
        this.lastAttemptAt = LocalDateTime.now();
    }

    public void addCompletedBadge(Long badgeId) {
        if (this.completedBadges == null) {
            this.completedBadges = new ArrayList<>();
        }
        if (!this.completedBadges.contains(badgeId)) {
            this.completedBadges.add(badgeId);
        }
    }

    public void removeCompletedBadge(Long badgeId) {
        if (this.completedBadges != null) {
            this.completedBadges.remove(badgeId);
        }
    }

    public boolean hasCompletedBadge(Long badgeId) {
        return this.completedBadges != null && this.completedBadges.contains(badgeId);
    }

    public int getCompletedBadgesCount() {
        return this.completedBadges != null ? this.completedBadges.size() : 0;
    }

    public void addTimeSpent(Integer minutes) {
        this.timeSpentMinutes += minutes;
    }

    public String getFormattedTimeSpent() {
        int hours = this.timeSpentMinutes / 60;
        int minutes = this.timeSpentMinutes % 60;
        return String.format("%dh %dm", hours, minutes);
    }

    public boolean isInProgress() {
        return this.startedAt != null && !this.isCompleted;
    }

    public boolean isNotStarted() {
        return this.startedAt == null;
    }

    public long getDaysSinceStarted() {
        if (this.startedAt == null) {
            return 0;
        }
        return java.time.Duration.between(this.startedAt, LocalDateTime.now()).toDays();
    }

    public long getDaysSinceCompleted() {
        if (this.completedAt == null) {
            return 0;
        }
        return java.time.Duration.between(this.completedAt, LocalDateTime.now()).toDays();
    }

    // Validation Methods
    public boolean isValid() {
        return pathwayProgress != null && element != null;
    }

    public List<String> getValidationErrors() {
        List<String> errors = new ArrayList<>();
        
        if (pathwayProgress == null) {
            errors.add("Pathway progress is required");
        }
        
        if (element == null) {
            errors.add("Element is required");
        }
        
        if (progressPercentage != null && (progressPercentage < 0 || progressPercentage > 100)) {
            errors.add("Progress percentage must be between 0 and 100");
        }
        
        if (timeSpentMinutes != null && timeSpentMinutes < 0) {
            errors.add("Time spent cannot be negative");
        }
        
        if (attemptsCount != null && attemptsCount < 0) {
            errors.add("Attempts count cannot be negative");
        }
        
        return errors;
    }

    // Utility Methods
    public void setMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new java.util.HashMap<>();
        }
        this.metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        return this.metadata != null ? this.metadata.get(key) : null;
    }
} 