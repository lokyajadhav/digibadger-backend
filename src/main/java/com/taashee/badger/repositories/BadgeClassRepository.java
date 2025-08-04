package com.taashee.badger.repositories;

import com.taashee.badger.models.BadgeClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BadgeClassRepository extends JpaRepository<BadgeClass, Long> {
    // Add custom queries if needed
    
    @Modifying
    @Query("DELETE FROM BadgeClass bc WHERE bc.organization.id = :organizationId")
    void deleteByOrganizationId(@Param("organizationId") Long organizationId);
    
    // Find badges by organization
    @Query("SELECT bc FROM BadgeClass bc WHERE bc.organization.id = :organizationId")
    List<BadgeClass> findByOrganizationId(@Param("organizationId") Long organizationId);
    
    // Find badges by multiple organizations
    @Query("SELECT bc FROM BadgeClass bc WHERE bc.organization.id IN :organizationIds")
    List<BadgeClass> findByOrganizationIdIn(@Param("organizationIds") List<Long> organizationIds);
    
    // Search badges by name or description
    @Query("SELECT bc FROM BadgeClass bc WHERE LOWER(bc.name) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(bc.description) LIKE LOWER(CONCAT('%', :description, '%'))")
    List<BadgeClass> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(@Param("name") String name, @Param("description") String description);
} 