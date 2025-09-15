package com.taashee.badger.repositories;

import com.taashee.badger.models.StepVersion;
import com.taashee.badger.models.PathwayVersion;
import com.taashee.badger.models.Pathway;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StepVersionRepository extends JpaRepository<StepVersion, Long> {
    
    List<StepVersion> findByPathwayVersionOrderByOrderIndex(PathwayVersion pathwayVersion);
    
    List<StepVersion> findByPathwayVersionAndMilestone(PathwayVersion pathwayVersion, Boolean milestone);
    
    List<StepVersion> findByPathwayVersionAndParentStepId(PathwayVersion pathwayVersion, Long parentStepId);
    
    @Query("SELECT sv FROM StepVersion sv WHERE sv.pathwayVersion IN (SELECT pv FROM PathwayVersion pv WHERE pv.pathway = :pathway)")
    List<StepVersion> findByPathway(@Param("pathway") Pathway pathway);
}