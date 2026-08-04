package com.example.controller;

import com.example.model.Asset;
import com.example.security.RoleAccess;
import com.example.service.AssetService;
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

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {
    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    public List<Asset> findAll(@RequestHeader("X-User-Role") String roleHeader) {
        RoleAccess.parseRole(roleHeader);
        return assetService.findAll();
    }

    @GetMapping("/{id}")
    public Asset findById(@PathVariable Long id, @RequestHeader("X-User-Role") String roleHeader) {
        RoleAccess.parseRole(roleHeader);
        return assetService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));
    }

    @PostMapping
    public ResponseEntity<Asset> create(@RequestBody Asset asset, @RequestHeader("X-User-Role") String roleHeader) {
        RoleAccess.requireAdminOrFundManager(RoleAccess.parseRole(roleHeader));
        asset.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(assetService.create(asset));
    }

    @PutMapping("/{id}")
    public Asset update(@PathVariable Long id,
                        @RequestBody Asset asset,
                        @RequestHeader("X-User-Role") String roleHeader) {
        RoleAccess.requireAdminOrFundManager(RoleAccess.parseRole(roleHeader));
        assetService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));
        asset.setId(id);
        int updated = assetService.update(asset);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found");
        }
        return assetService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestHeader("X-User-Role") String roleHeader) {
        RoleAccess.requireAdminOrFundManager(RoleAccess.parseRole(roleHeader));
        int deleted = assetService.delete(id);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found");
        }
        return ResponseEntity.noContent().build();
    }
}
