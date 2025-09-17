package com.taashee.badger.repositories;

import com.taashee.badger.models.PathwayVersion;
import com.taashee.badger.models.Pathway;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PathwayVersionRepository extends JpaRepository<PathwayVersion, Long> {
    
    List<PathwayVersion> findByPathwayOrderByVersionDesc(Pathway pathway);
    
    Optional<PathwayVersion> findByPathwayAndVersion(Pathway pathway, Integer version);
    
    Optional<PathwayVersion> findByPathwayAndStatus(Pathway pathway, PathwayVersion.VersionStatus status);
    
    @Query("SELECT pv FROM PathwayVersion pv WHERE pv.pathway = :pathway AND pv.status = 'PUBLISHED' ORDER BY pv.version DESC")
    List<PathwayVersion> findPublishedVersionsByPathway(@Param("pathway") Pathway pathway);
    
    @Query("SELECT pv FROM PathwayVersion pv WHERE pv.pathway = :pathway AND pv.status = 'DRAFT' ORDER BY pv.version DESC")
    Optional<PathwayVersion> findLatestDraftByPathway(@Param("pathway") Pathway pathway);
    
    @Query("SELECT MAX(pv.version) FROM PathwayVersion pv WHERE pv.pathway = :pathway")
    Optional<Integer> findMaxVersionByPathway(@Param("pathway") Pathway pathway);
    
    @Query("SELECT COUNT(pv) FROM PathwayVersion pv WHERE pv.pathway = :pathway AND pv.status = 'PUBLISHED'")
    Long countPublishedVersionsByPathway(@Param("pathway") Pathway pathway);
}