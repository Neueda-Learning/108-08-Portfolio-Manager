package com.example.model;

import java.math.BigDecimal;

public class PortfolioAnalytics {
    private BigDecimal totalInvestment;
    private BigDecimal currentValue;
    private BigDecimal profitLoss;
    private BigDecimal portfolioPerformancePercentage;
    private String benchmarkName;
    private BigDecimal benchmarkPerformancePercentage;
    private BigDecimal outperformancePercentage;

    public PortfolioAnalytics() {
    }

    public PortfolioAnalytics(BigDecimal totalInvestment, BigDecimal currentValue, BigDecimal profitLoss,
                              BigDecimal portfolioPerformancePercentage, String benchmarkName,
                              BigDecimal benchmarkPerformancePercentage, BigDecimal outperformancePercentage) {
        this.totalInvestment = totalInvestment;
        this.currentValue = currentValue;
        this.profitLoss = profitLoss;
        this.portfolioPerformancePercentage = portfolioPerformancePercentage;
        this.benchmarkName = benchmarkName;
        this.benchmarkPerformancePercentage = benchmarkPerformancePercentage;
        this.outperformancePercentage = outperformancePercentage;
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

    public BigDecimal getPortfolioPerformancePercentage() {
        return portfolioPerformancePercentage;
    }

    public void setPortfolioPerformancePercentage(BigDecimal portfolioPerformancePercentage) {
        this.portfolioPerformancePercentage = portfolioPerformancePercentage;
    }

    public String getBenchmarkName() {
        return benchmarkName;
    }

    public void setBenchmarkName(String benchmarkName) {
        this.benchmarkName = benchmarkName;
    }

    public BigDecimal getBenchmarkPerformancePercentage() {
        return benchmarkPerformancePercentage;
    }

    public void setBenchmarkPerformancePercentage(BigDecimal benchmarkPerformancePercentage) {
        this.benchmarkPerformancePercentage = benchmarkPerformancePercentage;
    }

    public BigDecimal getOutperformancePercentage() {
        return outperformancePercentage;
    }

    public void setOutperformancePercentage(BigDecimal outperformancePercentage) {
        this.outperformancePercentage = outperformancePercentage;
    }
}
