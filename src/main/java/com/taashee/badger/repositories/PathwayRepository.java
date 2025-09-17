package com.taashee.badger.repositories;

import com.taashee.badger.models.Pathway;
import com.taashee.badger.models.Organization;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PathwayRepository extends JpaRepository<Pathway, Long> {
    List<Pathway> findByOrganization(Organization organization);
    List<Pathway> findByOrganizationIdAndNameContaining(Long organizationId, String name);
}


