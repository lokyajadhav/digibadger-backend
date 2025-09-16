package com.taashee.badger.repositories;

import com.taashee.badger.models.PathwayProgress;
import com.taashee.badger.models.Pathway;
import com.taashee.badger.models.User;
import com.taashee.badger.models.Group;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PathwayProgressRepository extends JpaRepository<PathwayProgress, Long> {
    List<PathwayProgress> findByPathway(Pathway pathway);
    List<PathwayProgress> findByPathwayId(Long pathwayId);
    List<PathwayProgress> findByUser(User user);
    List<PathwayProgress> findByUserId(Long userId);
    List<PathwayProgress> findByGroup(Group group);
    List<PathwayProgress> findByGroupId(Long groupId);
    List<PathwayProgress> findByPathwayAndGroup(Pathway pathway, Group group);
    List<PathwayProgress> findByPathwayIdAndGroupId(Long pathwayId, Long groupId);
    Optional<PathwayProgress> findByPathwayAndUserAndGroup(Pathway pathway, User user, Group group);
    Optional<PathwayProgress> findByPathwayIdAndUserIdAndGroupId(Long pathwayId, Long userId, Long groupId);
}
