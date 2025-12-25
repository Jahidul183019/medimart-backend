package com.medimart.controller;

import com.medimart.service.OrderService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://medimart-frontend-coral.vercel.app"
})
public class InvoiceController {

    private final OrderService orderService;

    public InvoiceController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{orderId}/invoice")
    public ResponseEntity<byte[]> getInvoice(@PathVariable Long orderId) {

        byte[] pdf = orderService.generateInvoicePdf(orderId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=invoice_" + orderId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
