package com.taashee.badger.models;

import jakarta.persistence.*;

@Entity
@Table(name = "endorsement", uniqueConstraints = @UniqueConstraint(columnNames = {"endorser_id", "endorsee_id"}))
public class Endorsement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endorser_id", nullable = false)
    private BadgeClass endorser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endorsee_id", nullable = false)
    private BadgeClass endorsee;

    @Lob
    private String claim;
    @Lob
    private String description;

    public enum Status {
        UNACCEPTED, ACCEPTED, REVOKED, REJECTED
    }

    @Enumerated(EnumType.STRING)
    private Status status = Status.UNACCEPTED;

    private String rejectionReason;
    private String revocationReason;

    private boolean archived;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BadgeClass getEndorser() { return endorser; }
    public void setEndorser(BadgeClass endorser) { this.endorser = endorser; }
    public BadgeClass getEndorsee() { return endorsee; }
    public void setEndorsee(BadgeClass endorsee) { this.endorsee = endorsee; }
    public String getClaim() { return claim; }
    public void setClaim(String claim) { this.claim = claim; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getRevocationReason() { return revocationReason; }
    public void setRevocationReason(String revocationReason) { this.revocationReason = revocationReason; }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
} 