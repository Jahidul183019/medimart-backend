// src/main/java/com/medimart/dto/CardPaymentResponse.java
package com.medimart.dto;

import com.medimart.model.PaymentStatus;

public class CardPaymentResponse {
    private boolean success;
    private String message;
    private Long paymentId;
    private Long orderId;
    private PaymentStatus status;

    public CardPaymentResponse() {}

    public CardPaymentResponse(boolean success, String message,
                               Long paymentId, Long orderId,
                               PaymentStatus status) {
        this.success = success;
        this.message = message;
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.status = status;
    }

    // getters & setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
}
