package com.example.model;

import java.math.BigDecimal;
import java.util.List;

public class FundManagerDashboard {
    private Long fundManagerId;
    private String fundManagerName;
    private long totalCustomers;
    private long totalPortfolios;
    private BigDecimal assetsUnderManagement;
    private BigDecimal totalInvestment;
    private BigDecimal profitLoss;
    private BigDecimal averageReturnPercentage;
    private List<ManagedCustomerSummary> customers;
    private List<DashboardTimePoint> performanceTrend;
    private List<DashboardTimePoint> benchmarkTrend;
    private List<String> operationalAlerts;

    public FundManagerDashboard() {
    }

    public FundManagerDashboard(Long fundManagerId, String fundManagerName, long totalCustomers, long totalPortfolios,
                                BigDecimal assetsUnderManagement, BigDecimal totalInvestment, BigDecimal profitLoss,
                                BigDecimal averageReturnPercentage, List<ManagedCustomerSummary> customers,
                                List<DashboardTimePoint> performanceTrend, List<DashboardTimePoint> benchmarkTrend,
                                List<String> operationalAlerts) {
        this.fundManagerId = fundManagerId;
        this.fundManagerName = fundManagerName;
        this.totalCustomers = totalCustomers;
        this.totalPortfolios = totalPortfolios;
        this.assetsUnderManagement = assetsUnderManagement;
        this.totalInvestment = totalInvestment;
        this.profitLoss = profitLoss;
        this.averageReturnPercentage = averageReturnPercentage;
        this.customers = customers;
        this.performanceTrend = performanceTrend;
        this.benchmarkTrend = benchmarkTrend;
        this.operationalAlerts = operationalAlerts;
    }

    public Long getFundManagerId() {
        return fundManagerId;
    }

    public void setFundManagerId(Long fundManagerId) {
        this.fundManagerId = fundManagerId;
    }

    public String getFundManagerName() {
        return fundManagerName;
    }

    public void setFundManagerName(String fundManagerName) {
        this.fundManagerName = fundManagerName;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public long getTotalPortfolios() {
        return totalPortfolios;
    }

    public void setTotalPortfolios(long totalPortfolios) {
        this.totalPortfolios = totalPortfolios;
    }

    public BigDecimal getAssetsUnderManagement() {
        return assetsUnderManagement;
    }

    public void setAssetsUnderManagement(BigDecimal assetsUnderManagement) {
        this.assetsUnderManagement = assetsUnderManagement;
    }

    public BigDecimal getTotalInvestment() {
        return totalInvestment;
    }

    public void setTotalInvestment(BigDecimal totalInvestment) {
        this.totalInvestment = totalInvestment;
    }

    public BigDecimal getProfitLoss() {
        return profitLoss;
    }

    public void setProfitLoss(BigDecimal profitLoss) {
        this.profitLoss = profitLoss;
    }

    public BigDecimal getAverageReturnPercentage() {
        return averageReturnPercentage;
    }

    public void setAverageReturnPercentage(BigDecimal averageReturnPercentage) {
        this.averageReturnPercentage = averageReturnPercentage;
    }

    public List<ManagedCustomerSummary> getCustomers() {
        return customers;
    }

    public void setCustomers(List<ManagedCustomerSummary> customers) {
        this.customers = customers;
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

    public List<String> getOperationalAlerts() {
        return operationalAlerts;
    }

    public void setOperationalAlerts(List<String> operationalAlerts) {
        this.operationalAlerts = operationalAlerts;
    }
}
