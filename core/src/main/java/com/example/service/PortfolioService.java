package com.example.service;

import com.example.model.Benchmark;
import com.example.model.PortfolioAnalytics;
import com.example.model.Portfolio;
import com.example.repository.BenchmarkRepository;
import com.example.repository.PortfolioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final BenchmarkRepository benchmarkRepository;

    public PortfolioService(PortfolioRepository portfolioRepository, BenchmarkRepository benchmarkRepository) {
        this.portfolioRepository = portfolioRepository;
        this.benchmarkRepository = benchmarkRepository;
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
