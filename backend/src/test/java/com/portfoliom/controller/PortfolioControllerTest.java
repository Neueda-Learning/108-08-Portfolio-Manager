package com.portfoliom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfoliom.dto.PerformanceSummary;
import com.portfoliom.dto.PortfolioRequest;
import com.portfoliom.exception.GlobalExceptionHandler;
import com.portfoliom.exception.ResourceNotFoundException;
import com.portfoliom.model.Portfolio;
import com.portfoliom.model.Role;
import com.portfoliom.model.User;
import com.portfoliom.service.HoldingService;
import com.portfoliom.service.PortfolioService;
import com.portfoliom.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PortfolioControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Authentication authentication;

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private HoldingService holdingService;

    @Mock
    private UserService userService;

    @InjectMocks
    private PortfolioController portfolioController;

    @BeforeEach
    void setUp() {
        authentication = mock(Authentication.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(portfolioController)
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

    private Portfolio portfolio(long id, String name) {
        Portfolio portfolio = new Portfolio(name, "Description");
        portfolio.setId(id);
        return portfolio;
    }

    @Test
    void getAllPortfolios_returns200() throws Exception {
        when(userService.getCurrentUserId(authentication)).thenReturn(7L);
        when(portfolioService.getAll(7L)).thenReturn(List.of(portfolio(1L, "Tech Growth")));

        mockMvc.perform(get("/api/portfolios").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Tech Growth"));
    }

    @Test
    void getPortfolio_returns200_whenExists() throws Exception {
        when(userService.getCurrentUserId(authentication)).thenReturn(7L);
        when(portfolioService.getById(1L, 7L)).thenReturn(portfolio(1L, "Tech Growth"));

        mockMvc.perform(get("/api/portfolios/1").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getPortfolio_returns404_whenNotFound() throws Exception {
        when(userService.getCurrentUserId(authentication)).thenReturn(7L);
        when(portfolioService.getById(99L, 7L))
                .thenThrow(new ResourceNotFoundException("Portfolio not found with id: 99"));

        mockMvc.perform(get("/api/portfolios/99").principal(authentication))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Portfolio not found with id: 99"));
    }

    @Test
    void createPortfolio_returns201_withValidData() throws Exception {
        PortfolioRequest request = new PortfolioRequest();
        request.setName("New Portfolio");
        request.setDescription("Retirement");

        User owner = owner();
        Portfolio saved = new Portfolio("New Portfolio", "Retirement", owner);
        saved.setId(2L);
        when(userService.getCurrentUser(authentication)).thenReturn(owner);
        when(portfolioService.create(any(PortfolioRequest.class), eq(owner))).thenReturn(saved);

        mockMvc.perform(post("/api/portfolios")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Portfolio"));
    }

    @Test
    void createPortfolio_returns400_whenNameMissing() throws Exception {
        PortfolioRequest request = new PortfolioRequest();

        mockMvc.perform(post("/api/portfolios")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("Name is required"));
    }

    @Test
    void deletePortfolio_returns204() throws Exception {
        when(userService.getCurrentUserId(authentication)).thenReturn(7L);
        doNothing().when(portfolioService).delete(1L, 7L);

        mockMvc.perform(delete("/api/portfolios/1").principal(authentication))
                .andExpect(status().isNoContent());

        verify(portfolioService).delete(1L, 7L);
    }

    @Test
    void getPerformance_forwardsRefreshFlag() throws Exception {
        PerformanceSummary summary = new PerformanceSummary();
        summary.setPortfolioId(1L);
        summary.setPortfolioName("Core Portfolio");
        summary.setTotalCurrentValue(new BigDecimal("1800.00"));

        when(userService.getCurrentUserId(authentication)).thenReturn(7L);
        when(holdingService.getPerformance(1L, 7L, true)).thenReturn(summary);

        mockMvc.perform(get("/api/portfolios/1/performance")
                        .principal(authentication)
                        .param("refresh", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioName").value("Core Portfolio"));

        verify(holdingService).getPerformance(1L, 7L, true);
    }
}
