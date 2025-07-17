package com.taashee.badger.controllers;

import com.taashee.badger.models.IssuerStaff;
import com.taashee.badger.services.IssuerStaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// DTO for staff response
class IssuerStaffDTO {
    public Long id;
    public String staffRole;
    public boolean isSigner;
    public Long userId;
    public String email;
    public String firstName;
    public String lastName;
    public IssuerStaffDTO(com.taashee.badger.models.IssuerStaff staff) {
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
@RequestMapping("/api/issuers/{issuerId}/staff")
@Tag(name = "Issuer Staff Management", description = "Endpoints for managing issuer-specific staff. Author: Lokya Naik")
public class IssuerStaffController {
    @Autowired
    private IssuerStaffService issuerStaffService;

    @GetMapping
    @Operation(summary = "List all staff for an issuer", description = "Returns all staff members for the given issuer. Author: Lokya Naik")
    public List<IssuerStaffDTO> getStaff(@PathVariable Long issuerId) {
        return issuerStaffService.getStaffByIssuerId(issuerId)
            .stream().map(IssuerStaffDTO::new).toList();
    }

    @PostMapping
    @Operation(summary = "Add staff to an issuer", description = "Adds a staff member to the given issuer. Author: Lokya Naik")
    public ResponseEntity<?> addStaff(@PathVariable Long issuerId, @RequestBody IssuerStaff staff, @RequestParam(value = "confirm", required = false) Boolean confirm) {
        // Check if user exists
        boolean userExists = staff.getUser() != null && staff.getUser().getEmail() != null && issuerStaffService.userExists(staff.getUser().getEmail());
        if (!userExists && (confirm == null || !confirm)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User does not exist. Please confirm to create a new account and add as staff.");
        }
        IssuerStaff created = issuerStaffService.addStaffToIssuer(issuerId, staff);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{staffId}")
    @Operation(summary = "Update staff member's role or isSigner", description = "Updates the staff role or isSigner flag for a staff member. Author: Lokya Naik")
    public IssuerStaff updateStaff(@PathVariable Long issuerId, @PathVariable Long staffId, @RequestBody IssuerStaff staff) {
        return issuerStaffService.updateStaff(staffId, staff);
    }

    @DeleteMapping("/{staffId}")
    @Operation(summary = "Remove staff from an issuer", description = "Removes a staff member from the given issuer. Author: Lokya Naik")
    public ResponseEntity<Void> removeStaff(@PathVariable Long issuerId, @PathVariable Long staffId) {
        issuerStaffService.removeStaff(staffId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{staffId}/full")
    @Operation(summary = "Completely remove staff from issuer and Badger if confirmed", description = "Removes a staff member from the given issuer. If fullDelete is true and the user is not staff for any other issuer and has no other roles, deletes the user and all their invitations.")
    public ResponseEntity<Void> removeStaffCompletely(@PathVariable Long issuerId, @PathVariable Long staffId, @RequestParam(defaultValue = "false") boolean fullDelete) {
        issuerStaffService.removeStaffCompletely(issuerId, staffId, fullDelete);
        return ResponseEntity.noContent().build();
    }
} 