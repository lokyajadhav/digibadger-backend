package com.taashee.badger.repositories;

import com.taashee.badger.models.BadgeClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BadgeClassRepository extends JpaRepository<BadgeClass, Long> {
    // Add custom queries if needed
    
    @Modifying
    @Query("DELETE FROM BadgeClass bc WHERE bc.organization.id = :organizationId")
    void deleteByOrganizationId(@Param("organizationId") Long organizationId);
} 