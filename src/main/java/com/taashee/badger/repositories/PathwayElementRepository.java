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
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId")
    List<PathwayElement> findByPathwayId(@Param("pathwayId") Long pathwayId);
    
    // Find root elements (no parent) by pathway ID
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.parentElement IS NULL")
    List<PathwayElement> findByPathwayIdAndParentElementIsNull(@Param("pathwayId") Long pathwayId);
    
    // Find children of a specific element
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.parentElement.id = :parentElementId")
    List<PathwayElement> findByParentElementId(@Param("parentElementId") Long parentElementId);
    
    // Find elements by pathway ID and element type
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.elementType = :elementType")
    List<PathwayElement> findByPathwayIdAndElementType(@Param("pathwayId") Long pathwayId, @Param("elementType") PathwayElement.ElementType elementType);
    
    // Count elements by pathway ID
    @Query("SELECT COUNT(pe) FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId")
    long countByPathwayId(@Param("pathwayId") Long pathwayId);
    
    // Find elements by pathway ID and short code
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.shortCode = :shortCode")
    Optional<PathwayElement> findByPathwayIdAndShortCode(@Param("pathwayId") Long pathwayId, @Param("shortCode") String shortCode);
    
    // Find elements by pathway ID and counts towards parent
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.countsTowardsParent = :countsTowardsParent")
    List<PathwayElement> findByPathwayIdAndCountsTowardsParent(@Param("pathwayId") Long pathwayId, @Param("countsTowardsParent") Boolean countsTowardsParent);
    
    // Find elements by pathway ID and required count
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.requiredCount = :requiredCount")
    List<PathwayElement> findByPathwayIdAndRequiredCount(@Param("pathwayId") Long pathwayId, @Param("requiredCount") Integer requiredCount);
    
    // Find elements by pathway ID and difficulty level
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.difficultyLevel = :difficultyLevel")
    List<PathwayElement> findByPathwayIdAndDifficultyLevel(@Param("pathwayId") Long pathwayId, @Param("difficultyLevel") PathwayElement.DifficultyLevel difficultyLevel);
    
    // Find elements by pathway ID and completion rule
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.completionRule = :completionRule")
    List<PathwayElement> findByPathwayIdAndCompletionRule(@Param("pathwayId") Long pathwayId, @Param("completionRule") PathwayElement.CompletionRule completionRule);
    
    // Find optional elements by pathway ID
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.isOptional = true")
    List<PathwayElement> findByPathwayIdAndIsOptionalTrue(@Param("pathwayId") Long pathwayId);
    
    // Find required elements by pathway ID
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.isOptional = false")
    List<PathwayElement> findByPathwayIdAndIsOptionalFalse(@Param("pathwayId") Long pathwayId);
    
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
           "pe.id IN (SELECT pb.element.id FROM PathwayElementBadge pb WHERE pb.externalBadgeId IS NOT NULL)")
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
    
    // Find elements by pathway ID and competency ID
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "JSON_SEARCH(pe.competencies, 'one', :competencyId) IS NOT NULL")
    List<PathwayElement> findByPathwayIdAndCompetencyId(@Param("pathwayId") Long pathwayId, @Param("competencyId") String competencyId);
    
    // Find elements by pathway ID and minimum alignment strength
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "JSON_EXTRACT(pe.competencies, '$[*].alignmentStrength') >= :minStrength")
    List<PathwayElement> findByPathwayIdAndMinAlignmentStrength(@Param("pathwayId") Long pathwayId, @Param("minStrength") Double minStrength);
    
    // Find elements by pathway ID and alignment type
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "JSON_EXTRACT(pe.competencies, '$[*].alignmentType') = :alignmentType")
    List<PathwayElement> findByPathwayIdAndAlignmentType(@Param("pathwayId") Long pathwayId, @Param("alignmentType") String alignmentType);
    
    // Find elements by pathway ID and creation date range
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.createdAt BETWEEN :startDate AND :endDate")
    List<PathwayElement> findByPathwayIdAndCreatedAtBetween(@Param("pathwayId") Long pathwayId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find elements by pathway ID and update date range
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.updatedAt BETWEEN :startDate AND :endDate")
    List<PathwayElement> findByPathwayIdAndUpdatedAtBetween(@Param("pathwayId") Long pathwayId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find elements by pathway ID and order index range
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.orderIndex BETWEEN :startIndex AND :endIndex")
    List<PathwayElement> findByPathwayIdAndOrderIndexBetween(@Param("pathwayId") Long pathwayId, @Param("startIndex") Integer startIndex, @Param("endIndex") Integer endIndex);
    
    // Find elements by pathway ID and estimated duration hours range
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.estimatedDurationHours BETWEEN :minHours AND :maxHours")
    List<PathwayElement> findByPathwayIdAndEstimatedDurationHoursBetween(@Param("pathwayId") Long pathwayId, @Param("minHours") Double minHours, @Param("maxHours") Double maxHours);
    
    // Find elements by pathway ID and difficulty levels
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.difficultyLevel IN :difficultyLevels")
    List<PathwayElement> findByPathwayIdAndDifficultyLevelIn(@Param("pathwayId") Long pathwayId, @Param("difficultyLevels") List<PathwayElement.DifficultyLevel> difficultyLevels);
    
    // Find elements by pathway ID and element types
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.elementType IN :elementTypes")
    List<PathwayElement> findByPathwayIdAndElementTypeIn(@Param("pathwayId") Long pathwayId, @Param("elementTypes") List<PathwayElement.ElementType> elementTypes);
    
    // Find elements by pathway ID and completion rules
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.completionRule IN :completionRules")
    List<PathwayElement> findByPathwayIdAndCompletionRuleIn(@Param("pathwayId") Long pathwayId, @Param("completionRules") List<PathwayElement.CompletionRule> completionRules);
    
    // Find elements by pathway ID and required count range
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.requiredCount BETWEEN :minCount AND :maxCount")
    List<PathwayElement> findByPathwayIdAndRequiredCountBetween(@Param("pathwayId") Long pathwayId, @Param("minCount") Integer minCount, @Param("maxCount") Integer maxCount);
    
    // Find elements by pathway ID and optional statuses
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.isOptional IN :optionalStatuses")
    List<PathwayElement> findByPathwayIdAndIsOptionalIn(@Param("pathwayId") Long pathwayId, @Param("optionalStatuses") List<Boolean> optionalStatuses);
    
    // Find elements by pathway ID and counts towards parent statuses
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.countsTowardsParent IN :countsTowardsParentStatuses")
    List<PathwayElement> findByPathwayIdAndCountsTowardsParentIn(@Param("pathwayId") Long pathwayId, @Param("countsTowardsParentStatuses") List<Boolean> countsTowardsParentStatuses);
    
    // Find elements by pathway ID and short code containing
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND LOWER(pe.shortCode) LIKE LOWER(CONCAT('%', :shortCodePattern, '%'))")
    List<PathwayElement> findByPathwayIdAndShortCodeContainingIgnoreCase(@Param("pathwayId") Long pathwayId, @Param("shortCodePattern") String shortCodePattern);
    
    // Find elements by pathway ID and description containing
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND LOWER(pe.description) LIKE LOWER(CONCAT('%', :descriptionPattern, '%'))")
    List<PathwayElement> findByPathwayIdAndDescriptionContainingIgnoreCase(@Param("pathwayId") Long pathwayId, @Param("descriptionPattern") String descriptionPattern);
    
    // Find elements by pathway ID and tag pattern
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "JSON_SEARCH(pe.tags, 'one', :tagPattern) IS NOT NULL")
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
           "JSON_EXTRACT(pe.metadata, '$.*') LIKE %:keyPattern%")
    List<PathwayElement> findByPathwayIdAndMetadataKeyPattern(@Param("pathwayId") Long pathwayId, @Param("keyPattern") String keyPattern);
    
    // Find elements by pathway ID and metadata value pattern
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND " +
           "JSON_SEARCH(pe.metadata, 'one', :valuePattern) IS NOT NULL")
    List<PathwayElement> findByPathwayIdAndMetadataValuePattern(@Param("pathwayId") Long pathwayId, @Param("valuePattern") String valuePattern);
    
    // Find elements by pathway ID and parent element ID
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.parentElement.id = :parentElementId")
    List<PathwayElement> findByPathwayIdAndParentElementId(@Param("pathwayId") Long pathwayId, @Param("parentElementId") Long parentElementId);
    
    // Find next order index for pathway
    @Query("SELECT COALESCE(MAX(pe.orderIndex), 0) + 1 FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId")
    Integer findNextOrderIndex(@Param("pathwayId") Long pathwayId);
    
    // Find elements by pathway ID ordered by order index
    List<PathwayElement> findByPathwayIdOrderByOrderIndexAsc(Long pathwayId);
} 