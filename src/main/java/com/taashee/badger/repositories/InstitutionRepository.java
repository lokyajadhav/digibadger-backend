package com.taashee.badger.repositories;

import com.taashee.badger.models.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
 
public interface InstitutionRepository extends JpaRepository<Institution, Long> {
} 