package com.medimart.service;

import com.medimart.dto.AdminAnalyticsOverview;
import com.medimart.dto.CreateOrderRequest;
import com.medimart.dto.OrderResponse;
import com.medimart.dto.OrderResponse.OrderItemView;
import com.medimart.dto.TopSellingItemDTO;
import com.medimart.model.Medicine;
import com.medimart.model.Order;
import com.medimart.model.OrderItem;
import com.medimart.model.User;
import com.medimart.repository.MedicineRepository;
import com.medimart.repository.OrderItemRepository;
import com.medimart.repository.OrderRepository;
import com.medimart.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_SHIPPED = "SHIPPED";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_CANCEL_REQUESTED = "CANCEL_REQUESTED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_REJECTED_CANCEL = "REJECTED_CANCEL";

    private final OrderRepository orderRepository;
    private final MedicineRepository medicineRepository;
    private final InvoicePdfService invoicePdfService;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(
            OrderRepository orderRepository,
            MedicineRepository medicineRepository,
            InvoicePdfService invoicePdfService,
            UserRepository userRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.orderRepository = orderRepository;
        this.medicineRepository = medicineRepository;
        this.invoicePdfService = invoicePdfService;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
    }

    /* =========================
       Create Order (Discount + Snapshot fields)
       ========================= */

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest req) {
        Order order = new Order();
        order.setUserId(req.getUserId());
        order.setCustomerName(req.getCustomerName());
        order.setCustomerAddress(req.getCustomerAddress());
        order.setCustomerPhone(req.getCustomerPhone());

        order.setStatus(STATUS_PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        List<OrderItem> items = new ArrayList<>();
        double total = 0.0;

        if (req.getItems() != null) {
            for (CreateOrderRequest.Item dto : req.getItems()) {

                Medicine med = medicineRepository.findById(dto.getMedicineId())
                        .orElseThrow(() -> new RuntimeException("Medicine not found"));

                if (med.getQuantity() < dto.getQuantity()) {
                    throw new RuntimeException("Insufficient stock");
                }

                // reduce stock
                med.setQuantity(med.getQuantity() - dto.getQuantity());
                medicineRepository.save(med);

                // base prices
                double sellPrice = med.getPrice();
                double buyPrice = med.getBuyPrice();

                // discount calculation
                DiscountCalc calc = calculateDiscount(
                        sellPrice,
                        med.getDiscountType(),
                        med.getDiscountValue(),
                        med.isDiscountActive()
                );

                double finalUnit = calc.finalPricePerUnit;
                double discUnit = calc.discountPerUnit;

                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setMedicine(med);
                item.setQuantity(dto.getQuantity());

                // keep old field working (unitPrice = FINAL per unit)
                item.setUnitPrice(finalUnit);

                // snapshot fields for profit/analytics
                item.setBuyPriceAtSale(buyPrice);
                item.setSellPriceAtSale(sellPrice);
                item.setDiscountPerUnit(discUnit);
                item.setFinalPricePerUnit(finalUnit);

                items.add(item);

                total += finalUnit * dto.getQuantity();
            }
        }

        order.setItems(items);
        order.setTotalAmount(round2(total));

        Order saved = orderRepository.save(order);

        User u = saved.getUserId() != null
                ? userRepository.findById(saved.getUserId()).orElse(null)
                : null;

        return toOrderResponse(saved, u);
    }

    /* =========================
       Order history / fetch
       ========================= */

    @Transactional
    public List<OrderResponse> getHistory(Long userId) {
        User u = userId != null ? userRepository.findById(userId).orElse(null) : null;
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(o -> toOrderResponse(o, u))
                .toList();
    }

    @Transactional
    public Optional<OrderResponse> getOrderDtoById(Long id) {
        return orderRepository.findByIdWithItems(id)
                .map(o -> toOrderResponse(
                        o,
                        o.getUserId() != null
                                ? userRepository.findById(o.getUserId()).orElse(null)
                                : null
                ));
    }

    /* =========================
       Cancel Flow
       ========================= */

    @Transactional
    public OrderResponse requestCancel(Long orderId, Long userId, String reason) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!Objects.equals(order.getUserId(), userId)) {
            throw new RuntimeException("Unauthorized cancel request");
        }

        // allow cancel only in PENDING or PAID (your choice)
        if (!STATUS_PENDING.equals(order.getStatus()) && !STATUS_PAID.equals(order.getStatus())) {
            throw new RuntimeException("Order cannot be cancelled");
        }

        order.setStatus(STATUS_CANCEL_REQUESTED);
        order.setCancelReason(reason);
        order.setCancelRequestedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);
        User u = userRepository.findById(userId).orElse(null);

        return toOrderResponse(saved, u);
    }

    @Transactional
    public List<OrderResponse> getCancelRequests() {
        List<Order> orders = orderRepository.findCancelRequestsWithItems(STATUS_CANCEL_REQUESTED);

        Map<Long, User> userMap = userRepository.findAllById(
                orders.stream()
                        .map(Order::getUserId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(User::getId, u -> u));

        List<OrderResponse> result = new ArrayList<>();
        for (Order o : orders) {
            result.add(toOrderResponse(o, userMap.get(o.getUserId())));
        }
        return result;
    }

    @Transactional
    public OrderResponse approveCancel(Long orderId, Long adminId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!STATUS_CANCEL_REQUESTED.equals(order.getStatus())) {
            throw new RuntimeException("No cancel request exists");
        }

        // restock on approve
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                Medicine med = item.getMedicine();
                if (med != null) {
                    med.setQuantity(med.getQuantity() + item.getQuantity());
                    medicineRepository.save(med);
                }
            }
        }

        order.setStatus(STATUS_CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);

        User u = saved.getUserId() != null
                ? userRepository.findById(saved.getUserId()).orElse(null)
                : null;

        return toOrderResponse(saved, u);
    }

    @Transactional
    public OrderResponse rejectCancel(Long orderId, Long adminId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!STATUS_CANCEL_REQUESTED.equals(order.getStatus())) {
            throw new RuntimeException("No cancel request exists");
        }

        order.setStatus(STATUS_REJECTED_CANCEL);
        order.setUpdatedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);

        User u = saved.getUserId() != null
                ? userRepository.findById(saved.getUserId()).orElse(null)
                : null;

        return toOrderResponse(saved, u);
    }

    /* =========================
       Invoice
       ========================= */

    @Transactional
    public byte[] generateInvoicePdf(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!STATUS_PAID.equals(order.getStatus()) && !STATUS_DELIVERED.equals(order.getStatus())) {
            throw new RuntimeException("Invoice available only after payment");
        }

        return invoicePdfService.generateInvoicePdf(order);
    }

    /* =========================
        Admin Analytics
       ========================= */

    @Transactional
    public AdminAnalyticsOverview getAdminOverview() {
        AdminAnalyticsOverview dto = new AdminAnalyticsOverview();

        dto.setTotalOrders(orderRepository.count());
        dto.setTotalRevenue(safeDouble(orderRepository.sumTotalAmount()));

        dto.setPendingOrders(orderRepository.countByStatus(STATUS_PENDING));
        dto.setCancelledOrders(orderRepository.countByStatus(STATUS_CANCELLED));
        dto.setDeliveredOrders(orderRepository.countByStatus(STATUS_DELIVERED));

        dto.setTodayRevenue(safeDouble(orderRepository.sumRevenueFrom(
                LocalDateTime.now().toLocalDate().atStartOfDay()
        )));

        // ✅ analytics computed from order_items (DELIVERED/PAID only)
        Long units = orderItemRepository.sumTotalUnitsSold();
        Double sales = orderItemRepository.sumTotalRevenueFromItems(); // uses finalPricePerUnit fallback
        Double cost = orderItemRepository.sumTotalCostFromItems();     // uses buyPriceAtSale fallback

        double safeSales = safeDouble(sales);
        double safeCost = safeDouble(cost);

        dto.setTotalUnitsSold(units != null ? units : 0L);
        dto.setTotalSales(round2(safeSales));
        dto.setTotalCost(round2(safeCost));
        dto.setTotalProfit(round2(safeSales - safeCost));

        return dto;
    }

    @Transactional
    public List<TopSellingItemDTO> getTopSellingItems(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        return orderItemRepository.findTopSelling(PageRequest.of(0, safeLimit));
    }

    /* =========================
       Admin Orders
       ========================= */

    @Transactional
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();

        Map<Long, User> userMap = userRepository.findAllById(
                orders.stream()
                        .map(Order::getUserId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(User::getId, u -> u));

        return orders.stream()
                .map(o -> toOrderResponse(o, userMap.get(o.getUserId())))
                .toList();
    }

    @Transactional
    public OrderResponse updateStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);

        User u = saved.getUserId() != null
                ? userRepository.findById(saved.getUserId()).orElse(null)
                : null;

        return toOrderResponse(saved, u);
    }

    /* =========================
       Mapper
       ========================= */

    private OrderResponse toOrderResponse(Order order, User u) {
        OrderResponse dto = new OrderResponse();

        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setUserEmail(u != null ? u.getEmail() : null);

        dto.setCustomerName(
                order.getCustomerName() != null
                        ? order.getCustomerName()
                        : (u != null ? u.getEmail() : "Guest Order")
        );

        dto.setCustomerAddress(order.getCustomerAddress());
        dto.setCustomerPhone(order.getCustomerPhone());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount() != null ? order.getTotalAmount() : 0.0);

        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        dto.setCancelReason(order.getCancelReason());
        dto.setCancelRequestedAt(order.getCancelRequestedAt());
        dto.setCancelledAt(order.getCancelledAt());

        if (order.getItems() != null) {
            List<OrderItemView> views = new ArrayList<>();

            for (OrderItem i : order.getItems()) {
                OrderItemView v = new OrderItemView();
                v.setId(i.getId());

                Medicine med = i.getMedicine();
                if (med != null) {
                    v.setMedicineId(med.getId());
                    v.setMedicineName(med.getName());
                } else {
                    v.setMedicineId(null);
                    v.setMedicineName("Unknown Medicine");
                }

                v.setQuantity(i.getQuantity());

                // final unit price if present
                Double finalUnit = i.getFinalPricePerUnit();
                v.setUnitPrice(finalUnit != null ? finalUnit : i.getUnitPrice());

                views.add(v);
            }

            dto.setItems(views);
        }

        return dto;
    }

    /* =========================
       Helpers
       ========================= */

    private static class DiscountCalc {
        double discountPerUnit;
        double finalPricePerUnit;

        DiscountCalc(double d, double f) {
            this.discountPerUnit = d;
            this.finalPricePerUnit = f;
        }
    }

    private DiscountCalc calculateDiscount(double sellPrice, String discountType, double discountValue, boolean active) {
        if (!active || discountValue <= 0) {
            return new DiscountCalc(0.0, round2(sellPrice));
        }

        double discountAmount = 0.0;

        if ("PERCENT".equalsIgnoreCase(discountType)) {
            discountAmount = (sellPrice * discountValue) / 100.0;
        } else if ("FLAT".equalsIgnoreCase(discountType)) {
            discountAmount = discountValue;
        } else {
            discountAmount = 0.0;
        }

        if (discountAmount < 0) discountAmount = 0;
        if (discountAmount > sellPrice) discountAmount = sellPrice;

        return new DiscountCalc(round2(discountAmount), round2(sellPrice - discountAmount));
    }

    private double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }

    private double safeDouble(Double v) {
        return v != null ? v : 0.0;
    }
}
