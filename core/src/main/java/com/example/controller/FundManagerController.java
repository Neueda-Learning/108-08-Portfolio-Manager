package com.example.controller;

import com.example.model.Customer;
import com.example.model.FundManager;
import com.example.security.RoleAccess;
import com.example.security.RoleAccess.Role;
import com.example.service.CustomerService;
import com.example.service.FundManagerService;
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
@RequestMapping("/api/fund-managers")
public class FundManagerController {
    private final FundManagerService fundManagerService;
    private final CustomerService customerService;

    public FundManagerController(FundManagerService fundManagerService, CustomerService customerService) {
        this.fundManagerService = fundManagerService;
        this.customerService = customerService;
    }

    @GetMapping
    public List<FundManager> findAll(@RequestHeader("X-User-Role") String roleHeader) {
        Role role = RoleAccess.parseRole(roleHeader);
        RoleAccess.requireAdminOrFundManager(role);
        return fundManagerService.findAll();
    }

    @GetMapping("/{id}")
    public FundManager findById(@PathVariable Long id,
                                @RequestHeader("X-User-Role") String roleHeader,
                                @RequestHeader(value = "X-Fund-Manager-Id", required = false) Long fundManagerIdHeader) {
        Role role = RoleAccess.parseRole(roleHeader);
        if (role == Role.FUND_MANAGER && (fundManagerIdHeader == null || !id.equals(fundManagerIdHeader))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Fund manager can only view own profile");
        }
        if (role == Role.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer cannot access fund manager profile");
        }
        return fundManagerService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fund manager not found"));
    }

    @PostMapping
    public ResponseEntity<FundManager> create(@RequestBody FundManager fundManager,
                                              @RequestHeader("X-User-Role") String roleHeader) {
        RoleAccess.requireAdmin(RoleAccess.parseRole(roleHeader));
        fundManager.setId(null);
        if (fundManager.getCreatedAt() == null) {
            fundManager.setCreatedAt(LocalDateTime.now());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(fundManagerService.create(fundManager));
    }

    @GetMapping("/{id}/customers")
    public List<Customer> findCustomers(@PathVariable Long id,
                                        @RequestHeader("X-User-Role") String roleHeader,
                                        @RequestHeader(value = "X-Fund-Manager-Id", required = false) Long fundManagerIdHeader) {
        Role role = RoleAccess.parseRole(roleHeader);
        if (role == Role.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer cannot access fund manager customer list");
        }
        if (role == Role.FUND_MANAGER && (fundManagerIdHeader == null || !id.equals(fundManagerIdHeader))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Fund manager can only view own customers");
        }
        return customerService.findByFundManagerId(id);
    }

    @PutMapping("/{id}")
    public FundManager update(@PathVariable Long id,
                              @RequestBody FundManager fundManager,
                              @RequestHeader("X-User-Role") String roleHeader) {
        RoleAccess.requireAdmin(RoleAccess.parseRole(roleHeader));
        FundManager existing = fundManagerService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fund manager not found"));
        fundManager.setId(id);
        if (fundManager.getCreatedAt() == null) {
            fundManager.setCreatedAt(existing.getCreatedAt());
        }
        int updated = fundManagerService.update(fundManager);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fund manager not found");
        }
        return fundManagerService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fund manager not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestHeader("X-User-Role") String roleHeader) {
        RoleAccess.requireAdmin(RoleAccess.parseRole(roleHeader));
        int deleted = fundManagerService.delete(id);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fund manager not found");
        }
        return ResponseEntity.noContent().build();
    }
}
