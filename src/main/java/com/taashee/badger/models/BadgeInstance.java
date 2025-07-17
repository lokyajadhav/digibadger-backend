package com.taashee.badger.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class BadgeInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_class_id")
    private BadgeClass badgeClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issuer_id")
    private Issuer issuer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    private LocalDateTime issuedOn;
    private String publicKeyIssuer; // For now, as String; can be changed to entity if needed
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
    @Lob
    private String oldJson; // For legacy/compatibility
    private String signature;
    private boolean isPublic;
    private boolean includeEvidence;
    private String gradeAchieved;
    private boolean includeGradeAchieved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        AWARDED, REVOKED
    }

    // Getters and setters (or use Lombok @Data)
    public void setId(Long id) { this.id = id; }
    public void setBadgeClass(BadgeClass badgeClass) { this.badgeClass = badgeClass; }
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

    // ...
} 