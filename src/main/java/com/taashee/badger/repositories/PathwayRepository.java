package com.taashee.badger.repositories;

import com.taashee.badger.models.Pathway;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

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
    
    // === ENTERPRISE-GRADE REPOSITORY METHODS ===
    
    // Find pathway by ID and organization ID
    @Query("SELECT p FROM Pathway p WHERE p.id = :pathwayId AND p.organization.id = :organizationId")
    Optional<Pathway> findByIdAndOrganizationId(@Param("pathwayId") Long pathwayId, @Param("organizationId") Long organizationId);
    
    // Check if pathway exists by ID and organization ID
    @Query("SELECT COUNT(p) > 0 FROM Pathway p WHERE p.id = :pathwayId AND p.organization.id = :organizationId")
    boolean existsByIdAndOrganizationId(@Param("pathwayId") Long pathwayId, @Param("organizationId") Long organizationId);
    
    // Find pathways by organization ID and status
    @Query("SELECT p FROM Pathway p WHERE p.organization.id = :organizationId AND p.status = :status")
    List<Pathway> findByOrganizationIdAndStatus(@Param("organizationId") Long organizationId, @Param("status") Pathway.PathwayStatus status);
    
    // Find published pathway by ID, organization ID, and status
    @Query("SELECT p FROM Pathway p WHERE p.id = :pathwayId AND p.organization.id = :organizationId AND p.status = :status")
    Optional<Pathway> findByIdAndOrganizationIdAndStatus(@Param("pathwayId") Long pathwayId, @Param("organizationId") Long organizationId, @Param("status") Pathway.PathwayStatus status);
    
    // Find pathways by organization ID with pagination
    @Query("SELECT p FROM Pathway p WHERE p.organization.id = :organizationId ORDER BY p.createdAt DESC")
    List<Pathway> findByOrganizationIdOrderByCreatedAtDesc(@Param("organizationId") Long organizationId);
    
    // Find published pathways by organization ID with pagination
    @Query("SELECT p FROM Pathway p WHERE p.organization.id = :organizationId AND p.status = :status ORDER BY p.publishedAt DESC")
    List<Pathway> findByOrganizationIdAndStatusOrderByPublishedAtDesc(@Param("organizationId") Long organizationId, @Param("status") Pathway.PathwayStatus status);
    
    // Search pathways by organization ID and name/description
    @Query("SELECT p FROM Pathway p WHERE p.organization.id = :organizationId AND (p.name LIKE %:searchTerm% OR p.description LIKE %:searchTerm%)")
    List<Pathway> findByOrganizationIdAndNameOrDescriptionContaining(@Param("organizationId") Long organizationId, @Param("searchTerm") String searchTerm);
    
    // Find pathways created by a specific user in an organization
    @Query("SELECT p FROM Pathway p WHERE p.organization.id = :organizationId AND p.createdBy.email = :userEmail")
    List<Pathway> findByOrganizationIdAndCreatedByEmail(@Param("organizationId") Long organizationId, @Param("userEmail") String userEmail);
    
    // Find pathways published by a specific user in an organization
    @Query("SELECT p FROM Pathway p WHERE p.organization.id = :organizationId AND p.publishedBy.email = :userEmail")
    List<Pathway> findByOrganizationIdAndPublishedByEmail(@Param("organizationId") Long organizationId, @Param("userEmail") String userEmail);
    
    // Get pathway statistics for an organization
    @Query("SELECT p.status, COUNT(p) FROM Pathway p WHERE p.organization.id = :organizationId GROUP BY p.status")
    List<Object[]> getPathwayStatisticsByOrganization(@Param("organizationId") Long organizationId);
    
    // Find pathways by tags in an organization
    @Query("SELECT p FROM Pathway p WHERE p.organization.id = :organizationId AND p.tags::text LIKE %:tag%")
    List<Pathway> findByOrganizationIdAndTagsContaining(@Param("organizationId") Long organizationId, @Param("tag") String tag);
    
    // Find pathways by difficulty level in an organization
    @Query("SELECT p FROM Pathway p WHERE p.organization.id = :organizationId AND p.difficultyLevel = :difficultyLevel")
    List<Pathway> findByOrganizationIdAndDifficultyLevel(@Param("organizationId") Long organizationId, @Param("difficultyLevel") Pathway.DifficultyLevel difficultyLevel);
    
    // Find pathways by completion type in an organization
    @Query("SELECT p FROM Pathway p WHERE p.organization.id = :organizationId AND p.completionType = :completionType")
    List<Pathway> findByOrganizationIdAndCompletionType(@Param("organizationId") Long organizationId, @Param("completionType") Pathway.CompletionType completionType);
} 