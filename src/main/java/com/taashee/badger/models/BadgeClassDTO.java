package com.taashee.badger.models;

import java.util.List;
import java.util.Map;

public class BadgeClassDTO {
    public Long organizationId;
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
    public String expirationPeriod;
    public String expirationDate;
    public boolean archived;
    public List<String> tagNames;
    public List<AlignmentDTO> alignments;
    public Map<String, Object> extensions;
    public List<Long> institutionIds;

    public static class AlignmentDTO {
        public String targetName;
        public String targetUrl;
        public String targetDescription;
        public String targetFramework;
        public String targetCode;
    }
} 