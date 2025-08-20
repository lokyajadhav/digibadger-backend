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
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.pathwayProgress.id = :pathwayProgressId")
    List<PathwayElementProgress> findByPathwayProgressId(@Param("pathwayProgressId") Long pathwayProgressId);
    
    // Find progress by element ID
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.element.id = :elementId")
    List<PathwayElementProgress> findByElementId(@Param("elementId") Long elementId);
    
    // Find completed progress by pathway progress ID
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.pathwayProgress.id = :pathwayProgressId AND pep.isCompleted = true")
    List<PathwayElementProgress> findByPathwayProgressIdAndIsCompletedTrue(@Param("pathwayProgressId") Long pathwayProgressId);
    
    // Find incomplete progress by pathway progress ID
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.pathwayProgress.id = :pathwayProgressId AND pep.isCompleted = false")
    List<PathwayElementProgress> findByPathwayProgressIdAndIsCompletedFalse(@Param("pathwayProgressId") Long pathwayProgressId);
    
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
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.pathwayProgress.id = :pathwayProgressId AND pep.isCompleted = true AND pep.completedAt BETWEEN :startDate AND :endDate")
    List<PathwayElementProgress> findByPathwayProgressIdAndIsCompletedTrueAndCompletedAtBetween(
        @Param("pathwayProgressId") Long pathwayProgressId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find completed progress by element ID and completion date range
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.element.id = :elementId AND pep.isCompleted = true AND pep.completedAt BETWEEN :startDate AND :endDate")
    List<PathwayElementProgress> findByElementIdAndIsCompletedTrueAndCompletedAtBetween(
        @Param("elementId") Long elementId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find progress by pathway progress ID and creation date range
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.pathwayProgress.id = :pathwayProgressId AND pep.createdAt BETWEEN :startDate AND :endDate")
    List<PathwayElementProgress> findByPathwayProgressIdAndCreatedAtBetween(
        @Param("pathwayProgressId") Long pathwayProgressId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find progress by element ID and creation date range
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.element.id = :elementId AND pep.createdAt BETWEEN :startDate AND :endDate")
    List<PathwayElementProgress> findByElementIdAndCreatedAtBetween(
        @Param("elementId") Long elementId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find progress by pathway progress ID and update date range
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.pathwayProgress.id = :pathwayProgressId AND pep.updatedAt BETWEEN :startDate AND :endDate")
    List<PathwayElementProgress> findByPathwayProgressIdAndUpdatedAtBetween(
        @Param("pathwayProgressId") Long pathwayProgressId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find progress by element ID and update date range
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.element.id = :elementId AND pep.updatedAt BETWEEN :startDate AND :endDate")
    List<PathwayElementProgress> findByElementIdAndUpdatedAtBetween(
        @Param("elementId") Long elementId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find progress by pathway progress ID and completed badges (PostgreSQL compatible)
    @Query(value = "SELECT pep.* FROM pathway_element_progress pep WHERE pep.pathway_progress_id = :pathwayProgressId AND jsonb_array_length(pep.completed_badges) > 0", nativeQuery = true)
    List<PathwayElementProgress> findByPathwayProgressIdAndCompletedBadgesNotEmpty(@Param("pathwayProgressId") Long pathwayProgressId);
    
    // Find progress by element ID and completed badges (PostgreSQL compatible)
    @Query(value = "SELECT pep.* FROM pathway_element_progress pep WHERE pep.element_id = :elementId AND jsonb_array_length(pep.completed_badges) > 0", nativeQuery = true)
    List<PathwayElementProgress> findByElementIdAndCompletedBadgesNotEmpty(@Param("elementId") Long elementId);
    
    // Find progress by pathway progress ID and completed badges count (PostgreSQL compatible)
    @Query(value = "SELECT pep.* FROM pathway_element_progress pep WHERE pep.pathway_progress_id = :pathwayProgressId AND jsonb_array_length(pep.completed_badges) = :badgeCount", nativeQuery = true)
    List<PathwayElementProgress> findByPathwayProgressIdAndCompletedBadgesCount(@Param("pathwayProgressId") Long pathwayProgressId, @Param("badgeCount") int badgeCount);
    
    // Find progress by element ID and completed badges count (PostgreSQL compatible)
    @Query(value = "SELECT pep.* FROM pathway_element_progress pep WHERE pep.element_id = :elementId AND jsonb_array_length(pep.completed_badges) = :badgeCount", nativeQuery = true)
    List<PathwayElementProgress> findByElementIdAndCompletedBadgesCount(@Param("elementId") Long elementId, @Param("badgeCount") int badgeCount);
    
    // Find progress by pathway progress ID and completed badges search (PostgreSQL compatible)
    @Query(value = "SELECT pep.* FROM pathway_element_progress pep WHERE pep.pathway_progress_id = :pathwayProgressId AND pep.completed_badges::text LIKE CONCAT('%', :searchTerm, '%')", nativeQuery = true)
    List<PathwayElementProgress> findByPathwayProgressIdAndCompletedBadgesSearch(@Param("pathwayProgressId") Long pathwayProgressId, @Param("searchTerm") String searchTerm);
    
    // Find progress by element ID and completed badges search (PostgreSQL compatible)
    @Query(value = "SELECT pep.* FROM pathway_element_progress pep WHERE pep.element_id = :elementId AND pep.completed_badges::text LIKE CONCAT('%', :searchTerm, '%')", nativeQuery = true)
    List<PathwayElementProgress> findByElementIdAndCompletedBadgesSearch(@Param("elementId") Long elementId, @Param("searchTerm") String searchTerm);
    
    // Count completed progress by pathway progress ID
    @Query("SELECT COUNT(pep) FROM PathwayElementProgress pep WHERE pep.pathwayProgress.id = :pathwayProgressId AND pep.isCompleted = true")
    long countByPathwayProgressIdAndIsCompletedTrue(@Param("pathwayProgressId") Long pathwayProgressId);
    
    // Count incomplete progress by pathway progress ID
    @Query("SELECT COUNT(pep) FROM PathwayElementProgress pep WHERE pep.pathwayProgress.id = :pathwayProgressId AND pep.isCompleted = false")
    long countByPathwayProgressIdAndIsCompletedFalse(@Param("pathwayProgressId") Long pathwayProgressId);
    
    // Count completed progress by element ID
    @Query("SELECT COUNT(pep) FROM PathwayElementProgress pep WHERE pep.element.id = :elementId AND pep.isCompleted = true")
    long countByElementIdAndIsCompletedTrue(@Param("elementId") Long elementId);
    
    // Count incomplete progress by element ID
    @Query("SELECT COUNT(pep) FROM PathwayElementProgress pep WHERE pep.element.id = :elementId AND pep.isCompleted = false")
    long countByElementIdAndIsCompletedFalse(@Param("elementId") Long elementId);
    
    // Count progress by pathway progress ID
    @Query("SELECT COUNT(pep) FROM PathwayElementProgress pep WHERE pep.pathwayProgress.id = :pathwayProgressId")
    long countByPathwayProgressId(@Param("pathwayProgressId") Long pathwayProgressId);
    
    // Count progress by element ID
    @Query("SELECT COUNT(pep) FROM PathwayElementProgress pep WHERE pep.element.id = :elementId")
    long countByElementId(@Param("elementId") Long elementId);
    
    // Find progress by pathway progress ID and completion date range
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.pathwayProgress.id = :pathwayProgressId AND pep.isCompleted = true AND pep.completedAt BETWEEN :startDate AND :endDate ORDER BY pep.completedAt DESC")
    List<PathwayElementProgress> findByPathwayProgressIdAndIsCompletedTrueAndCompletedAtBetweenOrderByCompletedAtDesc(
        @Param("pathwayProgressId") Long pathwayProgressId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find progress by element ID and completion date range
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.element.id = :elementId AND pep.isCompleted = true AND pep.completedAt BETWEEN :startDate AND :endDate ORDER BY pep.completedAt DESC")
    List<PathwayElementProgress> findByElementIdAndIsCompletedTrueAndCompletedAtBetweenOrderByCompletedAtDesc(
        @Param("elementId") Long elementId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find progress by pathway progress ID and creation date range
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.pathwayProgress.id = :pathwayProgressId AND pep.createdAt BETWEEN :startDate AND :endDate ORDER BY pep.createdAt DESC")
    List<PathwayElementProgress> findByPathwayProgressIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        @Param("pathwayProgressId") Long pathwayProgressId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find progress by element ID and creation date range
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.element.id = :elementId AND pep.createdAt BETWEEN :startDate AND :endDate ORDER BY pep.createdAt DESC")
    List<PathwayElementProgress> findByElementIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        @Param("elementId") Long elementId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find progress by pathway progress ID and update date range
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.pathwayProgress.id = :pathwayProgressId AND pep.updatedAt BETWEEN :startDate AND :endDate ORDER BY pep.updatedAt DESC")
    List<PathwayElementProgress> findByPathwayProgressIdAndUpdatedAtBetweenOrderByUpdatedAtDesc(
        @Param("pathwayProgressId") Long pathwayProgressId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find progress by element ID and update date range
    @Query("SELECT pep FROM PathwayElementProgress pep WHERE pep.element.id = :elementId AND pep.updatedAt BETWEEN :startDate AND :endDate ORDER BY pep.updatedAt DESC")
    List<PathwayElementProgress> findByElementIdAndUpdatedAtBetweenOrderByUpdatedAtDesc(
        @Param("elementId") Long elementId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find progress by pathway progress ID and completed badges (PostgreSQL compatible)
    @Query(value = "SELECT pep.* FROM pathway_element_progress pep WHERE pep.pathway_progress_id = :pathwayProgressId AND jsonb_array_length(pep.completed_badges) > 0 ORDER BY pep.completed_at DESC", nativeQuery = true)
    List<PathwayElementProgress> findByPathwayProgressIdAndCompletedBadgesNotEmptyOrderByCompletedAtDesc(@Param("pathwayProgressId") Long pathwayProgressId);
    
    // Find progress by element ID and completed badges (PostgreSQL compatible)
    @Query(value = "SELECT pep.* FROM pathway_element_progress pep WHERE pep.element_id = :elementId AND jsonb_array_length(pep.completed_badges) > 0 ORDER BY pep.completed_at DESC", nativeQuery = true)
    List<PathwayElementProgress> findByElementIdAndCompletedBadgesNotEmptyOrderByCompletedAtDesc(@Param("elementId") Long elementId);
    
    // Find progress by pathway progress ID and completed badges count (PostgreSQL compatible)
    @Query(value = "SELECT pep.* FROM pathway_element_progress pep WHERE pep.pathway_progress_id = :pathwayProgressId AND jsonb_array_length(pep.completed_badges) = :badgeCount ORDER BY pep.completed_at DESC", nativeQuery = true)
    List<PathwayElementProgress> findByPathwayProgressIdAndCompletedBadgesCountOrderByCompletedAtDesc(@Param("pathwayProgressId") Long pathwayProgressId, @Param("badgeCount") int badgeCount);
    
    // Find progress by element ID and completed badges count (PostgreSQL compatible)
    @Query(value = "SELECT pep.* FROM pathway_element_progress pep WHERE pep.element_id = :elementId AND jsonb_array_length(pep.completed_badges) = :badgeCount ORDER BY pep.completed_at DESC", nativeQuery = true)
    List<PathwayElementProgress> findByElementIdAndCompletedBadgesCountOrderByCompletedAtDesc(@Param("elementId") Long elementId, @Param("badgeCount") int badgeCount);
    
    // Find progress by pathway progress ID and completed badges search (PostgreSQL compatible)
    @Query(value = "SELECT pep.* FROM pathway_element_progress pep WHERE pep.pathway_progress_id = :pathwayProgressId AND pep.completed_badges::text LIKE CONCAT('%', :searchTerm, '%') ORDER BY pep.completed_at DESC", nativeQuery = true)
    List<PathwayElementProgress> findByPathwayProgressIdAndCompletedBadgesSearchOrderByCompletedAtDesc(@Param("pathwayProgressId") Long pathwayProgressId, @Param("searchTerm") String searchTerm);
    
    // Find progress by element ID and completed badges search (PostgreSQL compatible)
    @Query(value = "SELECT pep.* FROM pathway_element_progress pep WHERE pep.element_id = :elementId AND pep.completed_badges::text LIKE CONCAT('%', :searchTerm, '%') ORDER BY pep.completed_at DESC", nativeQuery = true)
    List<PathwayElementProgress> findByElementIdAndCompletedBadgesSearchOrderByCompletedAtDesc(@Param("elementId") Long elementId, @Param("searchTerm") String searchTerm);
} 