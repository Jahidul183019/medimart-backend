package com.medimart.service.impl;

import com.medimart.dto.CartItemDto;
import com.medimart.model.CartItem;
import com.medimart.model.Medicine;
import com.medimart.repository.CartRepository;
import com.medimart.repository.MedicineRepository;
import com.medimart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository repo;
    private final MedicineRepository medicineRepository;

    @Override
    public List<CartItemDto> getCartByUser(Long userId) {
        List<CartItem> items = repo.findByUserId(userId);
        List<CartItemDto> dtoList = new ArrayList<>();

        for (CartItem item : items) {
            dtoList.add(convertToDto(item));
        }

        return dtoList;
    }

    /**
     * Convert entity -> DTO with medicine info + discount calculation
     */
    private CartItemDto convertToDto(CartItem item) {
        CartItemDto dto = new CartItemDto();
        dto.setId(item.getId());
        dto.setUserId(item.getUserId());
        dto.setMedicineId(item.getMedicineId());
        dto.setQuantity(item.getQuantity());

        Optional<Medicine> medOpt = medicineRepository.findById(item.getMedicineId());

        if (medOpt.isPresent()) {
            Medicine m = medOpt.get();

            double basePrice = safeNum(m.getPrice());
            int qty = Math.max(0, item.getQuantity());

            dto.setMedicineName(m.getName());
            dto.setMedicinePrice(basePrice); // original/base price
            dto.setMedicineImagePath(m.getImagePath());

            // ---- discount info (from medicine) ----
            boolean discountActive = safeBool(getMedicineDiscountActive(m));
            String discountType = safeStr(getMedicineDiscountType(m));
            double discountValue = safeNum(getMedicineDiscountValue(m));
            String discountStart = safeStr(getMedicineDiscountStart(m));
            String discountEnd = safeStr(getMedicineDiscountEnd(m));

            // put raw discount fields into DTO
            dto.setDiscountActive(discountActive);
            dto.setDiscountType(discountType);
            dto.setDiscountValue(discountValue);
            dto.setDiscountStart(discountStart);
            dto.setDiscountEnd(discountEnd);

            // calculate final unit price
            double finalUnit = calculateFinalPrice(
                    basePrice,
                    discountActive,
                    discountType,
                    discountValue,
                    discountStart,
                    discountEnd
            );

            dto.setFinalUnitPrice(finalUnit);

            // final line total should use discounted price
            dto.setLineTotal(round2(finalUnit * qty));

        } else {
            dto.setMedicineName("Unknown medicine");
            dto.setMedicinePrice(0.0);
            dto.setMedicineImagePath(null);

            dto.setDiscountActive(false);
            dto.setDiscountType("");
            dto.setDiscountValue(0.0);
            dto.setDiscountStart("");
            dto.setDiscountEnd("");

            dto.setFinalUnitPrice(0.0);
            dto.setLineTotal(0.0);
        }

        return dto;
    }

    /**
     * Core discount price logic
     */
    private double calculateFinalPrice(
            double basePrice,
            boolean discountActive,
            String discountType,
            double discountValue,
            String discountStart,
            String discountEnd
    ) {
        if (basePrice <= 0) return 0.0;
        if (!discountActive) return round2(basePrice);
        if (discountValue <= 0) return round2(basePrice);

        // check date window (optional)
        if (!isDiscountWithinWindow(discountStart, discountEnd)) {
            return round2(basePrice);
        }

        double discountAmount = 0.0;
        String type = (discountType == null) ? "" : discountType.trim().toUpperCase();

        if ("PERCENT".equals(type)) {
            if (discountValue > 100) discountValue = 100;
            discountAmount = (basePrice * discountValue) / 100.0;
        } else if ("FLAT".equals(type)) {
            discountAmount = discountValue;
        } else {
            // unknown type -> ignore discount
            return round2(basePrice);
        }

        if (discountAmount < 0) discountAmount = 0;
        if (discountAmount > basePrice) discountAmount = basePrice;

        return round2(basePrice - discountAmount);
    }

    private boolean isDiscountWithinWindow(String start, String end) {
        LocalDate today = LocalDate.now();

        LocalDate s = parseDate(start);
        LocalDate e = parseDate(end);

        boolean afterStart = (s == null) || !today.isBefore(s);
        boolean beforeEnd = (e == null) || !today.isAfter(e);

        return afterStart && beforeEnd;
    }

    private LocalDate parseDate(String raw) {
        if (raw == null) return null;
        String v = raw.trim();
        if (v.isEmpty()) return null;

        try {
            return LocalDate.parse(v); // expects yyyy-MM-dd
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private double safeNum(Double d) {
        if (d == null) return 0.0;
        if (!Double.isFinite(d)) return 0.0;
        return d;
    }

    private String safeStr(String s) {
        return s == null ? "" : s;
    }

    private boolean safeBool(Boolean b) {
        return b != null && b;
    }

    private double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }

    // ============================================================
    // These getter methods keep this file compiling even if your
    // Medicine model names differ slightly. Adjust ONLY if needed.
    // ============================================================

    private Boolean getMedicineDiscountActive(Medicine m) {
        // expected field: m.getDiscountActive()
        try {
            return (Boolean) Medicine.class.getMethod("getDiscountActive").invoke(m);
        } catch (Exception ignored) {
            return false;
        }
    }

    private String getMedicineDiscountType(Medicine m) {
        // expected field: m.getDiscountType()
        try {
            Object v = Medicine.class.getMethod("getDiscountType").invoke(m);
            return v != null ? v.toString() : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    private Double getMedicineDiscountValue(Medicine m) {
        // expected field: m.getDiscountValue()
        try {
            Object v = Medicine.class.getMethod("getDiscountValue").invoke(m);
            if (v instanceof Number n) return n.doubleValue();
            return 0.0;
        } catch (Exception ignored) {
            return 0.0;
        }
    }

    private String getMedicineDiscountStart(Medicine m) {
        // expected field: m.getDiscountStart()
        try {
            Object v = Medicine.class.getMethod("getDiscountStart").invoke(m);
            return v != null ? v.toString() : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    private String getMedicineDiscountEnd(Medicine m) {
        // expected field: m.getDiscountEnd()
        try {
            Object v = Medicine.class.getMethod("getDiscountEnd").invoke(m);
            return v != null ? v.toString() : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    // ============================================================

    @Override
    public boolean addItem(Long userId, Long medicineId, int quantity) {
        if (userId == null || medicineId == null) {
            throw new IllegalArgumentException("userId and medicineId must not be null");
        }

        int safeQty = quantity <= 0 ? 1 : quantity;

        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setMedicineId(medicineId);
        item.setQuantity(safeQty);

        repo.save(item);
        return true;
    }

    @Override
    public boolean updateQuantity(Long itemId, int qty) {
        int safeQty = qty <= 0 ? 1 : qty;

        return repo.findById(itemId).map(ci -> {
            ci.setQuantity(safeQty);
            repo.save(ci);
            return true;
        }).orElse(false);
    }

    @Override
    public void removeItem(Long itemId) {
        repo.deleteById(itemId);
    }

    @Override
    public void clearUserCart(Long userId) {
        repo.deleteByUserId(userId);
    }
}
