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
    private double discountValue;  // percentage (10) or flat (50)

    @Column(name = "discount_active")
    private boolean discountActive;

    @Column(name = "discount_start")
    private String discountStart;

    @Column(name = "discount_end")
    private String discountEnd;

    // ✅ Explicit method so compiler will ALWAYS find it (even if Lombok fails)
    public boolean isDiscountActive() {
        return discountActive;
    }

    // ✅ Optional: calculate discounted selling price
    public double getFinalPrice() {
        if (!isDiscountActive()) return price;

        String type = (discountType == null) ? "" : discountType.trim().toUpperCase();

        if ("PERCENT".equals(type)) {
            double pct = discountValue;     // e.g. 10 means 10%
            if (pct <= 0) return price;
            double discounted = price - (price * (pct / 100.0));
            return Math.max(0.0, discounted);
        }

        if ("FLAT".equals(type)) {
            double flat = discountValue;    // e.g. 50 means 50 টাকা off
            if (flat <= 0) return price;
            return Math.max(0.0, price - flat);
        }

        return price;
    }
}
