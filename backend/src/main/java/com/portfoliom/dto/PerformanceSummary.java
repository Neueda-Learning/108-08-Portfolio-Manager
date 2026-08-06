package com.portfoliom.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class PerformanceSummary {

    private Long portfolioId;
    private String portfolioName;
    private BigDecimal totalCostBasis;
    private BigDecimal totalCurrentValue;
    private BigDecimal totalGainLoss;
    private BigDecimal gainLossPercent;
    private List<HoldingPerformance> holdings;
    private Instant pricesAsOf;

    public Long getPortfolioId() { return portfolioId; }
    public void setPortfolioId(Long portfolioId) { this.portfolioId = portfolioId; }
    public String getPortfolioName() { return portfolioName; }
    public void setPortfolioName(String portfolioName) { this.portfolioName = portfolioName; }
    public BigDecimal getTotalCostBasis() { return totalCostBasis; }
    public void setTotalCostBasis(BigDecimal totalCostBasis) { this.totalCostBasis = totalCostBasis; }
    public BigDecimal getTotalCurrentValue() { return totalCurrentValue; }
    public void setTotalCurrentValue(BigDecimal totalCurrentValue) { this.totalCurrentValue = totalCurrentValue; }
    public BigDecimal getTotalGainLoss() { return totalGainLoss; }
    public void setTotalGainLoss(BigDecimal totalGainLoss) { this.totalGainLoss = totalGainLoss; }
    public BigDecimal getGainLossPercent() { return gainLossPercent; }
    public void setGainLossPercent(BigDecimal gainLossPercent) { this.gainLossPercent = gainLossPercent; }
    public List<HoldingPerformance> getHoldings() { return holdings; }
    public void setHoldings(List<HoldingPerformance> holdings) { this.holdings = holdings; }
    public Instant getPricesAsOf() { return pricesAsOf; }
    public void setPricesAsOf(Instant pricesAsOf) { this.pricesAsOf = pricesAsOf; }
}
