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

@Tag(name = "Recipient Dashboard", description = "APIs for recipient badge dashboard. Author: Lokya Naik")
@RestController
@RequestMapping("/api/recipients")
public class RecipientController {
    @Autowired
    private BadgeInstanceService badgeInstanceService;

    @Operation(summary = "List badges for recipient", description = "Get all badges awarded to a user. Only the user or ADMIN can access.")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/{userId}/badges")
    public ResponseEntity<ApiResponse<List<BadgeInstance>>> getBadgesForRecipient(@PathVariable Long userId) {
        List<BadgeInstance> badges = badgeInstanceService.getBadgesForRecipient(userId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", badges, null));
    }

    @Operation(summary = "Get badge detail for recipient", description = "Get badge instance detail for a user. Only the user or ADMIN can access.")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/{userId}/badges/{badgeInstanceId}")
    public ResponseEntity<ApiResponse<BadgeInstance>> getBadgeDetailForRecipient(@PathVariable Long userId, @PathVariable Long badgeInstanceId) {
        BadgeInstance badge = badgeInstanceService.getBadgeInstanceForRecipient(userId, badgeInstanceId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", badge, null));
    }
} 