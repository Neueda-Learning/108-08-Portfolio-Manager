package com.example.controller;

import com.example.model.PortfolioAnalytics;
import com.example.model.Portfolio;
import com.example.service.PortfolioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {
    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping
    public List<Portfolio> findAll() {
        return portfolioService.findAll();
    }

    @GetMapping("/{id}")
    public Portfolio findById(@PathVariable Long id) {
        return portfolioService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));
    }

    @GetMapping("/{id}/analytics")
    public PortfolioAnalytics analytics(@PathVariable Long id,
                                        @RequestParam(name = "benchmark", defaultValue = "NIFTY") String benchmark) {
        return portfolioService.getPortfolioAnalytics(id, benchmark)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));
    }

    @PostMapping
    public ResponseEntity<Portfolio> create(@RequestBody Portfolio portfolio) {
        portfolio.setId(null);
        if (portfolio.getCreatedAt() == null) {
            portfolio.setCreatedAt(LocalDateTime.now());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolioService.create(portfolio));
    }

    @PutMapping("/{id}")
    public Portfolio update(@PathVariable Long id, @RequestBody Portfolio portfolio) {
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
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        int deleted = portfolioService.delete(id);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found");
        }
        return ResponseEntity.noContent().build();
    }
}
