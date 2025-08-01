package com.taashee.badger.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PathwayElementDTO {
    
    private Long id;
    private Long pathwayId;
    private Long badgeClassId;
    private String badgeClassName;
    private String elementType;
    private Integer orderIndex;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private Boolean isCompleted;
} 