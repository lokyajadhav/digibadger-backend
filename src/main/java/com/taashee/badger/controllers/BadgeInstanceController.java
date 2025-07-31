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
import java.util.Optional;

@Tag(name = "Badge Instance Management", description = "APIs for managing badge instances. Author: Lokya Naik")
@RestController
@RequestMapping("/api/badge-instances")
public class BadgeInstanceController {
    @Autowired
    private BadgeInstanceService badgeInstanceService;
    @Autowired
    private ObjectMapper objectMapper;

    private BadgeInstanceDTO toResponseDTO(BadgeInstance bi) {
        BadgeInstanceDTO dto = new BadgeInstanceDTO();
        dto.id = bi.getId();
        dto.badgeClassId = bi.getBadgeClass() != null ? bi.getBadgeClass().getId() : null;
        dto.organizationId = bi.getOrganization() != null ? bi.getOrganization().getId() : null;
        dto.organizationName = bi.getOrganization() != null ? (bi.getOrganization().getNameEnglish() != null ? bi.getOrganization().getNameEnglish() : bi.getOrganization().getInstitutionName()) : null;
        dto.recipientId = bi.getRecipient() != null ? bi.getRecipient().getId() : null;
        dto.issuedOn = bi.getIssuedOn();
        dto.publicKeyOrganization = bi.getPublicKeyOrganization();
        dto.identifier = bi.getIdentifier();
        dto.recipientType = bi.getRecipientType();
        dto.awardType = bi.getAwardType();
        dto.directAwardBundle = bi.getDirectAwardBundle();
        dto.recipientIdentifier = bi.getRecipientIdentifier();
        dto.image = bi.getImage();
        dto.revoked = bi.isRevoked();
        dto.revocationReason = bi.getRevocationReason();
        dto.expiresAt = bi.getExpiresAt() != null ? bi.getExpiresAt().toString() : null;
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
        dto.recipientEmail = bi.getRecipient() != null ? bi.getRecipient().getEmail() : null;
        dto.description = bi.getDescription() != null ? bi.getDescription() : null;
        dto.learningOutcomes = bi.getLearningOutcomes() != null ? bi.getLearningOutcomes() : null;
        // Evidence
        if (bi.getEvidenceItems() != null) {
            dto.evidenceItems = bi.getEvidenceItems().stream().map(e -> {
                BadgeInstanceDTO.EvidenceDTO edto = new BadgeInstanceDTO.EvidenceDTO();
                edto.url = e.getEvidenceUrl(); // Map evidenceUrl to url for frontend
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

    @Operation(summary = "Create badge instance", description = "ADMIN/ORGANIZATION only: Create a new badge instance.")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @PostMapping("")
    public ResponseEntity<ApiResponse<BadgeInstance>> createBadgeInstance(@RequestBody BadgeInstanceDTO badgeInstanceDTO) {
        BadgeInstance created = badgeInstanceService.createBadgeInstanceFromDTO(badgeInstanceDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(HttpStatus.CREATED.value(), "Badge instance created", created, null));
    }

    @Operation(summary = "Get all badge instances", description = "Get a list of all badge instances.")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<BadgeInstanceDTO>>> getAllBadgeInstances() {
        List<BadgeInstance> badgeInstances = badgeInstanceService.getAllBadgeInstances();
        List<BadgeInstanceDTO> dtos = badgeInstances.stream().map(this::toResponseDTO).toList();
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", dtos, null));
    }

    @Operation(summary = "Get badge instance by ID", description = "Get badge instance details by ID.")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BadgeInstanceDTO>> getBadgeInstanceById(@PathVariable Long id) {
        Optional<BadgeInstance> badgeInstance = badgeInstanceService.getBadgeInstanceById(id);
        if (badgeInstance.isPresent()) {
            BadgeInstanceDTO responseDTO = toResponseDTO(badgeInstance.get());
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", responseDTO, null));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get user badges", description = "Get badges for the current user.")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserBadges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        try {
            Map<String, Object> result = badgeInstanceService.getUserBadges(page, size, search);
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", result, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Failed to fetch user badges", 
                    null, 
                    e.getMessage()
                ));
        }
    }

    @Operation(summary = "Update badge instance", description = "ADMIN/ORGANIZATION only: Update a badge instance.")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BadgeInstance>> updateBadgeInstance(@PathVariable Long id, @RequestBody BadgeInstanceDTO badgeInstanceDTO) {
        BadgeInstance updated = badgeInstanceService.updateBadgeInstanceFromDTO(id, badgeInstanceDTO);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Badge instance updated", updated, null));
    }

    @Operation(summary = "Delete badge instance", description = "ADMIN/ORGANIZATION only: Delete a badge instance.")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBadgeInstance(@PathVariable Long id) {
        badgeInstanceService.deleteBadgeInstance(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Badge instance deleted", null, null));
    }

    @Operation(summary = "Archive or unarchive badge instance", description = "ADMIN/ORGANIZATION only: Archive or unarchive a badge instance. Author: Lokya Naik")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @PutMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<BadgeInstance>> archiveBadgeInstance(@PathVariable Long id, @RequestBody Map<String, Boolean> archiveRequest) {
        boolean archive = archiveRequest.getOrDefault("archive", true);
        BadgeInstance updated = badgeInstanceService.archiveBadgeInstance(id, archive);
        String msg = archive ? "Badge instance archived" : "Badge instance unarchived";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, updated, null));
    }

    @Operation(summary = "Bulk archive/unarchive badge instances", description = "ADMIN/ORGANIZATION only: Bulk archive or unarchive badge instances. Author: Lokya Naik")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @PostMapping("/bulk-archive")
    public ResponseEntity<ApiResponse<List<BadgeInstance>>> bulkArchiveBadgeInstances(@RequestBody Map<String, Object> body) {
        List<Long> ids = ((List<?>) body.get("ids")).stream().map(id -> Long.valueOf(id.toString())).collect(Collectors.toList());
        boolean archive = (boolean) body.getOrDefault("archive", true);
        List<BadgeInstance> updated = badgeInstanceService.bulkArchiveBadgeInstances(ids, archive);
        String msg = archive ? "Badge instances archived" : "Badge instances unarchived";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, updated, null));
    }

    @Operation(summary = "Bulk delete badge instances", description = "ADMIN/ORGANIZATION only: Bulk delete badge instances. Author: Lokya Naik")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @PostMapping("/bulk-delete")
    public ResponseEntity<ApiResponse<Void>> bulkDeleteBadgeInstances(@RequestBody List<Long> ids) {
        badgeInstanceService.bulkDeleteBadgeInstances(ids);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Badge instances deleted", null, null));
    }

    @Operation(summary = "Revoke badge instance", description = "ISSUER only: Revoke a single badge instance. Only badge class owners can perform this action.")
    @PreAuthorize("hasRole('ISSUER')")
    @PutMapping("/{id}/revoke")
    public ResponseEntity<ApiResponse<BadgeInstanceDTO>> revokeBadgeInstance(@PathVariable Long id, @RequestBody(required = false) Map<String, String> revokeRequest) {
        try {
            String reason = null;
            if (revokeRequest != null && revokeRequest.containsKey("reason")) {
                reason = revokeRequest.get("reason");
            }
            
            BadgeInstance revokedInstance = badgeInstanceService.revokeBadgeInstance(id, reason);
            BadgeInstanceDTO responseDTO = toResponseDTO(revokedInstance);
            
            return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(), 
                "Badge instance has been revoked successfully", 
                responseDTO, 
                null
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(), 
                    "Cannot revoke badge instance", 
                    null, 
                    e.getMessage()
                ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Failed to revoke badge instance", 
                    null, 
                    e.getMessage()
                ));
        }
    }

    @Operation(summary = "Revoke badge instances", description = "ADMIN/ORGANIZATION only: Revoke badge instances in bulk. Author: Lokya Naik")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @PostMapping("/revoke-assertions")
    public ResponseEntity<ApiResponse<List<BadgeInstance>>> revokeBadgeInstances(@RequestBody Map<String, Object> body) {
        String revocationReason = (String) body.getOrDefault("revocation_reason", "");
        List<Map<String, Object>> assertions = (List<Map<String, Object>>) body.get("assertions");
        List<Long> ids = assertions.stream().map(a -> Long.valueOf(a.get("entity_id").toString())).collect(Collectors.toList());
        List<BadgeInstance> revoked = badgeInstanceService.revokeBadgeInstances(ids, revocationReason);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Badge instances revoked", revoked, null));
    }
} 