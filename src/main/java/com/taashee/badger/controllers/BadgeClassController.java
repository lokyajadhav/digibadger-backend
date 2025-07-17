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

@Tag(name = "Badge Class Management", description = "APIs for managing badge classes. Author: Lokya Naik")
@RestController
@RequestMapping("/api/badgeclasses")
public class BadgeClassController {
    @Autowired
    private BadgeClassService badgeClassService;

    @Operation(summary = "Create badge class", description = "ADMIN/ISSUER only: Create a new badge class.")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @PostMapping("")
    public ResponseEntity<ApiResponse<BadgeClass>> createBadgeClass(@RequestBody BadgeClass badgeClass) {
        BadgeClass created = badgeClassService.createBadgeClass(badgeClass);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(HttpStatus.CREATED.value(), "Badge class created", created, null));
    }

    @Operation(summary = "Get all badge classes", description = "Get a list of all badge classes.")
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<BadgeClass>>> getAllBadgeClasses() {
        List<BadgeClass> badgeClasses = badgeClassService.getAllBadgeClasses();
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", badgeClasses, null));
    }

    @Operation(summary = "Get badge class by ID", description = "Get badge class details by ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BadgeClass>> getBadgeClassById(@PathVariable Long id) {
        return badgeClassService.getBadgeClassById(id)
            .map(bc -> ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", bc, null)))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Badge class not found", null, "Badge class not found")));
    }

    @Operation(summary = "Update badge class", description = "ADMIN/ISSUER only: Update a badge class.")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BadgeClass>> updateBadgeClass(@PathVariable Long id, @RequestBody BadgeClass badgeClass) {
        BadgeClass updated = badgeClassService.updateBadgeClass(id, badgeClass);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Badge class updated", updated, null));
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