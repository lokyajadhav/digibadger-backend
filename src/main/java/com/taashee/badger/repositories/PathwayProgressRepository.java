package com.taashee.badger.repositories;

import com.taashee.badger.models.PathwayProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PathwayProgressRepository extends JpaRepository<PathwayProgress, Long> {
    
    // Find progress for a specific user and pathway
    Optional<PathwayProgress> findByUserIdAndPathwayId(Long userId, Long pathwayId);
    
    // Find all progress for a specific user
    List<PathwayProgress> findByUserId(Long userId);
    
    // Find all progress for a specific pathway
    List<PathwayProgress> findByPathwayId(Long pathwayId);
    
    // Find completed pathways for a user
    List<PathwayProgress> findByUserIdAndIsCompletedTrue(Long userId);
    
    // Count completed pathways for a user
    long countByUserIdAndIsCompletedTrue(Long userId);
    
    // Find progress by organization (for admin reporting)
    @Query("SELECT pp FROM PathwayProgress pp WHERE pp.pathway.organization.id = :organizationId")
    List<PathwayProgress> findByOrganizationId(@Param("organizationId") Long organizationId);
} 