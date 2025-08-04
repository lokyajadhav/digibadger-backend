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
    
    @Query("SELECT pp FROM PathwayProgress pp WHERE pp.pathway.id = :pathwayId AND pp.user.email = :userEmail")
    Optional<PathwayProgress> findByPathwayIdAndUserEmail(@Param("pathwayId") Long pathwayId, @Param("userEmail") String userEmail);
    
    @Query("SELECT pp FROM PathwayProgress pp WHERE pp.user.email = :userEmail")
    List<PathwayProgress> findByUserEmail(@Param("userEmail") String userEmail);
    
    @Query("SELECT pp FROM PathwayProgress pp WHERE pp.pathway.id = :pathwayId")
    List<PathwayProgress> findByPathwayId(@Param("pathwayId") Long pathwayId);
    
    @Query("SELECT pp FROM PathwayProgress pp WHERE pp.pathway.id = :pathwayId AND pp.user.id = :userId")
    Optional<PathwayProgress> findByPathwayIdAndUserId(@Param("pathwayId") Long pathwayId, @Param("userId") Long userId);
    

    
    @Query("SELECT pp FROM PathwayProgress pp WHERE pp.pathway.organization.id = :organizationId")
    List<PathwayProgress> findByOrganizationId(@Param("organizationId") Long organizationId);
    
    @Query("SELECT pp FROM PathwayProgress pp WHERE pp.isCompleted = true AND pp.pathway.id = :pathwayId")
    List<PathwayProgress> findCompletedByPathwayId(@Param("pathwayId") Long pathwayId);
    
    @Query("SELECT COUNT(pp) FROM PathwayProgress pp WHERE pp.pathway.id = :pathwayId")
    Long countByPathwayId(@Param("pathwayId") Long pathwayId);
    
    @Query("SELECT COUNT(pp) FROM PathwayProgress pp WHERE pp.pathway.id = :pathwayId AND pp.isCompleted = true")
    Long countCompletedByPathwayId(@Param("pathwayId") Long pathwayId);
    
    @Query("SELECT AVG(pp.progressPercentage) FROM PathwayProgress pp WHERE pp.pathway.id = :pathwayId")
    Double getAverageProgressByPathwayId(@Param("pathwayId") Long pathwayId);
} 