package com.taashee.badger.controllers;

import com.taashee.badger.models.User;
import com.taashee.badger.services.PasswordResetService;
import com.taashee.badger.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.Optional;

@Tag(name = "Password Reset", description = "APIs for password reset and forgot password functionality. Author: Lokya Naik")
@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {
    @Autowired
    private PasswordResetService passwordResetService;
    @Autowired
    private UserService userService;

    @Operation(summary = "Request password reset", description = "Sends a password reset email to the user. Author: Lokya Naik")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset email sent", content = @Content),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<com.taashee.badger.models.ApiResponse<Object>> forgotPassword(@RequestParam String email, @RequestParam String appUrl) {
        User user = userService.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new com.taashee.badger.models.ApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found", null, "User not found")
            );
        }
        passwordResetService.createPasswordResetTokenForUser(user, appUrl);
        return ResponseEntity.ok(new com.taashee.badger.models.ApiResponse<>(HttpStatus.OK.value(), "Password reset email sent", null, null));
    }

    @Operation(summary = "Reset password", description = "Resets the user's password using a valid token. Author: Lokya Naik")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset successful", content = @Content),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token", content = @Content)
    })
    @PostMapping("/reset-password")
    public ResponseEntity<com.taashee.badger.models.ApiResponse<Object>> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        Optional<User> userOpt = passwordResetService.validatePasswordResetToken(token);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new com.taashee.badger.models.ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Invalid or expired token", null, "Invalid or expired token")
            );
        }
        User user = userOpt.get();
        passwordResetService.resetPassword(user, newPassword);
        return ResponseEntity.ok(new com.taashee.badger.models.ApiResponse<>(HttpStatus.OK.value(), "Password reset successful", null, null));
    }
} 