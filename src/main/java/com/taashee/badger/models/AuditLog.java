package com.taashee.badger.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AuditAction action;

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private Long entityId;

    private String entityName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String changes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pathway_id")
    private Pathway pathway;

    private String ipAddress;
    private String userAgent;

    public enum AuditAction {
        CREATE, UPDATE, DELETE, PUBLISH, ARCHIVE, VIEW
    }

    // Constructors
    public AuditLog() {}

    public AuditLog(AuditAction action, String entityType, Long entityId, User user) {
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.user = user;
    }

    public AuditLog(AuditAction action, String entityType, Long entityId, String entityName, String description, User user) {
        this(action, entityType, entityId, user);
        this.entityName = entityName;
        this.description = description;
    }

    // Static factory methods for common audit actions
    public static AuditLog createPathwayLog(String action, Pathway pathway, User user, String description) {
        return new AuditLog(AuditAction.valueOf(action.toUpperCase()), "Pathway", pathway.getId(), pathway.getName(), description, user);
    }

    public static AuditLog createStepLog(String action, PathwayStep step, User user, String description) {
        return new AuditLog(AuditAction.valueOf(action.toUpperCase()), "PathwayStep", step.getId(), step.getName(), description, user);
    }

    public static AuditLog createRequirementLog(String action, StepRequirement requirement, User user, String description) {
        return new AuditLog(AuditAction.valueOf(action.toUpperCase()), "StepRequirement", requirement.getId(), "Requirement", description, user);
    }

    public static AuditLog createPublishLog(Pathway pathway, User user, Integer version) {
        return new AuditLog(AuditAction.PUBLISH, "Pathway", pathway.getId(), pathway.getName(), 
            "Published pathway as version " + version, user);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AuditAction getAction() { return action; }
    public void setAction(AuditAction action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getChanges() { return changes; }
    public void setChanges(String changes) { this.changes = changes; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Pathway getPathway() { return pathway; }
    public void setPathway(Pathway pathway) { this.pathway = pathway; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
}