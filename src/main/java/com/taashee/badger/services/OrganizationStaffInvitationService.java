package com.taashee.badger.services;

import com.taashee.badger.models.OrganizationStaffInvitation;
import java.util.List;
import java.util.Optional;

public interface OrganizationStaffInvitationService {
    OrganizationStaffInvitation createInvitation(OrganizationStaffInvitation invitation);
    Optional<OrganizationStaffInvitation> findByToken(String token);
    List<OrganizationStaffInvitation> findByOrganizationId(Long organizationId);
    List<OrganizationStaffInvitation> findByEmailAndStatus(String email, OrganizationStaffInvitation.Status status);
    List<OrganizationStaffInvitation> findByOrganizationIdAndStatus(Long organizationId, OrganizationStaffInvitation.Status status);
    OrganizationStaffInvitation acceptInvitation(String token);
    OrganizationStaffInvitation rejectInvitation(String token);
    Optional<OrganizationStaffInvitation> findById(Long id);
    OrganizationStaffInvitation save(OrganizationStaffInvitation invitation);
} 