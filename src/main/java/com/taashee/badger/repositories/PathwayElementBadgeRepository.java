package com.taashee.badger.repositories;

import com.taashee.badger.models.PathwayElementBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PathwayElementBadgeRepository extends JpaRepository<PathwayElementBadge, Long> {
    
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.id = :elementId")
    List<PathwayElementBadge> findByElementId(@Param("elementId") Long elementId);
    
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND peb.isRequired = true")
    List<PathwayElementBadge> findByElementIdAndIsRequiredTrue(@Param("elementId") Long elementId);
    
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.badgeClass.id = :badgeClassId")
    List<PathwayElementBadge> findByBadgeClassId(@Param("badgeClassId") Long badgeClassId);
    
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.pathway.organization.id = :organizationId")
    List<PathwayElementBadge> findByOrganizationId(@Param("organizationId") Long organizationId);
    
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.pathway.id = :pathwayId")
    List<PathwayElementBadge> findByPathwayId(@Param("pathwayId") Long pathwayId);
    
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND peb.badgeClass.id = :badgeClassId")
    Optional<PathwayElementBadge> findByElementIdAndBadgeClassId(@Param("elementId") Long elementId, @Param("badgeClassId") Long badgeClassId);
    
    @Query("SELECT COUNT(peb) > 0 FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND peb.badgeClass.id = :badgeClassId")
    boolean existsByElementIdAndBadgeClassId(@Param("elementId") Long elementId, @Param("badgeClassId") Long badgeClassId);
    
    @Query("DELETE FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND peb.badgeClass.id = :badgeClassId")
    void deleteByElementIdAndBadgeClassId(@Param("elementId") Long elementId, @Param("badgeClassId") Long badgeClassId);
    
    @Query("SELECT COUNT(peb) FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND peb.isRequired = true")
    long countRequiredBadgesByElementId(@Param("elementId") Long elementId);
    
    // Count badges by pathway
    @Query("SELECT COUNT(peb) FROM PathwayElementBadge peb WHERE peb.element.pathway.id = :pathwayId")
    long countByPathwayId(@Param("pathwayId") Long pathwayId);
} 