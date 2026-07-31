package com.example.controller;

import com.example.model.FundManager;
import com.example.service.FundManagerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/fund-managers")
public class FundManagerController {
    private final FundManagerService fundManagerService;

    public FundManagerController(FundManagerService fundManagerService) {
        this.fundManagerService = fundManagerService;
    }

    @GetMapping
    public List<FundManager> findAll() {
        return fundManagerService.findAll();
    }

    @GetMapping("/{id}")
    public FundManager findById(@PathVariable Long id) {
        return fundManagerService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fund manager not found"));
    }

    @PostMapping
    public ResponseEntity<FundManager> create(@RequestBody FundManager fundManager) {
        fundManager.setId(null);
        if (fundManager.getCreatedAt() == null) {
            fundManager.setCreatedAt(LocalDateTime.now());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(fundManagerService.create(fundManager));
    }

    @PutMapping("/{id}")
    public FundManager update(@PathVariable Long id, @RequestBody FundManager fundManager) {
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
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        int deleted = fundManagerService.delete(id);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fund manager not found");
        }
        return ResponseEntity.noContent().build();
    }
}
