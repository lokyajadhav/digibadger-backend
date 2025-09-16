package com.taashee.badger.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "pathway_group_subscriptions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"pathway_id", "group_id"}))
public class PathwayGroupSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pathway_id", nullable = false)
    @JsonBackReference
    private Pathway pathway;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @JsonBackReference
    private Group group;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime subscribedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscribed_by", nullable = false)
    @JsonBackReference
    private User subscribedBy;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Pathway getPathway() { return pathway; }
    public void setPathway(Pathway pathway) { this.pathway = pathway; }

    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }

    public LocalDateTime getSubscribedAt() { return subscribedAt; }
    public void setSubscribedAt(LocalDateTime subscribedAt) { this.subscribedAt = subscribedAt; }

    public User getSubscribedBy() { return subscribedBy; }
    public void setSubscribedBy(User subscribedBy) { this.subscribedBy = subscribedBy; }
}
