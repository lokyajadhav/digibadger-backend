package com.taashee.badger.controllers;

import com.taashee.badger.models.User;
import com.taashee.badger.services.EmailVerificationService;
import com.taashee.badger.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Tag(name = "Email Verification", description = "APIs for email verification and account activation. Author: Lokya Naik")
@RestController
@RequestMapping("/api/auth")
public class EmailVerificationController {
    @Autowired
    private EmailVerificationService emailVerificationService;
    @Autowired
    private UserService userService;

    @Operation(summary = "Verify email", description = "Verifies the user's email using a token and activates the account. Author: Lokya Naik")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email verified and account activated", content = @Content),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token", content = @Content)
    })
    @GetMapping("/verify-email")
    public ResponseEntity<com.taashee.badger.models.ApiResponse<Object>> verifyEmail(@RequestParam String token) {
        Optional<User> userOpt = emailVerificationService.validateVerificationToken(token);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new com.taashee.badger.models.ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Invalid or expired token", null, "Invalid or expired token")
            );
        }
        User user = userOpt.get();
        emailVerificationService.activateUser(user);
        return ResponseEntity.ok(new com.taashee.badger.models.ApiResponse<>(HttpStatus.OK.value(), "Email verified and account activated", null, null));
    }
} 