package com.taashee.badger.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PathwayElementDTO {
    
    private Long id;
    private Long pathwayId;
    private Long parentElementId;
    private String name;
    private String description;
    private String elementType;
    private Integer orderIndex;
    private String completionRule;
    private Integer requiredCount;
    private Boolean isOptional;
    private Boolean countsTowardsParent;
    private LocalDateTime createdAt;
    
    // Tree structure
    private List<PathwayElementDTO> children;
    
    // Badge associations
    private List<PathwayElementBadgeDTO> badges;
    
    // Progress tracking
    private Boolean isCompleted;
    private Integer completedBadges;
    private Integer totalBadges;
} 