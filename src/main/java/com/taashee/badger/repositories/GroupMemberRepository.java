package com.taashee.badger.repositories;

import com.taashee.badger.models.GroupMember;
import com.taashee.badger.models.Group;
import com.taashee.badger.models.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroup(Group group);
    List<GroupMember> findByGroupId(Long groupId);
    List<GroupMember> findByUser(User user);
    List<GroupMember> findByUserId(Long userId);
    Optional<GroupMember> findByGroupAndUser(Group group, User user);
    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
}
