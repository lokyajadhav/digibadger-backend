package com.taashee.badger.repositories;

import com.taashee.badger.models.PathwayGroupSubscription;
import com.taashee.badger.models.Pathway;
import com.taashee.badger.models.Group;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PathwayGroupSubscriptionRepository extends JpaRepository<PathwayGroupSubscription, Long> {
    List<PathwayGroupSubscription> findByPathway(Pathway pathway);
    List<PathwayGroupSubscription> findByPathwayId(Long pathwayId);
    List<PathwayGroupSubscription> findByGroup(Group group);
    List<PathwayGroupSubscription> findByGroupId(Long groupId);
    Optional<PathwayGroupSubscription> findByPathwayAndGroup(Pathway pathway, Group group);
    Optional<PathwayGroupSubscription> findByPathwayIdAndGroupId(Long pathwayId, Long groupId);
    boolean existsByPathwayIdAndGroupId(Long pathwayId, Long groupId);
}
