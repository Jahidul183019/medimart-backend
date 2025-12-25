package com.medimart.model;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many items belong to one order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    // Many order items can refer to one medicine
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    @Column(nullable = false)
    private int quantity;

    /**
     * Keep for backward compatibility (your existing code may use it).
     * We'll store FINAL per-unit price here as well.
     */
    @Column(name = "unit_price")
    private Double unitPrice;

    /* ============================
       Snapshot fields (for analytics)
       ============================ */

    @Column(name = "buy_price_at_sale")
    private Double buyPriceAtSale;     // cost per unit at checkout time

    @Column(name = "sell_price_at_sale")
    private Double sellPriceAtSale;    // base selling price per unit at checkout time

    @Column(name = "discount_per_unit")
    private Double discountPerUnit;    // discount amount per unit

    @Column(name = "final_price_per_unit")
    private Double finalPricePerUnit;  // final per unit after discount

    // ======== GETTERS & SETTERS ========

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getBuyPriceAtSale() {
        return buyPriceAtSale;
    }

    public void setBuyPriceAtSale(Double buyPriceAtSale) {
        this.buyPriceAtSale = buyPriceAtSale;
    }

    public Double getSellPriceAtSale() {
        return sellPriceAtSale;
    }

    public void setSellPriceAtSale(Double sellPriceAtSale) {
        this.sellPriceAtSale = sellPriceAtSale;
    }

    public Double getDiscountPerUnit() {
        return discountPerUnit;
    }

    public void setDiscountPerUnit(Double discountPerUnit) {
        this.discountPerUnit = discountPerUnit;
    }

    public Double getFinalPricePerUnit() {
        return finalPricePerUnit;
    }

    public void setFinalPricePerUnit(Double finalPricePerUnit) {
        this.finalPricePerUnit = finalPricePerUnit;
    }
}
