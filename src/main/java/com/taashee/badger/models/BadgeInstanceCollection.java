package com.taashee.badger.models;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "badge_instance_collections", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "user_id"})
})
public class BadgeInstanceCollection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 512)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Renamed from isPublic to publicCollection to avoid reserved keyword issues
    private boolean publicCollection = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "collection_badge_instances",
        joinColumns = @JoinColumn(name = "collection_id"),
        inverseJoinColumns = @JoinColumn(name = "badge_instance_id")
    )
    private Set<BadgeInstance> badgeInstances = new HashSet<>();

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public boolean isPublicCollection() { return publicCollection; }
    public void setPublicCollection(boolean publicCollection) { this.publicCollection = publicCollection; }

    public Set<BadgeInstance> getBadgeInstances() { return badgeInstances; }
    public void setBadgeInstances(Set<BadgeInstance> badgeInstances) { this.badgeInstances = badgeInstances; }

    public void setUserId(Long userId) {
        if (this.user == null || this.user.getId() == null || !this.user.getId().equals(userId)) {
            User u = new User();
            u.setId(userId);
            this.user = u;
        }
    }
} 