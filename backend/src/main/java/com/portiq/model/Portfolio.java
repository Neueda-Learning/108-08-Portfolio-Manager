package com.example.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Portfolio {
    private Long id;
    private Long customerId;
    private String portfolioName;
    private BigDecimal totalInvestment;
    private BigDecimal currentValue;
    private LocalDateTime createdAt;

    public Portfolio() {
    }

    public Portfolio(Long id, Long customerId, String portfolioName, BigDecimal totalInvestment, BigDecimal currentValue,
                     LocalDateTime createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.portfolioName = portfolioName;
        this.totalInvestment = totalInvestment;
        this.currentValue = currentValue;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getPortfolioName() {
        return portfolioName;
    }

    public void setPortfolioName(String portfolioName) {
        this.portfolioName = portfolioName;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
