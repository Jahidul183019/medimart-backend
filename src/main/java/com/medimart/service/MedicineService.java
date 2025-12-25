package com.medimart.service;

import com.medimart.dto.MedicineDto;
import com.medimart.model.Medicine;
import com.medimart.repository.MedicineRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MedicineService(MedicineRepository medicineRepository,
                           SimpMessagingTemplate messagingTemplate) {
        this.medicineRepository = medicineRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public List<Medicine> getAll() {
        return medicineRepository.findAll();
    }

    public Optional<Medicine> getById(Long id) {
        return medicineRepository.findById(id);
    }

    public Medicine create(Medicine medicine) {
        sanitizeDiscount(medicine);
        Medicine saved = medicineRepository.save(medicine);
        broadcastInventory();
        return saved;
    }

    public Optional<Medicine> update(Long id, Medicine updated) {
        return medicineRepository.findById(id).map(existing -> {

            existing.setName(updated.getName());
            existing.setCategory(updated.getCategory());
            existing.setPrice(updated.getPrice());
            existing.setBuyPrice(updated.getBuyPrice());
            existing.setQuantity(updated.getQuantity());
            existing.setExpiryDate(updated.getExpiryDate());

            if (updated.getImagePath() != null && !updated.getImagePath().isBlank()) {
                existing.setImagePath(updated.getImagePath());
            }

            existing.setDiscountType(updated.getDiscountType());
            existing.setDiscountValue(updated.getDiscountValue());
            existing.setDiscountActive(updated.isDiscountActive());
            existing.setDiscountStart(updated.getDiscountStart());
            existing.setDiscountEnd(updated.getDiscountEnd());

            sanitizeDiscount(existing);

            Medicine saved = medicineRepository.save(existing);
            broadcastInventory();
            return saved;
        });
    }

    public void delete(Long id) {
        medicineRepository.deleteById(id);
        broadcastInventory();
    }

    private void sanitizeDiscount(Medicine m) {

        if (!m.isDiscountActive()) {
            m.setDiscountType(null);
            m.setDiscountValue(0.0);
            m.setDiscountStart(null);
            m.setDiscountEnd(null);
            return;
        }

        String type = m.getDiscountType();
        if (type == null || (!type.equalsIgnoreCase("PERCENT") && !type.equalsIgnoreCase("FLAT"))) {
            m.setDiscountType("PERCENT");
        } else {
            m.setDiscountType(type.toUpperCase());
        }

        if (m.getDiscountValue() < 0.0) m.setDiscountValue(0.0);

        if ("PERCENT".equalsIgnoreCase(m.getDiscountType()) && m.getDiscountValue() > 100.0) {
            m.setDiscountValue(100.0);
        }

        // Optional safety for FLAT discount (prevents negative price)
        if ("FLAT".equalsIgnoreCase(m.getDiscountType()) && m.getDiscountValue() > m.getPrice()) {
            m.setDiscountValue(m.getPrice());
        }
    }

    private void broadcastInventory() {
        List<Medicine> all = medicineRepository.findAll();

        List<MedicineDto> payload = all.stream()
                .map(MedicineDto::fromEntity)
                .collect(Collectors.toList());

        messagingTemplate.convertAndSend("/topic/inventory", payload);
    }
}
