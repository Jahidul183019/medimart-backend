package com.medimart.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "medicines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private double price;

    @Column(name = "buy_price", nullable = false)
    private double buyPrice;

    private int quantity;

    @Column(name = "expiry_date")
    private String expiryDate;

    @Column(name = "image_path")
    private String imagePath;

    /* =======================
       Discount configuration
       ======================= */

    @Column(name = "discount_type")
    private String discountType;   // PERCENT or FLAT

    @Column(name = "discount_value")
    private double discountValue;

    // ✅ FIX: Boolean instead of boolean (NULL-safe)
    @Column(name = "discount_active")
    private Boolean discountActive;

    @Column(name = "discount_start")
    private String discountStart;

    @Column(name = "discount_end")
    private String discountEnd;

    /* =======================
       SAFE GETTERS
       ======================= */

    public boolean isDiscountActive() {
        return Boolean.TRUE.equals(discountActive);
    }

    /* =======================
       FINAL PRICE (OPTIONAL)
       ======================= */
    public double getFinalPrice() {
        if (!isDiscountActive()) return price;

        String type = discountType == null
                ? ""
                : discountType.trim().toUpperCase();

        double discount = 0.0;

        if ("PERCENT".equals(type)) {
            discount = price * (discountValue / 100.0);
        } else if ("FLAT".equals(type)) {
            discount = discountValue;
        }

        discount = Math.min(discount, price);
        return Math.max(0.0, price - discount);
    }
}
