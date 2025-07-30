package com.taashee.badger.models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.Map;

public class BadgeClassResponseDTO {
    public Long id;
    public String name;
    public String image;
    public String description;
    public String criteriaText;
    public String criteriaUrl;
    public boolean formal;
    public boolean isPrivate;
    public boolean narrativeRequired;
    public boolean evidenceRequired;
    public boolean awardNonValidatedNameAllowed;
    public boolean isMicroCredentials;
    public boolean directAwardingDisabled;
    public boolean selfEnrollmentDisabled;
    public String participation;
    public String assessmentType;
    public boolean assessmentIdVerified;
    public boolean assessmentSupervised;
    public String qualityAssuranceName;
    public String qualityAssuranceUrl;
    public String qualityAssuranceDescription;
    public boolean gradeAchievedRequired;
    public boolean stackable;
    public boolean eqfNlqfLevelVerified;
    public String badgeClassType;
    public Duration expirationPeriod;
    public LocalDateTime expirationDate;
    public boolean archived;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public Long organizationId;
    public OrganizationDTO organization;
    public List<String> tagNames;
    public List<AlignmentDTO> alignments;
    public List<Long> institutionIds;
    public Map<String, Object> extensions;
    public Long activeAwardsCount;

    public static class AlignmentDTO {
        public String targetName;
        public String targetUrl;
        public String targetDescription;
        public String targetFramework;
        public String targetCode;
    }

    public static class OrganizationDTO {
        public Long id;
        public String nameEnglish;
        public String descriptionEnglish;
        public String imageEnglish;
        public String urlEnglish;
        public String email;
        public String faculty;
        public String institutionName;
        public String institutionIdentifier;
        public String gradingTableUrl;
        public boolean archived;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
    }
} 