package com.taashee.badger.repositories;

import com.taashee.badger.models.Issuer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssuerRepository extends JpaRepository<Issuer, Long> {
    // Add custom queries if needed
} 