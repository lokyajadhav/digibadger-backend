package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.*;
import com.taashee.badger.repositories.*;
import com.taashee.badger.services.GroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final OrganizationStaffRepository organizationStaffRepository;
    private final UserRepository userRepository;
    private final OrganizationUserRepository organizationUserRepository;

    public GroupServiceImpl(GroupRepository groupRepository, GroupMemberRepository groupMemberRepository,
                           OrganizationStaffRepository organizationStaffRepository, UserRepository userRepository,
                           OrganizationUserRepository organizationUserRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.organizationStaffRepository = organizationStaffRepository;
        this.userRepository = userRepository;
        this.organizationUserRepository = organizationUserRepository;
    }

    @Override
    @Transactional
    public Group createGroup(Organization organization, User createdBy, String name, String description) {
        Group group = new Group();
        group.setOrganization(organization);
        group.setCreatedBy(createdBy);
        group.setName(name);
        group.setDescription(description);
        return groupRepository.save(group);
    }

    @Override
    public List<Group> listGroups(Organization organization) {
        return groupRepository.findByOrganization(organization);
    }

    @Override
    public List<Group> getAllGroups(Organization organization) {
        return groupRepository.findByOrganization(organization);
    }

    @Override
    public Group getGroup(Long groupId) {
        return groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
    }

    @Override
    @Transactional
    public Group updateGroup(Long groupId, String name, String description) {
        Group group = getGroup(groupId);
        group.setName(name);
        group.setDescription(description);
        return groupRepository.save(group);
    }

    @Override
    @Transactional
    public void deleteGroup(Long groupId) {
        Group group = getGroup(groupId);
        groupRepository.delete(group);
    }

    @Override
    @Transactional
    public GroupMember addMemberToGroup(Long groupId, Long userId) {
        Group group = getGroup(groupId);
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user is already a member
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new RuntimeException("User is already a member of this group");
        }
        
        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(user);
        return groupMemberRepository.save(member);
    }

    @Override
    @Transactional
    public void removeMemberFromGroup(Long groupId, Long userId) {
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this group"));
        groupMemberRepository.delete(member);
    }

    @Override
    public List<GroupMember> listGroupMembers(Long groupId) {
        return groupMemberRepository.findByGroupId(groupId);
    }

    @Override
    public List<User> listAvailableUsersForGroup(Long groupId) {
        Group group = getGroup(groupId);
        Organization organization = group.getOrganization();
        
        // Get all users from the same organization with ROLE "USER"
        List<OrganizationUser> orgUsers = organizationUserRepository.findByOrganizationId(organization.getId());
        
        // Get existing group members
        List<Long> existingMemberIds = groupMemberRepository.findByGroupId(groupId)
                .stream()
                .map(member -> member.getUser().getId())
                .collect(Collectors.toList());
        
        // Filter users with ROLE "USER" who are not already in the group
        return orgUsers.stream()
                .map(OrganizationUser::getUser)
                .filter(user -> user.getRoles() != null && user.getRoles().contains("USER"))
                .filter(user -> !existingMemberIds.contains(user.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean canManageGroups(User user, Organization organization) {
        // Check if user is an ISSUER with "owner" permission
        if (user.getRoles() == null || !user.getRoles().contains("ISSUER")) {
            return false;
        }
        
        OrganizationStaff staff = organizationStaffRepository.findByOrganizationIdAndUserId(organization.getId(), user.getId())
                .orElse(null);
        
        return staff != null && "owner".equals(staff.getStaffRole());
    }

    @Override
    public boolean canAddUserToGroup(User user, Long groupId, Long targetUserId) {
        Group group = getGroup(groupId);
        return canManageGroups(user, group.getOrganization());
    }
}
