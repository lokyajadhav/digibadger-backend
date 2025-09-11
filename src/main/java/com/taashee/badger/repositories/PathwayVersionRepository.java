package com.taashee.badger.repositories;

import com.taashee.badger.models.Pathway;
import com.taashee.badger.models.PathwayVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PathwayVersionRepository extends JpaRepository<PathwayVersion, Long> {
    List<PathwayVersion> findByPathwayOrderByVersionNumberDesc(Pathway pathway);
    Optional<PathwayVersion> findByPathwayAndVersionNumber(Pathway pathway, Long versionNumber);
    Optional<PathwayVersion> findTopByPathwayOrderByVersionNumberDesc(Pathway pathway);
}
