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
    
    // Find badges by element ID
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.id = :elementId")
    List<PathwayElementBadge> findByElementId(@Param("elementId") Long elementId);
    
    // Find required badges by element ID
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND peb.isRequired = true")
    List<PathwayElementBadge> findByElementIdAndIsRequiredTrue(@Param("elementId") Long elementId);
    
    // Find optional badges by element ID
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND peb.isRequired = false")
    List<PathwayElementBadge> findByElementIdAndIsRequiredFalse(@Param("elementId") Long elementId);
    
    // Find badges by badge class ID
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.badgeClass.id = :badgeClassId")
    List<PathwayElementBadge> findByBadgeClassId(@Param("badgeClassId") Long badgeClassId);
    
    // Find badges by source
    List<PathwayElementBadge> findByBadgeSource(String badgeSource);
    
    // Find verified badges
    List<PathwayElementBadge> findByVerifiedByIsNotNull();
    
    // Find unverified badges
    List<PathwayElementBadge> findByVerifiedByIsNull();
    
    // Find badges by element ID and source
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND peb.badgeSource = :badgeSource")
    List<PathwayElementBadge> findByElementIdAndBadgeSource(@Param("elementId") Long elementId, @Param("badgeSource") String badgeSource);
    
    // Find badges by element ID and required status
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND peb.isRequired = :isRequired")
    List<PathwayElementBadge> findByElementIdAndIsRequired(@Param("elementId") Long elementId, @Param("isRequired") Boolean isRequired);
    

    
    // Find badges by badge class ID and source
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.badgeClass.id = :badgeClassId AND peb.badgeSource = :badgeSource")
    List<PathwayElementBadge> findByBadgeClassIdAndBadgeSource(@Param("badgeClassId") Long badgeClassId, @Param("badgeSource") String badgeSource);
    
    // Find badges by badge class ID and required status
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.badgeClass.id = :badgeClassId AND peb.isRequired = :isRequired")
    List<PathwayElementBadge> findByBadgeClassIdAndIsRequired(@Param("badgeClassId") Long badgeClassId, @Param("isRequired") Boolean isRequired);
    
    // Find badges by badge class ID and verification status
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.badgeClass.id = :badgeClassId AND peb.verifiedBy IS NOT NULL")
    List<PathwayElementBadge> findByBadgeClassIdAndVerifiedByIsNotNull(@Param("badgeClassId") Long badgeClassId);
    
    // Find badges by badge class ID and verification status
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.badgeClass.id = :badgeClassId AND peb.verifiedBy IS NULL")
    List<PathwayElementBadge> findByBadgeClassIdAndVerifiedByIsNull(@Param("badgeClassId") Long badgeClassId);
    
    // Find badges by source and required status
    List<PathwayElementBadge> findByBadgeSourceAndIsRequired(String badgeSource, Boolean isRequired);
    
    // Find badges by source and verification status
    List<PathwayElementBadge> findByBadgeSourceAndVerifiedByIsNotNull(String badgeSource);
    
    // Find badges by source and verification status
    List<PathwayElementBadge> findByBadgeSourceAndVerifiedByIsNull(String badgeSource);
    
    // Find badges by element ID and external badge URL
    List<PathwayElementBadge> findByElementIdAndExternalBadgeUrlIsNotNull(Long elementId);
    
    // Find badges by element ID and external badge URL
    List<PathwayElementBadge> findByElementIdAndExternalBadgeUrlIsNull(Long elementId);
    
    // Find badges by badge class ID and external badge URL
    List<PathwayElementBadge> findByBadgeClassIdAndExternalBadgeUrlIsNotNull(Long badgeClassId);
    
    // Find badges by badge class ID and external badge URL
    List<PathwayElementBadge> findByBadgeClassIdAndExternalBadgeUrlIsNull(Long badgeClassId);
    
    // Find badges by source and external badge URL
    List<PathwayElementBadge> findByBadgeSourceAndExternalBadgeUrlIsNotNull(String badgeSource);
    
    // Find badges by source and external badge URL
    List<PathwayElementBadge> findByBadgeSourceAndExternalBadgeUrlIsNull(String badgeSource);
    
    // Find badges by element ID and verification date range
    List<PathwayElementBadge> findByElementIdAndVerifiedAtBetween(Long elementId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find badges by badge class ID and verification date range
    List<PathwayElementBadge> findByBadgeClassIdAndVerifiedAtBetween(Long badgeClassId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find badges by source and verification date range
    List<PathwayElementBadge> findByBadgeSourceAndVerifiedAtBetween(String badgeSource, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find badges by element ID and creation date range
    List<PathwayElementBadge> findByElementIdAndCreatedAtBetween(Long elementId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find badges by badge class ID and creation date range
    List<PathwayElementBadge> findByBadgeClassIdAndCreatedAtBetween(Long badgeClassId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find badges by source and creation date range
    List<PathwayElementBadge> findByBadgeSourceAndCreatedAtBetween(String badgeSource, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find badges by element ID and verification notes
    List<PathwayElementBadge> findByElementIdAndVerificationNotesContainingIgnoreCase(Long elementId, String notes);
    
    // Find badges by badge class ID and verification notes
    List<PathwayElementBadge> findByBadgeClassIdAndVerificationNotesContainingIgnoreCase(Long badgeClassId, String notes);
    
    // Find badges by source and verification notes
    List<PathwayElementBadge> findByBadgeSourceAndVerificationNotesContainingIgnoreCase(String badgeSource, String notes);
    
    // Find badges by element ID and external badge data
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND " +
           "JSON_SEARCH(peb.externalBadgeData, 'one', :searchTerm) IS NOT NULL")
    List<PathwayElementBadge> findByElementIdAndExternalBadgeDataSearch(@Param("elementId") Long elementId, @Param("searchTerm") String searchTerm);
    
    // Find badges by badge class ID and external badge data
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.badgeClass.id = :badgeClassId AND " +
           "JSON_SEARCH(peb.externalBadgeData, 'one', :searchTerm) IS NOT NULL")
    List<PathwayElementBadge> findByBadgeClassIdAndExternalBadgeDataSearch(@Param("badgeClassId") Long badgeClassId, @Param("searchTerm") String searchTerm);
    
    // Find badges by source and external badge data
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.badgeSource = :badgeSource AND " +
           "JSON_SEARCH(peb.externalBadgeData, 'one', :searchTerm) IS NOT NULL")
    List<PathwayElementBadge> findByBadgeSourceAndExternalBadgeDataSearch(@Param("badgeSource") String badgeSource, @Param("searchTerm") String searchTerm);
    
    // Find badges by element ID and external badge URL pattern
    List<PathwayElementBadge> findByElementIdAndExternalBadgeUrlContainingIgnoreCase(Long elementId, String urlPattern);
    
    // Find badges by badge class ID and external badge URL pattern
    List<PathwayElementBadge> findByBadgeClassIdAndExternalBadgeUrlContainingIgnoreCase(Long badgeClassId, String urlPattern);
    
    // Find badges by source and external badge URL pattern
    List<PathwayElementBadge> findByBadgeSourceAndExternalBadgeUrlContainingIgnoreCase(String badgeSource, String urlPattern);
    

    
    // Find badges by element ID and verification date range
    List<PathwayElementBadge> findByElementIdAndVerifiedAtBetweenOrderByVerifiedAtDesc(Long elementId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find badges by badge class ID and verification date range
    List<PathwayElementBadge> findByBadgeClassIdAndVerifiedAtBetweenOrderByVerifiedAtDesc(Long badgeClassId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find badges by source and verification date range
    List<PathwayElementBadge> findByBadgeSourceAndVerifiedAtBetweenOrderByVerifiedAtDesc(String badgeSource, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find badges by element ID and creation date range
    List<PathwayElementBadge> findByElementIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long elementId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find badges by badge class ID and creation date range
    List<PathwayElementBadge> findByBadgeClassIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long badgeClassId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find badges by source and creation date range
    List<PathwayElementBadge> findByBadgeSourceAndCreatedAtBetweenOrderByCreatedAtDesc(String badgeSource, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find badges by element ID and verification notes pattern
    List<PathwayElementBadge> findByElementIdAndVerificationNotesContainingIgnoreCaseOrderByVerifiedAtDesc(Long elementId, String notesPattern);
    
    // Find badges by badge class ID and verification notes pattern
    List<PathwayElementBadge> findByBadgeClassIdAndVerificationNotesContainingIgnoreCaseOrderByVerifiedAtDesc(Long badgeClassId, String notesPattern);
    
    // Find badges by source and verification notes pattern
    List<PathwayElementBadge> findByBadgeSourceAndVerificationNotesContainingIgnoreCaseOrderByVerifiedAtDesc(String badgeSource, String notesPattern);
    
    // Find badges by element ID and external badge URL pattern
    List<PathwayElementBadge> findByElementIdAndExternalBadgeUrlContainingIgnoreCaseOrderByCreatedAtDesc(Long elementId, String urlPattern);
    
    // Find badges by badge class ID and external badge URL pattern
    List<PathwayElementBadge> findByBadgeClassIdAndExternalBadgeUrlContainingIgnoreCaseOrderByCreatedAtDesc(Long badgeClassId, String urlPattern);
    
    // Find badges by source and external badge URL pattern
    List<PathwayElementBadge> findByBadgeSourceAndExternalBadgeUrlContainingIgnoreCaseOrderByCreatedAtDesc(String badgeSource, String urlPattern);
    
    // Find badges by element ID and external badge data pattern
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND " +
           "JSON_SEARCH(peb.externalBadgeData, 'one', :searchPattern) IS NOT NULL ORDER BY peb.createdAt DESC")
    List<PathwayElementBadge> findByElementIdAndExternalBadgeDataSearchOrderByCreatedAtDesc(@Param("elementId") Long elementId, @Param("searchPattern") String searchPattern);
    
    // Find badges by badge class ID and external badge data pattern
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.badgeClass.id = :badgeClassId AND " +
           "JSON_SEARCH(peb.externalBadgeData, 'one', :searchPattern) IS NOT NULL ORDER BY peb.createdAt DESC")
    List<PathwayElementBadge> findByBadgeClassIdAndExternalBadgeDataSearchOrderByCreatedAtDesc(@Param("badgeClassId") Long badgeClassId, @Param("searchPattern") String searchPattern);
    
    // Find badges by source and external badge data pattern
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.badgeSource = :badgeSource AND " +
           "JSON_SEARCH(peb.externalBadgeData, 'one', :searchPattern) IS NOT NULL ORDER BY peb.createdAt DESC")
    List<PathwayElementBadge> findByBadgeSourceAndExternalBadgeDataSearchOrderByCreatedAtDesc(@Param("badgeSource") String badgeSource, @Param("searchPattern") String searchPattern);
    
    // === ADDITIONAL METHODS FOR SERVICE IMPLEMENTATION ===
    
    // Find badge by element ID and badge class ID
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND peb.badgeClass.id = :badgeClassId")
    Optional<PathwayElementBadge> findByElementIdAndBadgeClassId(@Param("elementId") Long elementId, @Param("badgeClassId") Long badgeClassId);
    
    // Check if badge exists by element ID and badge class ID
    @Query("SELECT COUNT(peb) > 0 FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND peb.badgeClass.id = :badgeClassId")
    boolean existsByElementIdAndBadgeClassId(@Param("elementId") Long elementId, @Param("badgeClassId") Long badgeClassId);
    
    // Delete badge by element ID and badge class ID
    @Query("DELETE FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND peb.badgeClass.id = :badgeClassId")
    void deleteByElementIdAndBadgeClassId(@Param("elementId") Long elementId, @Param("badgeClassId") Long badgeClassId);
    
    // Find badges by pathway ID
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.pathway.id = :pathwayId")
    List<PathwayElementBadge> findByPathwayId(@Param("pathwayId") Long pathwayId);
    
    // Count required badges by element ID
    @Query("SELECT COUNT(peb) FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND peb.isRequired = true")
    long countByElementIdAndIsRequiredTrue(@Param("elementId") Long elementId);
    
} 