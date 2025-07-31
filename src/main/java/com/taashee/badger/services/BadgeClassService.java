package com.taashee.badger.services;

import com.taashee.badger.models.BadgeClass;
import com.taashee.badger.models.BadgeInstance;
import com.taashee.badger.models.BadgeInstanceAwardRequest;
import com.taashee.badger.models.BadgeClassDTO;
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
    BadgeClass createBadgeClassFromDTO(BadgeClassDTO badgeClassDTO);
    BadgeClass updateBadgeClassFromDTO(Long id, BadgeClassDTO badgeClassDTO);
    
    // New methods for badge recipients and import/export
    java.util.Map<String, Object> getBadgeRecipients(Long badgeClassId, int page, int size, String search, String status, String sortBy, String sortOrder, String startDate, String endDate);
    java.util.Map<String, Object> exportBadgeClass(Long badgeClassId, boolean includeAssertions, boolean compressOutput);
    BadgeClass importBadgeClass(Long badgeClassId, String badgeUrl, String badgeJson, String importType);
    
    // Privacy and archive management
    BadgeClass togglePrivacy(Long badgeClassId);
    List<BadgeInstance> getActiveBadgeInstances(Long badgeClassId);
    // Add more methods as needed (e.g., archive, search, etc.)
} 