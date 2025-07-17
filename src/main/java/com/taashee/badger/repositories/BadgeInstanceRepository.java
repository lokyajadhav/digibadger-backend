package com.taashee.badger.repositories;

import com.taashee.badger.models.BadgeInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BadgeInstanceRepository extends JpaRepository<BadgeInstance, Long> {
    // Add custom queries if needed
    List<BadgeInstance> findByRecipientId(Long userId);
    Optional<BadgeInstance> findByIdAndRecipientId(Long badgeInstanceId, Long userId);
} 