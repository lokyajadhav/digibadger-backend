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
@Table(name = "pathway_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PathwayProgress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pathway_id", nullable = false)
    @JsonIgnore
    private Pathway pathway;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    // Removed group association - simplified approach

    @Column(name = "progress_percentage")
    @Builder.Default
    private Double progressPercentage = 0.00;

    @Column(name = "completed_elements")
    @Builder.Default
    private Integer completedElements = 0;

    @Column(name = "total_elements")
    @Builder.Default
    private Integer totalElements = 0;

    @Column(name = "completed_badges")
    @Builder.Default
    private Integer completedBadges = 0;

    @Column(name = "total_badges")
    @Builder.Default
    private Integer totalBadges = 0;

    @Column(name = "is_completed")
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "completion_badge_issued")
    @Builder.Default
    private Boolean completionBadgeIssued = false;

    @Column(name = "completion_badge_issued_at")
    private LocalDateTime completionBadgeIssuedAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "time_spent_minutes")
    @Builder.Default
    private Integer timeSpentMinutes = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "achievements", columnDefinition = "JSON")
    private List<String> achievements;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "JSON")
    private Map<String, Object> metadata;

    @OneToMany(mappedBy = "pathwayProgress", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private List<PathwayElementProgress> elementProgress = new ArrayList<>();

    // JSON Properties for API responses
    @JsonProperty("pathwayId")
    public Long getPathwayId() {
        return pathway != null ? pathway.getId() : null;
    }

    @JsonProperty("pathwayName")
    public String getPathwayName() {
        return pathway != null ? pathway.getName() : null;
    }

    @JsonProperty("userId")
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    @JsonProperty("userEmail")
    public String getUserEmail() {
        return user != null ? user.getEmail() : null;
    }

    // Removed group-related properties - simplified approach

    @JsonProperty("elementProgressCount")
    public Integer getElementProgressCount() {
        return elementProgress != null ? elementProgress.size() : 0;
    }

    // Business Logic Methods
    public void startProgress() {
        if (this.startedAt == null) {
            this.startedAt = LocalDateTime.now();
        }
        this.lastActivityAt = LocalDateTime.now();
    }

    public void updateProgress() {
        this.lastActivityAt = LocalDateTime.now();
        this.calculateProgress();
    }

    public void completeProgress() {
        this.isCompleted = true;
        this.completedAt = LocalDateTime.now();
        this.progressPercentage = 100.0;
        this.lastActivityAt = LocalDateTime.now();
    }

    public void issueCompletionBadge() {
        this.completionBadgeIssued = true;
        this.completionBadgeIssuedAt = LocalDateTime.now();
    }

    public void addAchievement(String achievement) {
        if (this.achievements == null) {
            this.achievements = new ArrayList<>();
        }
        if (!this.achievements.contains(achievement)) {
            this.achievements.add(achievement);
        }
    }

    public void removeAchievement(String achievement) {
        if (this.achievements != null) {
            this.achievements.remove(achievement);
        }
    }

    public boolean hasAchievement(String achievement) {
        return this.achievements != null && this.achievements.contains(achievement);
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

    public long getDaysSinceLastActivity() {
        if (this.lastActivityAt == null) {
            return 0;
        }
        return java.time.Duration.between(this.lastActivityAt, LocalDateTime.now()).toDays();
    }

    private void calculateProgress() {
        if (this.totalElements > 0) {
            this.progressPercentage = (double) this.completedElements / this.totalElements * 100.0;
        }
    }

    public void setTotalElements(Integer totalElements) {
        this.totalElements = totalElements;
        this.calculateProgress();
    }

    public void setCompletedElements(Integer completedElements) {
        this.completedElements = completedElements;
        this.calculateProgress();
    }

    public void incrementCompletedElements() {
        this.completedElements++;
        this.calculateProgress();
    }

    public void decrementCompletedElements() {
        if (this.completedElements > 0) {
            this.completedElements--;
            this.calculateProgress();
        }
    }

    public void setTotalBadges(Integer totalBadges) {
        this.totalBadges = totalBadges;
    }

    public void setCompletedBadges(Integer completedBadges) {
        this.completedBadges = completedBadges;
    }

    public void incrementCompletedBadges() {
        this.completedBadges++;
    }

    public void decrementCompletedBadges() {
        if (this.completedBadges > 0) {
            this.completedBadges--;
        }
    }

    // Validation Methods
    public boolean isValid() {
        return pathway != null && user != null;
    }

    public List<String> getValidationErrors() {
        List<String> errors = new ArrayList<>();
        
        if (pathway == null) {
            errors.add("Pathway is required");
        }
        
        if (user == null) {
            errors.add("User is required");
        }
        
        if (progressPercentage != null && (progressPercentage < 0 || progressPercentage > 100)) {
            errors.add("Progress percentage must be between 0 and 100");
        }
        
        if (completedElements != null && completedElements < 0) {
            errors.add("Completed elements cannot be negative");
        }
        
        if (totalElements != null && totalElements < 0) {
            errors.add("Total elements cannot be negative");
        }
        
        if (completedElements != null && totalElements != null && completedElements > totalElements) {
            errors.add("Completed elements cannot exceed total elements");
        }
        
        if (timeSpentMinutes != null && timeSpentMinutes < 0) {
            errors.add("Time spent cannot be negative");
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

    public void addElementProgress(PathwayElementProgress elementProgress) {
        if (this.elementProgress == null) {
            this.elementProgress = new ArrayList<>();
        }
        elementProgress.setPathwayProgress(this);
        this.elementProgress.add(elementProgress);
    }

    public void removeElementProgress(PathwayElementProgress elementProgress) {
        if (this.elementProgress != null) {
            this.elementProgress.remove(elementProgress);
            elementProgress.setPathwayProgress(null);
        }
    }
} 