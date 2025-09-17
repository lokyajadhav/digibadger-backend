package com.taashee.badger.repositories;

import com.taashee.badger.models.Group;
import com.taashee.badger.models.Organization;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByOrganization(Organization organization);
    List<Group> findByOrganizationId(Long organizationId);
    List<Group> findByOrganizationIdAndNameContaining(Long organizationId, String name);
}
