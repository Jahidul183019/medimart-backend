package com.medimart.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * If null → broadcast notification
     * If not null → personal notification
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    /**
     * INFO, WARNING, OFFER, MEDICINE, SYSTEM
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type;

    @Column(nullable = false)
    private boolean readStatus = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /* =========================
       CONSTRUCTORS
    ========================== */

    public Notification() {
        this.createdAt = LocalDateTime.now();
    }

    public Notification(User user, String title, String message, NotificationType type) {
        this.user = user;           // null = broadcast
        this.title = title;
        this.message = message;
        this.type = type;
        this.readStatus = false;
        this.createdAt = LocalDateTime.now();
    }

    /* =========================
       GETTERS & SETTERS
    ========================== */

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public boolean isReadStatus() {
        return readStatus;
    }

    public void setReadStatus(boolean readStatus) {
        this.readStatus = readStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /* =========================
       HELPER METHODS
    ========================== */

    public void markAsRead() {
        this.readStatus = true;
    }

    public boolean isBroadcast() {
        return this.user == null;
    }
}
