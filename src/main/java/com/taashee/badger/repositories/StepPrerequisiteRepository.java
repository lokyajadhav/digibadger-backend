package com.taashee.badger.repositories;

import com.taashee.badger.models.StepPrerequisite;
import com.taashee.badger.models.PathwayStep;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StepPrerequisiteRepository extends JpaRepository<StepPrerequisite, Long> {
    List<StepPrerequisite> findByStep(PathwayStep step);
}


