package com.taashee.badger.services;

import com.taashee.badger.models.OrganizationStaff;
import java.util.List;

/**
 * Service for managing organization-specific staff.
 * Author: Lokya Naik
 */
public interface OrganizationStaffService {
    List<OrganizationStaff> getStaffByOrganizationId(Long organizationId);
    OrganizationStaff addStaffToOrganization(Long organizationId, OrganizationStaff staff);
    OrganizationStaff updateStaff(Long staffId, OrganizationStaff staff);
    void removeStaff(Long staffId);
    boolean userExists(String email);
    void removeStaffCompletely(Long organizationId, Long staffId, boolean fullDelete);
} 