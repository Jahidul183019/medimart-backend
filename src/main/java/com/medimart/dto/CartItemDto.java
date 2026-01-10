package com.medimart.dto;

import lombok.Data;

@Data
public class CartItemDto {

    // existing
    private Long id;
    private Long userId;
    private Long medicineId;

    private String medicineName;

    private double medicinePrice;

    private String medicineImagePath;

    private int quantity;

    private double lineTotal;


    // discounted price per unit (final selling price after discount)
    private double finalUnitPrice;

    // discount info (optional but useful to show badge)
    private boolean discountActive;
    private String discountType;     // "PERCENT" or "FLAT"
    private double discountValue;

    // optional discount window (string is fine since you used string in Medicine expiryDate)
    private String discountStart;    // yyyy-MM-dd or ""
    private String discountEnd;      // yyyy-MM-dd or ""
}
