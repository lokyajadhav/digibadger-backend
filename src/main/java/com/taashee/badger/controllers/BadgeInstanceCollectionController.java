package com.taashee.badger.controllers;

import com.taashee.badger.models.BadgeInstanceCollection;
import com.taashee.badger.models.ApiResponse;
import com.taashee.badger.services.BadgeInstanceCollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Tag(name = "Recipient Collections", description = "APIs for recipient badge collections. Author: Lokya Naik")
@RestController
@RequestMapping("/api/recipients/{userId}/collections")
public class BadgeInstanceCollectionController {
    @Autowired
    private BadgeInstanceCollectionService collectionService;

    @Operation(summary = "List collections for recipient", description = "Get all collections for a user. Only the user or ADMIN can access.")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BadgeInstanceCollection>>> getCollections(@PathVariable Long userId) {
        List<BadgeInstanceCollection> collections = collectionService.getCollectionsForUser(userId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", collections, null));
    }

    @Operation(summary = "Get collection detail", description = "Get a specific collection by ID.")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/{collectionId}")
    public ResponseEntity<ApiResponse<BadgeInstanceCollection>> getCollection(@PathVariable Long userId, @PathVariable Long collectionId) {
        Optional<BadgeInstanceCollection> collection = collectionService.getCollection(collectionId);
        return collection.map(c -> ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", c, null)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Not found", null, null)));
    }

    @Operation(summary = "Create collection", description = "Create a new collection for a user.")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<BadgeInstanceCollection>> createCollection(@PathVariable Long userId, @RequestBody BadgeInstanceCollection collection) {
        // Set user by userId
        collection.setUserId(userId);
        BadgeInstanceCollection created = collectionService.createCollection(collection);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(HttpStatus.CREATED.value(), "Created", created, null));
    }

    @Operation(summary = "Update collection", description = "Update a collection's details.")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @PutMapping("/{collectionId}")
    public ResponseEntity<ApiResponse<BadgeInstanceCollection>> updateCollection(@PathVariable Long userId, @PathVariable Long collectionId, @RequestBody BadgeInstanceCollection collection) {
        collection.setId(collectionId);
        collection.setUserId(userId);
        BadgeInstanceCollection updated = collectionService.updateCollection(collection);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Updated", updated, null));
    }

    @Operation(summary = "Delete collection", description = "Delete a collection.")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @DeleteMapping("/{collectionId}")
    public ResponseEntity<ApiResponse<Void>> deleteCollection(@PathVariable Long userId, @PathVariable Long collectionId) {
        collectionService.deleteCollection(collectionId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Deleted", null, null));
    }

    @Operation(summary = "Add badges to collection", description = "Add badge instances to a collection.")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @PostMapping("/{collectionId}/add-badges")
    public ResponseEntity<ApiResponse<BadgeInstanceCollection>> addBadges(@PathVariable Long userId, @PathVariable Long collectionId, @RequestBody Map<String, Set<Long>> body) {
        Set<Long> badgeInstanceIds = body.get("badgeInstanceIds");
        BadgeInstanceCollection updated = collectionService.addBadgesToCollection(collectionId, badgeInstanceIds);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Badges added", updated, null));
    }

    @Operation(summary = "Remove badges from collection", description = "Remove badge instances from a collection.")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @PostMapping("/{collectionId}/remove-badges")
    public ResponseEntity<ApiResponse<BadgeInstanceCollection>> removeBadges(@PathVariable Long userId, @PathVariable Long collectionId, @RequestBody Map<String, Set<Long>> body) {
        Set<Long> badgeInstanceIds = body.get("badgeInstanceIds");
        BadgeInstanceCollection updated = collectionService.removeBadgesFromCollection(collectionId, badgeInstanceIds);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Badges removed", updated, null));
    }
} 