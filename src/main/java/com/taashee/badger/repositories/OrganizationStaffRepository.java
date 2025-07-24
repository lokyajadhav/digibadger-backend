package com.taashee.badger.repositories;

import com.taashee.badger.models.OrganizationStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrganizationStaffRepository extends JpaRepository<OrganizationStaff, Long> {
    List<OrganizationStaff> findByOrganizationId(Long organizationId);
    List<OrganizationStaff> findByUserId(Long userId);
    void deleteByOrganizationId(Long organizationId);
} 