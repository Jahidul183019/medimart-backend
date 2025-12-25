// src/main/java/com/medimart/controller/PaymentController.java
package com.medimart.controller;

import com.medimart.dto.CardPaymentRequest;
import com.medimart.dto.CardPaymentResponse;
import com.medimart.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:5173") // allow frontend dev
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // POST /api/payments/charge
    @PostMapping("/charge")
    public ResponseEntity<CardPaymentResponse> charge(@RequestBody CardPaymentRequest req) {
        System.out.println("💳 Received payment request: " + req);

        CardPaymentResponse res = paymentService.chargeCard(req);

        return ResponseEntity.ok(res);
    }
}
