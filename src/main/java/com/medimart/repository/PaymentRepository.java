// src/main/java/com/medimart/repository/PaymentRepository.java
package com.medimart.repository;

import com.medimart.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
