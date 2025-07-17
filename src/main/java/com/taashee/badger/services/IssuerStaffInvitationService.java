package com.taashee.badger.services;

import com.taashee.badger.models.IssuerStaffInvitation;
import java.util.List;
import java.util.Optional;

public interface IssuerStaffInvitationService {
    IssuerStaffInvitation createInvitation(IssuerStaffInvitation invitation);
    Optional<IssuerStaffInvitation> findByToken(String token);
    List<IssuerStaffInvitation> findByIssuerId(Long issuerId);
    List<IssuerStaffInvitation> findByEmailAndStatus(String email, IssuerStaffInvitation.Status status);
    List<IssuerStaffInvitation> findByIssuerIdAndStatus(Long issuerId, IssuerStaffInvitation.Status status);
    IssuerStaffInvitation acceptInvitation(String token);
    IssuerStaffInvitation rejectInvitation(String token);
    Optional<IssuerStaffInvitation> findById(Long id);
    IssuerStaffInvitation save(IssuerStaffInvitation invitation);
} 