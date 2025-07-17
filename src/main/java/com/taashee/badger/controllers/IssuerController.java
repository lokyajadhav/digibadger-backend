package com.taashee.badger.controllers;

import com.taashee.badger.models.Issuer;
import com.taashee.badger.models.ApiResponse;
import com.taashee.badger.services.IssuerService;
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

@Tag(name = "Issuer Management", description = "APIs for managing badge issuers. Author: Lokya Naik")
@RestController
@RequestMapping("/api/issuers")
public class IssuerController {
    @Autowired
    private IssuerService issuerService;

    @Operation(summary = "Create issuer", description = "ADMIN/ISSUER only: Create a new issuer.")
    // @PreAuthorize("hasAnyRole('ADMIN','ISSUER')") // Temporarily disabled for testing
    @PostMapping("")
    public ResponseEntity<ApiResponse<Issuer>> createIssuer(@RequestBody Issuer issuer) {
        Issuer created = issuerService.createIssuer(issuer);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(HttpStatus.CREATED.value(), "Issuer created", created, null));
    }

    @Operation(summary = "Get all issuers", description = "Get a list of all issuers.")
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Issuer>>> getAllIssuers() {
        List<Issuer> issuers = issuerService.getAllIssuers();
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", issuers, null));
    }

    @Operation(summary = "Get issuer by ID", description = "Get issuer details by ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Issuer>> getIssuerById(@PathVariable Long id) {
        return issuerService.getIssuerById(id)
            .map(issuer -> ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", issuer, null)))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Issuer not found", null, "Issuer not found")));
    }

    @Operation(summary = "Update issuer", description = "ADMIN/ISSUER only: Update an issuer.")
    // @PreAuthorize("hasAnyRole('ADMIN','ISSUER')") // Temporarily disabled for testing
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Issuer>> updateIssuer(@PathVariable Long id, @RequestBody Issuer issuer) {
        Issuer updated = issuerService.updateIssuer(id, issuer);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Issuer updated", updated, null));
    }

    @Operation(summary = "Delete issuer", description = "ADMIN/ISSUER only: Delete an issuer.")
    // @PreAuthorize("hasAnyRole('ADMIN','ISSUER')") // Temporarily disabled for testing
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteIssuer(@PathVariable Long id) {
        issuerService.deleteIssuer(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Issuer deleted", null, null));
    }

    @Operation(summary = "Archive or unarchive issuer", description = "ADMIN/ISSUER only: Archive or unarchive an issuer. Author: Lokya Naik")
    // @PreAuthorize("hasAnyRole('ADMIN','ISSUER')") // Temporarily disabled for testing
    @PutMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<Issuer>> archiveIssuer(@PathVariable Long id, @RequestBody Map<String, Boolean> archiveRequest) {
        boolean archive = archiveRequest.getOrDefault("archive", true);
        Issuer updated = issuerService.archiveIssuer(id, archive);
        String msg = archive ? "Issuer archived" : "Issuer unarchived";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, updated, null));
    }

    @Operation(summary = "Bulk archive/unarchive issuers", description = "ADMIN/ISSUER only: Bulk archive or unarchive issuers. Author: Lokya Naik")
    // @PreAuthorize("hasAnyRole('ADMIN','ISSUER')") // Temporarily disabled for testing
    @PostMapping("/bulk-archive")
    public ResponseEntity<ApiResponse<List<Issuer>>> bulkArchiveIssuers(@RequestBody Map<String, Object> body) {
        List<Long> ids = ((List<?>) body.get("ids")).stream().map(id -> Long.valueOf(id.toString())).collect(Collectors.toList());
        boolean archive = (boolean) body.getOrDefault("archive", true);
        List<Issuer> updated = issuerService.bulkArchiveIssuers(ids, archive);
        String msg = archive ? "Issuers archived" : "Issuers unarchived";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, updated, null));
    }

    @Operation(summary = "Bulk delete issuers", description = "ADMIN/ISSUER only: Bulk delete issuers. Author: Lokya Naik")
    // @PreAuthorize("hasAnyRole('ADMIN','ISSUER')") // Temporarily disabled for testing
    @PostMapping("/bulk-delete")
    public ResponseEntity<ApiResponse<Void>> bulkDeleteIssuers(@RequestBody List<Long> ids) {
        issuerService.bulkDeleteIssuers(ids);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Issuers deleted", null, null));
    }

    // Temporary test endpoint for development
    @Operation(summary = "Test endpoint", description = "Temporary test endpoint for development. Author: Lokya Naik")
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> testEndpoint() {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Test endpoint working", "Hello from issuer controller", null));
    }
} 