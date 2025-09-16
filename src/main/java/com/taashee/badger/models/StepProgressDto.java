package com.taashee.badger.models;

import java.time.LocalDateTime;

public class StepProgressDto {
    private Long id;
    private Long stepVersionId;
    private String stepName;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long groupId;
    private String groupName;
    private StepProgress.ProgressStatus status;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public StepProgressDto() {}

    // Constructor from StepProgress entity
    public StepProgressDto(StepProgress stepProgress) {
        this.id = stepProgress.getId();
        this.stepVersionId = stepProgress.getStepVersion().getId();
        this.stepName = stepProgress.getStepVersion().getName();
        this.userId = stepProgress.getUser().getId();
        this.userName = stepProgress.getUser().getFirstName() + " " + stepProgress.getUser().getLastName();
        this.userEmail = stepProgress.getUser().getEmail();
        this.groupId = stepProgress.getGroup().getId();
        this.groupName = stepProgress.getGroup().getName();
        this.status = stepProgress.getStatus();
        this.completedAt = stepProgress.getCompletedAt();
        this.createdAt = stepProgress.getCreatedAt();
        this.updatedAt = stepProgress.getUpdatedAt();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStepVersionId() { return stepVersionId; }
    public void setStepVersionId(Long stepVersionId) { this.stepVersionId = stepVersionId; }

    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }

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

    public StepProgress.ProgressStatus getStatus() { return status; }
    public void setStatus(StepProgress.ProgressStatus status) { this.status = status; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
