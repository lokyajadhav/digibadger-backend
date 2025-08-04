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
    @Query("SELECT bi FROM BadgeInstance bi WHERE bi.recipient.id = :userId")
    List<BadgeInstance> findByRecipientId(@Param("userId") Long userId);
    
    @Query("SELECT bi FROM BadgeInstance bi WHERE bi.id = :badgeInstanceId AND bi.recipient.id = :userId")
    Optional<BadgeInstance> findByIdAndRecipientId(@Param("badgeInstanceId") Long badgeInstanceId, @Param("userId") Long userId);
    
    @Query("SELECT bi FROM BadgeInstance bi WHERE bi.badgeClass.id = :badgeClassId")
    List<BadgeInstance> findByBadgeClassId(@Param("badgeClassId") Long badgeClassId);
    
    @Modifying
    @Query("DELETE FROM BadgeInstance bi WHERE bi.badgeClass.organization.id = :organizationId")
    void deleteByBadgeClassOrganizationId(@Param("organizationId") Long organizationId);
    
    @Modifying
    @Query("DELETE FROM BadgeInstance bi WHERE bi.recipient.id = :recipientId")
    void deleteByRecipientId(@Param("recipientId") Long recipientId);
    
    @Query("SELECT bi FROM BadgeInstance bi WHERE bi.badgeClass.id = :badgeClassId AND bi.revoked = false")
    List<BadgeInstance> findByBadgeClassIdAndRevokedFalse(@Param("badgeClassId") Long badgeClassId);
} 