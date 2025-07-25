package com.taashee.badger.models;

import jakarta.persistence.*;

@Entity
@Table(name = "alignments")
public class Alignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_class_id")
    private BadgeClass badgeClass;

    private String targetName;
    private String targetUrl;
    private String targetDescription;
    private String targetFramework;
    private String targetCode;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BadgeClass getBadgeClass() { return badgeClass; }
    public void setBadgeClass(BadgeClass badgeClass) { this.badgeClass = badgeClass; }
    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }
    public String getTargetDescription() { return targetDescription; }
    public void setTargetDescription(String targetDescription) { this.targetDescription = targetDescription; }
    public String getTargetFramework() { return targetFramework; }
    public void setTargetFramework(String targetFramework) { this.targetFramework = targetFramework; }
    public String getTargetCode() { return targetCode; }
    public void setTargetCode(String targetCode) { this.targetCode = targetCode; }
} 