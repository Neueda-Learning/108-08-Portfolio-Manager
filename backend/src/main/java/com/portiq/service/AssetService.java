package com.example.service;

import com.example.model.Asset;
import com.example.model.PortfolioHolding;
import com.example.repository.AssetRepository;
import com.example.repository.PortfolioHoldingRepository;
import com.example.repository.PortfolioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class AssetService {
    private final AssetRepository assetRepository;
    private final PortfolioHoldingRepository portfolioHoldingRepository;
    private final PortfolioRepository portfolioRepository;

    public AssetService(AssetRepository assetRepository,
                        PortfolioHoldingRepository portfolioHoldingRepository,
                        PortfolioRepository portfolioRepository) {
        this.assetRepository = assetRepository;
        this.portfolioHoldingRepository = portfolioHoldingRepository;
        this.portfolioRepository = portfolioRepository;
    }

    public Asset create(Asset asset) {
        Asset normalized = normalizeAssetForMvp(asset);
        return assetRepository.create(normalized);
    }

    public Optional<Asset> findById(Long id) {
        return assetRepository.findById(id);
    }

    public List<Asset> findAll() {
        return assetRepository.findAll();
    }

    @Transactional
    public int update(Asset asset) {
        Asset normalized = normalizeAssetForMvp(asset);
        normalized.setId(asset.getId());
        int updated = assetRepository.update(normalized);
        if (updated > 0) {
            refreshHoldingsAndPortfoliosForAsset(normalized.getId(), normalized.getCurrentPrice());
        }
        return updated;
    }

    public int delete(Long id) {
        return assetRepository.delete(id);
    }

    private Asset normalizeAssetForMvp(Asset asset) {
        if (asset.getSymbol() == null || asset.getSymbol().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset symbol is required");
        }
        if (asset.getName() == null || asset.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset name is required");
        }
        if (asset.getAssetType() == null || !"STOCK".equalsIgnoreCase(asset.getAssetType().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MVP currently supports only STOCK assets");
        }
        BigDecimal currentPrice = asset.getCurrentPrice();
        if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current price must be a non-negative value");
        }
        return new Asset(
                asset.getId(),
                asset.getSymbol().trim().toUpperCase(Locale.ROOT),
                asset.getName().trim(),
                "STOCK",
                currentPrice
        );
    }

    private void refreshHoldingsAndPortfoliosForAsset(Long assetId, BigDecimal currentPrice) {
        if (assetId == null) {
            return;
        }

        portfolioHoldingRepository.refreshCurrentValueForAsset(assetId, currentPrice);
        for (PortfolioHolding holding : portfolioHoldingRepository.findByAssetId(assetId)) {
            portfolioRepository.refreshTotalsFromHoldings(holding.getPortfolioId());
        }
    }
}
