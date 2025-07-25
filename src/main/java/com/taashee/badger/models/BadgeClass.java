package com.taashee.badger.models;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Type;
import com.taashee.badger.models.Organization;

@Entity
public class BadgeClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    private String name;
    @Column(columnDefinition = "TEXT")
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "badgeclass_tags",
        joinColumns = @JoinColumn(name = "badgeclass_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags;

    @OneToMany(mappedBy = "badgeClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alignment> alignments;

    @Column(columnDefinition = "TEXT")
    private String extensions; // Store as JSON string for flexibility

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "badgeclass_institutions",
        joinColumns = @JoinColumn(name = "badgeclass_id"),
        inverseJoinColumns = @JoinColumn(name = "institution_id"))
    private Set<Institution> awardAllowedInstitutions;

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

    public void setName(String name) { this.name = name; }
    public void setImage(String image) { this.image = image; }
    public void setDescription(String description) { this.description = description; }
    public void setCriteriaText(String criteriaText) { this.criteriaText = criteriaText; }
    public void setFormal(boolean formal) { this.formal = formal; }
    public void setIsPrivate(boolean isPrivate) { this.isPrivate = isPrivate; }
    public void setNarrativeRequired(boolean narrativeRequired) { this.narrativeRequired = narrativeRequired; }
    public void setEvidenceRequired(boolean evidenceRequired) { this.evidenceRequired = evidenceRequired; }
    public void setAwardNonValidatedNameAllowed(boolean awardNonValidatedNameAllowed) { this.awardNonValidatedNameAllowed = awardNonValidatedNameAllowed; }
    public void setIsMicroCredentials(boolean isMicroCredentials) { this.isMicroCredentials = isMicroCredentials; }
    public void setDirectAwardingDisabled(boolean directAwardingDisabled) { this.directAwardingDisabled = directAwardingDisabled; }
    public void setSelfEnrollmentDisabled(boolean selfEnrollmentDisabled) { this.selfEnrollmentDisabled = selfEnrollmentDisabled; }
    public void setParticipation(String participation) { this.participation = participation; }
    public void setAssessmentType(String assessmentType) { this.assessmentType = assessmentType; }
    public void setAssessmentIdVerified(boolean assessmentIdVerified) { this.assessmentIdVerified = assessmentIdVerified; }
    public void setAssessmentSupervised(boolean assessmentSupervised) { this.assessmentSupervised = assessmentSupervised; }
    public void setQualityAssuranceName(String qualityAssuranceName) { this.qualityAssuranceName = qualityAssuranceName; }
    public void setQualityAssuranceUrl(String qualityAssuranceUrl) { this.qualityAssuranceUrl = qualityAssuranceUrl; }
    public void setQualityAssuranceDescription(String qualityAssuranceDescription) { this.qualityAssuranceDescription = qualityAssuranceDescription; }
    public void setGradeAchievedRequired(boolean gradeAchievedRequired) { this.gradeAchievedRequired = gradeAchievedRequired; }
    public void setStackable(boolean stackable) { this.stackable = stackable; }
    public void setEqfNlqfLevelVerified(boolean eqfNlqfLevelVerified) { this.eqfNlqfLevelVerified = eqfNlqfLevelVerified; }
    public void setBadgeClassType(String badgeClassType) { this.badgeClassType = badgeClassType; }
    public void setExpirationPeriod(Duration expirationPeriod) { this.expirationPeriod = expirationPeriod; }
    public void setOrganization(Organization organization) { this.organization = organization; }
    public void setTags(Set<Tag> tags) { this.tags = tags; }
    public void setAlignments(List<Alignment> alignments) { this.alignments = alignments; }
    public void setAwardAllowedInstitutions(Set<Institution> institutions) { this.awardAllowedInstitutions = institutions; }
    public void setExtensions(String extensions) { this.extensions = extensions; }

    public Long getId() { return this.id; }
    public String getName() { return this.name; }
    public String getImage() { return this.image; }
    public String getDescription() { return this.description; }
    public String getCriteriaText() { return this.criteriaText; }
    public boolean isFormal() { return this.formal; }
    public boolean isPrivate() { return this.isPrivate; }
    public boolean isNarrativeRequired() { return this.narrativeRequired; }
    public boolean isEvidenceRequired() { return this.evidenceRequired; }
    public boolean isAwardNonValidatedNameAllowed() { return this.awardNonValidatedNameAllowed; }
    public boolean isMicroCredentials() { return this.isMicroCredentials; }
    public boolean isDirectAwardingDisabled() { return this.directAwardingDisabled; }
    public boolean isSelfEnrollmentDisabled() { return this.selfEnrollmentDisabled; }
    public String getParticipation() { return this.participation; }
    public String getAssessmentType() { return this.assessmentType; }
    public boolean isAssessmentIdVerified() { return this.assessmentIdVerified; }
    public boolean isAssessmentSupervised() { return this.assessmentSupervised; }
    public String getQualityAssuranceName() { return this.qualityAssuranceName; }
    public String getQualityAssuranceUrl() { return this.qualityAssuranceUrl; }
    public String getQualityAssuranceDescription() { return this.qualityAssuranceDescription; }
    public boolean isGradeAchievedRequired() { return this.gradeAchievedRequired; }
    public boolean isStackable() { return this.stackable; }
    public boolean isEqfNlqfLevelVerified() { return this.eqfNlqfLevelVerified; }
    public String getBadgeClassType() { return this.badgeClassType; }
    public Duration getExpirationPeriod() { return this.expirationPeriod; }
    public LocalDateTime getCreatedAt() { return this.createdAt; }
    public LocalDateTime getUpdatedAt() { return this.updatedAt; }
    public Set<Tag> getTags() { return this.tags; }
    public List<Alignment> getAlignments() { return this.alignments; }
    public Set<Institution> getAwardAllowedInstitutions() { return this.awardAllowedInstitutions; }
    public String getExtensions() { return this.extensions; }

    public Organization getOrganization() { return this.organization; }
} 