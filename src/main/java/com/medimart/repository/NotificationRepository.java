package com.medimart.repository;

import com.medimart.model.Notification;
import com.medimart.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /* =========================
       CUSTOMER SIDE
    ========================== */

    /**
     * Get all notifications for a user:
     *  - personal notifications
     *  - broadcast notifications (user IS NULL)
     */
    @Query("""
        SELECT n FROM Notification n
        WHERE n.user IS NULL OR n.user = :user
        ORDER BY n.createdAt DESC
    """)
    List<Notification> findAllForUser(@Param("user") User user);

    /**
     * Unread notifications for a user
     */
    @Query("""
        SELECT n FROM Notification n
        WHERE (n.user IS NULL OR n.user = :user)
          AND n.readStatus = false
        ORDER BY n.createdAt DESC
    """)
    List<Notification> findUnreadForUser(@Param("user") User user);

    /**
     * Count unread notifications (for badge 🔔)
     */
    @Query("""
        SELECT COUNT(n) FROM Notification n
        WHERE (n.user IS NULL OR n.user = :user)
          AND n.readStatus = false
    """)
    long countUnreadForUser(@Param("user") User user);


    /* =========================
       ADMIN SIDE
    ========================== */

    /**
     * All notifications sent by admin
     */
    List<Notification> findAllByOrderByCreatedAtDesc();

    /**
     * Notifications for a specific user only
     */
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
}
