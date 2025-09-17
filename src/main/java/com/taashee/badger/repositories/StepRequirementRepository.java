package com.taashee.badger.repositories;

import com.taashee.badger.models.StepRequirement;
import com.taashee.badger.models.PathwayStep;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StepRequirementRepository extends JpaRepository<StepRequirement, Long> {
    List<StepRequirement> findByStep(PathwayStep step);
}


