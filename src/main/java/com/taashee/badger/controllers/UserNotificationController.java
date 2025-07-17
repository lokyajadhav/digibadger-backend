package com.taashee.badger.controllers;

import com.taashee.badger.models.UserNotification;
import com.taashee.badger.models.User;
import com.taashee.badger.models.ApiResponse;
import com.taashee.badger.repositories.UserNotificationRepository;
import com.taashee.badger.repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "User Notifications", description = "APIs for user notifications. Author: Lokya Naik")
@RestController
@RequestMapping("/api/user/notifications")
public class UserNotificationController {
    @Autowired
    private UserNotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "List notifications for user", description = "Get all notifications for a user. Author: Lokya Naik")
    @GetMapping
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserNotification>>> listNotifications(@RequestParam Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, "User not found", null, null));
        }
        List<UserNotification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        return ResponseEntity.ok(new ApiResponse<>(200, "Success", notifications, null));
    }

    @Operation(summary = "Mark notification as read", description = "Mark a notification as read. Author: Lokya Naik")
    @PostMapping("/read")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@RequestParam Long userId, @RequestBody Map<String, Long> body) {
        Long notificationId = body.get("notificationId");
        UserNotification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null || !notification.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, "Notification not found", null, null));
        }
        notification.setRead(true);
        notificationRepository.save(notification);
        return ResponseEntity.ok(new ApiResponse<>(200, "Marked as read", null, null));
    }
} 