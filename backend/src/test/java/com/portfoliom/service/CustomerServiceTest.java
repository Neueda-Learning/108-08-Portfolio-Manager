package com.portfoliom.service;

import com.portfoliom.dto.CustomerSummary;
import com.portfoliom.dto.HoldingPerformance;
import com.portfoliom.dto.PerformanceSummary;
import com.portfoliom.model.Role;
import com.portfoliom.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private HoldingService holdingService;

    @InjectMocks
    private CustomerService customerService;

    private User customer() {
        User customer = new User("customer1", "encoded", Role.OWNER);
        customer.setId(9L);
        customer.setName("Customer One");
        customer.setEmail("customer1@example.com");
        customer.setCreatedAt(LocalDateTime.of(2024, 1, 2, 10, 30));
        return customer;
    }

    private PerformanceSummary performanceSummary() {
        PerformanceSummary summary = new PerformanceSummary();
        summary.setTotalCostBasis(new BigDecimal("1000.00"));
        summary.setTotalCurrentValue(new BigDecimal("1200.00"));
        summary.setTotalGainLoss(new BigDecimal("200.00"));
        summary.setGainLossPercent(new BigDecimal("20.00"));
        summary.setHoldings(List.of(new HoldingPerformance()));
        return summary;
    }

    @Test
    void listCustomers_mapsPerformanceIntoSummaries() {
        User customer = customer();
        PerformanceSummary performance = performanceSummary();

        when(userService.listCustomers()).thenReturn(List.of(customer));
        when(holdingService.getAggregatePerformance(9L)).thenReturn(performance);

        List<CustomerSummary> result = customerService.listCustomers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("customer1");
        assertThat(result.get(0).getHoldingCount()).isEqualTo(1);
        assertThat(result.get(0).getTotalCurrentValue()).isEqualByComparingTo("1200.00");
    }

    @Test
    void getCustomerHoldings_passesRefreshFlagThrough() {
        User customer = customer();
        PerformanceSummary performance = performanceSummary();

        when(userService.getCustomerById(9L)).thenReturn(customer);
        when(holdingService.getAggregatePerformance(9L, true)).thenReturn(performance);

        PerformanceSummary result = customerService.getCustomerHoldings(9L, true);

        assertThat(result).isSameAs(performance);
    }

    @Test
    void deleteCustomer_deletesPortfoliosBeforeDeletingUser() {
        User customer = customer();
        when(userService.getCustomerById(9L)).thenReturn(customer);

        customerService.deleteCustomer(9L);

        InOrder inOrder = inOrder(portfolioService, userService);
        inOrder.verify(portfolioService).deleteAllForOwner(9L);
        inOrder.verify(userService).deleteUser(9L);
    }
}
