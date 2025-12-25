package com.medimart.dto;

public class AdminAnalyticsOverview {

    // existing
    private long totalOrders;
    private double totalRevenue;
    private long pendingOrders;
    private long cancelledOrders;
    private long deliveredOrders;
    private double todayRevenue;

    private long totalUnitsSold;
    private double totalSales;
    private double totalCost;
    private double totalProfit;

    // ===== getters & setters =====

    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public long getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; }

    public long getCancelledOrders() { return cancelledOrders; }
    public void setCancelledOrders(long cancelledOrders) { this.cancelledOrders = cancelledOrders; }

    public long getDeliveredOrders() { return deliveredOrders; }
    public void setDeliveredOrders(long deliveredOrders) { this.deliveredOrders = deliveredOrders; }

    public double getTodayRevenue() { return todayRevenue; }
    public void setTodayRevenue(double todayRevenue) { this.todayRevenue = todayRevenue; }

    public long getTotalUnitsSold() { return totalUnitsSold; }
    public void setTotalUnitsSold(long totalUnitsSold) { this.totalUnitsSold = totalUnitsSold; }

    public double getTotalSales() { return totalSales; }
    public void setTotalSales(double totalSales) { this.totalSales = totalSales; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public double getTotalProfit() { return totalProfit; }
    public void setTotalProfit(double totalProfit) { this.totalProfit = totalProfit; }
}
