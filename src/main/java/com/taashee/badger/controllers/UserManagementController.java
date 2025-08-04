package com.taashee.badger.controllers;

import com.taashee.badger.models.User;
import com.taashee.badger.models.ApiResponse;
import com.taashee.badger.services.UserService;
import com.taashee.badger.repositories.OrganizationStaffRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Set;

@Tag(name = "User Management", description = "APIs for user management and role assignment. ADMIN only. Author: Lokya Naik")
@RestController
@RequestMapping("/api/admin/users")
public class UserManagementController {
    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationStaffRepository organizationStaffRepository;

    @Operation(summary = "List all users", description = "ADMIN only: Get a list of all users.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "List of users", content = @Content(schema = @Schema(implementation = User.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("")
    public ResponseEntity<com.taashee.badger.models.ApiResponse<List<User>>> listUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(new com.taashee.badger.models.ApiResponse<>(HttpStatus.OK.value(), "Success", users, null));
    }

    @Operation(summary = "Assign roles to a user", description = "ADMIN only: Assign roles to a user.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Roles assigned", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{userId}/roles")
    public ResponseEntity<com.taashee.badger.models.ApiResponse<Object>> assignRoles(@PathVariable Long userId, @RequestBody Set<String> roles) {
        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new com.taashee.badger.models.ApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found", null, "User not found")
            );
        }
        user.setRoles(roles);
        userService.saveUser(user);
        return ResponseEntity.ok(new com.taashee.badger.models.ApiResponse<>(HttpStatus.OK.value(), "Roles assigned", null, null));
    }

    @Operation(summary = "Delete a user", description = "ADMIN only: Delete a user by userId. Author: Lokya Naik")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User deleted", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<com.taashee.badger.models.ApiResponse<Object>> deleteUser(@PathVariable Long userId, @RequestParam Long adminId) {
        if (userId.equals(adminId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new com.taashee.badger.models.ApiResponse<>(HttpStatus.FORBIDDEN.value(), "You cannot delete your own account.", null, "Self-deletion is not allowed.")
            );
        }
        // Prevent deletion if mapped as IssuerStaff
        if (!organizationStaffRepository.findByUserId(userId).isEmpty()) {
            return ResponseEntity.status(409).body(
                new com.taashee.badger.models.ApiResponse<>(409, "This user is mapped to organization(s) as staff. Please ask the organization admin to remove them first.", null, "User mapped to organization.")
            );
        }
        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new com.taashee.badger.models.ApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found", null, "User not found")
            );
        }
        userService.deleteUser(userId);
        return ResponseEntity.ok(new com.taashee.badger.models.ApiResponse<>(HttpStatus.OK.value(), "User deleted", null, null));
    }

    @Operation(summary = "Check if email exists with ISSUER role", description = "Checks if a user exists with the ISSUER role for organization creation. Returns a clear message for UI feedback. Author: Lokya Naik")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/check-issuer-email")
    public ResponseEntity<com.taashee.badger.models.ApiResponse<Void>> checkIssuerEmail(@RequestParam String email) {
        User user = userService.findByEmail(email);
        if (user == null) {
            return ResponseEntity.ok(new com.taashee.badger.models.ApiResponse<>(200, "This email is not registered in Badger.", null, null));
        }
        if (user.getRoles() != null && user.getRoles().contains("ISSUER")) {
            return ResponseEntity.ok(new com.taashee.badger.models.ApiResponse<>(200, "Eligible: This email is registered as an ISSUER.", null, null));
        } else {
            return ResponseEntity.ok(new com.taashee.badger.models.ApiResponse<>(200, "This email exists but does not have the ISSUER role. Please request the role.", null, null));
        }
    }

    // @PostMapping("/admin/users/import")
    // public ResponseEntity<?> importUsers(@RequestBody List<User> users) {
    //     // Old import logic (now handled by UserInvitationController)
    //     return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("This endpoint is deprecated. Use the bulk invite endpoint.");
    // }
} 