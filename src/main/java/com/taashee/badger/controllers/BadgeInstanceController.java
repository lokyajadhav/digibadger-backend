package com.taashee.badger.controllers;

import com.taashee.badger.models.BadgeInstance;
import com.taashee.badger.models.ApiResponse;
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

@Tag(name = "Badge Instance Management", description = "APIs for managing badge instances. Author: Lokya Naik")
@RestController
@RequestMapping("/api/badgeinstances")
public class BadgeInstanceController {
    @Autowired
    private BadgeInstanceService badgeInstanceService;

    @Operation(summary = "Create badge instance", description = "ADMIN/ISSUER only: Create a new badge instance.")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @PostMapping("")
    public ResponseEntity<ApiResponse<BadgeInstance>> createBadgeInstance(@RequestBody BadgeInstance badgeInstance) {
        BadgeInstance created = badgeInstanceService.createBadgeInstance(badgeInstance);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(HttpStatus.CREATED.value(), "Badge instance created", created, null));
    }

    @Operation(summary = "Get all badge instances", description = "Get a list of all badge instances.")
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<BadgeInstance>>> getAllBadgeInstances() {
        List<BadgeInstance> badgeInstances = badgeInstanceService.getAllBadgeInstances();
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", badgeInstances, null));
    }

    @Operation(summary = "Get badge instance by ID", description = "Get badge instance details by ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BadgeInstance>> getBadgeInstanceById(@PathVariable Long id) {
        return badgeInstanceService.getBadgeInstanceById(id)
            .map(bi -> ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", bi, null)))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Badge instance not found", null, "Badge instance not found")));
    }

    @Operation(summary = "Update badge instance", description = "ADMIN/ISSUER only: Update a badge instance.")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BadgeInstance>> updateBadgeInstance(@PathVariable Long id, @RequestBody BadgeInstance badgeInstance) {
        BadgeInstance updated = badgeInstanceService.updateBadgeInstance(id, badgeInstance);
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