package com.example.config;

import com.example.model.Asset;
import com.example.repository.AssetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Component
public class StockCsvSeeder implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(StockCsvSeeder.class);
    private static final String CSV_PATH = "data/stocks.csv";

    private final AssetRepository assetRepository;

    public StockCsvSeeder(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ClassPathResource resource = new ClassPathResource(CSV_PATH);
        if (!resource.exists()) {
            logger.warn("Stock CSV not found at classpath:{}", CSV_PATH);
            return;
        }
        seedFromCsv(resource);
    }

    private void seedFromCsv(ClassPathResource resource) throws IOException {
        int inserted = 0;
        int scanned = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // header
            if (line == null) {
                logger.warn("Stock CSV is empty");
                return;
            }
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                scanned++;
                String[] parts = line.split(",", -1);
                if (parts.length < 3) {
                    logger.warn("Skipping malformed stock row: {}", line);
                    continue;
                }
                String symbol = parts[0].trim().toUpperCase(Locale.ROOT);
                String name = parts[1].trim();
                String priceRaw = parts[2].trim();
                if (symbol.isEmpty() || name.isEmpty() || priceRaw.isEmpty()) {
                    logger.warn("Skipping incomplete stock row: {}", line);
                    continue;
                }
                BigDecimal currentPrice;
                try {
                    currentPrice = new BigDecimal(priceRaw);
                } catch (NumberFormatException ex) {
                    logger.warn("Skipping stock row with invalid price: {}", line);
                    continue;
                }
                if (assetRepository.findBySymbol(symbol).isEmpty()) {
                    assetRepository.create(new Asset(null, symbol, name, "STOCK", currentPrice));
                    inserted++;
                }
            }
        }
        logger.info("Stock CSV scan complete. scanned={}, inserted={}", scanned, inserted);
    }
}
