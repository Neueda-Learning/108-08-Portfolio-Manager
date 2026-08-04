package com.example.controller;

import com.example.model.Customer;
import com.example.model.Portfolio;
import com.example.security.RoleAccess;
import com.example.security.RoleAccess.Role;
import com.example.service.CustomerService;
import com.example.service.PortfolioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService customerService;
    private final PortfolioService portfolioService;

    public CustomerController(CustomerService customerService, PortfolioService portfolioService) {
        this.customerService = customerService;
        this.portfolioService = portfolioService;
    }

    @GetMapping
    public List<Customer> findAll(@RequestHeader("X-User-Role") String roleHeader) {
        Role role = RoleAccess.parseRole(roleHeader);
        RoleAccess.requireAdminOrFundManager(role);
        return customerService.findAll();
    }

    @GetMapping("/{id}")
    public Customer findById(@PathVariable Long id,
                             @RequestHeader("X-User-Role") String roleHeader,
                             @RequestHeader(value = "X-Customer-Id", required = false) Long customerIdHeader) {
        Role role = RoleAccess.parseRole(roleHeader);
        if (role == Role.CUSTOMER && (customerIdHeader == null || !id.equals(customerIdHeader))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer can only view own profile");
        }
        return customerService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    @GetMapping("/{id}/portfolios")
    public List<Portfolio> getCustomerPortfolios(@PathVariable Long id,
                                                 @RequestHeader("X-User-Role") String roleHeader,
                                                 @RequestHeader(value = "X-Customer-Id", required = false) Long customerIdHeader) {
        Role role = RoleAccess.parseRole(roleHeader);
        if (role == Role.CUSTOMER && (customerIdHeader == null || !id.equals(customerIdHeader))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer can only view own portfolio");
        }
        return portfolioService.findByCustomerId(id);
    }

    @PostMapping
    public ResponseEntity<Customer> create(@RequestBody Customer customer,
                                           @RequestHeader("X-User-Role") String roleHeader) {
        RoleAccess.requireAdminOrFundManager(RoleAccess.parseRole(roleHeader));
        customer.setId(null);
        if (customer.getCreatedAt() == null) {
            customer.setCreatedAt(LocalDateTime.now());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(customer));
    }

    @PutMapping("/{id}")
    public Customer update(@PathVariable Long id,
                           @RequestBody Customer customer,
                           @RequestHeader("X-User-Role") String roleHeader) {
        RoleAccess.requireAdminOrFundManager(RoleAccess.parseRole(roleHeader));
        Customer existing = customerService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        customer.setId(id);
        if (customer.getCreatedAt() == null) {
            customer.setCreatedAt(existing.getCreatedAt());
        }
        int updated = customerService.update(customer);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }
        return customerService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestHeader("X-User-Role") String roleHeader) {
        RoleAccess.requireAdminOrFundManager(RoleAccess.parseRole(roleHeader));
        int deleted = customerService.delete(id);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }
        return ResponseEntity.noContent().build();
    }
}
