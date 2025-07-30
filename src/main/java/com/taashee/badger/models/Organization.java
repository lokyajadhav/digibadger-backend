package com.taashee.badger.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String nameEnglish;
    
    @Column(columnDefinition = "TEXT")
    private String descriptionEnglish;
    
    @Column(columnDefinition = "TEXT")
    private String imageEnglish;
    
    @Column(length = 1000)
    private String urlEnglish;
    
    @Column(length = 255)
    private String email;
    
    @Column(length = 500)
    private String faculty;
    
    @Column(length = 500)
    private String institutionName;
    
    @Column(length = 255)
    private String institutionIdentifier;
    
    @Column(length = 1000)
    private String gradingTableUrl;

    private boolean archived;
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @Column(length = 1000)
    private String badgrApp;
    
    @Column(columnDefinition = "TEXT")
    private String oldJson;
    
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<BadgeClass> badges;
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<com.taashee.badger.models.OrganizationStaff> staff;

    // Getters and setters (only for English fields)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNameEnglish() { return nameEnglish; }
    public void setNameEnglish(String nameEnglish) { this.nameEnglish = nameEnglish; }
    public String getDescriptionEnglish() { return descriptionEnglish; }
    public void setDescriptionEnglish(String descriptionEnglish) { this.descriptionEnglish = descriptionEnglish; }
    public String getImageEnglish() { return imageEnglish; }
    public void setImageEnglish(String imageEnglish) { this.imageEnglish = imageEnglish; }
    public String getUrlEnglish() { return urlEnglish; }
    public void setUrlEnglish(String urlEnglish) { this.urlEnglish = urlEnglish; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }
    public boolean getArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getBadgrApp() { return badgrApp; }
    public void setBadgrApp(String badgrApp) { this.badgrApp = badgrApp; }
    public String getOldJson() { return oldJson; }
    public void setOldJson(String oldJson) { this.oldJson = oldJson; }
    @com.fasterxml.jackson.annotation.JsonIgnore
    public List<BadgeClass> getBadges() { return badges; }
    public void setBadges(List<BadgeClass> badges) { this.badges = badges; }
    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }
    public String getInstitutionIdentifier() { return institutionIdentifier; }
    public void setInstitutionIdentifier(String institutionIdentifier) { this.institutionIdentifier = institutionIdentifier; }
    public String getGradingTableUrl() { return gradingTableUrl; }
    public void setGradingTableUrl(String gradingTableUrl) { this.gradingTableUrl = gradingTableUrl; }
    @com.fasterxml.jackson.annotation.JsonIgnore
    public List<com.taashee.badger.models.OrganizationStaff> getStaff() { return staff; }
    public void setStaff(List<com.taashee.badger.models.OrganizationStaff> staff) { this.staff = staff; }
} 