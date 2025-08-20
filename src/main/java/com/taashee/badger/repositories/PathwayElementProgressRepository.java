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
    
    // Find progress by pathway progress ID
    List<PathwayElementProgress> findByPathwayProgressId(Long pathwayProgressId);
    
    // Find progress by element ID
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.element.id = :elementId")
    List<PathwayElementProgress> findByElementId(@Param("elementId") Long elementId);
    
    // Find completed progress by pathway progress ID
    List<PathwayElementProgress> findByPathwayProgressIdAndIsCompletedTrue(Long pathwayProgressId);
    
    // Find incomplete progress by pathway progress ID
    List<PathwayElementProgress> findByPathwayProgressIdAndIsCompletedFalse(Long pathwayProgressId);
    
    // Find completed progress by element ID
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.element.id = :elementId AND pep.isCompleted = true")
    List<PathwayElementProgress> findByElementIdAndIsCompletedTrue(@Param("elementId") Long elementId);
    
    // Find incomplete progress by element ID
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.element.id = :elementId AND pep.isCompleted = false")
    List<PathwayElementProgress> findByElementIdAndIsCompletedFalse(@Param("elementId") Long elementId);
    
    // Find progress by pathway progress ID and element ID
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.pathwayProgress.id = :pathwayProgressId AND pep.element.id = :elementId")
    Optional<PathwayElementProgress> findByPathwayProgressIdAndElementId(@Param("pathwayProgressId") Long pathwayProgressId, @Param("elementId") Long elementId);
    
    // Find completed progress by pathway progress ID and completion date range
    List<PathwayElementProgress> findByPathwayProgressIdAndIsCompletedTrueAndCompletedAtBetween(
        Long pathwayProgressId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find completed progress by element ID and completion date range
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.element.id = :elementId AND pep.isCompleted = true AND pep.completedAt BETWEEN :startDate AND :endDate")
    List<PathwayElementProgress> findByElementIdAndIsCompletedTrueAndCompletedAtBetween(
        @Param("elementId") Long elementId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find progress by pathway progress ID and creation date range
    List<PathwayElementProgress> findByPathwayProgressIdAndCreatedAtBetween(
        Long pathwayProgressId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find progress by element ID and creation date range
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.element.id = :elementId AND pep.createdAt BETWEEN :startDate AND :endDate")
    List<PathwayElementProgress> findByElementIdAndCreatedAtBetween(
        @Param("elementId") Long elementId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find progress by pathway progress ID and update date range
    List<PathwayElementProgress> findByPathwayProgressIdAndUpdatedAtBetween(
        Long pathwayProgressId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find progress by element ID and update date range
    List<PathwayElementProgress> findByElementIdAndUpdatedAtBetween(
        Long elementId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find progress by pathway progress ID and completed badges
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.pathwayProgress.id = :pathwayProgressId AND " +
           "JSON_LENGTH(pep.completedBadges) > 0")
    List<PathwayElementProgress> findByPathwayProgressIdAndCompletedBadgesNotEmpty(@Param("pathwayProgressId") Long pathwayProgressId);
    
    // Find progress by element ID and completed badges
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.element.id = :elementId AND " +
           "JSON_LENGTH(pep.completedBadges) > 0")
    List<PathwayElementProgress> findByElementIdAndCompletedBadgesNotEmpty(@Param("elementId") Long elementId);
    
    // Find progress by pathway progress ID and completed badges count
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.pathwayProgress.id = :pathwayProgressId AND " +
           "JSON_LENGTH(pep.completedBadges) = :badgeCount")
    List<PathwayElementProgress> findByPathwayProgressIdAndCompletedBadgesCount(@Param("pathwayProgressId") Long pathwayProgressId, @Param("badgeCount") int badgeCount);
    
    // Find progress by element ID and completed badges count
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.element.id = :elementId AND " +
           "JSON_LENGTH(pep.completedBadges) = :badgeCount")
    List<PathwayElementProgress> findByElementIdAndCompletedBadgesCount(@Param("elementId") Long elementId, @Param("badgeCount") int badgeCount);
    
    // Find progress by pathway progress ID and completed badges search
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.pathwayProgress.id = :pathwayProgressId AND " +
           "JSON_SEARCH(pep.completedBadges, 'one', :searchTerm) IS NOT NULL")
    List<PathwayElementProgress> findByPathwayProgressIdAndCompletedBadgesSearch(@Param("pathwayProgressId") Long pathwayProgressId, @Param("searchTerm") String searchTerm);
    
    // Find progress by element ID and completed badges search
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.element.id = :elementId AND " +
           "JSON_SEARCH(pep.completedBadges, 'one', :searchTerm) IS NOT NULL")
    List<PathwayElementProgress> findByElementIdAndCompletedBadgesSearch(@Param("elementId") Long elementId, @Param("searchTerm") String searchTerm);
    
    // Count completed progress by pathway progress ID
    long countByPathwayProgressIdAndIsCompletedTrue(Long pathwayProgressId);
    
    // Count incomplete progress by pathway progress ID
    long countByPathwayProgressIdAndIsCompletedFalse(Long pathwayProgressId);
    
    // Count completed progress by element ID
    long countByElementIdAndIsCompletedTrue(Long elementId);
    
    // Count incomplete progress by element ID
    long countByElementIdAndIsCompletedFalse(Long elementId);
    
    // Count progress by pathway progress ID
    long countByPathwayProgressId(Long pathwayProgressId);
    
    // Count progress by element ID
    long countByElementId(Long elementId);
    
    // Find progress by pathway progress ID and completion date range
    List<PathwayElementProgress> findByPathwayProgressIdAndIsCompletedTrueAndCompletedAtBetweenOrderByCompletedAtDesc(
        Long pathwayProgressId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find progress by element ID and completion date range
    List<PathwayElementProgress> findByElementIdAndIsCompletedTrueAndCompletedAtBetweenOrderByCompletedAtDesc(
        Long elementId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find progress by pathway progress ID and creation date range
    List<PathwayElementProgress> findByPathwayProgressIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        Long pathwayProgressId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find progress by element ID and creation date range
    List<PathwayElementProgress> findByElementIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        Long elementId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find progress by pathway progress ID and update date range
    List<PathwayElementProgress> findByPathwayProgressIdAndUpdatedAtBetweenOrderByUpdatedAtDesc(
        Long pathwayProgressId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find progress by element ID and update date range
    List<PathwayElementProgress> findByElementIdAndUpdatedAtBetweenOrderByUpdatedAtDesc(
        Long elementId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find progress by pathway progress ID and completed badges
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.pathwayProgress.id = :pathwayProgressId AND " +
           "JSON_LENGTH(pep.completedBadges) > 0 ORDER BY pep.completedAt DESC")
    List<PathwayElementProgress> findByPathwayProgressIdAndCompletedBadgesNotEmptyOrderByCompletedAtDesc(@Param("pathwayProgressId") Long pathwayProgressId);
    
    // Find progress by element ID and completed badges
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.element.id = :elementId AND " +
           "JSON_LENGTH(pep.completedBadges) > 0 ORDER BY pep.completedAt DESC")
    List<PathwayElementProgress> findByElementIdAndCompletedBadgesNotEmptyOrderByCompletedAtDesc(@Param("elementId") Long elementId);
    
    // Find progress by pathway progress ID and completed badges count
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.pathwayProgress.id = :pathwayProgressId AND " +
           "JSON_LENGTH(pep.completedBadges) = :badgeCount ORDER BY pep.completedAt DESC")
    List<PathwayElementProgress> findByPathwayProgressIdAndCompletedBadgesCountOrderByCompletedAtDesc(@Param("pathwayProgressId") Long pathwayProgressId, @Param("badgeCount") int badgeCount);
    
    // Find progress by element ID and completed badges count
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.element.id = :elementId AND " +
           "JSON_LENGTH(pep.completedBadges) = :badgeCount ORDER BY pep.completedAt DESC")
    List<PathwayElementProgress> findByElementIdAndCompletedBadgesCountOrderByCompletedAtDesc(@Param("elementId") Long elementId, @Param("badgeCount") int badgeCount);
    
    // Find progress by pathway progress ID and completed badges search
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.pathwayProgress.id = :pathwayProgressId AND " +
           "JSON_SEARCH(pep.completedBadges, 'one', :searchTerm) IS NOT NULL ORDER BY pep.completedAt DESC")
    List<PathwayElementProgress> findByPathwayProgressIdAndCompletedBadgesSearchOrderByCompletedAtDesc(@Param("pathwayProgressId") Long pathwayProgressId, @Param("searchTerm") String searchTerm);
    
    // Find progress by element ID and completed badges search
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.element.id = :elementId AND " +
           "JSON_SEARCH(pep.completedBadges, 'one', :searchTerm) IS NOT NULL ORDER BY pep.completedAt DESC")
    List<PathwayElementProgress> findByElementIdAndCompletedBadgesSearchOrderByCompletedAtDesc(@Param("elementId") Long elementId, @Param("searchTerm") String searchTerm);
} 