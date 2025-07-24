package com.taashee.badger.controllers;

import com.taashee.badger.models.OrganizationStaffInvitation;
import com.taashee.badger.services.OrganizationStaffInvitationService;
import com.taashee.badger.serviceimpl.OrganizationStaffInvitationServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;

class OrganizationStaffInvitationDTO {
    public Long id;
    public String email;
    public Long organizationId;
    public String staffRole;
    public Boolean isSigner;
    public String status;
    public String token;
    public Long createdById;
    public LocalDateTime createdAt;
    public LocalDateTime acceptedAt;
    public OrganizationStaffInvitationDTO(OrganizationStaffInvitation inv) {
        this.id = inv.getId();
        this.email = inv.getEmail();
        this.organizationId = inv.getOrganizationId();
        this.staffRole = inv.getStaffRole();
        this.isSigner = inv.getIsSigner();
        this.status = inv.getStatus() != null ? inv.getStatus().name() : null;
        this.token = inv.getToken();
        this.createdById = inv.getCreatedBy() != null ? inv.getCreatedBy().getId() : null;
        this.createdAt = inv.getCreatedAt();
        this.acceptedAt = inv.getAcceptedAt();
    }
}

@RestController
@RequestMapping("/api/organization-staff-invitations")
@Tag(name = "Organization Staff Invitations", description = "Endpoints for managing organization staff invitations. Author: Lokya Naik")
public class OrganizationStaffInvitationController {

    @Autowired
    private OrganizationStaffInvitationService invitationService;
    @Autowired
    private OrganizationStaffInvitationServiceImpl invitationServiceImpl;

    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @Operation(summary = "Create a new staff invitation", description = "Creates a new invitation for organization staff.")
    @PostMapping
    public ResponseEntity<OrganizationStaffInvitation> createInvitation(@RequestBody OrganizationStaffInvitation invitation) {
        return ResponseEntity.ok(invitationService.createInvitation(invitation));
    }

    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @Operation(summary = "Get invitation by token", description = "Fetches an invitation by its token.")
    @GetMapping("/token/{token}")
    public ResponseEntity<OrganizationStaffInvitation> getByToken(@PathVariable String token) {
        Optional<OrganizationStaffInvitation> invitation = invitationService.findByToken(token);
        return invitation.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @Operation(summary = "Get invitations by organization", description = "Fetches all invitations for a given organization.")
    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<List<OrganizationStaffInvitationDTO>> getByOrganization(@PathVariable Long organizationId) {
        List<OrganizationStaffInvitation> invitations = invitationService.findByOrganizationId(organizationId);
        List<OrganizationStaffInvitationDTO> dtos = invitations.stream().map(OrganizationStaffInvitationDTO::new).toList();
        return ResponseEntity.ok(dtos);
    }

    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @Operation(summary = "Accept invitation", description = "Accepts a staff invitation by token.")
    @PostMapping("/accept/{token}")
    public ResponseEntity<OrganizationStaffInvitationDTO> acceptInvitation(@PathVariable String token) {
        OrganizationStaffInvitation invitation = invitationService.acceptInvitation(token);
        return ResponseEntity.ok(new OrganizationStaffInvitationDTO(invitation));
    }

    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @Operation(summary = "Reject invitation", description = "Rejects (expires) a staff invitation by token.")
    @PostMapping("/reject/{token}")
    public ResponseEntity<OrganizationStaffInvitationDTO> rejectInvitation(@PathVariable String token) {
        OrganizationStaffInvitation invitation = invitationService.rejectInvitation(token);
        return ResponseEntity.ok(new OrganizationStaffInvitationDTO(invitation));
    }

    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZATION')")
    @Operation(summary = "Update a staff invitation", description = "Updates an existing staff invitation and resends the invitation email if still pending.")
    @PutMapping("/{invitationId}")
    public ResponseEntity<OrganizationStaffInvitationDTO> updateInvitation(
        @PathVariable Long invitationId,
        @RequestBody java.util.Map<String, Object> updates
    ) {
        OrganizationStaffInvitation invitation = invitationService.findById(invitationId)
            .orElseThrow(() -> new RuntimeException("Invitation not found"));
        if (updates.containsKey("email")) invitation.setEmail((String) updates.get("email"));
        if (updates.containsKey("staffRole")) invitation.setStaffRole((String) updates.get("staffRole"));
        if (updates.containsKey("isSigner")) invitation.setIsSigner((Boolean) updates.get("isSigner"));
        // Resend invitation email if still pending
        if (invitation.getStatus() == OrganizationStaffInvitation.Status.PENDING) {
            String acceptUrl = "http://localhost:5173/accept-organization-invitation?token=" + invitation.getToken();
            org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
            message.setTo(invitation.getEmail());
            message.setSubject("You're invited as staff to an Organization on Badger Management");
            message.setText("Hello,\n\nYou have been invited to join an Organization as staff (role: " + invitation.getStaffRole() + ").\nPlease click the link below to accept the invitation and activate your account:\n" + acceptUrl + "\n\nIf you did not expect this invitation, you can ignore this email.\n");
            message.setFrom("appadmin@taashee.com");
            invitationServiceImpl.sendCustomEmail(message);
        }
        OrganizationStaffInvitation saved = invitationService.save(invitation);
        return ResponseEntity.ok(new OrganizationStaffInvitationDTO(saved));
    }
} 