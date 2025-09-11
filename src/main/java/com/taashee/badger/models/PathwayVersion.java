package com.taashee.badger.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;

@Entity
public class PathwayVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pathway_id", nullable = false)
    private Pathway pathway;

    @Column(nullable = false)
    private Long versionNumber;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private PathwayStatus status;

    private Long completionBadgeId;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public Pathway getPathway() { return pathway; }
    public Long getVersionNumber() { return versionNumber; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public PathwayStatus getStatus() { return status; }
    public Long getCompletionBadgeId() { return completionBadgeId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setPathway(Pathway pathway) { this.pathway = pathway; }
    public void setVersionNumber(Long versionNumber) { this.versionNumber = versionNumber; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(PathwayStatus status) { this.status = status; }
    public void setCompletionBadgeId(Long completionBadgeId) { this.completionBadgeId = completionBadgeId; }
}
