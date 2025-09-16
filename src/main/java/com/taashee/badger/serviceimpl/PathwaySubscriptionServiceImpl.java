package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.*;
import com.taashee.badger.repositories.*;
import com.taashee.badger.services.PathwaySubscriptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PathwaySubscriptionServiceImpl implements PathwaySubscriptionService {
    private final PathwayGroupSubscriptionRepository subscriptionRepository;
    private final PathwayRepository pathwayRepository;
    private final GroupRepository groupRepository;
    private final OrganizationStaffRepository organizationStaffRepository;

    public PathwaySubscriptionServiceImpl(PathwayGroupSubscriptionRepository subscriptionRepository,
                                        PathwayRepository pathwayRepository,
                                        GroupRepository groupRepository,
                                        OrganizationStaffRepository organizationStaffRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.pathwayRepository = pathwayRepository;
        this.groupRepository = groupRepository;
        this.organizationStaffRepository = organizationStaffRepository;
    }

    @Override
    @Transactional
    public PathwayGroupSubscription subscribeGroupToPathway(Long pathwayId, Long groupId, User subscribedBy) {
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new RuntimeException("Pathway not found"));
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        
        // Check if pathway is published - only published pathways can be subscribed to
        if (pathway.getStatus() != PathwayStatus.PUBLISHED) {
            throw new RuntimeException("Only published pathways can be subscribed to by groups");
        }
        
        // Check if already subscribed
        if (subscriptionRepository.existsByPathwayIdAndGroupId(pathwayId, groupId)) {
            throw new RuntimeException("Group is already subscribed to this pathway");
        }
        
        // Verify both pathway and group belong to the same organization
        if (!pathway.getOrganization().getId().equals(group.getOrganization().getId())) {
            throw new RuntimeException("Pathway and group must belong to the same organization");
        }
        
        PathwayGroupSubscription subscription = new PathwayGroupSubscription();
        subscription.setPathway(pathway);
        subscription.setGroup(group);
        subscription.setSubscribedBy(subscribedBy);
        
        return subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public void unsubscribeGroupFromPathway(Long pathwayId, Long groupId) {
        PathwayGroupSubscription subscription = subscriptionRepository
                .findByPathwayIdAndGroupId(pathwayId, groupId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        subscriptionRepository.delete(subscription);
    }

    @Override
    public List<PathwayGroupSubscription> getPathwaySubscriptions(Long pathwayId) {
        return subscriptionRepository.findByPathwayId(pathwayId);
    }

    @Override
    public List<PathwayGroupSubscription> getGroupSubscriptions(Long groupId) {
        return subscriptionRepository.findByGroupId(groupId);
    }

    @Override
    public boolean canSubscribeToPathway(User user, Long pathwayId) {
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new RuntimeException("Pathway not found"));
        
        // Check if pathway is published - only published pathways can be subscribed to
        if (pathway.getStatus() != PathwayStatus.PUBLISHED) {
            return false;
        }
        
        // Check if user is an ISSUER
        if (user.getRoles() == null || !user.getRoles().contains("ISSUER")) {
            return false;
        }
        
        // Check if user has staff role in the organization
        OrganizationStaff staff = organizationStaffRepository
                .findByOrganizationIdAndUserId(pathway.getOrganization().getId(), user.getId())
                .orElse(null);
        
        return staff != null;
    }

    @Override
    public boolean canUnsubscribeFromPathway(User user, Long pathwayId) {
        return canSubscribeToPathway(user, pathwayId);
    }

    @Override
    public boolean hasActiveSubscriptions(Long pathwayId) {
        return !subscriptionRepository.findByPathwayId(pathwayId).isEmpty();
    }

    @Override
    @Transactional
    public void validatePathwayCanBeUnpublished(Long pathwayId) {
        if (hasActiveSubscriptions(pathwayId)) {
            throw new RuntimeException("Cannot unpublish pathway: groups are subscribed to it");
        }
    }
}
