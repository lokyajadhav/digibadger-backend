package com.taashee.badger.repositories;

import com.taashee.badger.models.UserInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInvitationRepository extends JpaRepository<UserInvitation, Long> {
    Optional<UserInvitation> findByToken(String token);
    Optional<UserInvitation> findByEmailAndStatus(String email, UserInvitation.Status status);
} 