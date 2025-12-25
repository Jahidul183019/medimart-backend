package com.medimart.repository;

import com.medimart.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findByStatusOrderByCreatedAtDesc(String status);

    List<Order> findByStatusOrderByCancelRequestedAtDesc(String status);

    @Query("""
        SELECT DISTINCT o
        FROM Order o
        LEFT JOIN FETCH o.items i
        LEFT JOIN FETCH i.medicine
        WHERE o.id = :id
    """)
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    @Query("""
        SELECT DISTINCT o
        FROM Order o
        LEFT JOIN FETCH o.items i
        LEFT JOIN FETCH i.medicine
        WHERE o.status = :status
        ORDER BY o.cancelRequestedAt DESC, o.createdAt DESC
    """)
    List<Order> findCancelRequestsWithItems(@Param("status") String status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o")
    Double sumTotalAmount();

    long countByStatus(String status);

    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.createdAt >= :from
    """)
    Double sumRevenueFrom(@Param("from") LocalDateTime from);
}
