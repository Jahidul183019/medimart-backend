// src/main/java/com/medimart/service/PaymentService.java
package com.medimart.service;

import com.medimart.dto.CardPaymentRequest;
import com.medimart.dto.CardPaymentResponse;
import com.medimart.model.Payment;
import com.medimart.model.PaymentStatus;
import com.medimart.model.Order;
import com.medimart.repository.PaymentRepository;
import com.medimart.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final OrderRepository orderRepo;

    public PaymentService(PaymentRepository paymentRepo,
                          OrderRepository orderRepo) {
        this.paymentRepo = paymentRepo;
        this.orderRepo = orderRepo;
    }

    public CardPaymentResponse chargeCard(CardPaymentRequest dto) {

        // 1) Find order
        Order order = orderRepo.findById(dto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 2) Use server-side total as single source of truth
        double orderTotal = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;

        // (Optional) if you want to just log mismatch instead of failing:
        // Double requested = dto.getAmount();
        // System.out.println("Requested amount = " + requested + ", order total = " + orderTotal);

        // 3) Fake card validation (demo only)
        String digits = dto.getCardNumber().replaceAll("\\s+", "");
        boolean approved = digits.length() == 16 && digits.endsWith("4"); // cards ending in 4 succeed

        // 4) Build Payment entity
        Payment p = new Payment();
        p.setOrderId(order.getId());
        p.setAmount(orderTotal); // ✅ always use DB amount
        String currency = (dto.getCurrency() != null && !dto.getCurrency().isBlank())
                ? dto.getCurrency()
                : "BDT";
        p.setCurrency(currency);
        p.setCardHolderName(dto.getNameOnCard());
        p.setCardLast4(digits.length() >= 4 ? digits.substring(digits.length() - 4) : digits);
        p.setPaymentTime(LocalDateTime.now());
        p.setTransactionId(UUID.randomUUID().toString());

        if (approved) {
            p.setStatus(PaymentStatus.SUCCESS);
            order.setStatus("PAID");              // or OrderStatus.PAID enum
        } else {
            p.setStatus(PaymentStatus.FAILED);
            order.setStatus("PAYMENT_FAILED");    // adapt if you have different statuses
        }

        // 5) Persist
        paymentRepo.save(p);
        orderRepo.save(order);

        // 6) Build response DTO
        return new CardPaymentResponse(
                approved,
                approved ? "Payment successful" : "Payment declined",
                p.getId(),
                order.getId(),
                p.getStatus()
        );
    }
}
