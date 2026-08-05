package com.portfoliom.controller;

import com.portfoliom.dto.HoldingRequest;
import com.portfoliom.model.Holding;
import com.portfoliom.service.HoldingService;
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
@RequestMapping("/api/portfolios/{portfolioId}/holdings")
@Tag(name = "Holdings", description = "Portfolio holding operations")
public class HoldingController {

    private final HoldingService holdingService;
    private final UserService userService;

    public HoldingController(HoldingService holdingService, UserService userService) {
        this.holdingService = holdingService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List all holdings in a portfolio")
    public List<Holding> getHoldings(@PathVariable Long portfolioId, Authentication authentication) {
        return holdingService.getHoldingsByPortfolio(portfolioId, userService.getCurrentUserId(authentication));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific holding")
    public Holding getHolding(@PathVariable Long portfolioId, @PathVariable Long id, Authentication authentication) {
        return holdingService.getHolding(portfolioId, id, userService.getCurrentUserId(authentication));
    }

    @PostMapping
    @Operation(summary = "Add a holding to a portfolio")
    public ResponseEntity<Holding> addHolding(
            @PathVariable Long portfolioId,
            @Valid @RequestBody HoldingRequest request,
            Authentication authentication) {
        Holding created = holdingService.addHolding(portfolioId, request, userService.getCurrentUser(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a holding")
    public Holding updateHolding(
            @PathVariable Long portfolioId,
            @PathVariable Long id,
            @Valid @RequestBody HoldingRequest request,
            Authentication authentication) {
        return holdingService.updateHolding(portfolioId, id, request, userService.getCurrentUserId(authentication));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a holding from a portfolio")
    public ResponseEntity<Void> removeHolding(@PathVariable Long portfolioId, @PathVariable Long id, Authentication authentication) {
        holdingService.removeHolding(portfolioId, id, userService.getCurrentUserId(authentication));
        return ResponseEntity.noContent().build();
    }
}
