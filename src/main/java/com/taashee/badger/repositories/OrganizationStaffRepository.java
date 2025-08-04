package com.taashee.badger.repositories;

import com.taashee.badger.models.OrganizationStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface OrganizationStaffRepository extends JpaRepository<OrganizationStaff, Long> {
    @Query("SELECT os FROM OrganizationStaff os WHERE os.organization.id = :organizationId")
    List<OrganizationStaff> findByOrganizationId(@Param("organizationId") Long organizationId);
    
    @Query("SELECT os FROM OrganizationStaff os WHERE os.user.id = :userId")
    List<OrganizationStaff> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT os FROM OrganizationStaff os WHERE os.user.id = :userId AND os.staffRole = :staffRole")
    List<OrganizationStaff> findByUserIdAndStaffRole(@Param("userId") Long userId, @Param("staffRole") String staffRole);
    
    @Query("SELECT os FROM OrganizationStaff os WHERE os.organization.id = :organizationId AND os.user.id = :userId")
    Optional<OrganizationStaff> findByOrganizationIdAndUserId(@Param("organizationId") Long organizationId, @Param("userId") Long userId);
    
    @Query("SELECT os FROM OrganizationStaff os WHERE os.user.id = :userId AND os.organization.id = :organizationId AND os.staffRole = :staffRole")
    Optional<OrganizationStaff> findByUserIdAndOrganizationIdAndStaffRole(@Param("userId") Long userId, @Param("organizationId") Long organizationId, @Param("staffRole") String staffRole);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM OrganizationStaff os WHERE os.organization.id = :organizationId")
    void deleteByOrganizationId(@Param("organizationId") Long organizationId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM OrganizationStaff os WHERE os.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
} 