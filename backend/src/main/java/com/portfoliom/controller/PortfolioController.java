package com.portfoliom.controller;

import com.portfoliom.dto.PerformanceSummary;
import com.portfoliom.dto.PortfolioRequest;
import com.portfoliom.model.Portfolio;
import com.portfoliom.model.User;
import com.portfoliom.service.HoldingService;
import com.portfoliom.service.PortfolioService;
import com.portfoliom.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
@Tag(name = "Portfolios", description = "Portfolio management operations")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final HoldingService holdingService;
    private final UserService userService;

    public PortfolioController(PortfolioService portfolioService, HoldingService holdingService, UserService userService) {
        this.portfolioService = portfolioService;
        this.holdingService = holdingService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List all portfolios for the current user")
    public List<Portfolio> getAllPortfolios(Authentication authentication) {
        return portfolioService.getAll(userService.getCurrentUserId(authentication));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a portfolio by ID")
    public Portfolio getPortfolio(@PathVariable Long id, Authentication authentication) {
        return portfolioService.getById(id, userService.getCurrentUserId(authentication));
    }

    @PostMapping
    @Operation(summary = "Create a new portfolio")
    public ResponseEntity<Portfolio> createPortfolio(@Valid @RequestBody PortfolioRequest request, Authentication authentication) {
        User owner = userService.getCurrentUser(authentication);
        Portfolio created = portfolioService.create(request, owner);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a portfolio")
    public Portfolio updatePortfolio(@PathVariable Long id, @Valid @RequestBody PortfolioRequest request, Authentication authentication) {
        return portfolioService.update(id, request, userService.getCurrentUserId(authentication));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a portfolio")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long id, Authentication authentication) {
        portfolioService.delete(id, userService.getCurrentUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/performance")
    @Operation(summary = "Get portfolio performance summary")
    public PerformanceSummary getPerformance(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean refresh,
            Authentication authentication) {
        return holdingService.getPerformance(id, userService.getCurrentUserId(authentication), refresh);
    }
}
