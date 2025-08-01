package com.taashee.badger.repositories;

import com.taashee.badger.models.RecipientGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecipientGroupRepository extends JpaRepository<RecipientGroup, Long> {
    
    // Find all recipient groups for a specific organization
    List<RecipientGroup> findByOrganizationId(Long organizationId);
    
    // Find active recipient groups for a specific organization
    List<RecipientGroup> findByOrganizationIdAndIsActiveTrue(Long organizationId);
    
    // Find recipient group by name and organization
    RecipientGroup findByNameAndOrganizationId(String name, Long organizationId);
    
    // Check if recipient group exists by name and organization
    boolean existsByNameAndOrganizationId(String name, Long organizationId);
    
    // Find recipient groups that contain a specific user
    @Query("SELECT rg FROM RecipientGroup rg JOIN rg.members m WHERE m.id = :userId")
    List<RecipientGroup> findByUserId(@Param("userId") Long userId);
} 