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

    private Double price;
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

    // computed (DTO only)
    private Double finalPrice;

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

        dto.setFinalPrice(round2(m.getFinalPrice())); // uses entity helper
        return dto;
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
