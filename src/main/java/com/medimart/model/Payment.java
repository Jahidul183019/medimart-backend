// src/main/java/com/medimart/model/Payment.java
package com.medimart.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    private Double amount;

    private String currency;

    private String cardHolderName;

    private String cardLast4;

    private LocalDateTime paymentTime;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String transactionId;

    // getters & setters

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }

    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Double getAmount() { return amount; }

    public void setAmount(Double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }

    public void setCurrency(String currency) { this.currency = currency; }

    public String getCardHolderName() { return cardHolderName; }

    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }

    public String getCardLast4() { return cardLast4; }

    public void setCardLast4(String cardLast4) { this.cardLast4 = cardLast4; }

    public LocalDateTime getPaymentTime() { return paymentTime; }

    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }

    public PaymentStatus getStatus() { return status; }

    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getTransactionId() { return transactionId; }

    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}
