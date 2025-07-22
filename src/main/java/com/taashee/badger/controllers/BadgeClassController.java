package com.taashee.badger.controllers;

import com.taashee.badger.models.BadgeClass;
import com.taashee.badger.models.ApiResponse;
import com.taashee.badger.services.BadgeClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import com.taashee.badger.models.BadgeInstanceAwardRequest;
import com.taashee.badger.models.BadgeInstance;
import java.util.stream.Collectors;
import com.taashee.badger.models.BadgeClassDTO;
import com.taashee.badger.models.BadgeClassResponseDTO;
import com.taashee.badger.models.Institution;

@Tag(name = "Badge Class Management", description = "APIs for managing badge classes. Author: Lokya Naik")
@RestController
@RequestMapping("/api/badgeclasses")
public class BadgeClassController {
    @Autowired
    private BadgeClassService badgeClassService;

    private BadgeClassResponseDTO toResponseDTO(BadgeClass badgeClass) {
        BadgeClassResponseDTO dto = new BadgeClassResponseDTO();
        dto.id = badgeClass.getId();
        dto.name = badgeClass.getName();
        dto.image = badgeClass.getImage();
        dto.description = badgeClass.getDescription();
        dto.criteriaText = badgeClass.getCriteriaText();
        dto.formal = badgeClass.isFormal();
        dto.isPrivate = badgeClass.isPrivate();
        dto.narrativeRequired = badgeClass.isNarrativeRequired();
        dto.evidenceRequired = badgeClass.isEvidenceRequired();
        dto.awardNonValidatedNameAllowed = badgeClass.isAwardNonValidatedNameAllowed();
        dto.isMicroCredentials = badgeClass.isMicroCredentials();
        dto.directAwardingDisabled = badgeClass.isDirectAwardingDisabled();
        dto.selfEnrollmentDisabled = badgeClass.isSelfEnrollmentDisabled();
        dto.participation = badgeClass.getParticipation();
        dto.assessmentType = badgeClass.getAssessmentType();
        dto.assessmentIdVerified = badgeClass.isAssessmentIdVerified();
        dto.assessmentSupervised = badgeClass.isAssessmentSupervised();
        dto.qualityAssuranceName = badgeClass.getQualityAssuranceName();
        dto.qualityAssuranceUrl = badgeClass.getQualityAssuranceUrl();
        dto.qualityAssuranceDescription = badgeClass.getQualityAssuranceDescription();
        dto.gradeAchievedRequired = badgeClass.isGradeAchievedRequired();
        dto.stackable = badgeClass.isStackable();
        dto.eqfNlqfLevelVerified = badgeClass.isEqfNlqfLevelVerified();
        dto.badgeClassType = badgeClass.getBadgeClassType();
        dto.expirationPeriod = badgeClass.getExpirationPeriod();
        dto.archived = badgeClass.getArchived();
        dto.createdAt = badgeClass.getCreatedAt();
        dto.updatedAt = badgeClass.getUpdatedAt();
        dto.issuerId = badgeClass.getIssuer() != null ? badgeClass.getIssuer().getId() : null;
        // tags
        dto.tagNames = badgeClass.getTags() != null ? badgeClass.getTags().stream().map(tag -> tag.getName()).toList() : null;
        // alignments
        dto.alignments = badgeClass.getAlignments() != null ? badgeClass.getAlignments().stream().map(a -> {
            BadgeClassResponseDTO.AlignmentDTO adto = new BadgeClassResponseDTO.AlignmentDTO();
            adto.targetName = a.getTargetName();
            adto.targetUrl = a.getTargetUrl();
            adto.targetDescription = a.getTargetDescription();
            adto.targetFramework = a.getTargetFramework();
            adto.targetCode = a.getTargetCode();
            return adto;
        }).toList() : null;
        // institutions
        dto.institutionIds = badgeClass.getAwardAllowedInstitutions() != null ? badgeClass.getAwardAllowedInstitutions().stream().map(Institution::getId).toList() : null;
        // extensions
        try {
            dto.extensions = badgeClass.getExtensions() != null ? new com.fasterxml.jackson.databind.ObjectMapper().readValue(badgeClass.getExtensions(), java.util.Map.class) : null;
        } catch (Exception e) {
            dto.extensions = null;
        }
        return dto;
    }

