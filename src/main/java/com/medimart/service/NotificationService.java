package com.medimart.service;

import com.medimart.model.Notification;
import com.medimart.model.NotificationType;
import com.medimart.model.User;
import com.medimart.repository.NotificationRepository;
import com.medimart.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public List<Notification> getNotificationsForUser(Long userId) {
        User user = getUserOrThrow(userId);
        return notificationRepository.findAllForUser(user);
    }

    public long countUnread(Long userId) {
        User user = getUserOrThrow(userId);
        return notificationRepository.countUnreadForUser(user);
    }

    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (notification.getUser() != null && !notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    public Notification sendBroadcast(String title, String message, NotificationType type) {
        String t = title == null ? "" : title.trim();
        String m = message == null ? "" : message.trim();
        if (t.isEmpty() || m.isEmpty()) throw new RuntimeException("Title and message are required");
        if (type == null) type = NotificationType.INFO;

        Notification notification = new Notification(null, t, m, type);
        return notificationRepository.save(notification);
    }

    public Notification sendToUser(Long userId, String title, String message, NotificationType type) {
        User user = getUserOrThrow(userId);

        String t = title == null ? "" : title.trim();
        String m = message == null ? "" : message.trim();
        if (t.isEmpty() || m.isEmpty()) throw new RuntimeException("Title and message are required");
        if (type == null) type = NotificationType.INFO;

        Notification notification = new Notification(user, t, m, type);
        return notificationRepository.save(notification);
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
