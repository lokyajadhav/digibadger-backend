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
    List<PathwayElement> findByPathwayIdOrderByOrderIndex(Long pathwayId);
    
    // Find elements by pathway and element type
    List<PathwayElement> findByPathwayIdAndElementTypeOrderByOrderIndex(Long pathwayId, String elementType);
    
    // Find the next order index for a pathway
    @Query("SELECT COALESCE(MAX(pe.orderIndex), 0) + 1 FROM PathwayElement pe WHERE pe.pathway.id = :pathwayId")
    Integer findNextOrderIndex(@Param("pathwayId") Long pathwayId);
    
    // Count elements in a pathway
    long countByPathwayId(Long pathwayId);
} 