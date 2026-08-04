package com.example.controller;

import com.example.model.PortfolioAnalytics;
import com.example.model.Portfolio;
import com.example.model.Customer;
import com.example.security.RoleAccess;
import com.example.security.RoleAccess.Role;
import com.example.service.CustomerService;
import com.example.service.PortfolioService;
import com.example.service.PortfolioService.PurchaseResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {
    private final PortfolioService portfolioService;
    private final CustomerService customerService;

    public PortfolioController(PortfolioService portfolioService, CustomerService customerService) {
        this.portfolioService = portfolioService;
        this.customerService = customerService;
    }

    @GetMapping
    public List<Portfolio> findAll(@RequestHeader("X-User-Role") String roleHeader) {
        Role role = RoleAccess.parseRole(roleHeader);
        RoleAccess.requireAdminOrFundManager(role);
        return portfolioService.findAll();
    }

    @GetMapping("/{id}")
    public Portfolio findById(@PathVariable Long id,
                              @RequestHeader("X-User-Role") String roleHeader,
                              @RequestHeader(value = "X-Customer-Id", required = false) Long customerIdHeader) {
        Role role = RoleAccess.parseRole(roleHeader);
        if (role == Role.CUSTOMER && (customerIdHeader == null
                || !portfolioService.isPortfolioOwnedByCustomer(id, customerIdHeader))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer can only view own portfolio");
        }
        return portfolioService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));
    }

    @GetMapping("/{id}/analytics")
    public PortfolioAnalytics analytics(@PathVariable Long id,
                                        @RequestHeader("X-User-Role") String roleHeader,
                                        @RequestHeader(value = "X-Customer-Id", required = false) Long customerIdHeader,
                                        @RequestParam(name = "benchmark", defaultValue = "NIFTY") String benchmark) {
        Role role = RoleAccess.parseRole(roleHeader);
        if (role == Role.CUSTOMER && (customerIdHeader == null
                || !portfolioService.isPortfolioOwnedByCustomer(id, customerIdHeader))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer can only view own analytics");
        }
        return portfolioService.getPortfolioAnalytics(id, benchmark)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));
    }

    @PostMapping
    public ResponseEntity<Portfolio> create(@RequestBody Portfolio portfolio,
                                            @RequestHeader("X-User-Role") String roleHeader) {
        RoleAccess.requireAdminOrFundManager(RoleAccess.parseRole(roleHeader));
        portfolio.setId(null);
        if (portfolio.getCreatedAt() == null) {
            portfolio.setCreatedAt(LocalDateTime.now());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolioService.create(portfolio));
    }

    @PutMapping("/{id}")
    public Portfolio update(@PathVariable Long id,
                            @RequestBody Portfolio portfolio,
                            @RequestHeader("X-User-Role") String roleHeader) {
        RoleAccess.requireAdminOrFundManager(RoleAccess.parseRole(roleHeader));
        Portfolio existing = portfolioService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));
        portfolio.setId(id);
        if (portfolio.getCreatedAt() == null) {
            portfolio.setCreatedAt(existing.getCreatedAt());
        }
        int updated = portfolioService.update(portfolio);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found");
        }
        return portfolioService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestHeader("X-User-Role") String roleHeader) {
        RoleAccess.requireAdminOrFundManager(RoleAccess.parseRole(roleHeader));
        int deleted = portfolioService.delete(id);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found");
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/transactions/purchase")
    public PurchaseResult purchase(@PathVariable Long id,
                                   @RequestBody PurchaseRequest request,
                                   @RequestHeader("X-User-Role") String roleHeader,
                                   @RequestHeader(value = "X-Fund-Manager-Id", required = false) Long fundManagerIdHeader) {
        Role role = RoleAccess.parseRole(roleHeader);
        RoleAccess.requireAdminOrFundManager(role);

        if (role == Role.FUND_MANAGER) {
            if (fundManagerIdHeader == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "X-Fund-Manager-Id header is required");
            }
            Portfolio portfolio = portfolioService.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));
            Customer customer = customerService.findById(portfolio.getCustomerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
            if (!fundManagerIdHeader.equals(customer.getFundManagerId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Fund manager can only buy for own customers");
            }
        }

        return portfolioService.buyAsset(id, request.getAssetSymbol(), request.getQuantity(), request.getPrice());
    }

    public static class PurchaseRequest {
        private String assetSymbol;
        private BigDecimal quantity;
        private BigDecimal price;

        public String getAssetSymbol() {
            return assetSymbol;
        }

        public void setAssetSymbol(String assetSymbol) {
            this.assetSymbol = assetSymbol;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
}
