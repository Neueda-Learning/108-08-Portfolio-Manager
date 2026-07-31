package com.example.controller;

import com.example.model.Account;
import com.example.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("status", "ok", "message", "Core API is connected");
    }

    @GetMapping("/accounts")
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @PostMapping("/accounts")
    public ResponseEntity<Account> createAccount(@RequestBody CreateAccountRequest request) {
        if (isBlank(request.name()) || isBlank(request.email())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name and email are required");
        }

        Account account = accountService.createAccount(new Account(0, request.name().trim(), request.email().trim()));
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public record CreateAccountRequest(String name, String email) {
    }
}
