package com.taashee.badger.repositories;

import com.taashee.badger.models.Pathway;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PathwayRepository extends JpaRepository<Pathway, Long> {
    
    // Find all pathways for a specific organization
    List<Pathway> findByOrganizationId(Long organizationId);
    
    // Find active pathways for a specific organization
    @Query("SELECT p FROM Pathway p WHERE p.organization.id = :organizationId")
    List<Pathway> findActivePathwaysByOrganization(@Param("organizationId") Long organizationId);
    
    // Find pathway by name and organization
    Pathway findByNameAndOrganizationId(String name, Long organizationId);
    
    // Check if pathway exists by name and organization
    boolean existsByNameAndOrganizationId(String name, Long organizationId);
} 