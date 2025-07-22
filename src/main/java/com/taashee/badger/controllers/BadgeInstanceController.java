package com.taashee.badger.controllers;

import com.taashee.badger.models.BadgeInstance;
import com.taashee.badger.models.ApiResponse;
import com.taashee.badger.models.BadgeInstanceDTO;
import com.taashee.badger.services.BadgeInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

@Tag(name = "Badge Instance Management", description = "APIs for managing badge instances. Author: Lokya Naik")
@RestController
@RequestMapping("/api/badgeinstances")
public class BadgeInstanceController {
    @Autowired
    private BadgeInstanceService badgeInstanceService;
    @Autowired
    private ObjectMapper objectMapper;

    private BadgeInstanceDTO toResponseDTO(BadgeInstance bi) {
        BadgeInstanceDTO dto = new BadgeInstanceDTO();
        dto.id = bi.getId();
        dto.badgeClassId = bi.getBadgeClass() != null ? bi.getBadgeClass().getId() : null;
        dto.issuerId = bi.getIssuer() != null ? bi.getIssuer().getId() : null;
        dto.recipientId = bi.getRecipient() != null ? bi.getRecipient().getId() : null;
        dto.issuedOn = bi.getIssuedOn();
        dto.publicKeyIssuer = bi.getPublicKeyIssuer();
        dto.identifier = bi.getIdentifier();
        dto.recipientType = bi.getRecipientType();
        dto.awardType = bi.getAwardType();
        dto.directAwardBundle = bi.getDirectAwardBundle();
        dto.recipientIdentifier = bi.getRecipientIdentifier();
        dto.image = bi.getImage();
        dto.revoked = bi.isRevoked();
        dto.revocationReason = bi.getRevocationReason();
        dto.expiresAt = bi.getExpiresAt();
        dto.acceptance = bi.getAcceptance();
        dto.narrative = bi.getNarrative();
        dto.hashed = bi.isHashed();
        dto.salt = bi.getSalt();
        dto.archived = bi.isArchived();
        dto.oldJson = bi.getOldJson();
        dto.signature = bi.getSignature();
        dto.isPublic = bi.getIsPublic();
        dto.includeEvidence = bi.getIncludeEvidence();
        dto.gradeAchieved = bi.getGradeAchieved();
        dto.includeGradeAchieved = bi.getIncludeGradeAchieved();
        dto.status = bi.getStatus() != null ? bi.getStatus().name() : null;
        dto.badgeClassName = bi.getBadgeClass() != null ? bi.getBadgeClass().getName() : null;
        dto.issuerName = bi.getIssuer() != null ? (bi.getIssuer().getNameEnglish() != null ? bi.getIssuer().getNameEnglish() : bi.getIssuer().getInstitutionName()) : null;
        dto.recipientEmail = bi.getRecipient() != null ? bi.getRecipient().getEmail() : null;
        dto.description = bi.getDescription() != null ? bi.getDescription() : null;
        dto.learningOutcomes = bi.getLearningOutcomes() != null ? bi.getLearningOutcomes() : null;
        // Evidence
        if (bi.getEvidenceItems() != null) {
            dto.evidenceItems = bi.getEvidenceItems().stream().map(e -> {
                BadgeInstanceDTO.EvidenceDTO edto = new BadgeInstanceDTO.EvidenceDTO();
                edto.evidenceUrl = e.getEvidenceUrl();
                edto.narrative = e.getNarrative();
                edto.name = e.getName();
                edto.description = e.getDescription();
                return edto;
            }).toList();
        }
        // Extensions
        try {
            dto.extensions = bi.getExtensions() != null ? objectMapper.readValue(bi.getExtensions(), java.util.Map.class) : null;
        } catch (Exception e) {
            dto.extensions = null;
        }
        return dto;
    }

    @Operation(summary = "Create badge instance", description = "ADMIN/ISSUER only: Create a new badge instance.")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @PostMapping("")
    public ResponseEntity<ApiResponse<BadgeInstance>> createBadgeInstance(@RequestBody BadgeInstanceDTO badgeInstanceDTO) {
        BadgeInstance created = badgeInstanceService.createBadgeInstanceFromDTO(badgeInstanceDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(HttpStatus.CREATED.value(), "Badge instance created", created, null));
    }

