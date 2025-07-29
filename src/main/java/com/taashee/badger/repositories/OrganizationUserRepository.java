package com.taashee.badger.repositories;

import com.taashee.badger.models.OrganizationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationUserRepository extends JpaRepository<OrganizationUser, Long> {
    
    @Query("SELECT ou FROM OrganizationUser ou WHERE ou.organization.id = :organizationId")
    List<OrganizationUser> findByOrganizationId(@Param("organizationId") Long organizationId);
    
    @Query("SELECT ou FROM OrganizationUser ou WHERE ou.user.id = :userId")
    List<OrganizationUser> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT ou FROM OrganizationUser ou WHERE ou.organization.id = :organizationId AND ou.user.id = :userId")
    Optional<OrganizationUser> findByOrganizationIdAndUserId(@Param("organizationId") Long organizationId, @Param("userId") Long userId);
    
    @Query("SELECT ou FROM OrganizationUser ou WHERE ou.user.email = :email")
    List<OrganizationUser> findByUserEmail(@Param("email") String email);
} 