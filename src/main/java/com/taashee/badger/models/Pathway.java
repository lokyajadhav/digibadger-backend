package com.taashee.badger.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.taashee.badger.models.Organization;
import com.taashee.badger.models.PathwayStep;
import com.taashee.badger.models.PathwayStatus;

@Entity
public class Pathway {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonBackReference
    private Organization organization;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private PathwayStatus status = PathwayStatus.DRAFT;

    private Long currentDraftVersionId;

    private Long completionBadgeId; // BadgeClass.id

    private String shortCode;

    // Alignment Data fields
    private String alignmentUrl;
    private String targetCode;
    private String frameworkName;

    // Completion Badge Configuration
    private Boolean completionBadgeExternal = false;

    // Prerequisite Configuration
    private String prerequisiteRule = "all"; // "all", "any", "n-of-m"
    private String prerequisiteSteps; // JSON array of step IDs

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "pathway", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @JsonManagedReference
    private List<PathwayStep> steps = new ArrayList<>();

    public Long getId() { return id; }
    public Organization getOrganization() { return organization; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public PathwayStatus getStatus() { return status; }
    public Long getCurrentDraftVersionId() { return currentDraftVersionId; }
    public Long getCompletionBadgeId() { return completionBadgeId; }
    public String getShortCode() { return shortCode; }
    public String getAlignmentUrl() { return alignmentUrl; }
    public String getTargetCode() { return targetCode; }
    public String getFrameworkName() { return frameworkName; }
    public Boolean getCompletionBadgeExternal() { return completionBadgeExternal; }
    public String getPrerequisiteRule() { return prerequisiteRule; }
    public String getPrerequisiteSteps() { return prerequisiteSteps; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<PathwayStep> getSteps() { return steps; }

    public void setId(Long id) { this.id = id; }
    public void setOrganization(Organization organization) { this.organization = organization; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(PathwayStatus status) { this.status = status; }
    public void setCurrentDraftVersionId(Long currentDraftVersionId) { this.currentDraftVersionId = currentDraftVersionId; }
    public void setCompletionBadgeId(Long completionBadgeId) { this.completionBadgeId = completionBadgeId; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public void setAlignmentUrl(String alignmentUrl) { this.alignmentUrl = alignmentUrl; }
    public void setTargetCode(String targetCode) { this.targetCode = targetCode; }
    public void setFrameworkName(String frameworkName) { this.frameworkName = frameworkName; }
    public void setCompletionBadgeExternal(Boolean completionBadgeExternal) { this.completionBadgeExternal = completionBadgeExternal; }
    public void setPrerequisiteRule(String prerequisiteRule) { this.prerequisiteRule = prerequisiteRule; }
    public void setPrerequisiteSteps(String prerequisiteSteps) { this.prerequisiteSteps = prerequisiteSteps; }
    public void setSteps(List<PathwayStep> steps) { this.steps = steps; }
}


