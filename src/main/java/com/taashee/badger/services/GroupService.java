package com.taashee.badger.services;

import com.taashee.badger.models.Group;
import com.taashee.badger.models.GroupMember;
import com.taashee.badger.models.Organization;
import com.taashee.badger.models.User;
import java.util.List;

public interface GroupService {
    // Group CRUD operations
    Group createGroup(Organization organization, User createdBy, String name, String description);
    List<Group> listGroups(Organization organization);
    List<Group> getAllGroups(Organization organization);
    Group getGroup(Long groupId);
    Group updateGroup(Long groupId, String name, String description);
    void deleteGroup(Long groupId);
    
    // Group member management
    GroupMember addMemberToGroup(Long groupId, Long userId);
    void removeMemberFromGroup(Long groupId, Long userId);
    List<GroupMember> listGroupMembers(Long groupId);
    List<User> listAvailableUsersForGroup(Long groupId);
    
    // Permission checks
    boolean canManageGroups(User user, Organization organization);
    boolean canAddUserToGroup(User user, Long groupId, Long targetUserId);
}
