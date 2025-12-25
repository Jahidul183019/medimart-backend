package com.medimart.dto;

public class TopSellingItemDTO {
    private Long medicineId;
    private String medicineName;
    private Long totalQty;
    private Double totalRevenue;

    public TopSellingItemDTO(Long medicineId, String medicineName, Number totalQty, Number totalRevenue) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.totalQty = totalQty == null ? 0L : totalQty.longValue();
        this.totalRevenue = totalRevenue == null ? 0.0 : totalRevenue.doubleValue();
    }

    public Long getMedicineId() { return medicineId; }
    public String getMedicineName() { return medicineName; }
    public Long getTotalQty() { return totalQty; }
    public Double getTotalRevenue() { return totalRevenue; }
}
