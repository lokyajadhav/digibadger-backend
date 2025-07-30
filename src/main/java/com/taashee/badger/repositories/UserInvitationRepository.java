package com.taashee.badger.repositories;

import com.taashee.badger.models.UserInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInvitationRepository extends JpaRepository<UserInvitation, Long> {
    Optional<UserInvitation> findByToken(String token);
    Optional<UserInvitation> findByEmailAndStatus(String email, UserInvitation.Status status);
    
    @Modifying
    @Query("DELETE FROM UserInvitation ui WHERE ui.createdBy.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
} 