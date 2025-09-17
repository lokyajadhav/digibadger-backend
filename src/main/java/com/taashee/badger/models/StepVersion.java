package com.taashee.badger.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
public class StepVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pathway_version_id", nullable = false)
    @JsonBackReference
    private PathwayVersion pathwayVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_step_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private PathwayStep originalStep;

    private Long parentStepId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String shortCode;
    private Boolean optionalStep;
    private Integer orderIndex;
    private Boolean milestone;
    private Long achievementBadgeId;
    private Boolean achievementExternal;
    private String prerequisiteRule;
    private String prerequisiteSteps;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "stepVersion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<RequirementVersion> requirementVersions = new ArrayList<>();

    // Constructors
    public StepVersion() {}

    public StepVersion(PathwayVersion pathwayVersion, PathwayStep originalStep) {
        this.pathwayVersion = pathwayVersion;
        this.originalStep = originalStep;
        
        // Copy step data
        this.parentStepId = originalStep.getParentStep() != null ? originalStep.getParentStep().getId() : null;
        this.name = originalStep.getName();
        this.description = originalStep.getDescription();
        this.shortCode = originalStep.getShortCode();
        this.optionalStep = originalStep.isOptionalStep();
        this.orderIndex = originalStep.getOrderIndex();
        this.milestone = originalStep.isMilestone();
        this.achievementBadgeId = originalStep.getAchievementBadgeId();
        this.achievementExternal = originalStep.getAchievementExternal();
        this.prerequisiteRule = originalStep.getPrerequisiteRule();
        this.prerequisiteSteps = originalStep.getPrerequisiteSteps();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PathwayVersion getPathwayVersion() { return pathwayVersion; }
    public void setPathwayVersion(PathwayVersion pathwayVersion) { this.pathwayVersion = pathwayVersion; }

    public PathwayStep getOriginalStep() { return originalStep; }
    public void setOriginalStep(PathwayStep originalStep) { this.originalStep = originalStep; }

    public Long getParentStepId() { return parentStepId; }
    public void setParentStepId(Long parentStepId) { this.parentStepId = parentStepId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public Boolean getOptionalStep() { return optionalStep; }
    public void setOptionalStep(Boolean optionalStep) { this.optionalStep = optionalStep; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public Boolean getMilestone() { return milestone; }
    public void setMilestone(Boolean milestone) { this.milestone = milestone; }

    public Long getAchievementBadgeId() { return achievementBadgeId; }
    public void setAchievementBadgeId(Long achievementBadgeId) { this.achievementBadgeId = achievementBadgeId; }

    public Boolean getAchievementExternal() { return achievementExternal; }
    public void setAchievementExternal(Boolean achievementExternal) { this.achievementExternal = achievementExternal; }

    public String getPrerequisiteRule() { return prerequisiteRule; }
    public void setPrerequisiteRule(String prerequisiteRule) { this.prerequisiteRule = prerequisiteRule; }

    public String getPrerequisiteSteps() { return prerequisiteSteps; }
    public void setPrerequisiteSteps(String prerequisiteSteps) { this.prerequisiteSteps = prerequisiteSteps; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<RequirementVersion> getRequirementVersions() { return requirementVersions; }
    public void setRequirementVersions(List<RequirementVersion> requirementVersions) { this.requirementVersions = requirementVersions; }
}