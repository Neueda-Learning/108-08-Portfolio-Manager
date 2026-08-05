package com.example.model;

import java.math.BigDecimal;

public class ManagedCustomerSummary {
    private Long customerId;
    private String customerName;
    private String email;
    private long portfolioCount;
    private BigDecimal totalInvestment;
    private BigDecimal currentValue;
    private BigDecimal profitLoss;
    private BigDecimal returnPercentage;

    public ManagedCustomerSummary() {
    }

    public ManagedCustomerSummary(Long customerId, String customerName, String email, long portfolioCount,
                                  BigDecimal totalInvestment, BigDecimal currentValue, BigDecimal profitLoss,
                                  BigDecimal returnPercentage) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.email = email;
        this.portfolioCount = portfolioCount;
        this.totalInvestment = totalInvestment;
        this.currentValue = currentValue;
        this.profitLoss = profitLoss;
        this.returnPercentage = returnPercentage;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getPortfolioCount() {
        return portfolioCount;
    }

    public void setPortfolioCount(long portfolioCount) {
        this.portfolioCount = portfolioCount;
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

    public BigDecimal getReturnPercentage() {
        return returnPercentage;
    }

    public void setReturnPercentage(BigDecimal returnPercentage) {
        this.returnPercentage = returnPercentage;
    }
}
