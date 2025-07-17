package com.taashee.badger.services;

import com.taashee.badger.models.BadgeClass;
import com.taashee.badger.models.BadgeInstance;
import com.taashee.badger.models.BadgeInstanceAwardRequest;
import java.util.List;
import java.util.Optional;

public interface BadgeClassService {
    BadgeClass createBadgeClass(BadgeClass badgeClass);
    BadgeClass updateBadgeClass(Long id, BadgeClass badgeClass);
    void deleteBadgeClass(Long id);
    Optional<BadgeClass> getBadgeClassById(Long id);
    List<BadgeClass> getAllBadgeClasses();
    BadgeClass archiveBadgeClass(Long id, boolean archive);
    List<BadgeInstance> awardEnrollments(Long badgeClassId, java.util.List<com.taashee.badger.models.BadgeInstanceAwardRequest> requests);
    List<BadgeClass> bulkArchiveBadgeClasses(List<Long> ids, boolean archive);
    void bulkDeleteBadgeClasses(List<Long> ids);
    // Add more methods as needed (e.g., archive, search, etc.)
} 