package com.medimart.controller;

import com.medimart.dto.AdminAnalyticsOverview;
import com.medimart.dto.TopSellingItemDTO;
import com.medimart.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:3000",
        "https://medimart-frontend-coral.vercel.app"
})
public class AdminAnalyticsController {

    private final OrderService orderService;

    public AdminAnalyticsController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/overview")
    public AdminAnalyticsOverview overview() {
        return orderService.getAdminOverview();
    }

    @GetMapping("/summary")
    public AdminAnalyticsOverview summary() {
        return orderService.getAdminOverview();
    }

    @GetMapping("/top-selling")
    public List<TopSellingItemDTO> topSelling(@RequestParam(defaultValue = "3") int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        return orderService.getTopSellingItems(safeLimit);
    }
}
