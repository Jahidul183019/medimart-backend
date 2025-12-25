// src/main/java/com/medimart/dto/CancelOrderRequest.java
package com.medimart.dto;

public class CancelOrderRequest {
    private Long userId;
    private String reason;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
