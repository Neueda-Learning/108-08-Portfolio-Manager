package com.portiq.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portiq.dto.HoldingRequest;
import com.portiq.model.HoldingType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Extracts holdings from a photo or screenshot of a brokerage/portfolio statement using a
 * vision-capable chat completion model.
 */
@Service
public class StatementScanService {

    private final ChatCompletionClient chatCompletionClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.insights.vision-model:}")
    private String visionModel;

    public StatementScanService(ChatCompletionClient chatCompletionClient) {
        this.chatCompletionClient = chatCompletionClient;
    }

    public boolean isAvailable() {
        return chatCompletionClient.isConfigured();
    }

    public List<HoldingRequest> extractHoldings(MultipartFile file) throws IOException {
        String base64 = Base64.getEncoder().encodeToString(file.getBytes());
        String contentType = file.getContentType() != null ? file.getContentType() : "image/png";
        String dataUrl = "data:" + contentType + ";base64," + base64;

        String instructions = "Look at this brokerage or portfolio statement image. Return ONLY a JSON array "
                + "(no prose, no markdown fences) of objects with keys: ticker, name, type (STOCK, BOND, or CASH), "
                + "quantity, purchasePrice, purchaseDate (YYYY-MM-DD, use today's date if not visible). "
                + "If a field cannot be read exactly, make a reasonable estimate rather than skipping the row.";

        List<Map<String, Object>> content = List.of(
                Map.of("type", "text", "text", instructions),
                Map.of("type", "image_url", "image_url", Map.of("url", dataUrl))
        );

        List<Map<String, Object>> messages = List.of(Map.of("role", "user", "content", content));

        String raw = chatCompletionClient.complete(visionModel, messages);
        String json = extractJsonArray(raw);

        List<Map<String, Object>> rows = objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        List<HoldingRequest> requests = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            requests.add(toRequest(row));
        }
        return requests;
    }

    private String extractJsonArray(String raw) {
        int start = raw.indexOf('[');
        int end = raw.lastIndexOf(']');
        if (start == -1 || end == -1 || end < start) {
            throw new IllegalStateException("Could not read a holdings list from the image");
        }
        return raw.substring(start, end + 1);
    }

    private HoldingRequest toRequest(Map<String, Object> row) {
        HoldingRequest request = new HoldingRequest();
        String ticker = String.valueOf(row.get("ticker")).trim().toUpperCase();
        request.setTicker(ticker);
        Object name = row.getOrDefault("name", ticker);
        request.setName(String.valueOf(name));

        HoldingType type = HoldingType.STOCK;
        try {
            type = HoldingType.valueOf(String.valueOf(row.getOrDefault("type", "STOCK")).trim().toUpperCase());
        } catch (Exception ignored) {
            // fall back to STOCK
        }
        request.setType(type);
        request.setQuantity(new BigDecimal(String.valueOf(row.get("quantity")).trim()));
        request.setPurchasePrice(new BigDecimal(String.valueOf(row.get("purchasePrice")).trim()));

        Object dateVal = row.get("purchaseDate");
        LocalDate date;
        try {
            date = dateVal != null ? LocalDate.parse(String.valueOf(dateVal).trim()) : LocalDate.now();
        } catch (Exception e) {
            date = LocalDate.now();
        }
        request.setPurchaseDate(date);
        return request;
    }
}
