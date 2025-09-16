package com.taashee.badger.repositories;

import com.taashee.badger.models.StepProgress;
import com.taashee.badger.models.StepVersion;
import com.taashee.badger.models.User;
import com.taashee.badger.models.Group;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StepProgressRepository extends JpaRepository<StepProgress, Long> {
    List<StepProgress> findByStepVersion(StepVersion stepVersion);
    List<StepProgress> findByStepVersionId(Long stepVersionId);
    List<StepProgress> findByUser(User user);
    List<StepProgress> findByUserId(Long userId);
    List<StepProgress> findByGroup(Group group);
    List<StepProgress> findByGroupId(Long groupId);
    List<StepProgress> findByStepVersionAndGroup(StepVersion stepVersion, Group group);
    List<StepProgress> findByStepVersionIdAndGroupId(Long stepVersionId, Long groupId);
    Optional<StepProgress> findByStepVersionAndUserAndGroup(StepVersion stepVersion, User user, Group group);
    Optional<StepProgress> findByStepVersionIdAndUserIdAndGroupId(Long stepVersionId, Long userId, Long groupId);
}
