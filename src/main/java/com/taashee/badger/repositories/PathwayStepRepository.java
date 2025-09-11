package com.taashee.badger.repositories;

import com.taashee.badger.models.Pathway;
import com.taashee.badger.models.PathwayStep;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PathwayStepRepository extends JpaRepository<PathwayStep, Long> {
    List<PathwayStep> findByPathwayOrderByOrderIndexAsc(Pathway pathway);
}


