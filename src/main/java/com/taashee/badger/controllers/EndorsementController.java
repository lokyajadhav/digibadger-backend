package com.taashee.badger.controllers;

import com.taashee.badger.models.Endorsement;
import com.taashee.badger.models.ApiResponse;
import com.taashee.badger.services.EndorsementService;
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

@Tag(name = "Endorsement Management", description = "APIs for managing endorsements. Author: Lokya Naik")
@RestController
@RequestMapping("/api/endorsements")
public class EndorsementController {
    @Autowired
    private EndorsementService endorsementService;

    @Operation(summary = "Create endorsement", description = "ADMIN/ISSUER only: Create a new endorsement.")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @PostMapping("")
    public ResponseEntity<ApiResponse<Endorsement>> createEndorsement(@RequestBody Endorsement endorsement) {
        Endorsement created = endorsementService.createEndorsement(endorsement);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(HttpStatus.CREATED.value(), "Endorsement created", created, null));
    }

    @Operation(summary = "Get all endorsements", description = "Get a list of all endorsements.")
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Endorsement>>> getAllEndorsements() {
        List<Endorsement> endorsements = endorsementService.getAllEndorsements();
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", endorsements, null));
    }

    @Operation(summary = "Get endorsement by ID", description = "Get endorsement details by ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Endorsement>> getEndorsementById(@PathVariable Long id) {
        return endorsementService.getEndorsementById(id)
            .map(e -> ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", e, null)))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Endorsement not found", null, "Endorsement not found")));
    }

    @Operation(summary = "Update endorsement", description = "ADMIN/ISSUER only: Update an endorsement.")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Endorsement>> updateEndorsement(@PathVariable Long id, @RequestBody Endorsement endorsement) {
        Endorsement updated = endorsementService.updateEndorsement(id, endorsement);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Endorsement updated", updated, null));
    }

    @Operation(summary = "Delete endorsement", description = "ADMIN/ISSUER only: Delete an endorsement.")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEndorsement(@PathVariable Long id) {
        endorsementService.deleteEndorsement(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Endorsement deleted", null, null));
    }

    @Operation(summary = "Accept endorsement", description = "ADMIN/ISSUER only: Accept an endorsement.")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<Endorsement>> acceptEndorsement(@PathVariable Long id) {
        Endorsement accepted = endorsementService.acceptEndorsement(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Endorsement accepted", accepted, null));
    }

    @Operation(summary = "Revoke endorsement", description = "ADMIN/ISSUER only: Revoke an endorsement.")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @PostMapping("/{id}/revoke")
    public ResponseEntity<ApiResponse<Endorsement>> revokeEndorsement(@PathVariable Long id, @RequestBody(required = false) String reason) {
        Endorsement revoked = endorsementService.revokeEndorsement(id, reason);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Endorsement revoked", revoked, null));
    }

    @Operation(summary = "Reject endorsement", description = "ADMIN/ISSUER only: Reject an endorsement.")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Endorsement>> rejectEndorsement(@PathVariable Long id, @RequestBody(required = false) String reason) {
        Endorsement rejected = endorsementService.rejectEndorsement(id, reason);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Endorsement rejected", rejected, null));
    }

    @Operation(summary = "Archive or unarchive endorsement", description = "ADMIN/ISSUER only: Archive or unarchive an endorsement. Author: Lokya Naik")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @PutMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<Endorsement>> archiveEndorsement(@PathVariable Long id, @RequestBody Map<String, Boolean> archiveRequest) {
        boolean archive = archiveRequest.getOrDefault("archive", true);
        Endorsement updated = endorsementService.archiveEndorsement(id, archive);
        String msg = archive ? "Endorsement archived" : "Endorsement unarchived";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, updated, null));
    }

    @Operation(summary = "Bulk archive/unarchive endorsements", description = "ADMIN/ISSUER only: Bulk archive or unarchive endorsements. Author: Lokya Naik")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @PostMapping("/bulk-archive")
    public ResponseEntity<ApiResponse<List<Endorsement>>> bulkArchiveEndorsements(@RequestBody Map<String, Object> body) {
        List<Long> ids = ((List<?>) body.get("ids")).stream().map(id -> Long.valueOf(id.toString())).collect(Collectors.toList());
        boolean archive = (boolean) body.getOrDefault("archive", true);
        List<Endorsement> updated = endorsementService.bulkArchiveEndorsements(ids, archive);
        String msg = archive ? "Endorsements archived" : "Endorsements unarchived";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, updated, null));
    }

    @Operation(summary = "Bulk delete endorsements", description = "ADMIN/ISSUER only: Bulk delete endorsements. Author: Lokya Naik")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    @PostMapping("/bulk-delete")
    public ResponseEntity<ApiResponse<Void>> bulkDeleteEndorsements(@RequestBody List<Long> ids) {
        endorsementService.bulkDeleteEndorsements(ids);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Endorsements deleted", null, null));
    }
} 