package com.taashee.badger.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;

@Entity
public class StepVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pathway_version_id", nullable = false)
    private PathwayVersion pathwayVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_step_version_id")
    private StepVersion parentStepVersion;

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

    private boolean milestone = false;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public PathwayVersion getPathwayVersion() { return pathwayVersion; }
    public StepVersion getParentStepVersion() { return parentStepVersion; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getShortCode() { return shortCode; }
    public boolean isOptionalStep() { return optionalStep; }
    public int getOrderIndex() { return orderIndex; }
    public String getAlignmentUrl() { return alignmentUrl; }
    public String getTargetCode() { return targetCode; }
    public String getFrameworkName() { return frameworkName; }
    public boolean isMilestone() { return milestone; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setPathwayVersion(PathwayVersion pathwayVersion) { this.pathwayVersion = pathwayVersion; }
    public void setParentStepVersion(StepVersion parentStepVersion) { this.parentStepVersion = parentStepVersion; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public void setOptionalStep(boolean optionalStep) { this.optionalStep = optionalStep; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    public void setAlignmentUrl(String alignmentUrl) { this.alignmentUrl = alignmentUrl; }
    public void setTargetCode(String targetCode) { this.targetCode = targetCode; }
    public void setFrameworkName(String frameworkName) { this.frameworkName = frameworkName; }
    public void setMilestone(boolean milestone) { this.milestone = milestone; }
}
