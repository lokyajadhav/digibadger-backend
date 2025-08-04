package com.taashee.badger.repositories;

import com.taashee.badger.models.PathwayElementProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PathwayElementProgressRepository extends JpaRepository<PathwayElementProgress, Long> {
    
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.pathwayProgress.id = :pathwayProgressId AND pep.element.id = :elementId")
    Optional<PathwayElementProgress> findByPathwayProgressIdAndElementId(@Param("pathwayProgressId") Long pathwayProgressId, @Param("elementId") Long elementId);
    
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.pathwayProgress.id = :pathwayProgressId")
    List<PathwayElementProgress> findByPathwayProgressId(@Param("pathwayProgressId") Long pathwayProgressId);
    
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.element.id = :elementId")
    List<PathwayElementProgress> findByElementId(@Param("elementId") Long elementId);
    
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.pathwayProgress.pathway.id = :pathwayId AND pep.isCompleted = true")
    List<PathwayElementProgress> findCompletedByPathwayId(@Param("pathwayId") Long pathwayId);
    
    @Query("SELECT COUNT(pep) FROM PathwayElementProgress pep WHERE pep.pathwayProgress.pathway.id = :pathwayId AND pep.isCompleted = true")
    Long countCompletedByPathwayId(@Param("pathwayId") Long pathwayId);
    
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.pathwayProgress.user.id = :userId AND pep.element.id = :elementId")
    List<PathwayElementProgress> findByUserIdAndElementId(@Param("userId") Long userId, @Param("elementId") Long elementId);
} 