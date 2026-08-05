package com.portfoliom.controller;

import com.portfoliom.dto.HoldingImportResult;
import com.portfoliom.dto.HoldingRequest;
import com.portfoliom.dto.PerformanceSummary;
import com.portfoliom.dto.PortfolioHistoryPoint;
import com.portfoliom.model.Holding;
import com.portfoliom.model.Portfolio;
import com.portfoliom.model.User;
import com.portfoliom.service.ExportService;
import com.portfoliom.service.HoldingImportService;
import com.portfoliom.service.HoldingService;
import com.portfoliom.service.PortfolioService;
import com.portfoliom.service.PriceHistoryService;
import com.portfoliom.service.StatementScanService;
import com.portfoliom.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Flat, cross-portfolio view of holdings backing the Dashboard and Reports pages, so the UI
 * does not need to navigate per-portfolio. Scoped to the current authenticated user.
 */
@RestController
@RequestMapping("/api/holdings")
@Tag(name = "Holdings (flat)", description = "Cross-portfolio holdings, import, export, and history")
public class HoldingsController {

    private final HoldingService holdingService;
    private final PortfolioService portfolioService;
    private final HoldingImportService holdingImportService;
    private final StatementScanService statementScanService;
    private final ExportService exportService;
    private final PriceHistoryService priceHistoryService;
    private final UserService userService;

    public HoldingsController(HoldingService holdingService, PortfolioService portfolioService,
                               HoldingImportService holdingImportService, StatementScanService statementScanService,
                               ExportService exportService, PriceHistoryService priceHistoryService,
                               UserService userService) {
        this.holdingService = holdingService;
        this.portfolioService = portfolioService;
        this.holdingImportService = holdingImportService;
        this.statementScanService = statementScanService;
        this.exportService = exportService;
        this.priceHistoryService = priceHistoryService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get performance for every holding across all portfolios")
    public PerformanceSummary getAllHoldings(Authentication authentication) {
        return holdingService.getAggregatePerformance(userService.getCurrentUserId(authentication));
    }

    @PostMapping
    @Operation(summary = "Add a holding (merges into an existing holding with the same ticker)")
    public ResponseEntity<Holding> addHolding(@Valid @RequestBody HoldingRequest request, Authentication authentication) {
        User owner = userService.getCurrentUser(authentication);
        Portfolio portfolio = portfolioService.getOrCreateDefault(owner);
        Holding created = holdingService.mergeOrCreate(portfolio, request, owner.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a holding")
    public Holding updateHolding(@PathVariable Long id, @Valid @RequestBody HoldingRequest request, Authentication authentication) {
        return holdingService.updateHoldingById(id, request, userService.getCurrentUserId(authentication));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a holding")
    public ResponseEntity<Void> deleteHolding(@PathVariable Long id, Authentication authentication) {
        holdingService.removeHoldingById(id, userService.getCurrentUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import holdings from a CSV file, merging duplicate tickers")
    public ResponseEntity<HoldingImportResult> importCsv(@RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
        return ResponseEntity.ok(holdingImportService.importCsv(file, userService.getCurrentUser(authentication)));
    }

    @GetMapping("/import/csv/sample")
    @Operation(summary = "Download a sample CSV template for holding import")
    public ResponseEntity<org.springframework.core.io.Resource> sampleCsv() {
        org.springframework.core.io.Resource resource = new ClassPathResource("sample-holdings.csv");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sample-holdings.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }

    @PostMapping(value = "/import/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import holdings by reading a statement screenshot")
    public ResponseEntity<?> importImage(@RequestParam("file") MultipartFile file, Authentication authentication) {
        if (!statementScanService.isAvailable()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "Image import is not configured on this server"));
        }
        try {
            List<HoldingRequest> extracted = statementScanService.extractHoldings(file);
            return ResponseEntity.ok(holdingImportService.importRequests(extracted, userService.getCurrentUser(authentication)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Export the holdings report as CSV")
    public ResponseEntity<byte[]> exportCsv(Authentication authentication) {
        byte[] csv = exportService.toCsv(holdingService.getAggregatePerformance(userService.getCurrentUserId(authentication)));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=portfoliom-holdings.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/export/pdf")
    @Operation(summary = "Export the holdings report as PDF")
    public ResponseEntity<byte[]> exportPdf(Authentication authentication) {
        byte[] pdf = exportService.toPdf(holdingService.getAggregatePerformance(userService.getCurrentUserId(authentication)));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=portfoliom-holdings.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/history")
    @Operation(summary = "Get portfolio value over time (range=1d|1w|1m|all)")
    public List<PortfolioHistoryPoint> getHistory(@RequestParam(defaultValue = "1m") String range, Authentication authentication) {
        List<Holding> holdings = holdingService.getAllHoldings(userService.getCurrentUserId(authentication));
        return priceHistoryService.getPortfolioHistory(holdings, range);
    }
}
