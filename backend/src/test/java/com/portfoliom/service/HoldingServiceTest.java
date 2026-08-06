package com.portfoliom.service;

import com.portfoliom.dto.HoldingPerformance;
import com.portfoliom.dto.HoldingRequest;
import com.portfoliom.dto.PerformanceSummary;
import com.portfoliom.exception.ResourceNotFoundException;
import com.portfoliom.model.Holding;
import com.portfoliom.model.HoldingType;
import com.portfoliom.model.Portfolio;
import com.portfoliom.model.Role;
import com.portfoliom.model.User;
import com.portfoliom.repository.HoldingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HoldingServiceTest {

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private PriceService priceService;

    @InjectMocks
    private HoldingService holdingService;

    private User owner;
    private Portfolio portfolio;
    private Holding holding;

    @BeforeEach
    void setUp() {
        owner = new User("owner", "encoded", Role.OWNER);
        owner.setId(7L);

        portfolio = new Portfolio("Tech Growth", "Tech stocks", owner);
        portfolio.setId(1L);

        holding = new Holding();
        holding.setId(10L);
        holding.setPortfolio(portfolio);
        holding.setTicker("AAPL");
        holding.setName("Apple Inc.");
        holding.setType(HoldingType.STOCK);
        holding.setQuantity(new BigDecimal("10"));
        holding.setPurchasePrice(new BigDecimal("150.00"));
        holding.setPurchaseDate(LocalDate.of(2023, 1, 15));
    }

    @Test
    void getHoldingsByPortfolio_returnsList() {
        when(portfolioService.getById(1L, 7L)).thenReturn(portfolio);
        when(holdingRepository.findByPortfolioId(1L)).thenReturn(List.of(holding));

        List<Holding> result = holdingService.getHoldingsByPortfolio(1L, 7L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTicker()).isEqualTo("AAPL");
    }

    @Test
    void getHolding_returnsHolding_whenExists() {
        when(portfolioService.getById(1L, 7L)).thenReturn(portfolio);
        when(holdingRepository.findByIdAndPortfolioId(10L, 1L)).thenReturn(Optional.of(holding));

        Holding result = holdingService.getHolding(1L, 10L, 7L);

        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void getHolding_throwsException_whenNotFound() {
        when(portfolioService.getById(1L, 7L)).thenReturn(portfolio);
        when(holdingRepository.findByIdAndPortfolioId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> holdingService.getHolding(1L, 99L, 7L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void mergeOrCreate_mergesExistingTickerWithWeightedAveragePrice() {
        HoldingRequest request = new HoldingRequest();
        request.setTicker("aapl");
        request.setName("Apple Inc.");
        request.setType(HoldingType.STOCK);
        request.setQuantity(new BigDecimal("5"));
        request.setPurchasePrice(new BigDecimal("200.00"));

        when(holdingRepository.findByPortfolio_Owner_Id(7L)).thenReturn(List.of(holding));
        when(holdingRepository.save(any(Holding.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Holding result = holdingService.mergeOrCreate(portfolio, request, 7L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getQuantity()).isEqualByComparingTo("15");
        assertThat(result.getPurchasePrice()).isEqualByComparingTo("166.6667");
    }

    @Test
    void mergeOrCreate_createsNewHoldingWhenTickerIsNew() {
        HoldingRequest request = new HoldingRequest();
        request.setTicker("msft");
        request.setName("Microsoft Corp.");
        request.setType(HoldingType.STOCK);
        request.setQuantity(new BigDecimal("3"));
        request.setPurchasePrice(new BigDecimal("330.00"));
        request.setPurchaseDate(LocalDate.of(2024, 1, 2));

        when(holdingRepository.findByPortfolio_Owner_Id(7L)).thenReturn(List.of(holding));
        when(holdingRepository.save(any(Holding.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Holding result = holdingService.mergeOrCreate(portfolio, request, 7L);

        assertThat(result.getTicker()).isEqualTo("MSFT");
        assertThat(result.getPortfolio()).isEqualTo(portfolio);
        assertThat(result.getPurchaseDate()).isEqualTo(LocalDate.of(2024, 1, 2));
    }

    @Test
    void updateHoldingById_updatesFieldsAndUppercasesTicker() {
        HoldingRequest request = new HoldingRequest();
        request.setTicker("nvda");
        request.setName("NVIDIA Corp.");
        request.setType(HoldingType.STOCK);
        request.setQuantity(new BigDecimal("12"));
        request.setPurchasePrice(new BigDecimal("450.00"));
        request.setPurchaseDate(LocalDate.of(2024, 2, 1));

        when(holdingRepository.findByIdAndPortfolio_Owner_Id(10L, 7L)).thenReturn(Optional.of(holding));
        when(holdingRepository.save(any(Holding.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Holding result = holdingService.updateHoldingById(10L, request, 7L);

        assertThat(result.getTicker()).isEqualTo("NVDA");
        assertThat(result.getName()).isEqualTo("NVIDIA Corp.");
        assertThat(result.getQuantity()).isEqualByComparingTo("12");
    }

    @Test
    void removeHoldingById_deletesHolding() {
        when(holdingRepository.findByIdAndPortfolio_Owner_Id(10L, 7L)).thenReturn(Optional.of(holding));

        holdingService.removeHoldingById(10L, 7L);

        verify(holdingRepository).delete(holding);
    }

    @Test
    void getAggregatePerformance_refreshInvalidatesPricesAndCalculatesTotals() {
        when(holdingRepository.findByPortfolio_Owner_Id(7L)).thenReturn(List.of(holding));
        when(priceService.getCurrentPrice("AAPL", new BigDecimal("150.00")))
                .thenReturn(new BigDecimal("180.00"));

        PerformanceSummary summary = holdingService.getAggregatePerformance(7L, true);

        assertThat(summary.getPortfolioName()).isEqualTo("All Holdings");
        assertThat(summary.getTotalCostBasis()).isEqualByComparingTo("1500.00");
        assertThat(summary.getTotalCurrentValue()).isEqualByComparingTo("1800.00");
        assertThat(summary.getTotalGainLoss()).isEqualByComparingTo("300.00");
        assertThat(summary.getGainLossPercent()).isEqualByComparingTo("20.00");
        assertThat(summary.getHoldings()).hasSize(1);
        assertThat(summary.getHoldings().get(0).getCurrentPrice()).isEqualByComparingTo("180.00");
        verify(priceService).invalidate("AAPL");
    }

    @Test
    void getPerformance_emptyPortfolio_returnsZeros() {
        when(portfolioService.getById(1L, 7L)).thenReturn(portfolio);
        when(holdingRepository.findByPortfolioId(1L)).thenReturn(List.of());

        PerformanceSummary summary = holdingService.getPerformance(1L, 7L);

        assertThat(summary.getTotalCostBasis()).isEqualByComparingTo("0.00");
        assertThat(summary.getTotalCurrentValue()).isEqualByComparingTo("0.00");
        assertThat(summary.getGainLossPercent()).isEqualByComparingTo("0.00");
    }
}
