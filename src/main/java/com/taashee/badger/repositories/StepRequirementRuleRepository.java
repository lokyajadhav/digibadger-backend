package com.taashee.badger.repositories;

import com.taashee.badger.models.PathwayStep;
import com.taashee.badger.models.StepRequirementRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StepRequirementRuleRepository extends JpaRepository<StepRequirementRule, Long> {
    Optional<StepRequirementRule> findByStep(PathwayStep step);
}
