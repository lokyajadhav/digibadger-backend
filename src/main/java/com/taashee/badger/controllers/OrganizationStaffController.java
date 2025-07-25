package com.taashee.badger.controllers;

import com.taashee.badger.models.OrganizationStaff;
import com.taashee.badger.services.OrganizationStaffService;
import com.taashee.badger.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

// DTO for staff response
class OrganizationStaffDTO {
    public Long id;
    public String staffRole;
    public boolean isSigner;
    public Long userId;
    public String email;
    public String firstName;
    public String lastName;
    public OrganizationStaffDTO(com.taashee.badger.models.OrganizationStaff staff) {
        this.id = staff.getId();
        this.staffRole = staff.getStaffRole();
        this.isSigner = staff.isSigner();
        if (staff.getUser() != null) {
            this.userId = staff.getUser().getId();
            this.email = staff.getUser().getEmail();
            this.firstName = staff.getUser().getFirstName();
            this.lastName = staff.getUser().getLastName();
        }
    }
}

@RestController
@RequestMapping("/api/organizations/{organizationId}/staff")
@Tag(name = "Organization Staff Management", description = "Endpoints for managing organization-specific staff. Author: Lokya Naik")
public class OrganizationStaffController {
    @Autowired
    private OrganizationStaffService organizationStaffService;
    @Autowired
    private UserService userService;

    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @GetMapping
    @Operation(summary = "List all staff for an organization", description = "Returns all staff members for the given organization. Author: Lokya Naik")
    public List<OrganizationStaffDTO> getStaff(@PathVariable Long organizationId) {
        return organizationStaffService.getStaffByOrganizationId(organizationId)
            .stream().map(OrganizationStaffDTO::new).toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @GetMapping("/me")
    @Operation(summary = "Get current user's staff record for this organization", description = "Returns the staff record for the current authenticated user in the given organization.")
    public ResponseEntity<OrganizationStaffDTO> getMyStaffRecord(@PathVariable Long organizationId) {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String email = (String) authentication.getPrincipal();
        com.taashee.badger.models.User user = userService.findByEmail(email);
        if (user == null) return ResponseEntity.status(404).build();
        OrganizationStaff staff = organizationStaffService.getStaffByOrganizationIdAndUserId(organizationId, user.getId());
        if (staff == null) return ResponseEntity.status(404).build();
        return ResponseEntity.ok(new OrganizationStaffDTO(staff));
    }

    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @PostMapping
    @Operation(summary = "Add staff to an organization", description = "Adds a staff member to the given organization. Author: Lokya Naik")
    public ResponseEntity<?> addStaff(@PathVariable Long organizationId, @RequestBody OrganizationStaff staff, @RequestParam(value = "confirm", required = false) Boolean confirm) {
        // Check if user exists
        boolean userExists = staff.getUser() != null && staff.getUser().getEmail() != null && organizationStaffService.userExists(staff.getUser().getEmail());
        if (!userExists && (confirm == null || !confirm)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User does not exist. Please confirm to create a new account and add as staff.");
        }
        OrganizationStaff created = organizationStaffService.addStaffToOrganization(organizationId, staff);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @PutMapping("/{staffId}")
    @Operation(summary = "Update staff member's role or isSigner", description = "Updates the staff role or isSigner flag for a staff member. Author: Lokya Naik")
    public OrganizationStaff updateStaff(@PathVariable Long organizationId, @PathVariable Long staffId, @RequestBody OrganizationStaff staff) {
        return organizationStaffService.updateStaff(staffId, staff);
    }

    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @DeleteMapping("/{staffId}")
    @Operation(summary = "Remove staff from an organization", description = "Removes a staff member from the given organization. Author: Lokya Naik")
    public ResponseEntity<Void> removeStaff(@PathVariable Long organizationId, @PathVariable Long staffId) {
        organizationStaffService.removeStaff(staffId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @DeleteMapping("/{staffId}/full")
    @Operation(summary = "Completely remove staff from organization and Badger if confirmed", description = "Removes a staff member from the given organization. If fullDelete is true and the user is not staff for any other organization and has no other roles, deletes the user and all their invitations.")
    public ResponseEntity<Void> removeStaffCompletely(@PathVariable Long organizationId, @PathVariable Long staffId, @RequestParam(defaultValue = "false") boolean fullDelete) {
        organizationStaffService.removeStaffCompletely(organizationId, staffId, fullDelete);
        return ResponseEntity.noContent().build();
    }
} 