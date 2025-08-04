package com.taashee.badger.repositories;

import com.taashee.badger.models.OrganizationStaffInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface OrganizationStaffInvitationRepository extends JpaRepository<OrganizationStaffInvitation, Long> {
    Optional<OrganizationStaffInvitation> findByToken(String token);
    
    @Query("SELECT osi FROM OrganizationStaffInvitation osi WHERE osi.organizationId = :organizationId")
    List<OrganizationStaffInvitation> findByOrganizationId(@Param("organizationId") Long organizationId);
    
    List<OrganizationStaffInvitation> findByEmailAndStatus(String email, OrganizationStaffInvitation.Status status);
    
    @Query("SELECT osi FROM OrganizationStaffInvitation osi WHERE osi.organizationId = :organizationId AND osi.status = :status")
    List<OrganizationStaffInvitation> findByOrganizationIdAndStatus(@Param("organizationId") Long organizationId, @Param("status") OrganizationStaffInvitation.Status status);
    
    @Query("SELECT osi FROM OrganizationStaffInvitation osi WHERE osi.email = :email AND osi.organizationId = :organizationId AND osi.status = :status")
    List<OrganizationStaffInvitation> findByEmailAndOrganizationIdAndStatus(@Param("email") String email, @Param("organizationId") Long organizationId, @Param("status") OrganizationStaffInvitation.Status status);
} 