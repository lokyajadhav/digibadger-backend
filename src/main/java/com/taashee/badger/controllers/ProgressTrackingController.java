package com.taashee.badger.controllers;

import com.taashee.badger.models.*;
import com.taashee.badger.repositories.*;
import com.taashee.badger.services.ProgressTrackingService;
import com.taashee.badger.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/organizations/{orgId}")
public class ProgressTrackingController {
    private final ProgressTrackingService progressService;
    private final OrganizationRepository organizationRepository;
    private final OrganizationStaffRepository organizationStaffRepository;
    private final OrganizationUserRepository organizationUserRepository;
    private final GroupMemberRepository groupMemberRepository;

    public ProgressTrackingController(ProgressTrackingService progressService, 
                                    OrganizationRepository organizationRepository,
                                    OrganizationStaffRepository organizationStaffRepository,
                                    OrganizationUserRepository organizationUserRepository,
                                    GroupMemberRepository groupMemberRepository) {
        this.progressService = progressService;
        this.organizationRepository = organizationRepository;
        this.organizationStaffRepository = organizationStaffRepository;
        this.organizationUserRepository = organizationUserRepository;
        this.groupMemberRepository = groupMemberRepository;
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

    // Group Progress APIs
    @GetMapping("/pathways/{pathwayId}/progress")
    public ResponseEntity<List<PathwayProgressDto>> getPathwayProgress(@PathVariable Long orgId, 
                                                                     @PathVariable Long pathwayId) {
        try {
            Organization organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            
            User currentUser = getCurrentUser();
            // Allow ISSUERs to view progress
            if (currentUser.getRoles() == null || !currentUser.getRoles().contains("ISSUER")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // This endpoint returns all progress for a pathway across all groups
            // Implementation depends on specific requirements
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/groups/{groupId}/progress")
    public ResponseEntity<Map<String, Object>> getGroupProgressSummary(@PathVariable Long orgId, 
                                                                      @PathVariable Long groupId,
                                                                      @RequestParam(required = false) Long pathwayId) {
        try {
            Organization organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            
            User currentUser = getCurrentUser();
            // Allow ISSUERs to view progress
            if (currentUser.getRoles() == null || !currentUser.getRoles().contains("ISSUER")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Map<String, Object> summary = progressService.getGroupProgressSummary(groupId, pathwayId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/pathways/{pathwayId}/steps/{stepId}/complete/{userId}")
    public ResponseEntity<StepProgressDto> completeStep(@PathVariable Long orgId, 
                                                      @PathVariable Long pathwayId,
                                                      @PathVariable Long stepId,
                                                      @PathVariable Long userId,
                                                      @RequestParam Long groupId) {
        try {
            Organization organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            
            User currentUser = getCurrentUser();
            // Allow ISSUERs to mark steps as complete (for testing/demo purposes)
            if (currentUser.getRoles() == null || !currentUser.getRoles().contains("ISSUER")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // For now, we'll use the stepId as stepVersionId since the system is simplified
            // In a full implementation, you'd need to find the correct step version
            Long stepVersionId = stepId; // This assumes stepId is actually stepVersionId
            
            progressService.completeStep(stepVersionId, userId, groupId);
            
            // Return the updated step progress
            StepProgress stepProgress = progressService.updateStepProgress(
                    stepVersionId, userId, groupId, StepProgress.ProgressStatus.COMPLETED);
            
            return ResponseEntity.ok(toStepProgressDto(stepProgress));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Individual Progress APIs
    @GetMapping("/users/{userId}/pathways")
    public ResponseEntity<List<PathwayProgressDto>> getUserPathwayProgress(@PathVariable Long orgId, 
                                                                         @PathVariable Long userId) {
        try {
            Organization organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            
            User currentUser = getCurrentUser();
            
            // Users can only view their own progress, ISSUERs can view any user's progress
            if (!currentUser.getId().equals(userId) && 
                (currentUser.getRoles() == null || !currentUser.getRoles().contains("ISSUER"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            List<PathwayProgress> progressList = progressService.getUserPathwayProgress(userId);
            List<PathwayProgressDto> progressDtos = progressList.stream()
                    .map(this::toPathwayProgressDto)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(progressDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/users/{userId}/subscribed-pathways")
    public ResponseEntity<List<Map<String, Object>>> getUserSubscribedPathways(@PathVariable Long orgId, 
                                                                             @PathVariable Long userId) {
        try {
            Organization organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            
            User currentUser = getCurrentUser();
            
            // Users can only view their own progress, ISSUERs can view any user's progress
            if (!currentUser.getId().equals(userId) && 
                (currentUser.getRoles() == null || !currentUser.getRoles().contains("ISSUER"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            List<Map<String, Object>> subscribedPathways = progressService.getUserSubscribedPathways(userId);
            return ResponseEntity.ok(subscribedPathways);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/users/{userId}/progress-summary")
    public ResponseEntity<Map<String, Object>> getIndividualProgressSummary(@PathVariable Long orgId, 
                                                                          @PathVariable Long userId) {
        try {
            Organization organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            
            User currentUser = getCurrentUser();
            
            // Users can only view their own progress, ISSUERs can view any user's progress
            if (!currentUser.getId().equals(userId) && 
                (currentUser.getRoles() == null || !currentUser.getRoles().contains("ISSUER"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Map<String, Object> summary = progressService.getIndividualProgressSummary(userId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Debug endpoint to test progress calculation
    @GetMapping("/debug/groups/{groupId}/progress")
    public ResponseEntity<Map<String, Object>> debugGroupProgress(@PathVariable Long orgId, 
                                                                @PathVariable Long groupId,
                                                                @RequestParam(required = false) Long pathwayId) {
        try {
            Organization organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            
            User currentUser = getCurrentUser();
            if (currentUser.getRoles() == null || !currentUser.getRoles().contains("ISSUER")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Map<String, Object> debugInfo = progressService.getGroupProgressSummary(groupId, pathwayId);
            
            // Add additional debug information
            debugInfo.put("debug_info", Map.of(
                "orgId", orgId,
                "groupId", groupId,
                "pathwayId", pathwayId,
                "timestamp", System.currentTimeMillis()
            ));
            
            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            Map<String, Object> errorInfo = Map.of(
                "error", e.getMessage(),
                "orgId", orgId,
                "groupId", groupId,
                "pathwayId", pathwayId
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorInfo);
        }
    }

    // Debug endpoint to check user organization status
    @GetMapping("/debug/user-organization-status")
    public ResponseEntity<Map<String, Object>> getUserOrganizationStatus() {
        try {
            User currentUser = getCurrentUser();
            
            // Check if user is an ISSUER
            boolean isIssuer = currentUser.getRoles() != null && currentUser.getRoles().contains("ISSUER");
            
            // Check organization staff relationships
            List<OrganizationStaff> staffRelationships = organizationStaffRepository.findByUserId(currentUser.getId());
            
            // Check organization user relationships  
            List<OrganizationUser> userRelationships = organizationUserRepository.findByUserId(currentUser.getId());
            
            // Check group memberships
            List<GroupMember> groupMemberships = groupMemberRepository.findByUser(currentUser);
            
            Map<String, Object> status = Map.of(
                "userId", currentUser.getId(),
                "userEmail", currentUser.getEmail(),
                "userRoles", currentUser.getRoles() != null ? currentUser.getRoles() : List.of(),
                "isIssuer", isIssuer,
                "staffRelationships", staffRelationships.stream().<Map<String, Object>>map(staff -> Map.of(
                    "orgId", staff.getOrganization().getId(),
                    "orgName", staff.getOrganization().getNameEnglish() != null ? staff.getOrganization().getNameEnglish() : "N/A",
                    "staffRole", staff.getStaffRole()
                )).collect(Collectors.toList()),
                "userRelationships", userRelationships.stream().<Map<String, Object>>map(user -> Map.of(
                    "orgId", user.getOrganization().getId(),
                    "orgName", user.getOrganization().getNameEnglish() != null ? user.getOrganization().getNameEnglish() : "N/A",
                    "userType", user.getUserType()
                )).collect(Collectors.toList()),
                "groupMemberships", groupMemberships.stream().<Map<String, Object>>map(member -> Map.of(
                    "groupId", member.getGroup().getId(),
                    "groupName", member.getGroup().getName(),
                    "orgId", member.getGroup().getOrganization().getId(),
                    "orgName", member.getGroup().getOrganization().getNameEnglish() != null ? member.getGroup().getOrganization().getNameEnglish() : "N/A",
                    "joinedAt", member.getJoinedAt()
                )).collect(Collectors.toList()),
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            Map<String, Object> errorInfo = Map.of(
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorInfo);
        }
    }

    // DTOs are defined in separate classes: PathwayProgressDto and StepProgressDto

    // Mappers
    private PathwayProgressDto toPathwayProgressDto(PathwayProgress progress) {
        return new PathwayProgressDto(progress);
    }

    private StepProgressDto toStepProgressDto(StepProgress progress) {
        return new StepProgressDto(progress);
    }
}
