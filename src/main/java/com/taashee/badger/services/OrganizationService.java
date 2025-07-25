package com.taashee.badger.services;

import com.taashee.badger.models.Organization;
import java.util.List;
import java.util.Optional;

public interface OrganizationService {
    Organization createOrganization(Organization organization);
    Organization updateOrganization(Long id, Organization organization);
    void deleteOrganization(Long id);
    Optional<Organization> getOrganizationById(Long id);
    List<Organization> getAllOrganizations();
    Organization archiveOrganization(Long id, boolean archive);
    List<Organization> bulkArchiveOrganizations(List<Long> ids, boolean archive);
    void bulkDeleteOrganizations(List<Long> ids);
    List<Organization> getOrganizationsForUser(String email);
    // Add more methods as needed (e.g., archive, search, etc.)
} 