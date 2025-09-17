package com.taashee.badger.repositories;

import com.taashee.badger.models.PathwayStep;
import com.taashee.badger.models.StepPrerequisiteRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StepPrerequisiteRuleRepository extends JpaRepository<StepPrerequisiteRule, Long> {
    Optional<StepPrerequisiteRule> findByStep(PathwayStep step);
}
