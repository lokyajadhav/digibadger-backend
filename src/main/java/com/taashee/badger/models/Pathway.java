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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "pathways")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Pathway extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completion_badge_id")
    @JsonIgnore
    private BadgeClass completionBadge;

    @Enumerated(EnumType.STRING)
    @Column(name = "completion_type", length = 50)
    @Builder.Default
    private CompletionType completionType = CompletionType.CONJUNCTION;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private PathwayStatus status = PathwayStatus.DRAFT;

    @Column(name = "version", length = 20)
    @Builder.Default
    private String version = "1.0.0";

    @Column(name = "is_template")
    @Builder.Default
    private Boolean isTemplate = false;

    @Column(name = "template_category", length = 100)
    private String templateCategory;

    @Column(name = "estimated_duration_hours")
    private Integer estimatedDurationHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", length = 20)
    private DifficultyLevel difficultyLevel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "JSON")
    private List<String> tags = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "JSON")
    private Map<String, Object> metadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonIgnore
    private User createdBy;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by")
    @JsonIgnore
    private User publishedBy;

    @OneToMany(mappedBy = "pathway", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private List<PathwayElement> elements = new ArrayList<>();

    // Removed recipient groups - simplified approach

    @OneToMany(mappedBy = "pathway", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private List<PathwayProgress> pathwayProgress = new ArrayList<>();

    // JSON Properties for API responses
    @JsonProperty("organizationId")
    public Long getOrganizationId() {
        return organization != null ? organization.getId() : null;
    }

    @JsonProperty("organizationName")
    public String getOrganizationName() {
        return organization != null ? organization.getNameEnglish() : null;
    }

    @JsonProperty("completionBadgeId")
    public Long getCompletionBadgeId() {
        return completionBadge != null ? completionBadge.getId() : null;
    }

    @JsonProperty("completionBadgeName")
    public String getCompletionBadgeName() {
        return completionBadge != null ? completionBadge.getName() : null;
    }

    @JsonProperty("createdByEmail")
    public String getCreatedByEmail() {
        return createdBy != null ? createdBy.getEmail() : null;
    }

    @JsonProperty("publishedByEmail")
    public String getPublishedByEmail() {
        return publishedBy != null ? publishedBy.getEmail() : null;
    }

    @JsonProperty("elementsCount")
    public Integer getElementsCount() {
        return elements != null ? elements.size() : 0;
    }

    @JsonProperty("isPublished")
    public Boolean getIsPublished() {
        return PathwayStatus.PUBLISHED.equals(status);
    }

    @JsonProperty("canEdit")
    public Boolean getCanEdit() {
        return PathwayStatus.DRAFT.equals(status) || PathwayStatus.REVIEW.equals(status);
    }

    // Enums
    public enum CompletionType {
        CONJUNCTION("All elements must be completed"),
        DISJUNCTION("Any element can be completed"),
        SEQUENCE("Elements must be completed in order"),
        WEIGHTED("Elements have different weights");

        private final String description;

        CompletionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum PathwayStatus {
        DRAFT("Draft"),
        REVIEW("Under Review"),
        PUBLISHED("Published"),
        ARCHIVED("Archived"),
        DEPRECATED("Deprecated");

        private final String displayName;

        PathwayStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum DifficultyLevel {
        BEGINNER("Beginner"),
        INTERMEDIATE("Intermediate"),
        ADVANCED("Advanced"),
        EXPERT("Expert");

        private final String displayName;

        DifficultyLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Business Logic Methods
    public boolean isEditable() {
        return PathwayStatus.DRAFT.equals(status) || PathwayStatus.REVIEW.equals(status);
    }

    public boolean isPublishable() {
        return PathwayStatus.DRAFT.equals(status) && hasValidStructure();
    }

    public boolean hasValidStructure() {
        return elements != null && !elements.isEmpty() && elements.stream()
                .anyMatch(element -> element.getParentElement() == null); // Has root elements
    }

    public List<PathwayElement> getRootElements() {
        return elements.stream()
                .filter(element -> element.getParentElement() == null)
                .sorted((e1, e2) -> Integer.compare(e1.getOrderIndex(), e2.getOrderIndex()))
                .toList();
    }

    public int getTotalElementsCount() {
        return elements != null ? elements.size() : 0;
    }

    public int getTotalBadgesCount() {
        return elements != null ? elements.stream()
                .mapToInt(element -> element.getBadges() != null ? element.getBadges().size() : 0)
                .sum() : 0;
    }

    public double getEstimatedDuration() {
        if (estimatedDurationHours != null) {
            return estimatedDurationHours;
        }
        // Calculate from elements if not set
        return elements != null ? elements.stream()
                .mapToDouble(element -> element.getEstimatedDurationHours() != null ? 
                        element.getEstimatedDurationHours() : 0)
                .sum() : 0;
    }

    // Validation Methods
    public boolean isValidForPublishing() {
        return name != null && !name.trim().isEmpty() &&
               description != null && !description.trim().isEmpty() &&
               hasValidStructure() &&
               PathwayStatus.DRAFT.equals(status);
    }

    public List<String> getValidationErrors() {
        List<String> errors = new ArrayList<>();
        
        if (name == null || name.trim().isEmpty()) {
            errors.add("Pathway name is required");
        }
        
        if (description == null || description.trim().isEmpty()) {
            errors.add("Pathway description is required");
        }
        
        if (!hasValidStructure()) {
            errors.add("Pathway must have at least one root element");
        }
        
        if (difficultyLevel == null) {
            errors.add("Difficulty level is required");
        }
        
        return errors;
    }

    // Utility Methods
    public void addElement(PathwayElement element) {
        if (elements == null) {
            elements = new ArrayList<>();
        }
        element.setPathway(this);
        elements.add(element);
    }

    public void removeElement(PathwayElement element) {
        if (elements != null) {
            elements.remove(element);
            element.setPathway(null);
        }
    }

    public void publish(User publisher) {
        if (isPublishable()) {
            this.status = PathwayStatus.PUBLISHED;
            this.publishedAt = LocalDateTime.now();
            this.publishedBy = publisher;
        }
    }

    public void unpublish() {
        if (PathwayStatus.PUBLISHED.equals(status)) {
            this.status = PathwayStatus.DRAFT;
            this.publishedAt = null;
            this.publishedBy = null;
        }
    }

    public void archive() {
        this.status = PathwayStatus.ARCHIVED;
    }

    public void addTag(String tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public void removeTag(String tag) {
        if (tags != null) {
            tags.remove(tag);
        }
    }

    public void setMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        return metadata != null ? metadata.get(key) : null;
    }
} 