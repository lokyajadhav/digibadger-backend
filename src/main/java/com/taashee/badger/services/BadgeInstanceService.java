package com.taashee.badger.services;

import com.taashee.badger.models.BadgeInstance;
import java.util.List;
import java.util.Optional;

public interface BadgeInstanceService {
    BadgeInstance createBadgeInstance(BadgeInstance badgeInstance);
    BadgeInstance updateBadgeInstance(Long id, BadgeInstance badgeInstance);
    void deleteBadgeInstance(Long id);
    Optional<BadgeInstance> getBadgeInstanceById(Long id);
    List<BadgeInstance> getAllBadgeInstances();
    List<BadgeInstance> getBadgesForRecipient(Long userId);
    BadgeInstance getBadgeInstanceForRecipient(Long userId, Long badgeInstanceId);
    BadgeInstance archiveBadgeInstance(Long id, boolean archive);
    List<BadgeInstance> bulkArchiveBadgeInstances(List<Long> ids, boolean archive);
    void bulkDeleteBadgeInstances(List<Long> ids);
    List<BadgeInstance> revokeBadgeInstances(List<Long> ids, String revocationReason);
    // Add more methods as needed (e.g., revoke, award, search, etc.)
} 