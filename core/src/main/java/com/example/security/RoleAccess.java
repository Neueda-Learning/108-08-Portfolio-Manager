package com.example.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

public final class RoleAccess {

    public enum Role {
        ADMIN,
        FUND_MANAGER,
        CUSTOMER
    }

    private RoleAccess() {
    }

    public static Role parseRole(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "X-User-Role header is required");
        }
        try {
            return Role.valueOf(rawRole.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unsupported user role");
        }
    }

    public static void requireAdminOrFundManager(Role role) {
        if (role != Role.ADMIN && role != Role.FUND_MANAGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin or fund manager can modify portfolios");
        }
    }

    public static void requireAdmin(Role role) {
        if (role != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin can perform this action");
        }
    }

    public static void requireCustomer(Role role) {
        if (role != Role.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer role required");
        }
    }
}