    @Operation(summary = "Create badge class", description = "ADMIN/ISSUER only: Create a new badge class.")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @PostMapping("")
    public ResponseEntity<ApiResponse<BadgeClassResponseDTO>> createBadgeClass(@RequestBody BadgeClassDTO badgeClassDTO) {
        BadgeClass created = badgeClassService.createBadgeClassFromDTO(badgeClassDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(HttpStatus.CREATED.value(), "Badge class created", toResponseDTO(created), null));
    }

    @Operation(summary = "Get all badge classes", description = "Get a list of all badge classes.")
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<BadgeClassResponseDTO>>> getAllBadgeClasses() {
        List<BadgeClass> badgeClasses = badgeClassService.getAllBadgeClasses();
        List<BadgeClassResponseDTO> dtos = badgeClasses.stream().map(this::toResponseDTO).toList();
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", dtos, null));
    }

    @Operation(summary = "Get badge class by ID", description = "Get badge class details by ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BadgeClassResponseDTO>> getBadgeClassById(@PathVariable Long id) {
        return badgeClassService.getBadgeClassById(id)
            .map(bc -> ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", toResponseDTO(bc), null)))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Badge class not found", null, "Badge class not found")));
    }

    @Operation(summary = "Update badge class", description = "ADMIN/ISSUER only: Update a badge class.")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BadgeClassResponseDTO>> updateBadgeClass(@PathVariable Long id, @RequestBody BadgeClassDTO badgeClassDTO) {
        BadgeClass updated = badgeClassService.updateBadgeClassFromDTO(id, badgeClassDTO);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Badge class updated", toResponseDTO(updated), null));
    }

    @Operation(summary = "Delete badge class", description = "ADMIN/ISSUER only: Delete a badge class.")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBadgeClass(@PathVariable Long id) {
        badgeClassService.deleteBadgeClass(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Badge class deleted", null, null));
    }

    @Operation(summary = "Archive or unarchive badge class", description = "ADMIN/ISSUER only: Archive or unarchive a badge class. Author: Lokya Naik")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @PutMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<BadgeClass>> archiveBadgeClass(@PathVariable Long id, @RequestBody Map<String, Boolean> archiveRequest) {
        boolean archive = archiveRequest.getOrDefault("archive", true);
        BadgeClass updated = badgeClassService.archiveBadgeClass(id, archive);
        String msg = archive ? "Badge class archived" : "Badge class unarchived";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, updated, null));
    }

    @Operation(summary = "Award badges to multiple recipients", description = "ADMIN/ISSUER only: Award badges in batch to recipients. Author: Lokya Naik")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @PostMapping("/{id}/award-enrollments")
    public ResponseEntity<ApiResponse<List<BadgeInstance>>> awardEnrollments(@PathVariable Long id, @RequestBody List<BadgeInstanceAwardRequest> requests) {
        List<BadgeInstance> awarded = badgeClassService.awardEnrollments(id, requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(HttpStatus.CREATED.value(), "Badges awarded", awarded, null));
    }

    @Operation(summary = "Bulk archive/unarchive badge classes", description = "ADMIN/ISSUER only: Bulk archive or unarchive badge classes. Author: Lokya Naik")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @PostMapping("/bulk-archive")
    public ResponseEntity<ApiResponse<List<BadgeClass>>> bulkArchiveBadgeClasses(@RequestBody Map<String, Object> body) {
        List<Long> ids = ((List<?>) body.get("ids")).stream().map(id -> Long.valueOf(id.toString())).collect(Collectors.toList());
        boolean archive = (boolean) body.getOrDefault("archive", true);
        List<BadgeClass> updated = badgeClassService.bulkArchiveBadgeClasses(ids, archive);
        String msg = archive ? "Badge classes archived" : "Badge classes unarchived";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, updated, null));
    }

    @Operation(summary = "Bulk delete badge classes", description = "ADMIN/ISSUER only: Bulk delete badge classes. Author: Lokya Naik")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @PostMapping("/bulk-delete")
    public ResponseEntity<ApiResponse<Void>> bulkDeleteBadgeClasses(@RequestBody List<Long> ids) {
        badgeClassService.bulkDeleteBadgeClasses(ids);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Badge classes deleted", null, null));
    }
} 