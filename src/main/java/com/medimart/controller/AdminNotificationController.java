package com.medimart.controller;

import com.medimart.model.Notification;
import com.medimart.model.NotificationType;
import com.medimart.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
@CrossOrigin(origins = "*")
public class AdminNotificationController {

    private final NotificationService notificationService;

    public AdminNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<?> send(@RequestBody Map<String, Object> body) {
        String title = String.valueOf(body.getOrDefault("title", "")).trim();
        String message = String.valueOf(body.getOrDefault("message", "")).trim();

        if (title.isEmpty() || message.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Title and message are required"));
        }

        NotificationType type;
        try {
            type = NotificationType.valueOf(
                    String.valueOf(body.getOrDefault("type", "INFO")).trim().toUpperCase()
            );
        } catch (Exception e) {
            type = NotificationType.INFO;
        }

        Object userIdObj = body.get("userId");
        Notification notification;

        if (userIdObj == null || String.valueOf(userIdObj).trim().isEmpty()) {
            notification = notificationService.sendBroadcast(title, message, type);
        } else {
            try {
                Long userId = Long.parseLong(String.valueOf(userIdObj).trim());
                notification = notificationService.sendToUser(userId, title, message, type);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid userId"));
            }
        }

        return ResponseEntity.ok(notification);
    }

    @PostMapping("/broadcast")
    public ResponseEntity<?> broadcast(@RequestBody Map<String, String> body) {
        String title = String.valueOf(body.getOrDefault("title", "")).trim();
        String message = String.valueOf(body.getOrDefault("message", "")).trim();

        if (title.isEmpty() || message.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Title and message are required"));
        }

        NotificationType type;
        try {
            type = NotificationType.valueOf(body.getOrDefault("type", "INFO").trim().toUpperCase());
        } catch (Exception e) {
            type = NotificationType.INFO;
        }

        return ResponseEntity.ok(notificationService.sendBroadcast(title, message, type));
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<?> notifyUser(@PathVariable Long userId, @RequestBody Map<String, String> body) {
        String title = String.valueOf(body.getOrDefault("title", "")).trim();
        String message = String.valueOf(body.getOrDefault("message", "")).trim();

        if (title.isEmpty() || message.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Title and message are required"));
        }

        NotificationType type;
        try {
            type = NotificationType.valueOf(body.getOrDefault("type", "INFO").trim().toUpperCase());
        } catch (Exception e) {
            type = NotificationType.INFO;
        }

        return ResponseEntity.ok(notificationService.sendToUser(userId, title, message, type));
    }
}
