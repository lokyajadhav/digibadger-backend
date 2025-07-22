package com.taashee.badger.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class BadgeInstanceDTO {
    public Long id;
    public Long badgeClassId;
    public Long issuerId;
    public Long recipientId;
    public LocalDateTime issuedOn;
    public String publicKeyIssuer;
    public String identifier;
    public String recipientType;
    public String awardType;
    public String directAwardBundle;
    public String recipientIdentifier;
    public String image;
    public boolean revoked;
    public String revocationReason;
    public LocalDateTime expiresAt;
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
    public String issuerName;
    public String recipientEmail;
    public String description;
    public String learningOutcomes;
    public List<EvidenceDTO> evidenceItems;
    public Map<String, Object> extensions;

    public static class EvidenceDTO {
        public String evidenceUrl;
        public String narrative;
        public String name;
        public String description;
    }
} 