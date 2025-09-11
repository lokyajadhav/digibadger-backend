package com.taashee.badger.repositories;

import com.taashee.badger.models.AuditLog;
import com.taashee.badger.models.Organization;
import com.taashee.badger.models.Pathway;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByOrganizationOrderByCreatedAtDesc(Organization organization);
    List<AuditLog> findByPathwayOrderByCreatedAtDesc(Pathway pathway);
}
