package com.taashee.badger.repositories;

import com.taashee.badger.models.IssuerStaffInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IssuerStaffInvitationRepository extends JpaRepository<IssuerStaffInvitation, Long> {
    Optional<IssuerStaffInvitation> findByToken(String token);
    List<IssuerStaffInvitation> findByIssuerId(Long issuerId);
    List<IssuerStaffInvitation> findByEmailAndStatus(String email, IssuerStaffInvitation.Status status);
    List<IssuerStaffInvitation> findByIssuerIdAndStatus(Long issuerId, IssuerStaffInvitation.Status status);
    List<IssuerStaffInvitation> findByEmailAndIssuerIdAndStatus(String email, Long issuerId, IssuerStaffInvitation.Status status);
} 