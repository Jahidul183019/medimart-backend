package com.medimart.repository;

import com.medimart.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    // add custom queries later if needed
}
