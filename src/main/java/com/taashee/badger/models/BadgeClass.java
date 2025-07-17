package com.taashee.badger.models;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
public class BadgeClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issuer_id")
    private Issuer issuer;

    private String name;
    private String image;
    private String description;
    private String criteriaText;
    private boolean formal;
    private boolean isPrivate;
    private boolean narrativeRequired;
    private boolean evidenceRequired;
    private boolean awardNonValidatedNameAllowed;
    private boolean isMicroCredentials;
    private boolean directAwardingDisabled;
    private boolean selfEnrollmentDisabled;
    private String participation;
    private String assessmentType;
    private boolean assessmentIdVerified;
    private boolean assessmentSupervised;
    private String qualityAssuranceName;
    private String qualityAssuranceUrl;
    private String qualityAssuranceDescription;
    private boolean gradeAchievedRequired;
    private boolean stackable;
    private boolean eqfNlqfLevelVerified;
    private String badgeClassType;
    @Lob
    private String oldJson; // For legacy/compatibility
    private Duration expirationPeriod;
    private boolean archived;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "badgeclass_staff",
        joinColumns = @JoinColumn(name = "badgeclass_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> staff;

    // For now, awardAllowedInstitutions and tags as String; can be changed to entity if needed
    private String awardAllowedInstitutions;
    private String tags;

    @OneToMany(mappedBy = "badgeClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BadgeInstance> instances;

    // Getters and setters (or use Lombok @Data)
    public void setId(Long id) { this.id = id; }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
    public boolean getArchived() {
        return this.archived;
    }

    // ...
} 