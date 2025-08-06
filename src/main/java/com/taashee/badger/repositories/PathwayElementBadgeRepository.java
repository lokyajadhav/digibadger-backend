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
    List<PathwayElementBadge> findByElementId(Long elementId);
    
    // Find required badges by element ID
    List<PathwayElementBadge> findByElementIdAndIsRequiredTrue(Long elementId);
    
    // Find optional badges by element ID
    List<PathwayElementBadge> findByElementIdAndIsRequiredFalse(Long elementId);
    
    // Find badges by badge class ID
    List<PathwayElementBadge> findByBadgeClassId(Long badgeClassId);
    
    // Find badges by source
    List<PathwayElementBadge> findByBadgeSource(String badgeSource);
    
    // Find verified badges
    List<PathwayElementBadge> findByVerifiedByIsNotNull();
    
    // Find unverified badges
    List<PathwayElementBadge> findByVerifiedByIsNull();
    
    // Find badges by element ID and source
    List<PathwayElementBadge> findByElementIdAndBadgeSource(Long elementId, String badgeSource);
    
    // Find badges by element ID and required status
    List<PathwayElementBadge> findByElementIdAndIsRequired(Long elementId, Boolean isRequired);
    

    
    // Find badges by badge class ID and source
    List<PathwayElementBadge> findByBadgeClassIdAndBadgeSource(Long badgeClassId, String badgeSource);
    
    // Find badges by badge class ID and required status
    List<PathwayElementBadge> findByBadgeClassIdAndIsRequired(Long badgeClassId, Boolean isRequired);
    
    // Find badges by badge class ID and verification status
    List<PathwayElementBadge> findByBadgeClassIdAndVerifiedByIsNotNull(Long badgeClassId);
    
    // Find badges by badge class ID and verification status
    List<PathwayElementBadge> findByBadgeClassIdAndVerifiedByIsNull(Long badgeClassId);
    
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
    Optional<PathwayElementBadge> findByElementIdAndBadgeClassId(Long elementId, Long badgeClassId);
    
    // Check if badge exists by element ID and badge class ID
    boolean existsByElementIdAndBadgeClassId(Long elementId, Long badgeClassId);
    
    // Delete badge by element ID and badge class ID
    void deleteByElementIdAndBadgeClassId(Long elementId, Long badgeClassId);
    
    // Find badges by pathway ID
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.pathway.id = :pathwayId")
    List<PathwayElementBadge> findByPathwayId(@Param("pathwayId") Long pathwayId);
    
    // Count required badges by element ID
    long countByElementIdAndIsRequiredTrue(Long elementId);
    
} 