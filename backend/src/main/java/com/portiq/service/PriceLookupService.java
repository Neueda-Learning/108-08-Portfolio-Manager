package com.portiq.service;

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

    private static final String YAHOO_API = "https://query2.finance.yahoo.com/v7/finance/quote?symbols=";

    private final RestTemplate restTemplate = new RestTemplate();

    @Cacheable(value = "prices", key = "#ticker", unless = "#result == null")
    @SuppressWarnings("unchecked")
    public BigDecimal fetchLivePrice(String ticker) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("Accept", "application/json");

            ResponseEntity<Map> response = restTemplate.exchange(
                    YAHOO_API + ticker,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class);

            if (response.getBody() != null) {
                Map<String, Object> quoteResponse = (Map<String, Object>) response.getBody().get("quoteResponse");
                if (quoteResponse != null) {
                    List<Map<String, Object>> results = (List<Map<String, Object>>) quoteResponse.get("result");
                    if (results != null && !results.isEmpty()) {
                        Object price = results.get(0).get("regularMarketPrice");
                        if (price != null) {
                            return new BigDecimal(price.toString());
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
