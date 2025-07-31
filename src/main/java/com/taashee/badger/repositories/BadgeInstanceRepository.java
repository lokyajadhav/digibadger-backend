package com.taashee.badger.repositories;

import com.taashee.badger.models.BadgeInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BadgeInstanceRepository extends JpaRepository<BadgeInstance, Long> {
    // Add custom queries if needed
    List<BadgeInstance> findByRecipientId(Long userId);
    Optional<BadgeInstance> findByIdAndRecipientId(Long badgeInstanceId, Long userId);
    List<BadgeInstance> findByBadgeClassId(Long badgeClassId);
    
    @Modifying
    @Query("DELETE FROM BadgeInstance bi WHERE bi.badgeClass.organization.id = :organizationId")
    void deleteByBadgeClassOrganizationId(@Param("organizationId") Long organizationId);
    
    @Modifying
    @Query("DELETE FROM BadgeInstance bi WHERE bi.recipient.id = :recipientId")
    void deleteByRecipientId(@Param("recipientId") Long recipientId);
    
    List<BadgeInstance> findByBadgeClassIdAndRevokedFalse(Long badgeClassId);
} 