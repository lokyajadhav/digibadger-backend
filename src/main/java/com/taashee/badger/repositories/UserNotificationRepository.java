package com.taashee.badger.repositories;

import com.taashee.badger.models.UserNotification;
import com.taashee.badger.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    List<UserNotification> findByUserOrderByCreatedAtDesc(User user);
} 