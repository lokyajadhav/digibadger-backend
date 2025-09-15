package com.taashee.badger.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
public class PathwayVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pathway_id", nullable = false)
    private Pathway pathway;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VersionStatus status;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String shortCode;
    private String alignmentUrl;
    private String targetCode;
    private String frameworkName;
    private Long completionBadgeId;
    private Boolean completionBadgeExternal;
    private String prerequisiteRule;
    private String prerequisiteSteps;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "pathwayVersion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<StepVersion> stepVersions = new ArrayList<>();

    public enum VersionStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }

    // Constructors
    public PathwayVersion() {}

    public PathwayVersion(Pathway pathway, Integer version, VersionStatus status, User createdBy) {
        this.pathway = pathway;
        this.version = version;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        
        // Copy pathway data
        this.name = pathway.getName();
        this.description = pathway.getDescription();
        this.shortCode = pathway.getShortCode();
        this.alignmentUrl = pathway.getAlignmentUrl();
        this.targetCode = pathway.getTargetCode();
        this.frameworkName = pathway.getFrameworkName();
        this.completionBadgeId = pathway.getCompletionBadgeId();
        this.completionBadgeExternal = pathway.getCompletionBadgeExternal();
        this.prerequisiteRule = pathway.getPrerequisiteRule();
        this.prerequisiteSteps = pathway.getPrerequisiteSteps();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Pathway getPathway() { return pathway; }
    public void setPathway(Pathway pathway) { this.pathway = pathway; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public VersionStatus getStatus() { return status; }
    public void setStatus(VersionStatus status) { this.status = status; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public String getAlignmentUrl() { return alignmentUrl; }
    public void setAlignmentUrl(String alignmentUrl) { this.alignmentUrl = alignmentUrl; }

    public String getTargetCode() { return targetCode; }
    public void setTargetCode(String targetCode) { this.targetCode = targetCode; }

    public String getFrameworkName() { return frameworkName; }
    public void setFrameworkName(String frameworkName) { this.frameworkName = frameworkName; }

    public Long getCompletionBadgeId() { return completionBadgeId; }
    public void setCompletionBadgeId(Long completionBadgeId) { this.completionBadgeId = completionBadgeId; }

    public Boolean getCompletionBadgeExternal() { return completionBadgeExternal; }
    public void setCompletionBadgeExternal(Boolean completionBadgeExternal) { this.completionBadgeExternal = completionBadgeExternal; }

    public String getPrerequisiteRule() { return prerequisiteRule; }
    public void setPrerequisiteRule(String prerequisiteRule) { this.prerequisiteRule = prerequisiteRule; }

    public String getPrerequisiteSteps() { return prerequisiteSteps; }
    public void setPrerequisiteSteps(String prerequisiteSteps) { this.prerequisiteSteps = prerequisiteSteps; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public List<StepVersion> getStepVersions() { return stepVersions; }
    public void setStepVersions(List<StepVersion> stepVersions) { this.stepVersions = stepVersions; }
}