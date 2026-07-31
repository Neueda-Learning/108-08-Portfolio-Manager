package com.example.model;

import java.math.BigDecimal;

public class PortfolioHolding {
    private Long id;
    private Long portfolioId;
    private Long assetId;
    private BigDecimal quantity;
    private BigDecimal averageBuyPrice;
    private BigDecimal investedAmount;
    private BigDecimal currentValue;

    public PortfolioHolding() {
    }

    public PortfolioHolding(Long id, Long portfolioId, Long assetId, BigDecimal quantity, BigDecimal averageBuyPrice,
                            BigDecimal investedAmount, BigDecimal currentValue) {
        this.id = id;
        this.portfolioId = portfolioId;
        this.assetId = assetId;
        this.quantity = quantity;
        this.averageBuyPrice = averageBuyPrice;
        this.investedAmount = investedAmount;
        this.currentValue = currentValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(Long portfolioId) {
        this.portfolioId = portfolioId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAverageBuyPrice() {
        return averageBuyPrice;
    }

    public void setAverageBuyPrice(BigDecimal averageBuyPrice) {
        this.averageBuyPrice = averageBuyPrice;
    }

    public BigDecimal getInvestedAmount() {
        return investedAmount;
    }

    public void setInvestedAmount(BigDecimal investedAmount) {
        this.investedAmount = investedAmount;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }
}
