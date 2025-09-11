package com.taashee.badger.repositories;

import com.taashee.badger.models.PathwayVersion;
import com.taashee.badger.models.StepVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StepVersionRepository extends JpaRepository<StepVersion, Long> {
    List<StepVersion> findByPathwayVersionOrderByOrderIndexAsc(PathwayVersion pathwayVersion);
}
