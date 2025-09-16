package com.taashee.badger.services;

import com.taashee.badger.models.PathwayGroupSubscription;
import com.taashee.badger.models.Pathway;
import com.taashee.badger.models.Group;
import com.taashee.badger.models.User;
import java.util.List;

public interface PathwaySubscriptionService {
    // Pathway subscription management
    PathwayGroupSubscription subscribeGroupToPathway(Long pathwayId, Long groupId, User subscribedBy);
    void unsubscribeGroupFromPathway(Long pathwayId, Long groupId);
    List<PathwayGroupSubscription> getPathwaySubscriptions(Long pathwayId);
    List<PathwayGroupSubscription> getGroupSubscriptions(Long groupId);
    
    // Permission checks
    boolean canSubscribeToPathway(User user, Long pathwayId);
    boolean canUnsubscribeFromPathway(User user, Long pathwayId);
    
    // Business logic
    boolean hasActiveSubscriptions(Long pathwayId);
    void validatePathwayCanBeUnpublished(Long pathwayId);
}
