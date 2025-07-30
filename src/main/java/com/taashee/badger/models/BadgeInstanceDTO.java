package com.taashee.badger.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class BadgeInstanceDTO {
    public Long id;
    public Long badgeClassId;
    public Long organizationId;
    public Long recipientId;
    public String recipientName;
    public String recipientEmail;
    public String issueDate; // ISO string from frontend
    public LocalDateTime issuedOn;
    public String publicKeyOrganization;
    public String identifier;
    public String recipientType;
    public String awardType;
    public String directAwardBundle;
    public String recipientIdentifier;
    public String image;
    public boolean revoked;
    public String revocationReason;
    public String expiresAt; // ISO string from frontend
    public LocalDateTime expirationDateTime;
    public String acceptance;
    public String narrative;
    public boolean hashed;
    public String salt;
    public boolean archived;
    public String oldJson;
    public String signature;
    public boolean isPublic;
    public boolean includeEvidence;
    public String gradeAchieved;
    public boolean includeGradeAchieved;
    public String status;
    public String badgeClassName;
    public String organizationName;
    public String description;
    public String learningOutcomes;
    public List<EvidenceDTO> evidenceItems;
    public Map<String, Object> extensions;

    public static class EvidenceDTO {
        public String url; // Changed from evidenceUrl to match frontend
        public String narrative;
        public String name;
        public String description;
    }
} 