package com.taashee.badger.repositories;

import com.taashee.badger.models.BadgeClass;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeClassRepository extends JpaRepository<BadgeClass, Long> {
    // Add custom queries if needed
} 