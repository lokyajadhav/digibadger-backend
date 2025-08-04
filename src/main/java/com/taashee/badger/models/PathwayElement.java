package com.taashee.badger.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "pathway_elements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PathwayElement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pathway_id", nullable = false)
    @JsonIgnore
    private Pathway pathway;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_element_id")
    @JsonIgnore
    private PathwayElement parentElement;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_code", length = 50)
    private String shortCode; // Course code or identifier

    @Enumerated(EnumType.STRING)
    @Column(name = "element_type", length = 50)
    @Builder.Default
    private ElementType elementType = ElementType.ELEMENT;

    @Column(name = "order_index")
    @Builder.Default
    private Integer orderIndex = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "completion_rule", length = 50)
    @Builder.Default
    private CompletionRule completionRule = CompletionRule.ALL;

    @Column(name = "required_count")
    @Builder.Default
    private Integer requiredCount = 1;

    @Column(name = "is_optional")
    @Builder.Default
    private Boolean isOptional = false;

    @Column(name = "counts_towards_parent")
    @Builder.Default
    private Boolean countsTowardsParent = true;

    @Column(name = "estimated_duration_hours")
    private Double estimatedDurationHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", length = 20)
    private DifficultyLevel difficultyLevel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "prerequisites", columnDefinition = "JSON")
    private List<Long> prerequisites = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "competencies", columnDefinition = "JSON")
    private List<CompetencyAlignment> competencies = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "JSON")
    private Map<String, Object> metadata;

    // Relationships
    @OneToMany(mappedBy = "parentElement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private List<PathwayElement> children = new ArrayList<>();

    @OneToMany(mappedBy = "element", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private List<PathwayElementBadge> badges = new ArrayList<>();

    @OneToMany(mappedBy = "element", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private List<PathwayElementProgress> elementProgress = new ArrayList<>();

    // JSON Properties for API responses
    @JsonProperty("pathwayId")
    public Long getPathwayId() {
        return pathway != null ? pathway.getId() : null;
    }

    @JsonProperty("parentElementId")
    public Long getParentElementId() {
        return parentElement != null ? parentElement.getId() : null;
    }

    @JsonProperty("childrenCount")
    public Integer getChildrenCount() {
        return children != null ? children.size() : 0;
    }

    @JsonProperty("badgesCount")
    public Integer getBadgesCount() {
        return badges != null ? badges.size() : 0;
    }

    @JsonProperty("hasChildren")
    public Boolean getHasChildren() {
        return children != null && !children.isEmpty();
    }

    @JsonProperty("isGroup")
    public Boolean getIsGroup() {
        return ElementType.GROUP.equals(elementType);
    }

    @JsonProperty("isMilestone")
    public Boolean getIsMilestone() {
        return ElementType.MILESTONE.equals(elementType);
    }

    @JsonProperty("isRequirement")
    public Boolean getIsRequirement() {
        return ElementType.REQUIREMENT.equals(elementType);
    }

    // Enums
    public enum ElementType {
        ELEMENT("Element", "Basic pathway element"),
        GROUP("Group", "Container for related elements"),
        REQUIREMENT("Requirement", "Specific requirement element"),
        MILESTONE("Milestone", "Major achievement milestone");

        private final String displayName;
        private final String description;

        ElementType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum CompletionRule {
        ALL("All", "All child elements must be completed"),
        SOME("Some", "Some child elements must be completed"),
        EITHER("Either", "Either of the child elements can be completed"),
        SEQUENCE("Sequence", "Elements must be completed in sequence"),
        WEIGHTED("Weighted", "Elements have different weights");

        private final String displayName;
        private final String description;

        CompletionRule(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
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

    // Competency Alignment Class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompetencyAlignment {
        private String framework; // e.g., "CASE", "Common Core"
        private String competencyId;
        private String competencyName;
        private String competencyDescription;
        private String alignmentType; // "exact", "partial", "prerequisite"
        private Double alignmentStrength; // 0.0 to 1.0
        private String alignmentUrl;
    }

    // Business Logic Methods
    public boolean isRootElement() {
        return parentElement == null;
    }

    public boolean isLeafElement() {
        return children == null || children.isEmpty();
    }

    public boolean hasPrerequisites() {
        return prerequisites != null && !prerequisites.isEmpty();
    }

    public boolean isPrerequisiteMet(List<Long> completedElementIds) {
        if (!hasPrerequisites()) {
            return true;
        }
        return prerequisites.stream().allMatch(completedElementIds::contains);
    }

    public boolean canBeCompleted(List<Long> completedElementIds) {
        // Check prerequisites
        if (!isPrerequisiteMet(completedElementIds)) {
            return false;
        }

        // Check if element has required badges
        if (hasRequiredBadges()) {
            return areRequiredBadgesEarned(completedElementIds);
        }

        // Check children completion rules
        if (getHasChildren()) {
            return isChildrenCompletionRuleMet(completedElementIds);
        }

        return true;
    }

    private boolean isChildrenCompletionRuleMet(List<Long> completedElementIds) {
        List<PathwayElement> completableChildren = children.stream()
                .filter(child -> child.countsTowardsParent)
                .toList();

        if (completableChildren.isEmpty()) {
            return true;
        }

        List<Long> completedChildIds = completableChildren.stream()
                .filter(child -> child.isCompleted(completedElementIds))
                .map(PathwayElement::getId)
                .toList();

        switch (completionRule) {
            case ALL:
                return completedChildIds.size() == completableChildren.size();
            case SOME:
                return completedChildIds.size() >= requiredCount;
            case EITHER:
                return !completedChildIds.isEmpty();
            case SEQUENCE:
                return isSequenceCompleted(completedElementIds);
            case WEIGHTED:
                return calculateWeightedProgress(completedElementIds) >= 1.0;
            default:
                return false;
        }
    }

    private boolean isSequenceCompleted(List<Long> completedElementIds) {
        List<PathwayElement> sortedChildren = getSortedChildren();
        int lastCompletedIndex = -1;

        for (int i = 0; i < sortedChildren.size(); i++) {
            PathwayElement child = sortedChildren.get(i);
            if (child.isCompleted(completedElementIds)) {
                lastCompletedIndex = i;
            } else {
                break;
            }
        }

        return lastCompletedIndex == sortedChildren.size() - 1;
    }

    private double calculateWeightedProgress(List<Long> completedElementIds) {
        List<PathwayElement> completableChildren = children.stream()
                .filter(child -> child.countsTowardsParent)
                .toList();

        if (completableChildren.isEmpty()) {
            return 0.0;
        }

        double totalWeight = completableChildren.size();
        double completedWeight = completableChildren.stream()
                .filter(child -> child.isCompleted(completedElementIds))
                .count();

        return completedWeight / totalWeight;
    }

    public boolean isCompleted(List<Long> completedElementIds) {
        return completedElementIds.contains(this.id);
    }

    public boolean hasRequiredBadges() {
        return badges != null && badges.stream().anyMatch(PathwayElementBadge::getIsRequired);
    }

    private boolean areRequiredBadgesEarned(List<Long> completedElementIds) {
        List<PathwayElementBadge> requiredBadges = getRequiredBadges();
        if (requiredBadges.isEmpty()) {
            return true;
        }

        // Check if any required badge is earned (for EITHER rule)
        if (CompletionRule.EITHER.equals(completionRule)) {
            return requiredBadges.stream().anyMatch(badge -> 
                completedElementIds.contains(badge.getBadgeClass().getId()));
        }

        // Check if all required badges are earned (for ALL rule)
        return requiredBadges.stream().allMatch(badge -> 
            completedElementIds.contains(badge.getBadgeClass().getId()));
    }

    public List<PathwayElement> getSortedChildren() {
        if (children == null) {
            return new ArrayList<>();
        }
        return children.stream()
                .sorted((e1, e2) -> Integer.compare(e1.getOrderIndex(), e2.getOrderIndex()))
                .toList();
    }

    public List<PathwayElementBadge> getRequiredBadges() {
        if (badges == null) {
            return new ArrayList<>();
        }
        return badges.stream()
                .filter(PathwayElementBadge::getIsRequired)
                .toList();
    }

    public List<PathwayElementBadge> getOptionalBadges() {
        if (badges == null) {
            return new ArrayList<>();
        }
        return badges.stream()
                .filter(badge -> !badge.getIsRequired())
                .toList();
    }

    public int getTotalBadgesCount() {
        return badges != null ? badges.size() : 0;
    }

    public int getRequiredBadgesCount() {
        return getRequiredBadges().size();
    }

    public int getOptionalBadgesCount() {
        return getOptionalBadges().size();
    }

    // Validation Methods
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
               pathway != null &&
               orderIndex != null && orderIndex >= 0;
    }

    public List<String> getValidationErrors() {
        List<String> errors = new ArrayList<>();
        
        if (name == null || name.trim().isEmpty()) {
            errors.add("Element name is required");
        }
        
        if (pathway == null) {
            errors.add("Element must belong to a pathway");
        }
        
        if (orderIndex == null || orderIndex < 0) {
            errors.add("Order index must be non-negative");
        }
        
        if (requiredCount != null && requiredCount < 1) {
            errors.add("Required count must be at least 1");
        }
        
        if (getHasChildren() && requiredCount != null && requiredCount > children.size()) {
            errors.add("Required count cannot exceed number of children");
        }
        
        return errors;
    }

    // Utility Methods
    public void addChild(PathwayElement child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        child.setParentElement(this);
        child.setPathway(this.pathway);
        children.add(child);
    }

    public void removeChild(PathwayElement child) {
        if (children != null) {
            children.remove(child);
            child.setParentElement(null);
        }
    }

    public void addBadge(PathwayElementBadge badge) {
        if (badges == null) {
            badges = new ArrayList<>();
        }
        badge.setElement(this);
        badges.add(badge);
    }

    public void removeBadge(PathwayElementBadge badge) {
        if (badges != null) {
            badges.remove(badge);
            badge.setElement(null);
        }
    }

    public void addPrerequisite(Long elementId) {
        if (prerequisites == null) {
            prerequisites = new ArrayList<>();
        }
        if (!prerequisites.contains(elementId)) {
            prerequisites.add(elementId);
        }
    }

    public void removePrerequisite(Long elementId) {
        if (prerequisites != null) {
            prerequisites.remove(elementId);
        }
    }

    public void addCompetency(CompetencyAlignment competency) {
        if (competencies == null) {
            competencies = new ArrayList<>();
        }
        competencies.add(competency);
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

    // Tree Navigation Methods
    public PathwayElement getRootElement() {
        if (isRootElement()) {
            return this;
        }
        return parentElement.getRootElement();
    }

    public int getDepth() {
        if (isRootElement()) {
            return 0;
        }
        return parentElement.getDepth() + 1;
    }

    public List<PathwayElement> getAncestors() {
        List<PathwayElement> ancestors = new ArrayList<>();
        PathwayElement current = parentElement;
        while (current != null) {
            ancestors.add(current);
            current = current.getParentElement();
        }
        return ancestors;
    }

    public List<PathwayElement> getDescendants() {
        List<PathwayElement> descendants = new ArrayList<>();
        if (getHasChildren()) {
            for (PathwayElement child : children) {
                descendants.add(child);
                descendants.addAll(child.getDescendants());
            }
        }
        return descendants;
    }

    public boolean isDescendantOf(PathwayElement ancestor) {
        if (parentElement == null) {
            return false;
        }
        if (parentElement.equals(ancestor)) {
            return true;
        }
        return parentElement.isDescendantOf(ancestor);
    }

    public boolean isAncestorOf(PathwayElement descendant) {
        return descendant.isDescendantOf(this);
    }
} 