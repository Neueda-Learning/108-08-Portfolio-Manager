package com.portfoliom.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PriceService {

    private final PriceLookupService priceLookupService;

    public PriceService(PriceLookupService priceLookupService) {
        this.priceLookupService = priceLookupService;
    }

    public BigDecimal getCurrentPrice(String ticker, BigDecimal fallback) {
        if (ticker == null || ticker.isBlank()) return fallback;
        BigDecimal live = priceLookupService.fetchLivePrice(ticker);
        return live != null ? live : fallback;
    }

    /** Forces the next {@link #getCurrentPrice} call for this ticker to hit Yahoo Finance live. */
    public void invalidate(String ticker) {
        if (ticker == null || ticker.isBlank()) return;
        priceLookupService.evict(ticker);
    }
}
