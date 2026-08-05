package com.example.model;

import java.math.BigDecimal;
import java.util.List;

public class CustomerDashboard {
    private Long customerId;
    private String customerName;
    private long totalPortfolios;
    private BigDecimal totalInvestment;
    private BigDecimal currentValue;
    private BigDecimal profitLoss;
    private BigDecimal overallReturnPercentage;
    private List<CustomerPortfolioSummary> portfolios;
    private List<AssetAllocationSlice> assetAllocation;
    private List<DashboardTimePoint> performanceTrend;
    private List<DashboardTimePoint> benchmarkTrend;
    private List<String> insights;

    public CustomerDashboard() {
    }

    public CustomerDashboard(Long customerId, String customerName, long totalPortfolios, BigDecimal totalInvestment,
                             BigDecimal currentValue, BigDecimal profitLoss, BigDecimal overallReturnPercentage,
                             List<CustomerPortfolioSummary> portfolios, List<AssetAllocationSlice> assetAllocation,
                             List<DashboardTimePoint> performanceTrend, List<DashboardTimePoint> benchmarkTrend,
                             List<String> insights) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.totalPortfolios = totalPortfolios;
        this.totalInvestment = totalInvestment;
        this.currentValue = currentValue;
        this.profitLoss = profitLoss;
        this.overallReturnPercentage = overallReturnPercentage;
        this.portfolios = portfolios;
        this.assetAllocation = assetAllocation;
        this.performanceTrend = performanceTrend;
        this.benchmarkTrend = benchmarkTrend;
        this.insights = insights;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public long getTotalPortfolios() {
        return totalPortfolios;
    }

    public void setTotalPortfolios(long totalPortfolios) {
        this.totalPortfolios = totalPortfolios;
    }

    public BigDecimal getTotalInvestment() {
        return totalInvestment;
    }

    public void setTotalInvestment(BigDecimal totalInvestment) {
        this.totalInvestment = totalInvestment;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public BigDecimal getProfitLoss() {
        return profitLoss;
    }

    public void setProfitLoss(BigDecimal profitLoss) {
        this.profitLoss = profitLoss;
    }

    public BigDecimal getOverallReturnPercentage() {
        return overallReturnPercentage;
    }

    public void setOverallReturnPercentage(BigDecimal overallReturnPercentage) {
        this.overallReturnPercentage = overallReturnPercentage;
    }

    public List<CustomerPortfolioSummary> getPortfolios() {
        return portfolios;
    }

    public void setPortfolios(List<CustomerPortfolioSummary> portfolios) {
        this.portfolios = portfolios;
    }

    public List<AssetAllocationSlice> getAssetAllocation() {
        return assetAllocation;
    }

    public void setAssetAllocation(List<AssetAllocationSlice> assetAllocation) {
        this.assetAllocation = assetAllocation;
    }

    public List<DashboardTimePoint> getPerformanceTrend() {
        return performanceTrend;
    }

    public void setPerformanceTrend(List<DashboardTimePoint> performanceTrend) {
        this.performanceTrend = performanceTrend;
    }

    public List<DashboardTimePoint> getBenchmarkTrend() {
        return benchmarkTrend;
    }

    public void setBenchmarkTrend(List<DashboardTimePoint> benchmarkTrend) {
        this.benchmarkTrend = benchmarkTrend;
    }

    public List<String> getInsights() {
        return insights;
    }

    public void setInsights(List<String> insights) {
        this.insights = insights;
    }
}
