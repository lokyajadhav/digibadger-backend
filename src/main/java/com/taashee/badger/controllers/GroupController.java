package com.taashee.badger.controllers;

import com.taashee.badger.models.*;
import com.taashee.badger.repositories.OrganizationRepository;
import com.taashee.badger.services.GroupService;
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
@RequestMapping("/api/organizations/{orgId}/groups")
public class GroupController {
    private final GroupService groupService;
    private final OrganizationRepository organizationRepository;

    public GroupController(GroupService groupService, OrganizationRepository organizationRepository) {
        this.groupService = groupService;
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
    public ResponseEntity<List<GroupDto>> listGroups(@PathVariable Long orgId) {
        try {
            Organization organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            
            User currentUser = getCurrentUser();
            if (!groupService.canManageGroups(currentUser, organization)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            List<Group> groups = groupService.listGroups(organization);
            List<GroupDto> groupDtos = groups.stream()
                    .map(this::toGroupDto)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(groupDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<GroupDto> createGroup(@PathVariable Long orgId, @RequestBody CreateGroupRequest request) {
        try {
            Organization organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            
            User currentUser = getCurrentUser();
            if (!groupService.canManageGroups(currentUser, organization)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Group group = groupService.createGroup(organization, currentUser, request.name(), request.description());
            return ResponseEntity.ok(toGroupDto(group));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDto> getGroup(@PathVariable Long orgId, @PathVariable Long groupId) {
        try {
            Organization organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            
            User currentUser = getCurrentUser();
            if (!groupService.canManageGroups(currentUser, organization)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Group group = groupService.getGroup(groupId);
            return ResponseEntity.ok(toGroupDto(group));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<GroupDto> updateGroup(@PathVariable Long orgId, @PathVariable Long groupId, 
                                              @RequestBody UpdateGroupRequest request) {
        try {
            Organization organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            
            User currentUser = getCurrentUser();
            if (!groupService.canManageGroups(currentUser, organization)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Group group = groupService.updateGroup(groupId, request.name(), request.description());
            return ResponseEntity.ok(toGroupDto(group));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long orgId, @PathVariable Long groupId) {
        try {
            Organization organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            
            User currentUser = getCurrentUser();
            if (!groupService.canManageGroups(currentUser, organization)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            groupService.deleteGroup(groupId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberDto>> listGroupMembers(@PathVariable Long orgId, @PathVariable Long groupId) {
        try {
            Organization organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            
            User currentUser = getCurrentUser();
            if (!groupService.canManageGroups(currentUser, organization)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            List<GroupMember> members = groupService.listGroupMembers(groupId);
            List<GroupMemberDto> memberDtos = members.stream()
                    .map(this::toGroupMemberDto)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(memberDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{groupId}/available-users")
    public ResponseEntity<List<UserDto>> listAvailableUsers(@PathVariable Long orgId, @PathVariable Long groupId) {
        try {
            Organization organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            
            User currentUser = getCurrentUser();
            if (!groupService.canManageGroups(currentUser, organization)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            List<User> availableUsers = groupService.listAvailableUsersForGroup(groupId);
            List<UserDto> userDtos = availableUsers.stream()
                    .map(this::toUserDto)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(userDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<GroupMemberDto> addMember(@PathVariable Long orgId, @PathVariable Long groupId, 
                                                   @PathVariable Long userId) {
        try {
            Organization organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            
            User currentUser = getCurrentUser();
            if (!groupService.canAddUserToGroup(currentUser, groupId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            GroupMember member = groupService.addMemberToGroup(groupId, userId);
            return ResponseEntity.ok(toGroupMemberDto(member));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long orgId, @PathVariable Long groupId, 
                                           @PathVariable Long userId) {
        try {
            Organization organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            
            User currentUser = getCurrentUser();
            if (!groupService.canManageGroups(currentUser, organization)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            groupService.removeMemberFromGroup(groupId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DTOs
    public record GroupDto(Long id, String name, String description, Long organizationId, 
                          String createdBy, String createdAt, int memberCount) {}
    
    public record GroupMemberDto(Long id, Long groupId, Long userId, String userName, String userEmail, String joinedAt) {}
    
    public record UserDto(Long id, String email, String firstName, String lastName) {}
    
    public record CreateGroupRequest(String name, String description) {}
    
    public record UpdateGroupRequest(String name, String description) {}

    // Mappers
    private GroupDto toGroupDto(Group group) {
        return new GroupDto(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getOrganization().getId(),
                group.getCreatedBy().getFirstName() + " " + group.getCreatedBy().getLastName(),
                group.getCreatedAt().toString(),
                group.getMembers().size()
        );
    }

    private GroupMemberDto toGroupMemberDto(GroupMember member) {
        return new GroupMemberDto(
                member.getId(),
                member.getGroup().getId(),
                member.getUser().getId(),
                member.getUser().getFirstName() + " " + member.getUser().getLastName(),
                member.getUser().getEmail(),
                member.getJoinedAt().toString()
        );
    }

    private UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}
