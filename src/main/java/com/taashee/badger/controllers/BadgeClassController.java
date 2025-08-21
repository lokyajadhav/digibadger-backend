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
import java.util.HashMap;
import com.taashee.badger.models.BadgeInstanceAwardRequest;
import com.taashee.badger.models.BadgeInstance;
import java.util.stream.Collectors;
import com.taashee.badger.models.BadgeClassDTO;
import com.taashee.badger.models.BadgeClassResponseDTO;
import com.taashee.badger.models.Institution;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;

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
        dto.criteriaUrl = badgeClass.getCriteriaUrl();
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
        dto.expirationDate = badgeClass.getExpirationDate();
        dto.archived = badgeClass.getArchived();
        dto.createdAt = badgeClass.getCreatedAt();
        dto.updatedAt = badgeClass.getUpdatedAt();
        dto.organizationId = badgeClass.getOrganization() != null ? badgeClass.getOrganization().getId() : null;
        
        // Include full organization object
        if (badgeClass.getOrganization() != null) {
            BadgeClassResponseDTO.OrganizationDTO orgDto = new BadgeClassResponseDTO.OrganizationDTO();
            orgDto.id = badgeClass.getOrganization().getId();
            orgDto.nameEnglish = badgeClass.getOrganization().getNameEnglish();
            orgDto.descriptionEnglish = badgeClass.getOrganization().getDescriptionEnglish();
            orgDto.imageEnglish = badgeClass.getOrganization().getImageEnglish();
            orgDto.urlEnglish = badgeClass.getOrganization().getUrlEnglish();
            orgDto.email = badgeClass.getOrganization().getEmail();
            orgDto.faculty = badgeClass.getOrganization().getFaculty();
            orgDto.institutionName = badgeClass.getOrganization().getInstitutionName();
            orgDto.institutionIdentifier = badgeClass.getOrganization().getInstitutionIdentifier();
            orgDto.gradingTableUrl = badgeClass.getOrganization().getGradingTableUrl();
            orgDto.archived = badgeClass.getOrganization().getArchived();
            orgDto.createdAt = badgeClass.getOrganization().getCreatedAt();
            orgDto.updatedAt = badgeClass.getOrganization().getUpdatedAt();
            dto.organization = orgDto;
        }
        
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
        // Count active awards (non-revoked instances)
        dto.activeAwardsCount = badgeClass.getInstances() != null ? 
            badgeClass.getInstances().stream()
                .filter(instance -> !instance.isRevoked())
                .count() : 0L;
        return dto;
    }

    @Operation(summary = "Create badge class", description = "ADMIN/ORGANIZATION only: Create a new badge class.")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @PostMapping("")
    public ResponseEntity<ApiResponse<BadgeClassResponseDTO>> createBadgeClass(@RequestBody BadgeClassDTO badgeClassDTO) {
        BadgeClass created = badgeClassService.createBadgeClassFromDTO(badgeClassDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(HttpStatus.CREATED.value(), "Badge class created", toResponseDTO(created), null));
    }

    @Operation(summary = "Get all badge classes", description = "Get a list of all badge classes.")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<BadgeClassResponseDTO>>> getAllBadgeClasses() {
        List<BadgeClass> badgeClasses = badgeClassService.getAllBadgeClasses();
        List<BadgeClassResponseDTO> dtos = badgeClasses.stream().map(this::toResponseDTO).toList();
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", dtos, null));
    }

    @Operation(summary = "Get paginated badge classes", description = "Get a paginated list of badge classes with search and filtering.")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaginatedBadgeClasses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String search) {
        
        List<BadgeClass> allBadgeClasses = badgeClassService.getAllBadgeClasses();
        
        // Apply search filter if provided
        if (search != null && !search.trim().isEmpty()) {
            allBadgeClasses = allBadgeClasses.stream()
                .filter(bc -> bc.getName() != null && bc.getName().toLowerCase().contains(search.toLowerCase()))
                .toList();
        }
        
        // Calculate pagination
        int total = allBadgeClasses.size();
        int totalPages = (int) Math.ceil((double) total / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, total);
        
        List<BadgeClass> paginatedBadgeClasses = allBadgeClasses.subList(startIndex, endIndex);
        List<BadgeClassResponseDTO> dtos = paginatedBadgeClasses.stream().map(this::toResponseDTO).toList();
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", dtos);
        response.put("totalElements", total);
        response.put("totalPages", totalPages);
        response.put("currentPage", page);
        response.put("size", size);
        response.put("first", page == 0);
        response.put("last", page >= totalPages - 1);
        
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", response, null));
    }

    @Operation(summary = "Get badge class by ID", description = "Get badge class details by ID.")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BadgeClassResponseDTO>> getBadgeClassById(@PathVariable Long id) {
        return badgeClassService.getBadgeClassById(id)
            .map(bc -> ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", toResponseDTO(bc), null)))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Badge class not found", null, "Badge class not found")));
    }

    @Operation(summary = "Update badge class", description = "ADMIN/ORGANIZATION only: Update a badge class.")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BadgeClassResponseDTO>> updateBadgeClass(@PathVariable Long id, @RequestBody BadgeClassDTO badgeClassDTO) {
        BadgeClass updated = badgeClassService.updateBadgeClassFromDTO(id, badgeClassDTO);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Badge class updated", toResponseDTO(updated), null));
    }

    @Operation(summary = "Delete badge class", description = "ADMIN/ORGANIZATION only: Delete a badge class.")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBadgeClass(@PathVariable Long id) {
        badgeClassService.deleteBadgeClass(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Badge class deleted", null, null));
    }

    @Operation(summary = "Get badge recipients", description = "Get paginated list of badge recipients with filtering and sorting.")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @GetMapping("/{id}/recipients")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBadgeRecipients(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        try {
            Map<String, Object> result = badgeClassService.getBadgeRecipients(id, page, size, search, status, sortBy, sortOrder, startDate, endDate);
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Badge recipients retrieved", result, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error retrieving badge recipients", null, e.getMessage()));
        }
    }

    @Operation(summary = "Export badge class", description = "Export badge class as JSON with optional assertions.")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @GetMapping("/{id}/export")
    public ResponseEntity<ApiResponse<Map<String, Object>>> exportBadgeClass(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean includeAssertions,
            @RequestParam(defaultValue = "false") boolean compressOutput) {
        
        try {
            Map<String, Object> result = badgeClassService.exportBadgeClass(id, includeAssertions, compressOutput);
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Badge class exported", result, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error exporting badge class", null, e.getMessage()));
        }
    }

    @Operation(summary = "Import badge class", description = "Import badge class from URL, JSON, or file.")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @PostMapping("/{id}/import")
    public ResponseEntity<ApiResponse<BadgeClassResponseDTO>> importBadgeClass(
            @PathVariable Long id,
            @RequestParam(required = false) String badgeUrl,
            @RequestParam(required = false) String badgeJson,
            @RequestParam(required = false) String importType) {
        
        try {
            BadgeClass updated = badgeClassService.importBadgeClass(id, badgeUrl, badgeJson, importType);
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Badge class imported", toResponseDTO(updated), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error importing badge class", null, e.getMessage()));
        }
    }

    @Operation(summary = "Toggle badge class privacy", description = "ISSUER only: Toggle badge class between public and private. Only badge class owners can perform this action.")
    @PreAuthorize("hasRole('ISSUER')")
    @PutMapping("/{id}/toggle-privacy")
    public ResponseEntity<ApiResponse<BadgeClassResponseDTO>> toggleBadgeClassPrivacy(@PathVariable Long id) {
        try {
            BadgeClass updatedBadgeClass = badgeClassService.togglePrivacy(id);
            BadgeClassResponseDTO responseDTO = toResponseDTO(updatedBadgeClass);
            
            String message = updatedBadgeClass.isPrivate() ? 
                "Badge class has been made private" : 
                "Badge class has been made public";
                
            return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(), 
                message, 
                responseDTO, 
                null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Failed to toggle badge class privacy", 
                    null, 
                    e.getMessage()
                ));
        }
    }

    @Operation(summary = "Archive or unarchive badge class", description = "ISSUER only: Archive or unarchive a badge class. Only badge class owners can perform this action. Author: Lokya Naik")
    @PreAuthorize("hasRole('ISSUER')")
    @PutMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<BadgeClassResponseDTO>> archiveBadgeClass(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> archiveRequest) {
        try {
            // Debug logging
            System.out.println("Archive request received for badge class ID: " + id);

            System.out.println("Archive request received for badge class ID: " + id);
            System.out.println("Archive request body: " + archiveRequest);
            
            Boolean archive = null;
            
            if (archiveRequest != null) {
                Object archiveObj = archiveRequest.get("archive");
                if (archiveObj instanceof Boolean) {
                    archive = (Boolean) archiveObj;
                } else if (archiveObj instanceof String) {
                    archive = Boolean.parseBoolean((String) archiveObj);
                } else if (archiveObj != null) {
                    archive = Boolean.valueOf(archiveObj.toString());
                }
            }
            
            System.out.println("Archive parameter value: " + archive);
            
            if (archive == null) {
                System.out.println("Archive parameter is null - returning 400");
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(
                        HttpStatus.BAD_REQUEST.value(), 
                        "Archive parameter is required", 
                        null, 
                        "Missing or invalid 'archive' parameter in request body. Expected: {\"archive\": true} or {\"archive\": false}"
                    ));
            }
            
            System.out.println("Calling badgeClassService.archiveBadgeClass with archive=" + archive);
            BadgeClass updatedBadgeClass = badgeClassService.archiveBadgeClass(id, archive);
            BadgeClassResponseDTO responseDTO = toResponseDTO(updatedBadgeClass);
            
            String message = archive ? 
                "Badge class has been archived successfully" : 
                "Badge class has been unarchived successfully";
            
            System.out.println("Archive operation successful: " + message);
                
            return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(), 
                message, 
                responseDTO, 
                null
            ));
        } catch (IllegalStateException e) {
            System.out.println("IllegalStateException caught: " + e.getMessage());
            String userMessage;
            if (e.getMessage().contains("active (non-revoked) badge instances")) {
                userMessage = "Cannot archive this badge class because it has active badge instances. Please revoke all awarded badges first before archiving.";
            } else if (e.getMessage().contains("don't have permission")) {
                userMessage = "You don't have permission to modify this badge class. Only badge class owners can archive badges.";
            } else {
                userMessage = e.getMessage();
            }
            
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(), 
                    "Cannot archive badge class", 
                    null, 
                    userMessage
                ));
        } catch (Exception e) {
            System.out.println("Exception caught: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Failed to archive/unarchive badge class", 
                    null, 
                    e.getMessage()
                ));
        }
    }

    @Operation(summary = "Award badges to multiple recipients", description = "ADMIN/ORGANIZATION only: Award badges in batch to recipients. Author: Lokya Naik")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @PostMapping("/{id}/award-enrollments")
    public ResponseEntity<ApiResponse<List<BadgeInstance>>> awardEnrollments(@PathVariable Long id, @RequestBody List<BadgeInstanceAwardRequest> requests) {
        List<BadgeInstance> awarded = badgeClassService.awardEnrollments(id, requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(HttpStatus.CREATED.value(), "Badges awarded", awarded, null));
    }

    @Operation(summary = "Bulk archive/unarchive badge classes", description = "ADMIN/ORGANIZATION only: Bulk archive or unarchive badge classes. Author: Lokya Naik")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @PostMapping("/bulk-archive")
    public ResponseEntity<ApiResponse<List<BadgeClass>>> bulkArchiveBadgeClasses(@RequestBody Map<String, Object> body) {
        List<Long> ids = ((List<?>) body.get("ids")).stream().map(id -> Long.valueOf(id.toString())).collect(Collectors.toList());
        boolean archive = (boolean) body.getOrDefault("archive", true);
        List<BadgeClass> updated = badgeClassService.bulkArchiveBadgeClasses(ids, archive);
        String msg = archive ? "Badge classes archived" : "Badge classes unarchived";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, updated, null));
    }

    @Operation(summary = "Bulk delete badge classes", description = "ADMIN/ORGANIZATION only: Bulk delete badge classes. Author: Lokya Naik")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @PostMapping("/bulk-delete")
    public ResponseEntity<ApiResponse<Void>> bulkDeleteBadgeClasses(@RequestBody List<Long> ids) {
        badgeClassService.bulkDeleteBadgeClasses(ids);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Badge classes deleted", null, null));
    }

    @Operation(summary = "Debug badge class details", description = "Debug endpoint to check badge class details and permissions")
    @PreAuthorize("hasRole('ISSUER')")
    @GetMapping("/{id}/debug")
    public ResponseEntity<ApiResponse<Map<String, Object>>> debugBadgeClass(@PathVariable Long id) {
        try {
            Map<String, Object> debugInfo = new HashMap<>();
            
            // Check if badge class exists
            Optional<BadgeClass> badgeClassOpt = badgeClassService.getBadgeClassById(id);
            if (badgeClassOpt.isEmpty()) {
                debugInfo.put("error", "Badge class not found");
                return ResponseEntity.ok(new ApiResponse<>(
                    HttpStatus.OK.value(), 
                    "Debug info", 
                    debugInfo, 
                    null
                ));
            }
            
            BadgeClass badgeClass = badgeClassOpt.get();
            debugInfo.put("badgeClassId", badgeClass.getId());
            debugInfo.put("badgeClassName", badgeClass.getName());
            debugInfo.put("organizationId", badgeClass.getOrganization() != null ? badgeClass.getOrganization().getId() : null);
            debugInfo.put("isArchived", badgeClass.getArchived());
            debugInfo.put("isPrivate", badgeClass.isPrivate());
            
            // Get current user info
            String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<String> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();
            
            debugInfo.put("userEmail", email);
            debugInfo.put("userRoles", roles);
            
            // Check active badge instances
            List<BadgeInstance> activeInstances = badgeClassService.getActiveBadgeInstances(id);
            debugInfo.put("activeBadgeInstances", activeInstances.size());
            debugInfo.put("canArchive", activeInstances.isEmpty());
            
            return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(), 
                "Debug info", 
                debugInfo, 
                null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Debug failed", 
                    null, 
                    e.getMessage()
                ));
        }
    }
} 