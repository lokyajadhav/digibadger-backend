package com.taashee.badger.repositories;

import com.taashee.badger.models.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    // Add custom queries if needed
} 