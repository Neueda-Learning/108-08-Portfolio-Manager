package com.portfoliom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.portfoliom.dto.CustomerRequest;
import com.portfoliom.dto.PerformanceSummary;
import com.portfoliom.dto.PortfolioHistoryPoint;
import com.portfoliom.exception.GlobalExceptionHandler;
import com.portfoliom.model.Holding;
import com.portfoliom.model.HoldingType;
import com.portfoliom.model.Portfolio;
import com.portfoliom.model.Role;
import com.portfoliom.model.User;
import com.portfoliom.service.CustomerService;
import com.portfoliom.service.HoldingService;
import com.portfoliom.service.PortfolioService;
import com.portfoliom.service.PriceHistoryService;
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
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ManagerControllerTest {

    private MockMvc mockMvc;
    private Authentication authentication;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private CustomerService customerService;

    @Mock
    private UserService userService;

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private HoldingService holdingService;

    @Mock
    private PriceHistoryService priceHistoryService;

    @InjectMocks
    private ManagerController managerController;

    @BeforeEach
    void setUp() {
        authentication = mock(Authentication.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(managerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private User manager() {
        User manager = new User("fundmanager", "encoded", Role.FUND_MANAGER);
        manager.setId(1L);
        manager.setName("Fund Manager");
        return manager;
    }

    private User customer() {
        User customer = new User("customer1", "encoded", Role.OWNER);
        customer.setId(9L);
        customer.setName("Customer One");
        customer.setEmail("customer1@example.com");
        return customer;
    }

    @Test
    void getCustomerHoldings_passesRefreshFlag() throws Exception {
        PerformanceSummary summary = new PerformanceSummary();
        summary.setPortfolioName("All Holdings");
        summary.setTotalCurrentValue(new BigDecimal("2500.00"));
        summary.setPricesAsOf(Instant.parse("2024-01-01T10:00:00Z"));

        when(customerService.getCustomerHoldings(9L, true)).thenReturn(summary);

        mockMvc.perform(get("/api/manager/customers/9/holdings").param("refresh", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCurrentValue").value(2500.0));

        verify(customerService).getCustomerHoldings(9L, true);
    }

    @Test
    void createCustomer_returns400_whenUsernameAlreadyExists() throws Exception {
        CustomerRequest request = new CustomerRequest();
        request.setUsername("customer1");
        request.setPassword("Password123!");
        request.setName("Customer One");
        request.setEmail("customer1@example.com");

        when(userService.getCurrentUser(authentication)).thenReturn(manager());
        when(customerService.createCustomer(any(CustomerRequest.class), any(User.class)))
                .thenThrow(new IllegalArgumentException("Username 'customer1' is already taken"));

        mockMvc.perform(post("/api/manager/customers")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username 'customer1' is already taken"));
    }

    @Test
    void addCustomerHolding_usesDefaultPortfolioAndMergeOrCreate() throws Exception {
        com.portfoliom.dto.HoldingRequest request = new com.portfoliom.dto.HoldingRequest();
        request.setTicker("AAPL");
        request.setName("Apple Inc.");
        request.setType(HoldingType.STOCK);
        request.setQuantity(new BigDecimal("4"));
        request.setPurchasePrice(new BigDecimal("190.00"));

        User customer = customer();
        Portfolio portfolio = new Portfolio("Customer Portfolio", "Default portfolio", customer);
        portfolio.setId(3L);
        Holding created = new Holding();
        created.setId(40L);
        created.setPortfolio(portfolio);
        created.setTicker("AAPL");
        created.setName("Apple Inc.");
        created.setType(HoldingType.STOCK);
        created.setQuantity(new BigDecimal("4"));
        created.setPurchasePrice(new BigDecimal("190.00"));

        when(userService.getCustomerById(9L)).thenReturn(customer);
        when(portfolioService.getOrCreateDefault(customer)).thenReturn(portfolio);
        when(holdingService.mergeOrCreate(eq(portfolio), any(com.portfoliom.dto.HoldingRequest.class), eq(9L)))
                .thenReturn(created);

        mockMvc.perform(post("/api/manager/customers/9/holdings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(40));
    }

    @Test
    void getCustomerHistory_returnsTimelinePoints() throws Exception {
        Holding holding = new Holding();
        holding.setTicker("AAPL");

        PortfolioHistoryPoint point = new PortfolioHistoryPoint();
        point.setTimestamp(1720000000L);
        point.setValue(new BigDecimal("2500.00"));

        when(holdingService.getAllHoldings(9L)).thenReturn(List.of(holding));
        when(priceHistoryService.getPortfolioHistory(anyList(), eq("1m"))).thenReturn(List.of(point));

        mockMvc.perform(get("/api/manager/customers/9/holdings/history").param("range", "1m"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value").value(2500.0));
    }
}
