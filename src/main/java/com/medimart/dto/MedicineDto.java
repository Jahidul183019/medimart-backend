package com.medimart.dto;

import com.medimart.model.Medicine;
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

    private double price;
    private double buyPrice;
    private int quantity;

    private String expiryDate;
    private String imagePath;

    // discount fields
    private String discountType;     // "PERCENT" or "FLAT"
    private double discountValue;
    private boolean discountActive;
    private String discountStart;
    private String discountEnd;

    // computed field (DTO only)
    private double finalPrice;

    /* ==========================
       ENTITY → DTO
       ========================== */
    public static MedicineDto fromEntity(Medicine m) {
        MedicineDto dto = new MedicineDto();

        dto.setId(m.getId());
        dto.setName(m.getName());
        dto.setCategory(m.getCategory());

        dto.setPrice(m.getPrice());
        dto.setBuyPrice(m.getBuyPrice());
        dto.setQuantity(m.getQuantity());

        dto.setExpiryDate(m.getExpiryDate());
        dto.setImagePath(m.getImagePath());

        dto.setDiscountType(m.getDiscountType());
        dto.setDiscountValue(m.getDiscountValue());
        dto.setDiscountActive(m.isDiscountActive());
        dto.setDiscountStart(m.getDiscountStart());
        dto.setDiscountEnd(m.getDiscountEnd());

        dto.setFinalPrice(calculateFinalPrice(m));

        return dto;
    }

    /* ==========================
       FINAL PRICE CALC
       ========================== */
    private static double calculateFinalPrice(Medicine m) {
        if (!m.isDiscountActive()) {
            return round2(m.getPrice());
        }

        double discount = 0.0;

        if ("PERCENT".equalsIgnoreCase(m.getDiscountType())) {
            discount = (m.getPrice() * m.getDiscountValue()) / 100.0;
        } else if ("FLAT".equalsIgnoreCase(m.getDiscountType())) {
            discount = m.getDiscountValue();
        }

        discount = Math.min(discount, m.getPrice());
        return round2(m.getPrice() - discount);
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
