package com.taashee.badger.repositories;

import com.taashee.badger.models.PathwayElement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PathwayElementRepository extends JpaRepository<PathwayElement, Long> {
    
    // Find all elements for a specific pathway
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId ORDER BY pe.orderIndex")
    List<PathwayElement> findByPathwayIdOrderByOrderIndex(@Param("pathwayId") Long pathwayId);
    
    // Find the next order index for a pathway
    @Query("SELECT COALESCE(MAX(pe.orderIndex), 0) + 1 FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId")
    Integer findNextOrderIndex(@Param("pathwayId") Long pathwayId);
    
    // Count elements in a pathway
    @Query("SELECT COUNT(pe) FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId")
    long countByPathwayId(@Param("pathwayId") Long pathwayId);
    
    // Find root elements (no parent)
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.parentElement IS NULL ORDER BY pe.orderIndex")
    List<PathwayElement> findByPathwayIdAndParentElementIsNullOrderByOrderIndex(@Param("pathwayId") Long pathwayId);
    
    // Find children of a specific element
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.parentElement.id = :parentElementId ORDER BY pe.orderIndex")
    List<PathwayElement> findByParentElementIdOrderByOrderIndex(@Param("parentElementId") Long parentElementId);
    
    // Find elements by pathway and parent element
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.parentElement.id = :parentElementId ORDER BY pe.orderIndex")
    List<PathwayElement> findByPathwayIdAndParentElementIdOrderByOrderIndex(@Param("pathwayId") Long pathwayId, @Param("parentElementId") Long parentElementId);
    
    // Count children of an element
    @Query("SELECT COUNT(pe) FROM PathwayElement pe WHERE pe.parentElement.id = :parentElementId")
    long countByParentElementId(@Param("parentElementId") Long parentElementId);
    
    // Find elements by completion rule
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.completionRule = :completionRule ORDER BY pe.orderIndex")
    List<PathwayElement> findByPathwayIdAndCompletionRuleOrderByOrderIndex(@Param("pathwayId") Long pathwayId, @Param("completionRule") PathwayElement.CompletionRule completionRule);
    
    // Find optional elements
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.isOptional = true ORDER BY pe.orderIndex")
    List<PathwayElement> findByPathwayIdAndIsOptionalTrueOrderByOrderIndex(@Param("pathwayId") Long pathwayId);
    
    // Find required elements
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.isOptional = false ORDER BY pe.orderIndex")
    List<PathwayElement> findByPathwayIdAndIsOptionalFalseOrderByOrderIndex(@Param("pathwayId") Long pathwayId);
    
    // Check if element has children
    @Query("SELECT COUNT(pe) > 0 FROM PathwayElement pe WHERE pe.parentElement.id = :parentElementId")
    boolean existsByParentElementId(@Param("parentElementId") Long parentElementId);
    
    // Find all elements for a pathway (simplified)
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId")
    List<PathwayElement> findByPathwayId(@Param("pathwayId") Long pathwayId);
    
    // Find root elements (no parent) - simplified
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.parentElement IS NULL")
    List<PathwayElement> findByPathwayIdAndParentElementIdIsNull(@Param("pathwayId") Long pathwayId);
    
    // Find elements by pathway and parent element - simplified
    @Query("SELECT pe FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId AND pe.parentElement.id = :parentElementId")
    List<PathwayElement> findByPathwayIdAndParentElementId(@Param("pathwayId") Long pathwayId, @Param("parentElementId") Long parentElementId);
} 