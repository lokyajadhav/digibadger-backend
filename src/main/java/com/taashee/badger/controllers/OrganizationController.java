package com.taashee.badger.controllers;

import com.taashee.badger.models.Organization;
import com.taashee.badger.models.ApiResponse;
import com.taashee.badger.services.OrganizationService;
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

@Tag(name = "Organization Management", description = "APIs for managing organizations. Author: Lokya Naik")
@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {
    @Autowired
    private OrganizationService organizationService;

    @Operation(summary = "Create organization", description = "ADMIN/ORGANIZATION only: Create a new organization.")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("")
    public ResponseEntity<ApiResponse<Organization>> createOrganization(@RequestBody Organization organization) {
        Organization created = organizationService.createOrganization(organization);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(HttpStatus.CREATED.value(), "Organization created", created, null));
    }

    @Operation(summary = "Get all organizations", description = "Get a list of all organizations.")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Organization>>> getAllOrganizations() {
        List<Organization> organizations = organizationService.getAllOrganizations();
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", organizations, null));
    }

    @Operation(summary = "Get organization by ID", description = "Get organization details by ID.")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Organization>> getOrganizationById(@PathVariable Long id) {
        return organizationService.getOrganizationById(id)
            .map(organization -> ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", organization, null)))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Organization not found", null, "Organization not found")));
    }

    @Operation(summary = "Update organization", description = "ADMIN/ORGANIZATION only: Update an organization.")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Organization>> updateOrganization(@PathVariable Long id, @RequestBody Organization organization) {
        Organization updated = organizationService.updateOrganization(id, organization);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Organization updated", updated, null));
    }

    @Operation(summary = "Delete organization", description = "ADMIN/ORGANIZATION only: Delete an organization.")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrganization(@PathVariable Long id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Organization deleted", null, null));
    }

    @Operation(summary = "Archive or unarchive organization", description = "ADMIN/ORGANIZATION only: Archive or unarchive an organization. Author: Lokya Naik")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<Organization>> archiveOrganization(@PathVariable Long id, @RequestBody Map<String, Boolean> archiveRequest) {
        boolean archive = archiveRequest.getOrDefault("archive", true);
        Organization updated = organizationService.archiveOrganization(id, archive);
        String msg = archive ? "Organization archived" : "Organization unarchived";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, updated, null));
    }

    @Operation(summary = "Bulk archive/unarchive organizations", description = "ADMIN/ORGANIZATION only: Bulk archive or unarchive organizations. Author: Lokya Naik")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/bulk-archive")
    public ResponseEntity<ApiResponse<List<Organization>>> bulkArchiveOrganizations(@RequestBody Map<String, Object> body) {
        List<Long> ids = ((List<?>) body.get("ids")).stream().map(id -> Long.valueOf(id.toString())).collect(Collectors.toList());
        boolean archive = (boolean) body.getOrDefault("archive", true);
        List<Organization> updated = organizationService.bulkArchiveOrganizations(ids, archive);
        String msg = archive ? "Organizations archived" : "Organizations unarchived";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, updated, null));
    }

    @Operation(summary = "Bulk delete organizations", description = "ADMIN/ORGANIZATION only: Bulk delete organizations. Author: Lokya Naik")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/bulk-delete")
    public ResponseEntity<ApiResponse<Void>> bulkDeleteOrganizations(@RequestBody List<Long> ids) {
        organizationService.bulkDeleteOrganizations(ids);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Organizations deleted", null, null));
    }

    @Operation(summary = "Get organizations for current user (ISSUER)", description = "Returns all organizations where the current authenticated user is mapped as staff (including owner). Author: Lokya Naik")
    @PreAuthorize("hasRole('ISSUER')")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<Organization>>> getMyOrganizations() {
        String email = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Organization> orgs = organizationService.getOrganizationsForUser(email);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", orgs, null));
    }

    // Temporary test endpoint for development
    @Operation(summary = "Test endpoint", description = "Temporary test endpoint for development. Author: Lokya Naik")
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> testEndpoint() {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Test endpoint working", "Hello from organization controller", null));
    }
} 