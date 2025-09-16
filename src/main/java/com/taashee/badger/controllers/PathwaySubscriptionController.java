package com.taashee.badger.controllers;

import com.taashee.badger.models.*;
import com.taashee.badger.repositories.OrganizationRepository;
import com.taashee.badger.services.PathwaySubscriptionService;
import com.taashee.badger.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/organizations/{orgId}/pathways/{pathwayId}/groups")
public class PathwaySubscriptionController {
    private final PathwaySubscriptionService subscriptionService;
    private final OrganizationRepository organizationRepository;

    public PathwaySubscriptionController(PathwaySubscriptionService subscriptionService, 
                                       OrganizationRepository organizationRepository) {
        this.subscriptionService = subscriptionService;
        this.organizationRepository = organizationRepository;
    }

    @Autowired
    private UserService userService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new RuntimeException("User not authenticated");
        }
        String email = auth.getPrincipal().toString();
        return userService.findByEmail(email);
    }

    @GetMapping
    public ResponseEntity<List<PathwaySubscriptionDto>> getPathwaySubscriptions(@PathVariable Long orgId, 
                                                                                @PathVariable Long pathwayId) {
        try {
            Organization organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            
            User currentUser = getCurrentUser();
            if (!subscriptionService.canSubscribeToPathway(currentUser, pathwayId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            List<PathwayGroupSubscription> subscriptions = subscriptionService.getPathwaySubscriptions(pathwayId);
            List<PathwaySubscriptionDto> subscriptionDtos = subscriptions.stream()
                    .map(this::toPathwaySubscriptionDto)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(subscriptionDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<PathwaySubscriptionDto> subscribeGroup(@PathVariable Long orgId, 
                                                               @PathVariable Long pathwayId,
                                                               @RequestBody SubscribeGroupRequest request) {
        try {
            Organization organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            
            User currentUser = getCurrentUser();
            if (!subscriptionService.canSubscribeToPathway(currentUser, pathwayId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            PathwayGroupSubscription subscription = subscriptionService.subscribeGroupToPathway(
                    pathwayId, request.groupId(), currentUser);
            
            return ResponseEntity.ok(toPathwaySubscriptionDto(subscription));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> unsubscribeGroup(@PathVariable Long orgId, 
                                               @PathVariable Long pathwayId,
                                               @PathVariable Long groupId) {
        try {
            Organization organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            
            User currentUser = getCurrentUser();
            if (!subscriptionService.canUnsubscribeFromPathway(currentUser, pathwayId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            subscriptionService.unsubscribeGroupFromPathway(pathwayId, groupId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DTOs
    public record PathwaySubscriptionDto(Long id, Long pathwayId, String pathwayName, 
                                       Long groupId, String groupName, String subscribedBy, 
                                       String subscribedAt) {}
    
    public record SubscribeGroupRequest(Long groupId) {}

    // Mappers
    private PathwaySubscriptionDto toPathwaySubscriptionDto(PathwayGroupSubscription subscription) {
        return new PathwaySubscriptionDto(
                subscription.getId(),
                subscription.getPathway().getId(),
                subscription.getPathway().getName(),
                subscription.getGroup().getId(),
                subscription.getGroup().getName(),
                subscription.getSubscribedBy().getFirstName() + " " + subscription.getSubscribedBy().getLastName(),
                subscription.getSubscribedAt().toString()
        );
    }
}
