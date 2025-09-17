package com.taashee.badger.models;

import java.time.LocalDateTime;

public class PathwayProgressDto {
    private Long id;
    private Long pathwayId;
    private String pathwayName;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long groupId;
    private String groupName;
    private Integer percent;
    private Boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public PathwayProgressDto() {}

    // Constructor from PathwayProgress entity
    public PathwayProgressDto(PathwayProgress progress) {
        this.id = progress.getId();
        this.pathwayId = progress.getPathway().getId();
        this.pathwayName = progress.getPathway().getName();
        this.userId = progress.getUser().getId();
        this.userName = progress.getUser().getFirstName() + " " + progress.getUser().getLastName();
        this.userEmail = progress.getUser().getEmail();
        this.groupId = progress.getGroup().getId();
        this.groupName = progress.getGroup().getName();
        this.percent = progress.getPercent();
        this.completed = progress.getCompleted();
        this.createdAt = progress.getCreatedAt();
        this.updatedAt = progress.getUpdatedAt();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPathwayId() { return pathwayId; }
    public void setPathwayId(Long pathwayId) { this.pathwayId = pathwayId; }

    public String getPathwayName() { return pathwayName; }
    public void setPathwayName(String pathwayName) { this.pathwayName = pathwayName; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public Integer getPercent() { return percent; }
    public void setPercent(Integer percent) { this.percent = percent; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
