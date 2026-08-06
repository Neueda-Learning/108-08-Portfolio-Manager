package com.portfoliom.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Isolated in its own bean (rather than a private method on PriceService) so the
 * {@code @Cacheable} annotation goes through the Spring proxy - calling a cached method from
 * within the same class bypasses the proxy and silently disables caching.
 */
@Service
public class PriceLookupService {

    // Yahoo's legacy v7 quote endpoint now returns 401 Unauthorized for unauthenticated callers.
    // The v8 chart endpoint (also used by PriceSeriesFetcher for history) still works and reports
    // the live price via meta.regularMarketPrice, so it doubles as the live quote source.
    private static final String CHART_API = "https://query1.finance.yahoo.com/v8/finance/chart/";

    private final RestTemplate restTemplate = new RestTemplate();

    @Cacheable(value = "prices", key = "#ticker", unless = "#result == null")
    @SuppressWarnings("unchecked")
    public BigDecimal fetchLivePrice(String ticker) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("Accept", "application/json");

            ResponseEntity<Map> response = restTemplate.exchange(
                    CHART_API + ticker + "?range=1d&interval=1m",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class);

            Map<String, Object> body = response.getBody();
            if (body == null) return null;

            Map<String, Object> chart = (Map<String, Object>) body.get("chart");
            if (chart == null) return null;
            List<Map<String, Object>> results = (List<Map<String, Object>>) chart.get("result");
            if (results == null || results.isEmpty()) return null;

            Map<String, Object> meta = (Map<String, Object>) results.get(0).get("meta");
            if (meta == null) return null;

            Object price = meta.get("regularMarketPrice");
            if (price != null) {
                return new BigDecimal(price.toString());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Drops the cached quote for a ticker so the next {@link #fetchLivePrice} call re-fetches
     * live from Yahoo Finance instead of returning a stale value. Used by the manual "Refresh"
     * action in the UI.
     */
    @CacheEvict(value = "prices", key = "#ticker")
    public void evict(String ticker) {
        // No-op body - eviction happens via the annotation.
    }
}
