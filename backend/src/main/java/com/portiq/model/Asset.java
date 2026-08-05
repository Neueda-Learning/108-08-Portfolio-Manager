package com.example.model;

import java.math.BigDecimal;

public class Asset {
    private Long id;
    private String symbol;
    private String name;
    private String assetType;
    private BigDecimal currentPrice;

    public Asset() {
    }

    public Asset(Long id, String symbol, String name, String assetType, BigDecimal currentPrice) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.assetType = assetType;
        this.currentPrice = currentPrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }
}
