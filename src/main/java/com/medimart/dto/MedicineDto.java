package com.medimart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineDto {

    private Long id;
    private String name;
    private String category;

    // base selling price (MRP)
    private Double price;

    // cost price (admin-only analytics)
    private Double buyPrice;

    private Integer quantity;
    private String expiryDate;
    private String imagePath;

    // discount fields
    private String discountType;     // "PERCENT" or "FLAT"
    private Double discountValue;
    private Boolean discountActive;
    private String discountStart;
    private String discountEnd;

    // computed field (frontend uses this)
    private Double finalPrice;
}
