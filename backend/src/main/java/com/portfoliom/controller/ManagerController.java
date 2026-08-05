package com.portfoliom.controller;

import com.portfoliom.dto.CustomerRequest;
import com.portfoliom.dto.CustomerSummary;
import com.portfoliom.dto.HoldingRequest;
import com.portfoliom.dto.PerformanceSummary;
import com.portfoliom.dto.PortfolioHistoryPoint;
import com.portfoliom.model.Holding;
import com.portfoliom.model.Portfolio;
import com.portfoliom.model.User;
import com.portfoliom.service.CustomerService;
import com.portfoliom.service.HoldingService;
import com.portfoliom.service.PortfolioService;
import com.portfoliom.service.PriceHistoryService;
import com.portfoliom.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Fund manager-only endpoints for administering customer (owner) accounts and their portfolios.
 * Restricted to the FUND_MANAGER role in SecurityConfig.
 */
@RestController
@RequestMapping("/api/manager")
@Tag(name = "Fund Manager", description = "Manage customer accounts and their holdings")
public class ManagerController {

    private final CustomerService customerService;
    private final UserService userService;
    private final PortfolioService portfolioService;
    private final HoldingService holdingService;
    private final PriceHistoryService priceHistoryService;

    public ManagerController(CustomerService customerService, UserService userService, PortfolioService portfolioService,
                              HoldingService holdingService, PriceHistoryService priceHistoryService) {
        this.customerService = customerService;
        this.userService = userService;
        this.portfolioService = portfolioService;
        this.holdingService = holdingService;
        this.priceHistoryService = priceHistoryService;
    }

    @GetMapping("/customers")
    @Operation(summary = "List all customers with a performance snapshot")
    public List<CustomerSummary> listCustomers() {
        return customerService.listCustomers();
    }

    @PostMapping("/customers")
    @Operation(summary = "Create a new customer account")
    public ResponseEntity<?> createCustomer(@Valid @RequestBody CustomerRequest request, Authentication authentication) {
        try {
            User manager = userService.getCurrentUser(authentication);
            User created = customerService.createCustomer(request, manager);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/customers/{id}")
    @Operation(summary = "Remove a customer account and all of their data")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/customers/{id}")
    @Operation(summary = "Get a single customer's profile and performance snapshot")
    public CustomerSummary getCustomer(@PathVariable Long id) {
        return customerService.getCustomer(id);
    }

    @GetMapping("/customers/{id}/holdings")
    @Operation(summary = "Get a customer's aggregate holdings performance")
    public PerformanceSummary getCustomerHoldings(@PathVariable Long id) {
        return customerService.getCustomerHoldings(id);
    }

    @PostMapping("/customers/{id}/holdings")
    @Operation(summary = "Add a holding to a customer's portfolio")
    public ResponseEntity<Holding> addCustomerHolding(@PathVariable Long id, @Valid @RequestBody HoldingRequest request) {
        User customer = userService.getCustomerById(id);
        Portfolio portfolio = portfolioService.getOrCreateDefault(customer);
        Holding created = holdingService.mergeOrCreate(portfolio, request, customer.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/customers/{id}/holdings/{holdingId}")
    @Operation(summary = "Update a customer's holding")
    public Holding updateCustomerHolding(@PathVariable Long id, @PathVariable Long holdingId,
                                         @Valid @RequestBody HoldingRequest request) {
        return holdingService.updateHoldingById(holdingId, request, id);
    }

    @DeleteMapping("/customers/{id}/holdings/{holdingId}")
    @Operation(summary = "Remove a customer's holding")
    public ResponseEntity<Void> deleteCustomerHolding(@PathVariable Long id, @PathVariable Long holdingId) {
        holdingService.removeHoldingById(holdingId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/customers/{id}/holdings/history")
    @Operation(summary = "Get a customer's portfolio value over time (range=1d|1w|1m|all)")
    public List<PortfolioHistoryPoint> getCustomerHistory(@PathVariable Long id,
                                                           @RequestParam(defaultValue = "1m") String range) {
        List<Holding> holdings = holdingService.getAllHoldings(id);
        return priceHistoryService.getPortfolioHistory(holdings, range);
    }
}
