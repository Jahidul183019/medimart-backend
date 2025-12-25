package com.medimart.controller;

import com.medimart.model.Notification;
import com.medimart.model.NotificationType;
import com.medimart.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /* =========================================================
       CUSTOMER APIs
    ========================================================= */

    /**
     * Get all notifications for logged-in user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                notificationService.getNotificationsForUser(userId)
        );
    }

    /**
     * Get unread notification count (🔔 badge)
     */
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @PathVariable Long userId
    ) {
        long count = notificationService.countUnread(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Mark notification as read
     */
    @PutMapping("/{notificationId}/read/{userId}")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long notificationId,
            @PathVariable Long userId
    ) {
        notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok().build();
    }

    /* =========================================================
       ADMIN APIs
    ========================================================= */

    /**
     * Admin: Send broadcast notification to ALL users
     */
    @PostMapping("/admin/broadcast")
    public ResponseEntity<Notification> sendBroadcast(
            @RequestBody Map<String, String> body
    ) {
        String title = body.get("title");
        String message = body.get("message");

        NotificationType type = NotificationType.valueOf(
                body.getOrDefault("type", "INFO")
        );

        // ✅ FIXED: type goes into service and is saved in DB
        Notification notification =
                notificationService.sendBroadcast(title, message, type);

        return ResponseEntity.ok(notification);
    }

    /**
     * Admin: Send notification to ONE user
     */
    @PostMapping("/admin/user/{userId}")
    public ResponseEntity<Notification> sendToUser(
            @PathVariable Long userId,
            @RequestBody Map<String, String> body
    ) {
        String title = body.get("title");
        String message = body.get("message");

        NotificationType type = NotificationType.valueOf(
                body.getOrDefault("type", "INFO")
        );

        // ✅ FIXED: type goes into service and is saved in DB
        Notification notification =
                notificationService.sendToUser(userId, title, message, type);

        return ResponseEntity.ok(notification);
    }

    /**
     * Admin: View all notifications
     */
    @GetMapping("/admin/all")
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(
                notificationService.getAllNotifications()
        );
    }
}
