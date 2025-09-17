package com.taashee.badger.repositories;

import com.taashee.badger.models.RequirementVersion;
import com.taashee.badger.models.StepVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RequirementVersionRepository extends JpaRepository<RequirementVersion, Long> {
    
    List<RequirementVersion> findByStepVersion(StepVersion stepVersion);
}
