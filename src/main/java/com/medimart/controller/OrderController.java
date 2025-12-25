// src/main/java/com/medimart/controller/OrderController.java
package com.medimart.controller;

import com.medimart.dto.CancelOrderRequest;
import com.medimart.dto.CreateOrderRequest;
import com.medimart.dto.OrderResponse;
import com.medimart.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /* ============================
       CUSTOMER: CREATE ORDER
       ============================ */
    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody CreateOrderRequest req) {
        OrderResponse response = orderService.createOrder(req);
        return ResponseEntity.ok(response);
    }

    /* ============================
       CUSTOMER: ORDER HISTORY
       ============================ */
    @GetMapping("/history/{userId}")
    public List<OrderResponse> getHistory(@PathVariable Long userId) {
        return orderService.getHistory(userId);
    }

    /* ============================
       ADMIN: LIST CANCEL REQUESTS
       (keep this BEFORE single-order mapping for clarity)
       ============================ */
    @GetMapping("/cancel-requests")
    public List<OrderResponse> getCancelRequests() {
        return orderService.getCancelRequests();
    }

    /* ============================
       CUSTOMER/ADMIN: SINGLE ORDER
       ✅ digits-only to avoid conflict with /cancel-requests
       ============================ */
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<OrderResponse> getOne(@PathVariable Long id) {
        return orderService.getOrderDtoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /* ============================
       CUSTOMER: REQUEST CANCEL (NOT instant cancel)
       ============================ */
    @PostMapping("/{orderId}/cancel-request")
    public ResponseEntity<OrderResponse> requestCancel(
            @PathVariable Long orderId,
            @RequestBody CancelOrderRequest body
    ) {
        String reason = body.getReason() == null ? "" : body.getReason().trim();

        if (body.getUserId() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (reason.length() < 5) {
            return ResponseEntity.badRequest().build();
        }

        OrderResponse resp = orderService.requestCancel(orderId, body.getUserId(), reason);
        return ResponseEntity.ok(resp);
    }

    /* ============================
       ADMIN: LIST ALL ORDERS
       ============================ */
    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }

    /* ============================
       ADMIN: APPROVE CANCEL REQUEST
       ============================ */
    @PatchMapping("/{orderId}/cancel/approve")
    public ResponseEntity<OrderResponse> approveCancel(
            @PathVariable Long orderId,
            @RequestParam Long adminId
    ) {
        OrderResponse updated = orderService.approveCancel(orderId, adminId);
        return ResponseEntity.ok(updated);
    }

    /* ============================
       ADMIN: REJECT CANCEL REQUEST
       ============================ */
    @PatchMapping("/{orderId}/cancel/reject")
    public ResponseEntity<OrderResponse> rejectCancel(
            @PathVariable Long orderId,
            @RequestParam Long adminId
    ) {
        OrderResponse updated = orderService.rejectCancel(orderId, adminId);
        return ResponseEntity.ok(updated);
    }

    /* ============================
       ADMIN: UPDATE ORDER STATUS
       ============================ */
    @PatchMapping("/{id:\\d+}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        OrderResponse updated = orderService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }
}
