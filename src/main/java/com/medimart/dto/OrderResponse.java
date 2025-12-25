package com.medimart.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderResponse {

    public static class OrderItemView {
        private Long id;
        private Long medicineId;
        private String medicineName;
        private Integer quantity;     // safer than int (null-safe)
        private Double unitPrice;     // safer than double (null-safe)

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getMedicineId() { return medicineId; }
        public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

        public String getMedicineName() { return medicineName; }
        public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public Double getUnitPrice() { return unitPrice; }
        public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
    }

    private Long id;
    private Long userId;

    private String userEmail;

    private String customerName;
    private String customerAddress;
    private String customerPhone;

    private String status;

    private Double totalAmount;      // safer than double
    private String currency = "BDT";

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String cancelReason;
    private LocalDateTime cancelRequestedAt;
    private LocalDateTime cancelledAt;

    private List<OrderItemView> items = new ArrayList<>();  // ✅ never null

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

    public LocalDateTime getCancelRequestedAt() { return cancelRequestedAt; }
    public void setCancelRequestedAt(LocalDateTime cancelRequestedAt) { this.cancelRequestedAt = cancelRequestedAt; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public List<OrderItemView> getItems() { return items; }
    public void setItems(List<OrderItemView> items) {
        this.items = (items == null) ? new ArrayList<>() : items;  // ✅ never null
    }
}
