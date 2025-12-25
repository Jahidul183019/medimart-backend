package com.medimart.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ============================
       USER | CUSTOMER INFO
       ============================ */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_address")
    private String customerAddress;

    @Column(name = "customer_phone")
    private String customerPhone;

    /* ============================
       ORDER STATUS
       ============================ */
    // PENDING, PAID, SHIPPED, DELIVERED, CANCEL_REQUESTED, CANCELLED, REJECTED_CANCEL
    @Column(name = "status")
    private String status;

    @Column(name = "total_amount")
    private Double totalAmount;

    /* ============================
       TIMESTAMPS
       ============================ */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /* ============================
       CANCELLATION FLOW
       ============================ */

    // When customer requests cancellation (waiting admin approval)
    @Column(name = "cancel_requested_at")
    private LocalDateTime cancelRequestedAt;

    // Customer-provided reason
    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    // When admin approves and order becomes CANCELLED
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // Admin review metadata (approve/reject time + reviewer)
    @Column(name = "cancel_reviewed_at")
    private LocalDateTime cancelReviewedAt;

    @Column(name = "cancel_reviewed_by")
    private Long cancelReviewedBy;

    /* ============================
       ORDER ITEMS
       ============================ */
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();
}
