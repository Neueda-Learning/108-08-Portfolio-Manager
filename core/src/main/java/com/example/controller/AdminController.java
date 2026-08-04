package com.example.controller;

import com.example.model.Admin;
import com.example.security.RoleAccess;
import com.example.service.AdminService;
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
@RequestMapping("/api/admins")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public List<Admin> findAll(@RequestHeader("X-User-Role") String roleHeader) {
        RoleAccess.requireAdmin(RoleAccess.parseRole(roleHeader));
        return adminService.findAll();
    }

    @GetMapping("/{id}")
    public Admin findById(@PathVariable Long id, @RequestHeader("X-User-Role") String roleHeader) {
        RoleAccess.requireAdmin(RoleAccess.parseRole(roleHeader));
        return adminService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));
    }

    @PostMapping
    public ResponseEntity<Admin> create(@RequestBody Admin admin, @RequestHeader("X-User-Role") String roleHeader) {
        RoleAccess.requireAdmin(RoleAccess.parseRole(roleHeader));
        admin.setId(null);
        if (admin.getCreatedAt() == null) {
            admin.setCreatedAt(LocalDateTime.now());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.create(admin));
    }

    @PutMapping("/{id}")
    public Admin update(@PathVariable Long id,
                        @RequestBody Admin admin,
                        @RequestHeader("X-User-Role") String roleHeader) {
        RoleAccess.requireAdmin(RoleAccess.parseRole(roleHeader));
        Admin existing = adminService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));
        admin.setId(id);
        if (admin.getCreatedAt() == null) {
            admin.setCreatedAt(existing.getCreatedAt());
        }
        int updated = adminService.update(admin);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found");
        }
        return adminService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestHeader("X-User-Role") String roleHeader) {
        RoleAccess.requireAdmin(RoleAccess.parseRole(roleHeader));
        int deleted = adminService.delete(id);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found");
        }
        return ResponseEntity.noContent().build();
    }
}
