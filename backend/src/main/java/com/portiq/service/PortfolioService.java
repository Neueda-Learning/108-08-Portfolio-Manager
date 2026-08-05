package com.example.service;

import com.example.model.Benchmark;
import com.example.model.Asset;
import com.example.model.PortfolioHolding;
import com.example.model.PortfolioAnalytics;
import com.example.model.Portfolio;
import com.example.model.TransactionHistory;
import com.example.repository.AssetRepository;
import com.example.repository.BenchmarkRepository;
import com.example.repository.PortfolioHoldingRepository;
import com.example.repository.PortfolioRepository;
import com.example.repository.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final BenchmarkRepository benchmarkRepository;
    private final AssetRepository assetRepository;
    private final PortfolioHoldingRepository portfolioHoldingRepository;
    private final TransactionRepository transactionRepository;

    public PortfolioService(PortfolioRepository portfolioRepository,
                            BenchmarkRepository benchmarkRepository,
                            AssetRepository assetRepository,
                            PortfolioHoldingRepository portfolioHoldingRepository,
                            TransactionRepository transactionRepository) {
        this.portfolioRepository = portfolioRepository;
        this.benchmarkRepository = benchmarkRepository;
        this.assetRepository = assetRepository;
        this.portfolioHoldingRepository = portfolioHoldingRepository;
        this.transactionRepository = transactionRepository;
    }

    public Portfolio create(Portfolio portfolio) {
        return portfolioRepository.create(portfolio);
    }

    public Optional<Portfolio> findById(Long id) {
        return portfolioRepository.findById(id);
    }

    public List<Portfolio> findAll() {
        return portfolioRepository.findAll();
    }

    public List<Portfolio> findByCustomerId(Long customerId) {
        return portfolioRepository.findByCustomerId(customerId);
    }

    public int update(Portfolio portfolio) {
        return portfolioRepository.update(portfolio);
    }

    public int delete(Long id) {
        return portfolioRepository.delete(id);
    }

    public Optional<PortfolioAnalytics> getPortfolioAnalytics(Long portfolioId, String benchmarkName) {
        Optional<Portfolio> portfolioOptional = portfolioRepository.findById(portfolioId);
        if (portfolioOptional.isEmpty()) {
            return Optional.empty();
        }

        Portfolio portfolio = portfolioOptional.get();
        BigDecimal totalInvestment = safeAmount(portfolio.getTotalInvestment());
        BigDecimal currentValue = safeAmount(portfolio.getCurrentValue());
        BigDecimal profitLoss = currentValue.subtract(totalInvestment);
        BigDecimal portfolioPerformance = percentageChange(totalInvestment, currentValue);

        BigDecimal benchmarkPerformance = BigDecimal.ZERO;
        Optional<Benchmark> earliest = benchmarkRepository.findEarliestByName(benchmarkName);
        Optional<Benchmark> latest = benchmarkRepository.findLatestByName(benchmarkName);
        if (earliest.isPresent() && latest.isPresent()) {
            benchmarkPerformance = percentageChange(earliest.get().getValue(), latest.get().getValue());
        }

        PortfolioAnalytics analytics = new PortfolioAnalytics(
                totalInvestment,
                currentValue,
                profitLoss,
                portfolioPerformance,
                benchmarkName,
                benchmarkPerformance,
                portfolioPerformance.subtract(benchmarkPerformance)
        );
        return Optional.of(analytics);
    }

    public boolean isPortfolioOwnedByCustomer(Long portfolioId, Long customerId) {
        return portfolioRepository.findById(portfolioId)
                .map(portfolio -> portfolio.getCustomerId().equals(customerId))
                .orElse(false);
    }

    @Transactional
    public PurchaseResult buyAsset(Long portfolioId, String assetSymbol, BigDecimal quantity, BigDecimal price) {
        if (assetSymbol == null || assetSymbol.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset symbol is required");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price must be greater than zero");
        }

        String normalizedSymbol = assetSymbol.trim().toUpperCase(Locale.ROOT);
        Portfolio lockedPortfolio = portfolioRepository.findByIdForUpdate(portfolioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));

        Asset asset = assetRepository.findBySymbol(normalizedSymbol)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));

        BigDecimal markPrice = asset.getCurrentPrice() != null && asset.getCurrentPrice().compareTo(BigDecimal.ZERO) > 0
                ? asset.getCurrentPrice()
                : price;

        TransactionHistory transaction = transactionRepository.create(
                new TransactionHistory(
                        null,
                        portfolioId,
                        asset.getId(),
                        "BUY",
                        quantity,
                        price,
                        LocalDateTime.now()
                )
        );

        PortfolioHolding holding = portfolioHoldingRepository.upsertBuyHolding(
                portfolioId,
                asset.getId(),
                quantity,
                price,
                markPrice
        );

        portfolioRepository.refreshTotalsFromHoldings(portfolioId);
        Portfolio updatedPortfolio = portfolioRepository.findById(portfolioId).orElse(lockedPortfolio);

        return new PurchaseResult(transaction, holding, updatedPortfolio);
    }

    public static final class PurchaseResult {
        private final TransactionHistory transaction;
        private final PortfolioHolding holding;
        private final Portfolio portfolio;

        public PurchaseResult(TransactionHistory transaction, PortfolioHolding holding, Portfolio portfolio) {
            this.transaction = transaction;
            this.holding = holding;
            this.portfolio = portfolio;
        }

        public TransactionHistory getTransaction() {
            return transaction;
        }

        public PortfolioHolding getHolding() {
            return holding;
        }

        public Portfolio getPortfolio() {
            return portfolio;
        }
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private BigDecimal percentageChange(BigDecimal base, BigDecimal current) {
        if (base == null || base.compareTo(BigDecimal.ZERO) == 0 || current == null) {
            return BigDecimal.ZERO;
        }
        return current.subtract(base)
                .multiply(BigDecimal.valueOf(100))
                .divide(base, 4, RoundingMode.HALF_UP);
    }
}
