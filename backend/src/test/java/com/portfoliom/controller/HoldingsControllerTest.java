package com.portfoliom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.portfoliom.dto.HoldingRequest;
import com.portfoliom.dto.PerformanceSummary;
import com.portfoliom.dto.PortfolioHistoryPoint;
import com.portfoliom.exception.GlobalExceptionHandler;
import com.portfoliom.model.Holding;
import com.portfoliom.model.HoldingType;
import com.portfoliom.model.Portfolio;
import com.portfoliom.model.Role;
import com.portfoliom.model.User;
import com.portfoliom.service.ExportService;
import com.portfoliom.service.HoldingImportService;
import com.portfoliom.service.HoldingService;
import com.portfoliom.service.PortfolioService;
import com.portfoliom.service.PriceHistoryService;
import com.portfoliom.service.StatementScanService;
import com.portfoliom.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HoldingsControllerTest {

    private MockMvc mockMvc;
    private Authentication authentication;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private HoldingService holdingService;

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private HoldingImportService holdingImportService;

    @Mock
    private StatementScanService statementScanService;

    @Mock
    private ExportService exportService;

    @Mock
    private PriceHistoryService priceHistoryService;

    @Mock
    private UserService userService;

    @InjectMocks
    private HoldingsController holdingsController;

    @BeforeEach
    void setUp() {
        authentication = mock(Authentication.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(holdingsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private User owner() {
        User owner = new User("owner", "encoded", Role.OWNER);
        owner.setId(7L);
        owner.setName("Owner");
        owner.setEmail("owner@example.com");
        return owner;
    }

    private Portfolio portfolio() {
        Portfolio portfolio = new Portfolio("My Portfolio", "Default portfolio", owner());
        portfolio.setId(1L);
        return portfolio;
    }

    private Holding holding() {
        Holding holding = new Holding();
        holding.setId(10L);
        holding.setPortfolio(portfolio());
        holding.setTicker("AAPL");
        holding.setName("Apple Inc.");
        holding.setType(HoldingType.STOCK);
        holding.setQuantity(new BigDecimal("10"));
        holding.setPurchasePrice(new BigDecimal("150.00"));
        holding.setPurchaseDate(LocalDate.of(2023, 1, 15));
        return holding;
    }

    @Test
    void getAllHoldings_passesRefreshFlag() throws Exception {
        PerformanceSummary summary = new PerformanceSummary();
        summary.setPortfolioName("All Holdings");
        summary.setTotalCurrentValue(new BigDecimal("1800.00"));
        summary.setPricesAsOf(Instant.parse("2024-01-01T10:00:00Z"));

        when(userService.getCurrentUserId(authentication)).thenReturn(7L);
        when(holdingService.getAggregatePerformance(7L, true)).thenReturn(summary);

        mockMvc.perform(get("/api/holdings")
                        .principal(authentication)
                        .param("refresh", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioName").value("All Holdings"));

        verify(holdingService).getAggregatePerformance(7L, true);
    }

    @Test
    void addHolding_usesDefaultPortfolioAndMergeOrCreate() throws Exception {
        HoldingRequest request = new HoldingRequest();
        request.setTicker("msft");
        request.setName("Microsoft Corp.");
        request.setType(HoldingType.STOCK);
        request.setQuantity(new BigDecimal("3"));
        request.setPurchasePrice(new BigDecimal("330.00"));
        request.setPurchaseDate(LocalDate.of(2024, 1, 2));

        User owner = owner();
        Portfolio portfolio = portfolio();
        Holding created = holding();
        created.setTicker("MSFT");
        created.setName("Microsoft Corp.");

        when(userService.getCurrentUser(authentication)).thenReturn(owner);
        when(portfolioService.getOrCreateDefault(owner)).thenReturn(portfolio);
        when(holdingService.mergeOrCreate(eq(portfolio), any(HoldingRequest.class), eq(7L))).thenReturn(created);

        mockMvc.perform(post("/api/holdings")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticker").value("MSFT"));
    }

    @Test
    void importImage_returns503_whenScannerIsUnavailable() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "statement.png", "image/png", new byte[]{1, 2, 3});

        when(statementScanService.isAvailable()).thenReturn(false);

        mockMvc.perform(multipart("/api/holdings/import/image")
                        .file(file)
                        .principal(authentication))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Image import is not configured on this server"));
    }

    @Test
    void getHistory_returnsTimelinePoints() throws Exception {
        PortfolioHistoryPoint point = new PortfolioHistoryPoint();
        point.setTimestamp(1720000000L);
        point.setValue(new BigDecimal("1200.00"));

        when(userService.getCurrentUserId(authentication)).thenReturn(7L);
        when(holdingService.getAllHoldings(7L)).thenReturn(List.of(holding()));
        when(priceHistoryService.getPortfolioHistory(anyList(), eq("1w"))).thenReturn(List.of(point));

        mockMvc.perform(get("/api/holdings/history")
                        .principal(authentication)
                        .param("range", "1w"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value").value(1200.0));
    }
}
