package com.taashee.badger.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pathway_element_badges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PathwayElementBadge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "element_id", nullable = false)
    @JsonIgnore
    private PathwayElement element;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_class_id")
    @JsonIgnore
    private BadgeClass badgeClass;

    @Enumerated(EnumType.STRING)
    @Column(name = "badge_source", length = 50)
    @Builder.Default
    private BadgeSource badgeSource = BadgeSource.BADGR;

    @Column(name = "external_badge_url", length = 500)
    private String externalBadgeUrl;

    @Column(name = "external_badge_id", length = 255)
    private String externalBadgeId;

    @Column(name = "external_issuer_name", length = 255)
    private String externalIssuerName;

    @Column(name = "external_issuer_url", length = 500)
    private String externalIssuerUrl;

    @Column(name = "badge_name", length = 255)
    private String badgeName;

    @Column(name = "badge_description", columnDefinition = "TEXT")
    private String badgeDescription;

    @Column(name = "badge_image_url", length = 500)
    private String badgeImageUrl;

    @Column(name = "is_required")
    @Builder.Default
    private Boolean isRequired = true;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verified_by")
    private Long verifiedBy;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "external_badge_data", columnDefinition = "JSON")
    private Map<String, Object> externalBadgeData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "JSON")
    private Map<String, Object> metadata;

    // JSON Properties for API responses
    @JsonProperty("elementId")
    public Long getElementId() {
        return element != null ? element.getId() : null;
    }

    @JsonProperty("badgeClassId")
    public Long getBadgeClassId() {
        return badgeClass != null ? badgeClass.getId() : null;
    }

    @JsonProperty("badgeClassName")
    public String getBadgeClassName() {
        return badgeClass != null ? badgeClass.getName() : badgeName;
    }

    @JsonProperty("badgeClassDescription")
    public String getBadgeClassDescription() {
        return badgeClass != null ? badgeClass.getDescription() : badgeDescription;
    }

    @JsonProperty("badgeClassImageUrl")
    public String getBadgeClassImageUrl() {
        return badgeClass != null ? badgeClass.getImage() : badgeImageUrl;
    }

    @JsonProperty("issuerName")
    public String getIssuerName() {
        if (badgeClass != null && badgeClass.getOrganization() != null) {
            return badgeClass.getOrganization().getNameEnglish();
        }
        return externalIssuerName;
    }

    @JsonProperty("issuerUrl")
    public String getIssuerUrl() {
        if (badgeClass != null && badgeClass.getOrganization() != null) {
            return badgeClass.getOrganization().getUrlEnglish();
        }
        return externalIssuerUrl;
    }

    @JsonProperty("isExternal")
    public Boolean getIsExternal() {
        return !BadgeSource.BADGR.equals(badgeSource);
    }

    @JsonProperty("displayName")
    public String getDisplayName() {
        return badgeClass != null ? badgeClass.getName() : badgeName;
    }

    @JsonProperty("displayDescription")
    public String getDisplayDescription() {
        return badgeClass != null ? badgeClass.getDescription() : badgeDescription;
    }

    @JsonProperty("displayImageUrl")
    public String getDisplayImageUrl() {
        return badgeClass != null ? badgeClass.getImage() : badgeImageUrl;
    }

    // Enums
    public enum BadgeSource {
        BADGR("Badgr", "Internal Badgr badge"),
        CANVAS("Canvas", "Canvas LMS badge"),
        MOODLE("Moodle", "Moodle badge"),
        ACLAIM("Acclaim", "Acclaim badge"),
        CREDLY("Credly", "Credly badge"),
        EXTERNAL("External", "External badge from other platform"),
        MANUAL("Manual", "Manually entered badge");

        private final String displayName;
        private final String description;

        BadgeSource(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    // Business Logic Methods
    public boolean isInternalBadge() {
        return BadgeSource.BADGR.equals(badgeSource);
    }

    public boolean isExternalBadge() {
        return !isInternalBadge();
    }

    public boolean isVerifiedBadge() {
        return isVerified != null && isVerified;
    }

    public boolean canBeEarned() {
        // Internal badges can always be earned
        if (isInternalBadge()) {
            return true;
        }
        
        // External badges need to be verified
        return isVerifiedBadge();
    }

    public String getBadgeUrl() {
        if (isInternalBadge() && badgeClass != null) {
            return "/badges/" + badgeClass.getId();
        }
        return externalBadgeUrl;
    }

    public String getInternalIssuerUrl() {
        if (isInternalBadge() && badgeClass != null && badgeClass.getOrganization() != null) {
            return badgeClass.getOrganization().getUrlEnglish();
        }
        return externalIssuerUrl;
    }

    // Validation Methods
    public boolean isValid() {
        if (isInternalBadge()) {
            return badgeClass != null && element != null;
        } else {
            return externalBadgeUrl != null && 
                   badgeName != null && 
                   !badgeName.trim().isEmpty() &&
                   element != null;
        }
    }

    public List<String> getValidationErrors() {
        List<String> errors = new ArrayList<>();
        
        if (element == null) {
            errors.add("Element is required");
        }
        
        if (isInternalBadge()) {
            if (badgeClass == null) {
                errors.add("Badge class is required for internal badges");
            }
        } else {
            if (externalBadgeUrl == null || externalBadgeUrl.trim().isEmpty()) {
                errors.add("External badge URL is required");
            }
            
            if (badgeName == null || badgeName.trim().isEmpty()) {
                errors.add("Badge name is required for external badges");
            }
            
            if (externalIssuerName == null || externalIssuerName.trim().isEmpty()) {
                errors.add("External issuer name is required");
            }
        }
        
        if (badgeSource == null) {
            errors.add("Badge source is required");
        }
        
        return errors;
    }

    // Utility Methods
    public void verify(Long verifiedByUserId, String notes) {
        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
        this.verifiedBy = verifiedByUserId;
        this.verificationNotes = notes;
    }

    public void unverify() {
        this.isVerified = false;
        this.verifiedAt = null;
        this.verifiedBy = null;
        this.verificationNotes = null;
    }

    public void setMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        return metadata != null ? metadata.get(key) : null;
    }

    public void setExternalBadgeData(String key, Object value) {
        if (externalBadgeData == null) {
            externalBadgeData = new java.util.HashMap<>();
        }
        externalBadgeData.put(key, value);
    }

    public Object getExternalBadgeData(String key) {
        return externalBadgeData != null ? externalBadgeData.get(key) : null;
    }

    // Factory Methods for Different Badge Sources
    public static PathwayElementBadge createInternalBadge(PathwayElement element, BadgeClass badgeClass, boolean isRequired) {
        return PathwayElementBadge.builder()
                .element(element)
                .badgeClass(badgeClass)
                .badgeSource(BadgeSource.BADGR)
                .isRequired(isRequired)
                .isVerified(true)
                .verifiedAt(LocalDateTime.now())
                .build();
    }

    public static PathwayElementBadge createExternalBadge(
            PathwayElement element, 
            String badgeName, 
            String badgeDescription, 
            String badgeImageUrl,
            String externalBadgeUrl,
            String externalIssuerName,
            String externalIssuerUrl,
            BadgeSource source,
            boolean isRequired) {
        
        return PathwayElementBadge.builder()
                .element(element)
                .badgeName(badgeName)
                .badgeDescription(badgeDescription)
                .badgeImageUrl(badgeImageUrl)
                .externalBadgeUrl(externalBadgeUrl)
                .externalIssuerName(externalIssuerName)
                .externalIssuerUrl(externalIssuerUrl)
                .badgeSource(source)
                .isRequired(isRequired)
                .isVerified(false)
                .build();
    }

    public static PathwayElementBadge createFromExternalData(
            PathwayElement element,
            Map<String, Object> externalData,
            BadgeSource source,
            boolean isRequired) {
        
        return PathwayElementBadge.builder()
                .element(element)
                .badgeName((String) externalData.get("name"))
                .badgeDescription((String) externalData.get("description"))
                .badgeImageUrl((String) externalData.get("image"))
                .externalBadgeUrl((String) externalData.get("url"))
                .externalIssuerName((String) externalData.get("issuerName"))
                .externalIssuerUrl((String) externalData.get("issuerUrl"))
                .externalBadgeId((String) externalData.get("id"))
                .badgeSource(source)
                .isRequired(isRequired)
                .isVerified(false)
                .externalBadgeData(externalData)
                .build();
    }
} 