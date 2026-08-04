package com.example.model;

import java.math.BigDecimal;

public class PortfolioHoldingView {
    private Long id;
    private Long portfolioId;
    private Long assetId;
    private String assetSymbol;
    private String assetName;
    private String assetType;
    private BigDecimal quantity;
    private BigDecimal averageBuyPrice;
    private BigDecimal investedAmount;
    private BigDecimal currentValue;
    private BigDecimal currentPrice;

    public PortfolioHoldingView() {
    }

    public PortfolioHoldingView(Long id,
                                Long portfolioId,
                                Long assetId,
                                String assetSymbol,
                                String assetName,
                                String assetType,
                                BigDecimal quantity,
                                BigDecimal averageBuyPrice,
                                BigDecimal investedAmount,
                                BigDecimal currentValue,
                                BigDecimal currentPrice) {
        this.id = id;
        this.portfolioId = portfolioId;
        this.assetId = assetId;
        this.assetSymbol = assetSymbol;
        this.assetName = assetName;
        this.assetType = assetType;
        this.quantity = quantity;
        this.averageBuyPrice = averageBuyPrice;
        this.investedAmount = investedAmount;
        this.currentValue = currentValue;
        this.currentPrice = currentPrice;
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

    public String getAssetSymbol() {
        return assetSymbol;
    }

    public void setAssetSymbol(String assetSymbol) {
        this.assetSymbol = assetSymbol;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
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

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }
}