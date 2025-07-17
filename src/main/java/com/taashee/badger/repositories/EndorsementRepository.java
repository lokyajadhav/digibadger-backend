package com.taashee.badger.repositories;

import com.taashee.badger.models.Endorsement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EndorsementRepository extends JpaRepository<Endorsement, Long> {
    // Add custom queries if needed
} 