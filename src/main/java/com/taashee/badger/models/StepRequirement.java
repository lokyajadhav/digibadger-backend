package com.taashee.badger.models;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.taashee.badger.models.PathwayStep;
import com.taashee.badger.models.StepRequirementType;

@Entity
public class StepRequirement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    @JsonBackReference
    private PathwayStep step;

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

    public Long getId() { return id; }
    public PathwayStep getStep() { return step; }
    public StepRequirementType getType() { return type; }
    public Long getBadgeClassId() { return badgeClassId; }
    public String getThirdPartyUrl() { return thirdPartyUrl; }
    public String getThirdPartyJson() { return thirdPartyJson; }
    public String getExperienceName() { return experienceName; }
    public String getExperienceDescription() { return experienceDescription; }
    public String getGroupKey() { return groupKey; }

    public void setId(Long id) { this.id = id; }
    public void setStep(PathwayStep step) { this.step = step; }
    public void setType(StepRequirementType type) { this.type = type; }
    public void setBadgeClassId(Long badgeClassId) { this.badgeClassId = badgeClassId; }
    public void setThirdPartyUrl(String thirdPartyUrl) { this.thirdPartyUrl = thirdPartyUrl; }
    public void setThirdPartyJson(String thirdPartyJson) { this.thirdPartyJson = thirdPartyJson; }
    public void setExperienceName(String experienceName) { this.experienceName = experienceName; }
    public void setExperienceDescription(String experienceDescription) { this.experienceDescription = experienceDescription; }
    public void setGroupKey(String groupKey) { this.groupKey = groupKey; }
}


