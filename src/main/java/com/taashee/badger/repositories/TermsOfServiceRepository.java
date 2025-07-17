package com.taashee.badger.repositories;

import com.taashee.badger.models.TermsOfService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsOfServiceRepository extends JpaRepository<TermsOfService, Long> {
    TermsOfService findTopByTypeOrderByVersionDesc(TermsOfService.Type type);
} 