package com.medimart.controller;

import com.medimart.dto.MedicineDto;
import com.medimart.model.Medicine;
import com.medimart.service.MedicineService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/medicines")
@CrossOrigin(origins = "*")
public class MedicineController {

    private final MedicineService medicineService;

    @Value("${medimart.upload-dir:uploads}")
    private String uploadDir;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    /* ==========================
       GET
       ========================== */

    @GetMapping
    public List<MedicineDto> getAll() {
        return medicineService.getAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicineDto> getOne(@PathVariable Long id) {
        return medicineService.getById(id)
                .map(m -> ResponseEntity.ok(toDto(m)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /* ==========================
       CREATE (JSON – DEFAULT)
       ========================== */

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@RequestBody Medicine med) {

        // Box values so it compiles whether entity uses primitive or wrapper
        Double price = med.getPrice();
        Integer qty = med.getQuantity();
        Double buy = med.getBuyPrice();

        // basic validation
        if (price == null || price <= 0)
            return ResponseEntity.badRequest().body("Price must be > 0");

        if (qty == null || qty < 0) med.setQuantity(0);

        if (buy == null || buy < 0) med.setBuyPrice(0.0);

        if (med.getBuyPrice() > med.getPrice())
            return ResponseEntity.badRequest().body("Buy Price cannot exceed Selling Price");

        normalizeDiscount(med);

        Medicine saved = medicineService.create(med);
        return ResponseEntity.ok(toDto(saved));
    }

    /* ==========================
       CREATE (MULTIPART – IMAGE)
       ========================== */

    @PostMapping(path = "/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createMultipart(
            @RequestParam String name,
            @RequestParam String category,
            @RequestParam double price,
            @RequestParam(required = false) Double buyPrice,
            @RequestParam int quantity,
            @RequestParam String expiryDate,

            @RequestParam(required = false) Boolean discountActive,
            @RequestParam(required = false) String discountType,
            @RequestParam(required = false) Double discountValue,
            @RequestParam(required = false) String discountStart,
            @RequestParam(required = false) String discountEnd,

            @RequestPart(required = false) MultipartFile image
    ) throws IOException {

        if (price <= 0)
            return ResponseEntity.badRequest().body("Price must be > 0");

        if (quantity < 0)
            return ResponseEntity.badRequest().body("Quantity must be >= 0");

        if (buyPrice != null && buyPrice < 0)
            return ResponseEntity.badRequest().body("Buy Price must be >= 0");

        if (buyPrice != null && buyPrice > price)
            return ResponseEntity.badRequest().body("Buy Price cannot exceed Selling Price");

        Medicine med = new Medicine();
        med.setName(name);
        med.setCategory(category);
        med.setPrice(price);
        med.setBuyPrice(buyPrice != null ? buyPrice : 0.0);
        med.setQuantity(quantity);
        med.setExpiryDate(expiryDate);

        applyDiscountFields(med, discountActive, discountType, discountValue, discountStart, discountEnd);

        if (image != null && !image.isEmpty()) {
            med.setImagePath(storeImageFile(image));
        }

        Medicine saved = medicineService.create(med);
        return ResponseEntity.ok(toDto(saved));
    }

    /* ==========================
       UPDATE (JSON – DEFAULT)
       ========================== */

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Medicine updated) {

        // if price is missing in JSON, it may default to 0 for primitives – protect that
        Double price = updated.getPrice();
        if (price != null && price <= 0) {
            return ResponseEntity.badRequest().body("Price must be > 0");
        }

        normalizeDiscount(updated);

        return medicineService.update(id, updated)
                .map(m -> ResponseEntity.ok(toDto(m)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /* ==========================
       UPDATE (MULTIPART – IMAGE)
       ========================== */

    @PutMapping(path = "/{id}/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateMultipart(
            @PathVariable Long id,

            @RequestParam String name,
            @RequestParam String category,
            @RequestParam double price,
            @RequestParam(required = false) Double buyPrice,
            @RequestParam int quantity,
            @RequestParam String expiryDate,

            @RequestParam(required = false) Boolean discountActive,
            @RequestParam(required = false) String discountType,
            @RequestParam(required = false) Double discountValue,
            @RequestParam(required = false) String discountStart,
            @RequestParam(required = false) String discountEnd,

            @RequestPart(required = false) MultipartFile image
    ) throws IOException {

        Optional<Medicine> opt = medicineService.getById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        if (price <= 0)
            return ResponseEntity.badRequest().body("Price must be > 0");

        if (quantity < 0)
            return ResponseEntity.badRequest().body("Quantity must be >= 0");

        if (buyPrice != null && buyPrice < 0)
            return ResponseEntity.badRequest().body("Buy Price must be >= 0");

        if (buyPrice != null && buyPrice > price)
            return ResponseEntity.badRequest().body("Buy Price cannot exceed Selling Price");

        Medicine existing = opt.get();

        existing.setName(name);
        existing.setCategory(category);
        existing.setPrice(price);
        existing.setBuyPrice(buyPrice != null ? buyPrice : existing.getBuyPrice());
        existing.setQuantity(quantity);
        existing.setExpiryDate(expiryDate);

        applyDiscountFields(existing, discountActive, discountType, discountValue, discountStart, discountEnd);

        if (image != null && !image.isEmpty()) {
            existing.setImagePath(storeImageFile(image));
        }

        return medicineService.update(id, existing)
                .map(m -> ResponseEntity.ok(toDto(m)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /* ==========================
       DELETE
       ========================== */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        medicineService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /* ==========================
       DISCOUNT HELPERS (COMPILER-SAFE)
       ========================== */

    private void normalizeDiscount(Medicine m) {
        // Works whether isDiscountActive() returns boolean OR Boolean
        Boolean activeObj = m.isDiscountActive();
        boolean active = Boolean.TRUE.equals(activeObj);

        Double priceObj = m.getPrice();
        double price = (priceObj != null ? priceObj : 0.0);

        if (!active) {
            m.setDiscountActive(false);
            m.setDiscountType(null);
            m.setDiscountValue(0.0);
            m.setDiscountStart(null);
            m.setDiscountEnd(null);
            return;
        }

        String type = (m.getDiscountType() == null ? "PERCENT" : m.getDiscountType().toUpperCase());
        if (!type.equals("PERCENT") && !type.equals("FLAT")) type = "PERCENT";

        Double valObj = m.getDiscountValue();
        double val = Math.max(0.0, valObj != null ? valObj : 0.0);

        if (type.equals("PERCENT") && val > 100) val = 100;
        if (type.equals("FLAT") && val > price) val = price;

        m.setDiscountActive(true);
        m.setDiscountType(type);
        m.setDiscountValue(val);
    }

    private void applyDiscountFields(
            Medicine m,
            Boolean active,
            String type,
            Double value,
            String start,
            String end
    ) {
        m.setDiscountActive(active != null && active);
        m.setDiscountType(type);
        m.setDiscountValue(value != null ? value : 0.0);
        m.setDiscountStart(start);
        m.setDiscountEnd(end);

        normalizeDiscount(m);
    }

    /* ==========================
       DTO + FINAL PRICE (SAFE)
       ========================== */

    private MedicineDto toDto(Medicine m) {
        MedicineDto dto = new MedicineDto();
        dto.setId(m.getId());
        dto.setName(m.getName());
        dto.setCategory(m.getCategory());

        dto.setPrice(m.getPrice());
        dto.setBuyPrice(m.getBuyPrice());
        dto.setQuantity(m.getQuantity());
        dto.setExpiryDate(m.getExpiryDate());
        dto.setImagePath(m.getImagePath());

        Boolean activeObj = m.isDiscountActive();
        dto.setDiscountActive(Boolean.TRUE.equals(activeObj));
        dto.setDiscountType(m.getDiscountType());
        dto.setDiscountValue(m.getDiscountValue());
        dto.setDiscountStart(m.getDiscountStart());
        dto.setDiscountEnd(m.getDiscountEnd());

        dto.setFinalPrice(calcFinalPrice(m));
        return dto;
    }

    private double calcFinalPrice(Medicine m) {
        Double priceObj = m.getPrice();
        double price = (priceObj != null ? priceObj : 0.0);

        boolean active = Boolean.TRUE.equals(m.isDiscountActive());
        if (!active) return round2(price);

        Double discValObj = m.getDiscountValue();
        double discVal = (discValObj != null ? discValObj : 0.0);

        String type = (m.getDiscountType() != null ? m.getDiscountType() : "PERCENT");

        double discount = 0.0;
        if ("PERCENT".equalsIgnoreCase(type)) {
            discount = (price * discVal) / 100.0;
        } else if ("FLAT".equalsIgnoreCase(type)) {
            discount = discVal;
        }

        discount = Math.min(discount, price);
        return round2(price - discount);
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private String storeImageFile(MultipartFile image) throws IOException {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String original = image.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf('.'))
                : "";

        String filename = "med_" + UUID.randomUUID() + ext;
        Path target = uploadPath.resolve(filename);

        Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return uploadDir + "/" + filename;
    }
}
