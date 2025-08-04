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
    @Query("SELECT p FROM Pathway p WHERE p.organization.id = :organizationId")
    List<Pathway> findByOrganizationId(@Param("organizationId") Long organizationId);
    
    // Find active pathways for a specific organization
    @Query("SELECT p FROM Pathway p WHERE p.organization.id = :organizationId")
    List<Pathway> findActivePathwaysByOrganization(@Param("organizationId") Long organizationId);
    
    // Find pathway by name and organization
    @Query("SELECT p FROM Pathway p WHERE p.name = :name AND p.organization.id = :organizationId")
    Pathway findByNameAndOrganizationId(@Param("name") String name, @Param("organizationId") Long organizationId);
    
    // Check if pathway exists by name and organization
    @Query("SELECT COUNT(p) > 0 FROM Pathway p WHERE p.name = :name AND p.organization.id = :organizationId")
    boolean existsByNameAndOrganizationId(@Param("name") String name, @Param("organizationId") Long organizationId);
    
    // Find published pathways by organization IDs and status
    @Query("SELECT p FROM Pathway p WHERE p.organization.id IN :organizationIds AND p.status = :status")
    List<Pathway> findByOrganizationIdInAndStatus(@Param("organizationIds") List<Long> organizationIds, @Param("status") Pathway.PathwayStatus status);
    
    // Find pathways by status
    List<Pathway> findByStatus(Pathway.PathwayStatus status);
    
    // Find template pathways
    List<Pathway> findByIsTemplateTrue();
    
    // Find pathways by difficulty level
    List<Pathway> findByDifficultyLevel(Pathway.DifficultyLevel difficultyLevel);
    
    // Find pathways by completion type
    List<Pathway> findByCompletionType(Pathway.CompletionType completionType);
    
    // Search pathways by name containing
    @Query("SELECT p FROM Pathway p WHERE p.name LIKE %:searchTerm% OR p.description LIKE %:searchTerm%")
    List<Pathway> searchByNameOrDescription(@Param("searchTerm") String searchTerm);
    
    // Count pathways by organization
    @Query("SELECT COUNT(p) FROM Pathway p WHERE p.organization.id = :organizationId")
    Long countByOrganizationId(@Param("organizationId") Long organizationId);
    
    // Count published pathways by organization
    @Query("SELECT COUNT(p) FROM Pathway p WHERE p.organization.id = :organizationId AND p.status = :status")
    Long countByOrganizationIdAndStatus(@Param("organizationId") Long organizationId, @Param("status") Pathway.PathwayStatus status);
} 