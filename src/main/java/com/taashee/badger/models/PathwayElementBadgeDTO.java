package com.taashee.badger.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PathwayElementBadgeDTO {
    
    private Long id;
    private Long elementId;
    private Long badgeClassId;
    private String badgeClassName;
    private String badgeSource;
    private String externalBadgeUrl;
    private String externalBadgeData;
    private Boolean isRequired;
    private LocalDateTime createdAt;
    
    // Additional badge information for display
    private String badgeDescription;
    private String badgeImageUrl;
    private String badgeCriteria;
} 