package com.taashee.badger.repositories;

import com.taashee.badger.models.OrganizationStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface OrganizationStaffRepository extends JpaRepository<OrganizationStaff, Long> {
    List<OrganizationStaff> findByOrganizationId(Long organizationId);
    List<OrganizationStaff> findByUserId(Long userId);
    List<OrganizationStaff> findByUserIdAndStaffRole(Long userId, String staffRole);
    Optional<OrganizationStaff> findByOrganizationIdAndUserId(Long organizationId, Long userId);
    Optional<OrganizationStaff> findByUserIdAndOrganizationIdAndStaffRole(Long userId, Long organizationId, String staffRole);
    @Modifying
    @Transactional
    @Query("DELETE FROM OrganizationStaff os WHERE os.organization.id = :organizationId")
    void deleteByOrganizationId(Long organizationId);
} 