package com.medimart.dto;

import java.time.Instant;

public record OrderSummaryDto(
        Long id,
        String customerName,
        Double totalAmount,
        String status,
        Instant createdAt
) {}
