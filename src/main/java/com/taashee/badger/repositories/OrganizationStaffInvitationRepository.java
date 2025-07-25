package com.taashee.badger.repositories;

import com.taashee.badger.models.OrganizationStaffInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrganizationStaffInvitationRepository extends JpaRepository<OrganizationStaffInvitation, Long> {
    Optional<OrganizationStaffInvitation> findByToken(String token);
    List<OrganizationStaffInvitation> findByOrganizationId(Long organizationId);
    List<OrganizationStaffInvitation> findByEmailAndStatus(String email, OrganizationStaffInvitation.Status status);
    List<OrganizationStaffInvitation> findByOrganizationIdAndStatus(Long organizationId, OrganizationStaffInvitation.Status status);
    List<OrganizationStaffInvitation> findByEmailAndOrganizationIdAndStatus(String email, Long organizationId, OrganizationStaffInvitation.Status status);
} 