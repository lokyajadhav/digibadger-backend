package com.taashee.badger.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PathwayDTO {
    
    private Long id;
    private String name;
    private String description;
    private Long organizationId;
    private String organizationName;
    private String completionType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PathwayElementDTO> elements;
    private Integer totalElements;
    private Integer completedElements;
    private Double progressPercentage;
    private Boolean isCompleted;
} 