    @Operation(summary = "Get all badge instances", description = "Get a list of all badge instances.")
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<BadgeInstanceDTO>>> getAllBadgeInstances() {
        List<BadgeInstance> badgeInstances = badgeInstanceService.getAllBadgeInstances();
        List<BadgeInstanceDTO> dtos = badgeInstances.stream().map(this::toResponseDTO).toList();
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", dtos, null));
    }

    @Operation(summary = "Get badge instance by ID", description = "Get badge instance details by ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BadgeInstanceDTO>> getBadgeInstanceById(@PathVariable Long id) {
        return badgeInstanceService.getBadgeInstanceById(id)
            .map(bi -> ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", toResponseDTO(bi), null)))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Badge instance not found", null, "Badge instance not found")));
    }

    @Operation(summary = "Update badge instance", description = "ADMIN/ISSUER only: Update a badge instance.")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BadgeInstance>> updateBadgeInstance(@PathVariable Long id, @RequestBody BadgeInstanceDTO badgeInstanceDTO) {
        BadgeInstance updated = badgeInstanceService.updateBadgeInstanceFromDTO(id, badgeInstanceDTO);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Badge instance updated", updated, null));
    }

    @Operation(summary = "Delete badge instance", description = "ADMIN/ISSUER only: Delete a badge instance.")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBadgeInstance(@PathVariable Long id) {
        badgeInstanceService.deleteBadgeInstance(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Badge instance deleted", null, null));
    }

    @Operation(summary = "Archive or unarchive badge instance", description = "ADMIN/ISSUER only: Archive or unarchive a badge instance. Author: Lokya Naik")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @PutMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<BadgeInstance>> archiveBadgeInstance(@PathVariable Long id, @RequestBody Map<String, Boolean> archiveRequest) {
        boolean archive = archiveRequest.getOrDefault("archive", true);
        BadgeInstance updated = badgeInstanceService.archiveBadgeInstance(id, archive);
        String msg = archive ? "Badge instance archived" : "Badge instance unarchived";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, updated, null));
    }

    @Operation(summary = "Bulk archive/unarchive badge instances", description = "ADMIN/ISSUER only: Bulk archive or unarchive badge instances. Author: Lokya Naik")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @PostMapping("/bulk-archive")
    public ResponseEntity<ApiResponse<List<BadgeInstance>>> bulkArchiveBadgeInstances(@RequestBody Map<String, Object> body) {
        List<Long> ids = ((List<?>) body.get("ids")).stream().map(id -> Long.valueOf(id.toString())).collect(Collectors.toList());
        boolean archive = (boolean) body.getOrDefault("archive", true);
        List<BadgeInstance> updated = badgeInstanceService.bulkArchiveBadgeInstances(ids, archive);
        String msg = archive ? "Badge instances archived" : "Badge instances unarchived";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, updated, null));
    }

    @Operation(summary = "Bulk delete badge instances", description = "ADMIN/ISSUER only: Bulk delete badge instances. Author: Lokya Naik")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @PostMapping("/bulk-delete")
    public ResponseEntity<ApiResponse<Void>> bulkDeleteBadgeInstances(@RequestBody List<Long> ids) {
        badgeInstanceService.bulkDeleteBadgeInstances(ids);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Badge instances deleted", null, null));
    }

    @Operation(summary = "Revoke badge instances", description = "ADMIN/ISSUER only: Revoke badge instances in bulk. Author: Lokya Naik")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @PostMapping("/revoke-assertions")
    public ResponseEntity<ApiResponse<List<BadgeInstance>>> revokeBadgeInstances(@RequestBody Map<String, Object> body) {
        String revocationReason = (String) body.getOrDefault("revocation_reason", "");
        List<Map<String, Object>> assertions = (List<Map<String, Object>>) body.get("assertions");
        List<Long> ids = assertions.stream().map(a -> Long.valueOf(a.get("entity_id").toString())).collect(Collectors.toList());
        List<BadgeInstance> revoked = badgeInstanceService.revokeBadgeInstances(ids, revocationReason);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Badge instances revoked", revoked, null));
    }
} 