package com.taashee.badger.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
public class RequirementVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_version_id", nullable = false)
    @JsonBackReference
    private StepVersion stepVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_requirement_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private StepRequirement originalRequirement;

    @Enumerated(EnumType.STRING)
    private StepRequirementType type;

    // For earned credentials
    private Long badgeClassId;

    // For third party
    @Column(columnDefinition = "TEXT")
    private String thirdPartyUrl;

    @Column(columnDefinition = "TEXT")
    private String thirdPartyJson;

    // For manual experience
    private String experienceName;

    @Column(columnDefinition = "TEXT")
    private String experienceDescription;

    private String groupKey;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public RequirementVersion() {}

    public RequirementVersion(StepVersion stepVersion, StepRequirement originalRequirement) {
        this.stepVersion = stepVersion;
        this.originalRequirement = originalRequirement;
        
        // Copy requirement data
        this.type = originalRequirement.getType();
        this.badgeClassId = originalRequirement.getBadgeClassId();
        this.thirdPartyUrl = originalRequirement.getThirdPartyUrl();
        this.thirdPartyJson = originalRequirement.getThirdPartyJson();
        this.experienceName = originalRequirement.getExperienceName();
        this.experienceDescription = originalRequirement.getExperienceDescription();
        this.groupKey = originalRequirement.getGroupKey();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StepVersion getStepVersion() { return stepVersion; }
    public void setStepVersion(StepVersion stepVersion) { this.stepVersion = stepVersion; }

    public StepRequirement getOriginalRequirement() { return originalRequirement; }
    public void setOriginalRequirement(StepRequirement originalRequirement) { this.originalRequirement = originalRequirement; }

    public StepRequirementType getType() { return type; }
    public void setType(StepRequirementType type) { this.type = type; }

    public Long getBadgeClassId() { return badgeClassId; }
    public void setBadgeClassId(Long badgeClassId) { this.badgeClassId = badgeClassId; }

    public String getThirdPartyUrl() { return thirdPartyUrl; }
    public void setThirdPartyUrl(String thirdPartyUrl) { this.thirdPartyUrl = thirdPartyUrl; }

    public String getThirdPartyJson() { return thirdPartyJson; }
    public void setThirdPartyJson(String thirdPartyJson) { this.thirdPartyJson = thirdPartyJson; }

    public String getExperienceName() { return experienceName; }
    public void setExperienceName(String experienceName) { this.experienceName = experienceName; }

    public String getExperienceDescription() { return experienceDescription; }
    public void setExperienceDescription(String experienceDescription) { this.experienceDescription = experienceDescription; }

    public String getGroupKey() { return groupKey; }
    public void setGroupKey(String groupKey) { this.groupKey = groupKey; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
