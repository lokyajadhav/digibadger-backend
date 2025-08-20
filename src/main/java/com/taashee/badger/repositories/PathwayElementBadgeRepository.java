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
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND peb.externalBadgeUrl IS NOT NULL")
    List<PathwayElementBadge> findByElementIdAndExternalBadgeUrlIsNotNull(@Param("elementId") Long elementId);
    
    // Find badges by element ID and external badge URL
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND peb.externalBadgeUrl IS NULL")
    List<PathwayElementBadge> findByElementIdAndExternalBadgeUrlIsNull(@Param("elementId") Long elementId);
    
    // Find badges by badge class ID and external badge URL
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.badgeClass.id = :badgeClassId AND peb.externalBadgeUrl IS NOT NULL")
    List<PathwayElementBadge> findByBadgeClassIdAndExternalBadgeUrlIsNotNull(@Param("badgeClassId") Long badgeClassId);
    
    // Find badges by badge class ID and external badge URL
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.badgeClass.id = :badgeClassId AND peb.externalBadgeUrl IS NULL")
    List<PathwayElementBadge> findByBadgeClassIdAndExternalBadgeUrlIsNull(@Param("badgeClassId") Long badgeClassId);
    
    // Find badges by source and external badge URL
    List<PathwayElementBadge> findByBadgeSourceAndExternalBadgeUrlIsNotNull(String badgeSource);
    
    // Find badges by source and external badge URL
    List<PathwayElementBadge> findByBadgeSourceAndExternalBadgeUrlIsNull(String badgeSource);
    
    // Find badges by element ID and verification date range
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND peb.verifiedAt BETWEEN :startDate AND :endDate")
    List<PathwayElementBadge> findByElementIdAndVerifiedAtBetween(@Param("elementId") Long elementId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find badges by badge class ID and verification date range
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.badgeClass.id = :badgeClassId AND peb.verifiedAt BETWEEN :startDate AND :endDate")
    List<PathwayElementBadge> findByBadgeClassIdAndVerifiedAtBetween(@Param("badgeClassId") Long badgeClassId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find badges by source and verification date range
    List<PathwayElementBadge> findByBadgeSourceAndVerifiedAtBetween(String badgeSource, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find badges by element ID and creation date range
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND peb.createdAt BETWEEN :startDate AND :endDate")
    List<PathwayElementBadge> findByElementIdAndCreatedAtBetween(@Param("elementId") Long elementId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find badges by badge class ID and creation date range
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.badgeClass.id = :badgeClassId AND peb.createdAt BETWEEN :startDate AND :endDate")
    List<PathwayElementBadge> findByBadgeClassIdAndCreatedAtBetween(@Param("badgeClassId") Long badgeClassId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find badges by source and creation date range
    List<PathwayElementBadge> findByBadgeSourceAndCreatedAtBetween(String badgeSource, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find badges by element ID and verification notes
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND LOWER(peb.verificationNotes) LIKE LOWER(CONCAT('%', :notes, '%'))")
    List<PathwayElementBadge> findByElementIdAndVerificationNotesContainingIgnoreCase(@Param("elementId") Long elementId, @Param("notes") String notes);
    
    // Find badges by badge class ID and verification notes
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.badgeClass.id = :badgeClassId AND LOWER(peb.verificationNotes) LIKE LOWER(CONCAT('%', :notes, '%'))")
    List<PathwayElementBadge> findByBadgeClassIdAndVerificationNotesContainingIgnoreCase(@Param("badgeClassId") Long badgeClassId, @Param("notes") String notes);
    
    // Find badges by source and verification notes
    List<PathwayElementBadge> findByBadgeSourceAndVerificationNotesContainingIgnoreCase(String badgeSource, String notes);
    
    // Find badges by element ID and external badge data (PostgreSQL compatible)
    @Query(value = "SELECT peb.* FROM pathway_element_badges peb WHERE peb.element_id = :elementId AND peb.external_badge_data::text LIKE CONCAT('%', :searchTerm, '%')", nativeQuery = true)
    List<PathwayElementBadge> findByElementIdAndExternalBadgeDataSearch(@Param("elementId") Long elementId, @Param("searchTerm") String searchTerm);
    
    // Find badges by badge class ID and external badge data (PostgreSQL compatible)
    @Query(value = "SELECT peb.* FROM pathway_element_badges peb WHERE peb.badge_class_id = :badgeClassId AND peb.external_badge_data::text LIKE CONCAT('%', :searchTerm, '%')", nativeQuery = true)
    List<PathwayElementBadge> findByBadgeClassIdAndExternalBadgeDataSearch(@Param("badgeClassId") Long badgeClassId, @Param("searchTerm") String searchTerm);
    
    // Find badges by source and external badge data
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.badgeSource = :badgeSource AND " +
           "JSON_SEARCH(peb.externalBadgeData, 'one', :searchTerm) IS NOT NULL")
    List<PathwayElementBadge> findByBadgeSourceAndExternalBadgeDataSearch(@Param("badgeSource") String badgeSource, @Param("searchTerm") String searchTerm);
    
    // Find badges by element ID and external badge URL pattern
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND LOWER(peb.externalBadgeUrl) LIKE LOWER(CONCAT('%', :urlPattern, '%'))")
    List<PathwayElementBadge> findByElementIdAndExternalBadgeUrlContainingIgnoreCase(@Param("elementId") Long elementId, @Param("urlPattern") String urlPattern);
    
    // Find badges by badge class ID and external badge URL pattern
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.badgeClass.id = :badgeClassId AND LOWER(peb.externalBadgeUrl) LIKE LOWER(CONCAT('%', :urlPattern, '%'))")
    List<PathwayElementBadge> findByBadgeClassIdAndExternalBadgeUrlContainingIgnoreCase(@Param("badgeClassId") Long badgeClassId, @Param("urlPattern") String urlPattern);
    
    // Find badges by source and external badge URL pattern
    List<PathwayElementBadge> findByBadgeSourceAndExternalBadgeUrlContainingIgnoreCase(String badgeSource, String urlPattern);
    

    
    // Find badges by element ID and verification date range
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND peb.verifiedAt BETWEEN :startDate AND :endDate ORDER BY peb.verifiedAt DESC")
    List<PathwayElementBadge> findByElementIdAndVerifiedAtBetweenOrderByVerifiedAtDesc(@Param("elementId") Long elementId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find badges by badge class ID and verification date range
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.badgeClass.id = :badgeClassId AND peb.verifiedAt BETWEEN :startDate AND :endDate ORDER BY peb.verifiedAt DESC")
    List<PathwayElementBadge> findByBadgeClassIdAndVerifiedAtBetweenOrderByVerifiedAtDesc(@Param("badgeClassId") Long badgeClassId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find badges by source and verification date range
    List<PathwayElementBadge> findByBadgeSourceAndVerifiedAtBetweenOrderByVerifiedAtDesc(String badgeSource, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find badges by element ID and creation date range
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND peb.createdAt BETWEEN :startDate AND :endDate ORDER BY peb.createdAt DESC")
    List<PathwayElementBadge> findByElementIdAndCreatedAtBetweenOrderByCreatedAtDesc(@Param("elementId") Long elementId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find badges by badge class ID and creation date range
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.badgeClass.id = :badgeClassId AND peb.createdAt BETWEEN :startDate AND :endDate ORDER BY peb.createdAt DESC")
    List<PathwayElementBadge> findByBadgeClassIdAndCreatedAtBetweenOrderByCreatedAtDesc(@Param("badgeClassId") Long badgeClassId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find badges by source and creation date range
    List<PathwayElementBadge> findByBadgeSourceAndCreatedAtBetweenOrderByCreatedAtDesc(String badgeSource, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find badges by element ID and verification notes pattern
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND LOWER(peb.verificationNotes) LIKE LOWER(CONCAT('%', :notesPattern, '%')) ORDER BY peb.verifiedAt DESC")
    List<PathwayElementBadge> findByElementIdAndVerificationNotesContainingIgnoreCaseOrderByVerifiedAtDesc(@Param("elementId") Long elementId, @Param("notesPattern") String notesPattern);
    
    // Find badges by badge class ID and verification notes pattern
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.badgeClass.id = :badgeClassId AND LOWER(peb.verificationNotes) LIKE LOWER(CONCAT('%', :notesPattern, '%')) ORDER BY peb.verifiedAt DESC")
    List<PathwayElementBadge> findByBadgeClassIdAndVerificationNotesContainingIgnoreCaseOrderByVerifiedAtDesc(@Param("badgeClassId") Long badgeClassId, @Param("notesPattern") String notesPattern);
    
    // Find badges by source and verification notes pattern
    List<PathwayElementBadge> findByBadgeSourceAndVerificationNotesContainingIgnoreCaseOrderByVerifiedAtDesc(String badgeSource, String notesPattern);
    
    // Find badges by element ID and external badge URL pattern
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.element.id = :elementId AND LOWER(peb.externalBadgeUrl) LIKE LOWER(CONCAT('%', :urlPattern, '%')) ORDER BY peb.createdAt DESC")
    List<PathwayElementBadge> findByElementIdAndExternalBadgeUrlContainingIgnoreCaseOrderByCreatedAtDesc(@Param("elementId") Long elementId, @Param("urlPattern") String urlPattern);
    
    // Find badges by badge class ID and external badge URL pattern
    @Query("SELECT peb FROM PathwayElementBadge peb WHERE peb.badgeClass.id = :badgeClassId AND LOWER(peb.externalBadgeUrl) LIKE LOWER(CONCAT('%', :urlPattern, '%')) ORDER BY peb.createdAt DESC")
    List<PathwayElementBadge> findByBadgeClassIdAndExternalBadgeUrlContainingIgnoreCaseOrderByCreatedAtDesc(@Param("badgeClassId") Long badgeClassId, @Param("urlPattern") String urlPattern);
    
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