package com.portfoliom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.portfoliom.dto.HoldingRequest;
import com.portfoliom.exception.GlobalExceptionHandler;
import com.portfoliom.model.Holding;
import com.portfoliom.model.HoldingType;
import com.portfoliom.model.Portfolio;
import com.portfoliom.model.Role;
import com.portfoliom.model.User;
import com.portfoliom.service.HoldingService;
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
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class HoldingControllerTest {

    private MockMvc mockMvc;
    private Authentication authentication;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Mock
    private HoldingService holdingService;

    @Mock
    private UserService userService;

    @InjectMocks
    private HoldingController holdingController;

    @BeforeEach
    void setUp() {
        authentication = mock(Authentication.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(holdingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Holding buildHolding() {
        Portfolio p = new Portfolio("Tech Growth", "desc");
        p.setId(1L);

        Holding h = new Holding();
        h.setId(10L);
        h.setPortfolio(p);
        h.setTicker("AAPL");
        h.setName("Apple Inc.");
        h.setType(HoldingType.STOCK);
        h.setQuantity(new BigDecimal("10"));
        h.setPurchasePrice(new BigDecimal("150.00"));
        h.setPurchaseDate(LocalDate.of(2023, 1, 15));
        return h;
    }

    private User owner() {
        User user = new User("owner", "encoded", Role.OWNER);
        user.setId(7L);
        return user;
    }

    @Test
    void getHoldings_returns200() throws Exception {
        when(userService.getCurrentUserId(authentication)).thenReturn(7L);
        when(holdingService.getHoldingsByPortfolio(1L, 7L)).thenReturn(List.of(buildHolding()));

        mockMvc.perform(get("/api/portfolios/1/holdings").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticker").value("AAPL"));
    }

    @Test
    void getHolding_returns200() throws Exception {
        when(userService.getCurrentUserId(authentication)).thenReturn(7L);
        when(holdingService.getHolding(1L, 10L, 7L)).thenReturn(buildHolding());

        mockMvc.perform(get("/api/portfolios/1/holdings/10").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticker").value("AAPL"));
    }

    @Test
    void addHolding_returns201_withValidData() throws Exception {
        HoldingRequest req = new HoldingRequest();
        req.setTicker("TSLA");
        req.setName("Tesla Inc.");
        req.setType(HoldingType.STOCK);
        req.setQuantity(new BigDecimal("5"));
        req.setPurchasePrice(new BigDecimal("200.00"));
        req.setPurchaseDate(LocalDate.of(2023, 3, 1));

        Holding saved = buildHolding();
        saved.setTicker("TSLA");

        when(userService.getCurrentUser(authentication)).thenReturn(owner());
        when(holdingService.addHolding(eq(1L), any(HoldingRequest.class), any(User.class))).thenReturn(saved);

        mockMvc.perform(post("/api/portfolios/1/holdings")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticker").value("TSLA"));
    }

    @Test
    void addHolding_returns400_whenTickerMissing() throws Exception {
        HoldingRequest req = new HoldingRequest();
        req.setName("Apple Inc.");
        req.setType(HoldingType.STOCK);
        req.setQuantity(new BigDecimal("10"));
        req.setPurchasePrice(new BigDecimal("150.00"));

        mockMvc.perform(post("/api/portfolios/1/holdings")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.ticker").value("Ticker is required"));
    }

    @Test
    void removeHolding_returns204() throws Exception {
        when(userService.getCurrentUserId(authentication)).thenReturn(7L);
        doNothing().when(holdingService).removeHolding(1L, 10L, 7L);

        mockMvc.perform(delete("/api/portfolios/1/holdings/10").principal(authentication))
                .andExpect(status().isNoContent());

        verify(holdingService).removeHolding(1L, 10L, 7L);
    }
}
