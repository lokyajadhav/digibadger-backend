package com.taashee.badger.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.taashee.badger.models.Pathway;
import com.taashee.badger.models.StepRequirement;
import com.taashee.badger.models.StepPrerequisite;

@Entity
public class PathwayStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pathway_id", nullable = false)
    @JsonBackReference
    private Pathway pathway;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_step_id")
    @JsonBackReference
    private PathwayStep parentStep;
    
    // Add parentStepId for JSON serialization
    @Transient
    public Long getParentStepId() {
        return parentStep != null ? parentStep.getId() : null;
    }

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String shortCode;

    private boolean optionalStep = false;

    private int orderIndex = 0;

    private String alignmentUrl;
    private String targetCode;
    private String frameworkName;

    private boolean milestone = false; // end of pathway node

    // Achievement Configuration
    private Long achievementBadgeId; // BadgeClass.id for achievement
    private Boolean achievementExternal = false;

    @OneToMany(mappedBy = "step", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StepRequirement> requirements = new ArrayList<>();

    @OneToMany(mappedBy = "step", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StepPrerequisite> prerequisites = new ArrayList<>();

    // Helper method to get children (steps that have this step as parent)
    @Transient
    public List<PathwayStep> getChildren() {
        // This will be populated by the service layer
        return new ArrayList<>();
    }

    public Long getId() { return id; }
    public Pathway getPathway() { return pathway; }
    public PathwayStep getParentStep() { return parentStep; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getShortCode() { return shortCode; }
    public boolean isOptionalStep() { return optionalStep; }
    public int getOrderIndex() { return orderIndex; }
    public String getAlignmentUrl() { return alignmentUrl; }
    public String getTargetCode() { return targetCode; }
    public String getFrameworkName() { return frameworkName; }
    public boolean isMilestone() { return milestone; }
    public Long getAchievementBadgeId() { return achievementBadgeId; }
    public Boolean getAchievementExternal() { return achievementExternal; }
    public List<StepRequirement> getRequirements() { return requirements; }
    public List<StepPrerequisite> getPrerequisites() { return prerequisites; }

    public void setId(Long id) { this.id = id; }
    public void setPathway(Pathway pathway) { this.pathway = pathway; }
    public void setParentStep(PathwayStep parentStep) { this.parentStep = parentStep; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public void setOptionalStep(boolean optionalStep) { this.optionalStep = optionalStep; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    public void setAlignmentUrl(String alignmentUrl) { this.alignmentUrl = alignmentUrl; }
    public void setTargetCode(String targetCode) { this.targetCode = targetCode; }
    public void setFrameworkName(String frameworkName) { this.frameworkName = frameworkName; }
    public void setMilestone(boolean milestone) { this.milestone = milestone; }
    public void setAchievementBadgeId(Long achievementBadgeId) { this.achievementBadgeId = achievementBadgeId; }
    public void setAchievementExternal(Boolean achievementExternal) { this.achievementExternal = achievementExternal; }
    public void setRequirements(List<StepRequirement> requirements) { this.requirements = requirements; }
    public void setPrerequisites(List<StepPrerequisite> prerequisites) { this.prerequisites = prerequisites; }
}


