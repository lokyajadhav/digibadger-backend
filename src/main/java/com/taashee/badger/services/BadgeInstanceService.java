package com.taashee.badger.services;

import com.taashee.badger.models.BadgeInstance;
import com.taashee.badger.models.BadgeInstanceDTO;
import java.util.List;
import java.util.Optional;
import java.util.Map;

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
    BadgeInstance revokeBadgeInstance(Long id, String revocationReason);
    Map<String, Object> getUserBadges(int page, int size, String search);
    BadgeInstance createBadgeInstanceFromDTO(BadgeInstanceDTO badgeInstanceDTO);
    BadgeInstance updateBadgeInstanceFromDTO(Long id, BadgeInstanceDTO badgeInstanceDTO);
    // Add more methods as needed (e.g., revoke, award, search, etc.)
} 