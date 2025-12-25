package com.medimart.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    private Long userId;           // from authService (profileId / id)

    private String customerName;
    private String customerAddress;
    private String customerPhone;
    private String paymentMethod;  // e.g. "CASH", "CARD"

    private List<Item> items;

    @Data
    public static class Item {
        private Long medicineId;
        private int quantity;
        private double price;      // unit price from frontend
    }
}
