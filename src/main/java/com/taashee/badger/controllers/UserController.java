package com.taashee.badger.controllers;

import com.taashee.badger.models.User;
import com.taashee.badger.models.OrganizationUser;
import com.taashee.badger.repositories.UserRepository;
import com.taashee.badger.repositories.OrganizationUserRepository;
import com.taashee.badger.models.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Tag(name = "User Management", description = "APIs for user management and validation")
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrganizationUserRepository organizationUserRepository;

    @Operation(summary = "Validate email and organization mapping", description = "Check if email exists and is mapped to the specified organization.")
    @GetMapping("/validate-email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateEmail(
            @RequestParam String email,
            @RequestParam Long organizationId) {
        
        try {
            // Check if user exists
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (!userOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("valid", false);
                response.put("message", "Email doesn't exist");
                return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Email validation result", response, null));
            }
            
            User user = userOpt.get();
            
            // Check if user is mapped to the organization
            List<OrganizationUser> orgUsers = organizationUserRepository.findByUserId(user.getId());
            boolean isMappedToOrg = orgUsers.stream()
                .anyMatch(ou -> ou.getOrganization().getId().equals(organizationId));
            
            Map<String, Object> response = new HashMap<>();
            if (isMappedToOrg) {
                response.put("valid", true);
                response.put("message", "Email is valid and mapped to this organization");
                response.put("user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName()
                ));
            } else {
                response.put("valid", false);
                response.put("message", "Email exists but not mapped to this organization. Please enter a valid email.");
            }
            
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Email validation result", response, null));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error validating email", null, e.getMessage()));
        }
    }
} 