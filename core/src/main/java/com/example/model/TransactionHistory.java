package com.example.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionHistory {
    private Long id;
    private Long portfolioId;
    private Long assetId;
    private String transactionType;
    private BigDecimal quantity;
    private BigDecimal price;
    private LocalDateTime transactionDate;

    public TransactionHistory() {
    }

    public TransactionHistory(Long id, Long portfolioId, Long assetId, String transactionType, BigDecimal quantity,
                              BigDecimal price, LocalDateTime transactionDate) {
        this.id = id;
        this.portfolioId = portfolioId;
        this.assetId = assetId;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.price = price;
        this.transactionDate = transactionDate;
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

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
}
