package com.taashee.badger.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.annotations.Type;

@Entity
public class BadgeInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_class_id")
    private BadgeClass badgeClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    private LocalDateTime issuedOn;
    private String publicKeyOrganization; // For now, as String; can be changed to entity if needed
    private String identifier;
    private String recipientType;
    private String awardType;
    private String directAwardBundle; // For now, as String; can be changed to entity if needed
    private String recipientIdentifier;
    private String image;
    private boolean revoked;
    private String revocationReason;
    private LocalDateTime expiresAt;
    private String acceptance;
    private String narrative;
    private boolean hashed;
    private String salt;
    private boolean archived;
    // Make fields private and use getters
    private String oldJson;
    private String signature;
    private boolean isPublic;
    private boolean includeEvidence;
    private String gradeAchieved;
    private boolean includeGradeAchieved;
    private Status status;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(columnDefinition = "TEXT")
    private String learningOutcomes;
    @Column(columnDefinition = "TEXT")
    private String extensions;

    @OneToMany(mappedBy = "badgeInstance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evidence> evidenceItems;

    public enum Status {
        ACTIVE, REVOKED, EXPIRED
    }

    // Getters and setters (or use Lombok @Data)
    public void setId(Long id) { this.id = id; }
    public void setBadgeClass(BadgeClass badgeClass) { this.badgeClass = badgeClass; }
    public void setOrganization(Organization organization) { this.organization = organization; }
    public void setRecipient(User recipient) { this.recipient = recipient; }
    public void setIssuedOn(java.time.LocalDateTime issuedOn) { this.issuedOn = issuedOn; }
    public void setPublicKeyOrganization(String publicKeyOrganization) { this.publicKeyOrganization = publicKeyOrganization; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }
    public void setRecipientIdentifier(String recipientIdentifier) { this.recipientIdentifier = recipientIdentifier; }
    public void setRecipientType(String recipientType) { this.recipientType = recipientType; }
    public void setAwardType(String awardType) { this.awardType = awardType; }
    public void setNarrative(String narrative) { this.narrative = narrative; }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
    public String getRevocationReason() { return revocationReason; }
    public void setRevocationReason(String revocationReason) { this.revocationReason = revocationReason; }
    public Long getId() { return id; }
    public void setImage(String image) { this.image = image; }
    public void setExpiresAt(java.time.LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setAcceptance(String acceptance) { this.acceptance = acceptance; }
    public void setHashed(boolean hashed) { this.hashed = hashed; }
    public void setDirectAwardBundle(String directAwardBundle) { this.directAwardBundle = directAwardBundle; }
    public void setSalt(String salt) { this.salt = salt; }
    public void setOldJson(String oldJson) { this.oldJson = oldJson; }
    public void setSignature(String signature) { this.signature = signature; }
    public void setIsPublic(boolean isPublic) { this.isPublic = isPublic; }
    public void setIncludeEvidence(boolean includeEvidence) { this.includeEvidence = includeEvidence; }
    public void setGradeAchieved(String gradeAchieved) { this.gradeAchieved = gradeAchieved; }
    public void setIncludeGradeAchieved(boolean includeGradeAchieved) { this.includeGradeAchieved = includeGradeAchieved; }
    public void setStatus(Status status) { this.status = status; }
    public void setDescription(String description) { this.description = description; }
    public void setLearningOutcomes(String learningOutcomes) { this.learningOutcomes = learningOutcomes; }
    public void setExtensions(String extensions) { this.extensions = extensions; }
    public void setEvidenceItems(java.util.List<Evidence> evidenceItems) { this.evidenceItems = evidenceItems; }
    public BadgeClass getBadgeClass() { return this.badgeClass; }
    public Organization getOrganization() { return this.organization; }
    public User getRecipient() { return this.recipient; }
    public LocalDateTime getIssuedOn() { return this.issuedOn; }
    public String getPublicKeyOrganization() { return this.publicKeyOrganization; }
    public String getIdentifier() { return this.identifier; }
    public String getRecipientType() { return this.recipientType; }
    public String getAwardType() { return this.awardType; }
    public String getDirectAwardBundle() { return this.directAwardBundle; }
    public String getRecipientIdentifier() { return this.recipientIdentifier; }
    public String getImage() { return this.image; }
    public LocalDateTime getExpiresAt() { return this.expiresAt; }
    public String getAcceptance() { return this.acceptance; }
    public String getNarrative() { return this.narrative; }
    public boolean isHashed() { return this.hashed; }
    public String getSalt() { return this.salt; }
    public String getOldJson() { return this.oldJson; }
    public String getSignature() { return this.signature; }
    public boolean getIsPublic() { return this.isPublic; }
    public boolean getIncludeEvidence() { return this.includeEvidence; }
    public String getGradeAchieved() { return this.gradeAchieved; }
    public boolean getIncludeGradeAchieved() { return this.includeGradeAchieved; }
    public Status getStatus() { return this.status; }
    public String getDescription() { return this.description; }
    public String getLearningOutcomes() { return this.learningOutcomes; }
    public List<Evidence> getEvidenceItems() { return this.evidenceItems; }
    public String getExtensions() { return this.extensions; }
    
    // Method to dynamically determine the current status
    public Status getCurrentStatus() {
        if (this.revoked) {
            return Status.REVOKED;
        }
        if (this.expiresAt != null && this.expiresAt.isBefore(java.time.LocalDateTime.now())) {
            return Status.EXPIRED;
        }
        return Status.ACTIVE;
    }
} 