package com.portfoliom.service;

import com.portfoliom.dto.CustomerRequest;
import com.portfoliom.dto.CustomerSummary;
import com.portfoliom.dto.PerformanceSummary;
import com.portfoliom.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Backs the fund manager's customer management screens: listing customers with a performance
 * snapshot, creating/removing customer accounts, and managing an individual customer's holdings.
 */
@Service
@Transactional
public class CustomerService {

    private final UserService userService;
    private final PortfolioService portfolioService;
    private final HoldingService holdingService;

    public CustomerService(UserService userService, PortfolioService portfolioService, HoldingService holdingService) {
        this.userService = userService;
        this.portfolioService = portfolioService;
        this.holdingService = holdingService;
    }

    @Transactional(readOnly = true)
    public List<CustomerSummary> listCustomers() {
        return userService.listCustomers().stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    public User createCustomer(CustomerRequest request, User manager) {
        return userService.createCustomer(request.getUsername(), request.getPassword(), request.getName(),
                request.getEmail(), manager);
    }

    public void deleteCustomer(Long customerId) {
        User customer = userService.getCustomerById(customerId);
        portfolioService.deleteAllForOwner(customer.getId());
        userService.deleteUser(customer.getId());
    }

    @Transactional(readOnly = true)
    public CustomerSummary getCustomer(Long customerId) {
        return toSummary(userService.getCustomerById(customerId));
    }

    @Transactional(readOnly = true)
    public PerformanceSummary getCustomerHoldings(Long customerId) {
        userService.getCustomerById(customerId);
        return holdingService.getAggregatePerformance(customerId);
    }

    private CustomerSummary toSummary(User user) {
        PerformanceSummary performance = holdingService.getAggregatePerformance(user.getId());
        CustomerSummary summary = new CustomerSummary();
        summary.setId(user.getId());
        summary.setUsername(user.getUsername());
        summary.setName(user.getName());
        summary.setEmail(user.getEmail());
        summary.setCreatedAt(user.getCreatedAt());
        summary.setHoldingCount(performance.getHoldings().size());
        summary.setTotalCostBasis(performance.getTotalCostBasis());
        summary.setTotalCurrentValue(performance.getTotalCurrentValue());
        summary.setTotalGainLoss(performance.getTotalGainLoss());
        summary.setGainLossPercent(performance.getGainLossPercent());
        return summary;
    }
}
