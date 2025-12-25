package com.medimart.repository;

import com.medimart.dto.TopSellingItemDTO;
import com.medimart.model.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder_Id(Long orderId);

    @Query("""
        SELECT COALESCE(SUM(oi.quantity), 0)
        FROM OrderItem oi
        JOIN oi.order o
        WHERE o.status IN ('DELIVERED', 'PAID')
    """)
    Long sumTotalUnitsSold();

    @Query("""
        SELECT COALESCE(SUM(
            1.0 * oi.quantity * COALESCE(oi.finalPricePerUnit, oi.unitPrice, 0)
        ), 0)
        FROM OrderItem oi
        JOIN oi.order o
        WHERE o.status IN ('DELIVERED', 'PAID')
    """)
    Double sumTotalRevenueFromItems();

    @Query("""
        SELECT COALESCE(SUM(
            1.0 * oi.quantity * COALESCE(oi.buyPriceAtSale, oi.medicine.buyPrice, 0)
        ), 0)
        FROM OrderItem oi
        JOIN oi.order o
        WHERE o.status IN ('DELIVERED', 'PAID')
    """)
    Double sumTotalCostFromItems();

    // TOP SELLING
    @Query("""
        SELECT new com.medimart.dto.TopSellingItemDTO(
            m.id,
            m.name,
            SUM(oi.quantity),
            SUM(1.0 * oi.quantity * COALESCE(oi.finalPricePerUnit, oi.unitPrice, 0))
        )
        FROM OrderItem oi
        JOIN oi.medicine m
        JOIN oi.order o
        WHERE o.status IN ('DELIVERED', 'PAID')
        GROUP BY m.id, m.name
        ORDER BY SUM(oi.quantity) DESC
    """)
    List<TopSellingItemDTO> findTopSelling(Pageable pageable);
}
