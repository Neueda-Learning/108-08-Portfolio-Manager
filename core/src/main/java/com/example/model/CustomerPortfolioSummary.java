package com.example.model;

import java.math.BigDecimal;

public class CustomerPortfolioSummary {
    private Long portfolioId;
    private String portfolioName;
    private BigDecimal totalInvestment;
    private BigDecimal currentValue;
    private BigDecimal profitLoss;
    private BigDecimal returnPercentage;

    public CustomerPortfolioSummary() {
    }

    public CustomerPortfolioSummary(Long portfolioId, String portfolioName, BigDecimal totalInvestment,
                                    BigDecimal currentValue, BigDecimal profitLoss, BigDecimal returnPercentage) {
        this.portfolioId = portfolioId;
        this.portfolioName = portfolioName;
        this.totalInvestment = totalInvestment;
        this.currentValue = currentValue;
        this.profitLoss = profitLoss;
        this.returnPercentage = returnPercentage;
    }

    public Long getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(Long portfolioId) {
        this.portfolioId = portfolioId;
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
