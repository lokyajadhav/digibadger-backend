package com.taashee.badger.repositories;

import com.taashee.badger.models.PathwayElement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PathwayElementRepository extends JpaRepository<PathwayElement, Long> {
    
    // Find elements by pathway ID
    List<PathwayElement> findByPathwayId(Long pathwayId);
    
    // Find root elements (no parent) by pathway ID
    List<PathwayElement> findByPathwayIdAndParentElementIsNull(Long pathwayId);
    
    // Find children of a specific element
    List<PathwayElement> findByParentElementId(Long parentElementId);
    
    // Find elements by pathway ID and element type
    List<PathwayElement> findByPathwayIdAndElementType(Long pathwayId, PathwayElement.ElementType elementType);
    
    // Count elements by pathway ID
    long countByPathwayId(Long pathwayId);
    
    // Find elements by pathway ID and short code
    Optional<PathwayElement> findByPathwayIdAndShortCode(Long pathwayId, String shortCode);
    
    // Find elements by pathway ID and counts towards parent
    List<PathwayElement> findByPathwayIdAndCountsTowardsParent(Long pathwayId, Boolean countsTowardsParent);
    
    // Find elements by pathway ID and required count
    List<PathwayElement> findByPathwayIdAndRequiredCount(Long pathwayId, Integer requiredCount);
    
    // Find elements by pathway ID and difficulty level
    List<PathwayElement> findByPathwayIdAndDifficultyLevel(Long pathwayId, PathwayElement.DifficultyLevel difficultyLevel);
    
    // Find elements by pathway ID and completion rule
    List<PathwayElement> findByPathwayIdAndCompletionRule(Long pathwayId, PathwayElement.CompletionRule completionRule);
    
    // Find optional elements by pathway ID
    List<PathwayElement> findByPathwayIdAndIsOptionalTrue(Long pathwayId);
    
    // Find required elements by pathway ID
    List<PathwayElement> findByPathwayIdAndIsOptionalFalse(Long pathwayId);
    
    // Find elements by pathway ID and estimated duration range
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "pe.estimatedDurationHours BETWEEN :minHours AND :maxHours")
    List<PathwayElement> findByPathwayIdAndEstimatedDurationRange(@Param("pathwayId") Long pathwayId, 
                                                                 @Param("minHours") Double minHours, 
                                                                 @Param("maxHours") Double maxHours);
    
    // Find elements by pathway ID and badge count
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "(SELECT COUNT(pb) FROM PathwayElementBadge pb WHERE pb.element.id = pe.id) = :badgeCount")
    List<PathwayElement> findByPathwayIdAndBadgeCount(@Param("pathwayId") Long pathwayId, @Param("badgeCount") int badgeCount);
    
    // Find elements that can be completed (no prerequisites or prerequisites met)
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "(pe.prerequisites IS NULL OR JSON_LENGTH(pe.prerequisites) = 0 OR " +
           "pe.id IN (SELECT pep.element.id FROM PathwayElementProgress pep WHERE pep.isCompleted = true))")
    List<PathwayElement> findCompletableElementsByPathwayId(@Param("pathwayId") Long pathwayId);
    
    // Find elements by pathway ID and competency framework
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "JSON_EXTRACT(pe.competencies, '$[*].framework') LIKE %:framework%")
    List<PathwayElement> findByPathwayIdAndCompetencyFramework(@Param("pathwayId") Long pathwayId, @Param("framework") String framework);
    
    // Find elements by pathway ID and metadata search
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "JSON_SEARCH(pe.metadata, 'one', :searchTerm) IS NOT NULL")
    List<PathwayElement> findByPathwayIdAndMetadataSearch(@Param("pathwayId") Long pathwayId, @Param("searchTerm") String searchTerm);
    
    // Find elements by pathway ID and completion status
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "pe.id IN (SELECT pep.element.id FROM PathwayElementProgress pep WHERE pep.isCompleted = :isCompleted)")
    List<PathwayElement> findByPathwayIdAndCompletionStatus(@Param("pathwayId") Long pathwayId, @Param("isCompleted") Boolean isCompleted);
    
    // Find elements by pathway ID and progress percentage
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "pe.id IN (SELECT pep.element.id FROM PathwayElementProgress pep WHERE " +
           "pep.pathwayProgress.progressPercentage >= :minPercentage)")
    List<PathwayElement> findByPathwayIdAndMinProgressPercentage(@Param("pathwayId") Long pathwayId, @Param("minPercentage") Double minPercentage);
    
    // Find elements by pathway ID and badge verification status
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "pe.id IN (SELECT pb.element.id FROM PathwayElementBadge pb WHERE pb.verifiedBy IS NOT NULL)")
    List<PathwayElement> findByPathwayIdAndBadgeVerified(@Param("pathwayId") Long pathwayId);
    
    // Find elements by pathway ID and badge source
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "pe.id IN (SELECT pb.element.id FROM PathwayElementBadge pb WHERE pb.badgeSource = :badgeSource)")
    List<PathwayElement> findByPathwayIdAndBadgeSource(@Param("pathwayId") Long pathwayId, @Param("badgeSource") String badgeSource);
    
    // Find elements by pathway ID and external badge
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "pe.id IN (SELECT pb.element.id FROM PathwayElementBadge pb WHERE pb.externalBadgeUrl IS NOT NULL)")
    List<PathwayElement> findByPathwayIdAndExternalBadge(@Param("pathwayId") Long pathwayId);
    
    // Find elements by pathway ID and required badge count
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "(SELECT COUNT(pb) FROM PathwayElementBadge pb WHERE pb.element.id = pe.id AND pb.isRequired = true) = :requiredCount")
    List<PathwayElement> findByPathwayIdAndRequiredBadgeCount(@Param("pathwayId") Long pathwayId, @Param("requiredCount") int requiredCount);
    
    // Find elements by pathway ID and optional badge count
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "(SELECT COUNT(pb) FROM PathwayElementBadge pb WHERE pb.element.id = pe.id AND pb.isRequired = false) = :optionalCount")
    List<PathwayElement> findByPathwayIdAndOptionalBadgeCount(@Param("pathwayId") Long pathwayId, @Param("optionalCount") int optionalCount);
    
    // Find elements by pathway ID and total badge count
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "(SELECT COUNT(pb) FROM PathwayElementBadge pb WHERE pb.element.id = pe.id) = :totalCount")
    List<PathwayElement> findByPathwayIdAndTotalBadgeCount(@Param("pathwayId") Long pathwayId, @Param("totalCount") int totalCount);
    
    // Find elements by pathway ID and competency alignment
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "JSON_EXTRACT(pe.competencies, '$[*].competencyId') LIKE %:competencyId%")
    List<PathwayElement> findByPathwayIdAndCompetencyId(@Param("pathwayId") Long pathwayId, @Param("competencyId") String competencyId);
    
    // Find elements by pathway ID and alignment strength
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "JSON_EXTRACT(pe.competencies, '$[*].alignmentStrength') >= :minStrength")
    List<PathwayElement> findByPathwayIdAndMinAlignmentStrength(@Param("pathwayId") Long pathwayId, @Param("minStrength") Double minStrength);
    
    // Find elements by pathway ID and alignment type
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "JSON_EXTRACT(pe.competencies, '$[*].alignmentType') = :alignmentType")
    List<PathwayElement> findByPathwayIdAndAlignmentType(@Param("pathwayId") Long pathwayId, @Param("alignmentType") String alignmentType);
    
    // Find elements by pathway ID and creation date range
    List<PathwayElement> findByPathwayIdAndCreatedAtBetween(Long pathwayId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find elements by pathway ID and update date range
    List<PathwayElement> findByPathwayIdAndUpdatedAtBetween(Long pathwayId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Find elements by pathway ID and order index range
    List<PathwayElement> findByPathwayIdAndOrderIndexBetween(Long pathwayId, Integer startIndex, Integer endIndex);
    
    // Find elements by pathway ID and estimated duration
    List<PathwayElement> findByPathwayIdAndEstimatedDurationHoursBetween(Long pathwayId, Double minHours, Double maxHours);
    
    // Find elements by pathway ID and difficulty level range
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "pe.difficultyLevel IN :difficultyLevels")
    List<PathwayElement> findByPathwayIdAndDifficultyLevelIn(@Param("pathwayId") Long pathwayId, @Param("difficultyLevels") List<PathwayElement.DifficultyLevel> difficultyLevels);
    
    // Find elements by pathway ID and element type in
    List<PathwayElement> findByPathwayIdAndElementTypeIn(Long pathwayId, List<PathwayElement.ElementType> elementTypes);
    
    // Find elements by pathway ID and completion rule in
    List<PathwayElement> findByPathwayIdAndCompletionRuleIn(Long pathwayId, List<PathwayElement.CompletionRule> completionRules);
    
    // Find elements by pathway ID and required count range
    List<PathwayElement> findByPathwayIdAndRequiredCountBetween(Long pathwayId, Integer minCount, Integer maxCount);
    
    // Find elements by pathway ID and optional status
    List<PathwayElement> findByPathwayIdAndIsOptionalIn(Long pathwayId, List<Boolean> optionalStatuses);
    
    // Find elements by pathway ID and counts towards parent status
    List<PathwayElement> findByPathwayIdAndCountsTowardsParentIn(Long pathwayId, List<Boolean> countsTowardsParentStatuses);
    
    // Find elements by pathway ID and short code pattern
    List<PathwayElement> findByPathwayIdAndShortCodeContainingIgnoreCase(Long pathwayId, String shortCodePattern);
    
    // Find elements by pathway ID and description pattern
    List<PathwayElement> findByPathwayIdAndDescriptionContainingIgnoreCase(Long pathwayId, String descriptionPattern);
    
    // Find elements by pathway ID and tag pattern
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "JSON_SEARCH(pe.metadata, 'one', :tagPattern, NULL, '$[*]') IS NOT NULL")
    List<PathwayElement> findByPathwayIdAndTagPattern(@Param("pathwayId") Long pathwayId, @Param("tagPattern") String tagPattern);
    
    // Find elements by pathway ID and competency pattern
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "JSON_SEARCH(pe.competencies, 'one', :competencyPattern) IS NOT NULL")
    List<PathwayElement> findByPathwayIdAndCompetencyPattern(@Param("pathwayId") Long pathwayId, @Param("competencyPattern") String competencyPattern);
    
    // Find elements by pathway ID and prerequisite pattern
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "JSON_SEARCH(pe.prerequisites, 'one', :prerequisitePattern) IS NOT NULL")
    List<PathwayElement> findByPathwayIdAndPrerequisitePattern(@Param("pathwayId") Long pathwayId, @Param("prerequisitePattern") String prerequisitePattern);
    
    // Find elements by pathway ID and metadata key pattern
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "JSON_SEARCH(pe.metadata, 'one', :keyPattern, NULL, '$[*]') IS NOT NULL")
    List<PathwayElement> findByPathwayIdAndMetadataKeyPattern(@Param("pathwayId") Long pathwayId, @Param("keyPattern") String keyPattern);
    
    // Find elements by pathway ID and metadata value pattern
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "JSON_SEARCH(pe.metadata, 'one', :valuePattern, NULL, '$[*]') IS NOT NULL")
    List<PathwayElement> findByPathwayIdAndMetadataValuePattern(@Param("pathwayId") Long pathwayId, @Param("valuePattern") String valuePattern);
    
    // Find elements by pathway ID and parent element ID
    List<PathwayElement> findByPathwayIdAndParentElementId(Long pathwayId, Long parentElementId);
    
    // Find next order index for pathway
    @Query("SELECT COALESCE(MAX(pe.orderIndex), 0) + 1 FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId")
    Integer findNextOrderIndex(@Param("pathwayId") Long pathwayId);
    
    // Find elements by pathway ID ordered by order index
    List<PathwayElement> findByPathwayIdOrderByOrderIndexAsc(Long pathwayId);
} 