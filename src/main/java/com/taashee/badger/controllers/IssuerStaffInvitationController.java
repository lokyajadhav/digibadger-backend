package com.taashee.badger.controllers;

import com.taashee.badger.models.IssuerStaffInvitation;
import com.taashee.badger.services.IssuerStaffInvitationService;
import com.taashee.badger.serviceimpl.IssuerStaffInvitationServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

class IssuerStaffInvitationDTO {
    public Long id;
    public String email;
    public Long issuerId;
    public String staffRole;
    public Boolean isSigner;
    public String status;
    public String token;
    public Long createdById;
    public LocalDateTime createdAt;
    public LocalDateTime acceptedAt;
    public IssuerStaffInvitationDTO(IssuerStaffInvitation inv) {
        this.id = inv.getId();
        this.email = inv.getEmail();
        this.issuerId = inv.getIssuerId();
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
@RequestMapping("/api/issuer-staff-invitations")
@Tag(name = "Issuer Staff Invitations", description = "Endpoints for managing issuer staff invitations. Author: Lokya Naik")
public class IssuerStaffInvitationController {

    @Autowired
    private IssuerStaffInvitationService invitationService;
    @Autowired
    private IssuerStaffInvitationServiceImpl invitationServiceImpl;

    @Operation(summary = "Create a new staff invitation", description = "Creates a new invitation for issuer staff.")
    @PostMapping
    public ResponseEntity<IssuerStaffInvitation> createInvitation(@RequestBody IssuerStaffInvitation invitation) {
        return ResponseEntity.ok(invitationService.createInvitation(invitation));
    }

    @Operation(summary = "Get invitation by token", description = "Fetches an invitation by its token.")
    @GetMapping("/token/{token}")
    public ResponseEntity<IssuerStaffInvitation> getByToken(@PathVariable String token) {
        Optional<IssuerStaffInvitation> invitation = invitationService.findByToken(token);
        return invitation.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get invitations by issuer", description = "Fetches all invitations for a given issuer.")
    @GetMapping("/issuer/{issuerId}")
    public ResponseEntity<List<IssuerStaffInvitationDTO>> getByIssuer(@PathVariable Long issuerId) {
        List<IssuerStaffInvitation> invitations = invitationService.findByIssuerId(issuerId);
        List<IssuerStaffInvitationDTO> dtos = invitations.stream().map(IssuerStaffInvitationDTO::new).toList();
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Accept invitation", description = "Accepts a staff invitation by token.")
    @PostMapping("/accept/{token}")
    public ResponseEntity<IssuerStaffInvitationDTO> acceptInvitation(@PathVariable String token) {
        IssuerStaffInvitation invitation = invitationService.acceptInvitation(token);
        return ResponseEntity.ok(new IssuerStaffInvitationDTO(invitation));
    }

    @Operation(summary = "Reject invitation", description = "Rejects (expires) a staff invitation by token.")
    @PostMapping("/reject/{token}")
    public ResponseEntity<IssuerStaffInvitationDTO> rejectInvitation(@PathVariable String token) {
        IssuerStaffInvitation invitation = invitationService.rejectInvitation(token);
        return ResponseEntity.ok(new IssuerStaffInvitationDTO(invitation));
    }

    @Operation(summary = "Update a staff invitation", description = "Updates an existing staff invitation and resends the invitation email if still pending.")
    @PutMapping("/{invitationId}")
    public ResponseEntity<IssuerStaffInvitationDTO> updateInvitation(
        @PathVariable Long invitationId,
        @RequestBody java.util.Map<String, Object> updates
    ) {
        IssuerStaffInvitation invitation = invitationService.findById(invitationId)
            .orElseThrow(() -> new RuntimeException("Invitation not found"));
        if (updates.containsKey("email")) invitation.setEmail((String) updates.get("email"));
        if (updates.containsKey("staffRole")) invitation.setStaffRole((String) updates.get("staffRole"));
        if (updates.containsKey("isSigner")) invitation.setIsSigner((Boolean) updates.get("isSigner"));
        // Resend invitation email if still pending
        if (invitation.getStatus() == IssuerStaffInvitation.Status.PENDING) {
            String acceptUrl = "http://localhost:5173/accept-issuer-invitation?token=" + invitation.getToken();
            org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
            message.setTo(invitation.getEmail());
            message.setSubject("You're invited as staff to an Issuer on Badger Management");
            message.setText("Hello,\n\nYou have been invited to join an Issuer as staff (role: " + invitation.getStaffRole() + ").\nPlease click the link below to accept the invitation and activate your account:\n" + acceptUrl + "\n\nIf you did not expect this invitation, you can ignore this email.\n");
            message.setFrom("appadmin@taashee.com");
            invitationServiceImpl.sendCustomEmail(message);
        }
        IssuerStaffInvitation saved = invitationService.save(invitation);
        return ResponseEntity.ok(new IssuerStaffInvitationDTO(saved));
    }
} 